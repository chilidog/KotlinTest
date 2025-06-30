import { EventEmitter } from 'node:events';
import winston, { createLogger, Logger } from 'winston';
import { TelemetryData } from './bluetooth-manager.js';

export interface MAVLinkMessage {
  messageId: number;
  systemId: number;
  componentId: number;
  payload: Record<string, any>;
  timestamp: number;
}

export interface WebSocketMessage {
  type: 'telemetry' | 'command' | 'status' | 'video';
  deviceId: string;
  timestamp: number;
  data: any;
}

export interface ProcessedTelemetry {
  deviceId: string;
  timestamp: number;
  parrotNative: TelemetryData;
  mavlinkFormat: MAVLinkMessage[];
  webSocketFormat: WebSocketMessage;
  governmentMetrics: GovernmentComplianceMetrics;
}

export interface GovernmentComplianceMetrics {
  flightTime: number;
  maxAltitude: number;
  geofenceStatus: 'INSIDE' | 'OUTSIDE' | 'UNKNOWN';
  emergencyStop: boolean;
  dataIntegrity: 'VALID' | 'CORRUPTED' | 'MISSING';
  encryptionStatus: 'ENCRYPTED' | 'UNENCRYPTED';
  auditTrail: AuditEntry[];
}

export interface AuditEntry {
  timestamp: number;
  action: string;
  operator: string;
  result: 'SUCCESS' | 'FAILURE';
  details: string;
}

export class TelemetryProcessor extends EventEmitter {
  private logger: Logger;
  private telemetryBuffer: Map<string, TelemetryData[]>;
  private complianceMetrics: Map<string, GovernmentComplianceMetrics>;
  private auditLog: AuditEntry[];

  constructor() {
    super();
    this.telemetryBuffer = new Map();
    this.complianceMetrics = new Map();
    this.auditLog = [];

    this.logger = createLogger({
      level: process.env.ARSDK_LOG_LEVEL || 'info',
      format: winston.format.combine(
        winston.format.timestamp(),
        winston.format.json()
      ),
      transports: [
        new winston.transports.Console({
          format: winston.format.json() // Use JSON format for MCP compatibility
        }),
        new winston.transports.File({
          filename: 'government-audit.log',
          format: winston.format.json()
        })
      ]
    });
  }

  /**
   * Process raw Parrot telemetry into multiple protocol formats
   */
  processTelemetry(deviceId: string, rawTelemetry: TelemetryData): ProcessedTelemetry {
    this.addAuditEntry('TELEMETRY_PROCESSED', 'SYSTEM', 'SUCCESS', 
      `Processed telemetry for device ${deviceId}`);

    // Update telemetry buffer
    if (!this.telemetryBuffer.has(deviceId)) {
      this.telemetryBuffer.set(deviceId, []);
    }
    const buffer = this.telemetryBuffer.get(deviceId)!;
    buffer.push(rawTelemetry);
    
    // Keep only last 100 entries
    if (buffer.length > 100) {
      buffer.shift();
    }

    // Convert to MAVLink format
    const mavlinkMessages = this.convertToMAVLink(deviceId, rawTelemetry);

    // Convert to WebSocket format
    const webSocketMessage = this.convertToWebSocket(deviceId, rawTelemetry);

    // Update government compliance metrics
    const complianceMetrics = this.updateComplianceMetrics(deviceId, rawTelemetry);

    const processed: ProcessedTelemetry = {
      deviceId,
      timestamp: rawTelemetry.timestamp,
      parrotNative: rawTelemetry,
      mavlinkFormat: mavlinkMessages,
      webSocketFormat: webSocketMessage,
      governmentMetrics: complianceMetrics
    };

    this.emit('telemetryProcessed', processed);
    return processed;
  }

  /**
   * Convert Parrot telemetry to MAVLink messages
   */
  private convertToMAVLink(deviceId: string, telemetry: TelemetryData): MAVLinkMessage[] {
    const messages: MAVLinkMessage[] = [];

    // GLOBAL_POSITION_INT (Message ID: 33)
    messages.push({
      messageId: 33,
      systemId: 1,
      componentId: 1,
      timestamp: telemetry.timestamp,
      payload: {
        time_boot_ms: telemetry.timestamp,
        lat: Math.round(telemetry.position.latitude * 1e7),
        lon: Math.round(telemetry.position.longitude * 1e7),
        alt: Math.round(telemetry.position.altitude * 1000),
        relative_alt: Math.round(telemetry.position.altitude * 1000),
        vx: Math.round(telemetry.velocity.x * 100),
        vy: Math.round(telemetry.velocity.y * 100),
        vz: Math.round(telemetry.velocity.z * 100),
        hdg: Math.round(telemetry.attitude.yaw * 100)
      }
    });

    // ATTITUDE (Message ID: 30)
    messages.push({
      messageId: 30,
      systemId: 1,
      componentId: 1,
      timestamp: telemetry.timestamp,
      payload: {
        time_boot_ms: telemetry.timestamp,
        roll: telemetry.attitude.roll * Math.PI / 180,
        pitch: telemetry.attitude.pitch * Math.PI / 180,
        yaw: telemetry.attitude.yaw * Math.PI / 180,
        rollspeed: 0,
        pitchspeed: 0,
        yawspeed: 0
      }
    });

    // SYS_STATUS (Message ID: 1)
    messages.push({
      messageId: 1,
      systemId: 1,
      componentId: 1,
      timestamp: telemetry.timestamp,
      payload: {
        onboard_control_sensors_present: 0x3FF,
        onboard_control_sensors_enabled: 0x3FF,
        onboard_control_sensors_health: 0x3FF,
        load: 0,
        voltage_battery: Math.round(telemetry.battery.voltage * 1000),
        current_battery: -1,
        battery_remaining: Math.round(telemetry.battery.percentage),
        drop_rate_comm: 0,
        errors_comm: 0,
        errors_count1: 0,
        errors_count2: 0,
        errors_count3: 0,
        errors_count4: 0
      }
    });

    this.logger.debug('Converted telemetry to MAVLink', { 
      deviceId, 
      messageCount: messages.length 
    });

    return messages;
  }

