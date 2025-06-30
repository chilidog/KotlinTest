import { EventEmitter } from 'node:events';
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
export declare class TelemetryProcessor extends EventEmitter {
    private logger;
    private telemetryBuffer;
    private complianceMetrics;
    private auditLog;
    constructor();
    /**
     * Process raw Parrot telemetry into multiple protocol formats
     */
    processTelemetry(deviceId: string, rawTelemetry: TelemetryData): ProcessedTelemetry;
    /**
     * Convert Parrot telemetry to MAVLink messages
     */
    private convertToMAVLink;
    /**
     * Convert Parrot telemetry to WebSocket format
     */
    private convertToWebSocket;
    /**
     * Update government compliance metrics
     */
    private updateComplianceMetrics;
    /**
     * Calculate distance between two GPS coordinates
     */
    private calculateDistance;
    /**
     * Validate telemetry data integrity
     */
    private validateTelemetryIntegrity;
    /**
     * Add audit trail entry for government compliance
     */
    private addAuditEntry;
    /**
     * Get telemetry buffer for a device
     */
    getTelemetryBuffer(deviceId: string): TelemetryData[];
    /**
     * Get compliance metrics for a device
     */
    getComplianceMetrics(deviceId: string): GovernmentComplianceMetrics | undefined;
    /**
     * Get full audit log
     */
    getAuditLog(): AuditEntry[];
    /**
     * Export compliance report
     */
    exportComplianceReport(): string;
}
//# sourceMappingURL=telemetry-processor.d.ts.map