  /**
   * Convert Parrot telemetry to WebSocket format
   */
  private convertToWebSocket(deviceId: string, telemetry: TelemetryData): WebSocketMessage {
    return {
      type: 'telemetry',
      deviceId,
      timestamp: telemetry.timestamp,
      data: {
        position: telemetry.position,
        attitude: telemetry.attitude,
        velocity: telemetry.velocity,
        battery: telemetry.battery,
        gps: telemetry.gps,
        flightMode: telemetry.flightMode,
        armed: telemetry.armed,
        // Additional ControlStation format fields
        controlStation: {
          protocol: 'PARROT_ARSDK',
          quality: 'HIGH',
          latency: Math.random() * 50 + 10, // 10-60ms simulated
          source: 'MCP_SERVER'
        }
      }
    };
  }

  /**
   * Update government compliance metrics
   */
  private updateComplianceMetrics(deviceId: string, telemetry: TelemetryData): GovernmentComplianceMetrics {
    if (!this.complianceMetrics.has(deviceId)) {
      this.complianceMetrics.set(deviceId, {
        flightTime: 0,
        maxAltitude: 0,
        geofenceStatus: 'UNKNOWN',
        emergencyStop: false,
        dataIntegrity: 'VALID',
        encryptionStatus: 'ENCRYPTED',
        auditTrail: []
      });
    }

    const metrics = this.complianceMetrics.get(deviceId)!;
    
    // Update max altitude
    if (telemetry.position.altitude > metrics.maxAltitude) {
      metrics.maxAltitude = telemetry.position.altitude;
      this.addAuditEntry('MAX_ALTITUDE_UPDATED', 'SYSTEM', 'SUCCESS',
        `New max altitude: ${metrics.maxAltitude}m for ${deviceId}`);
    }

    // Check geofence (example: restricted area)
    const restrictedLat = 40.7128;
    const restrictedLon = -74.0060;
    const distance = this.calculateDistance(
      telemetry.position.latitude,
      telemetry.position.longitude,
      restrictedLat,
      restrictedLon
    );
    
    metrics.geofenceStatus = distance > 1000 ? 'INSIDE' : 'OUTSIDE'; // 1km geofence

    // Validate data integrity
    metrics.dataIntegrity = this.validateTelemetryIntegrity(telemetry) ? 'VALID' : 'CORRUPTED';

    // Blue sUAS compliance check
    if (telemetry.position.altitude > 120) { // 400ft limit
      this.addAuditEntry('ALTITUDE_VIOLATION', 'SYSTEM', 'FAILURE',
        `Altitude ${telemetry.position.altitude}m exceeds 120m limit for ${deviceId}`);
    }

    return metrics;
  }

  /**
   * Calculate distance between two GPS coordinates
   */
  private calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const R = 6371000; // Earth's radius in meters
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
              Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
              Math.sin(dLon/2) * Math.sin(dLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
  }

  /**
   * Validate telemetry data integrity
   */
  private validateTelemetryIntegrity(telemetry: TelemetryData): boolean {
    // Basic validation checks
    if (!telemetry.timestamp || telemetry.timestamp <= 0) return false;
    if (!telemetry.position) return false;
    if (Math.abs(telemetry.position.latitude) > 90) return false;
    if (Math.abs(telemetry.position.longitude) > 180) return false;
    if (telemetry.battery.percentage < 0 || telemetry.battery.percentage > 100) return false;
    
    return true;
  }

  /**
   * Add audit trail entry for government compliance
   */
  private addAuditEntry(action: string, operator: string, result: 'SUCCESS' | 'FAILURE', details: string): void {
    const entry: AuditEntry = {
      timestamp: Date.now(),
      action,
      operator,
      result,
      details
    };

    this.auditLog.push(entry);
    
    // Keep only last 1000 entries
    if (this.auditLog.length > 1000) {
      this.auditLog.shift();
    }

    this.logger.info('Audit entry added', entry);
  }

  /**
   * Get telemetry buffer for a device
   */
  getTelemetryBuffer(deviceId: string): TelemetryData[] {
    return this.telemetryBuffer.get(deviceId) || [];
  }

  /**
   * Get compliance metrics for a device
   */
  getComplianceMetrics(deviceId: string): GovernmentComplianceMetrics | undefined {
    return this.complianceMetrics.get(deviceId);
  }

  /**
   * Get full audit log
   */
  getAuditLog(): AuditEntry[] {
    return [...this.auditLog];
  }

  /**
   * Export compliance report
   */
  exportComplianceReport(): string {
    const report = {
      timestamp: new Date().toISOString(),
      devices: Array.from(this.complianceMetrics.entries()).map(([deviceId, metrics]) => ({
        deviceId,
        metrics
      })),
      auditLog: this.auditLog,
      summary: {
        totalDevices: this.complianceMetrics.size,
        totalAuditEntries: this.auditLog.length,
        lastUpdate: new Date().toISOString()
      }
    };

    return JSON.stringify(report, null, 2);
  }
}
