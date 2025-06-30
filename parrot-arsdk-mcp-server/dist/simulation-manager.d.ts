/**
 * Simulation Manager for Parrot ARSDK MCP Server
 * Simulates a fleet of government-grade drones for demonstration purposes
 */
import { EventEmitter } from 'events';
export interface SimulatedDrone {
    id: string;
    name: string;
    model: 'ANAFI_USA' | 'ANAFI_AI' | 'ANAFI_STANDARD';
    bluetoothAddress: string;
    rssi: number;
    isConnected: boolean;
    isStreamingVideo: boolean;
    telemetry: DroneTelemetry;
    compliance: ComplianceStatus;
    battery: BatteryStatus;
    gps: GPSStatus;
}
export interface DroneTelemetry {
    latitude: number;
    longitude: number;
    altitude: number;
    speed: number;
    heading: number;
    pitch: number;
    roll: number;
    yaw: number;
    timestamp: number;
}
export interface ComplianceStatus {
    blueUAS: boolean;
    ndaaCompliant: boolean;
    fipsCompliant: boolean;
    securityLevel: 'HIGH' | 'MEDIUM' | 'LOW';
    certificateValid: boolean;
}
export interface BatteryStatus {
    percentage: number;
    voltage: number;
    temperature: number;
    timeRemaining: number;
}
export interface GPSStatus {
    satelliteCount: number;
    hdop: number;
    vdop: number;
    fix: 'NO_FIX' | '2D' | '3D' | 'DGPS';
}
export declare class SimulationManager extends EventEmitter {
    private drones;
    private simulationActive;
    private telemetryInterval?;
    private videoStreams;
    constructor();
    private initializeFleet;
    private generateInitialTelemetry;
    startSimulation(): void;
    stopSimulation(): void;
    scanForDrones(): SimulatedDrone[];
    connectToDrone(droneId: string): boolean;
    disconnectDrone(droneId: string): boolean;
    startVideoStream(droneId: string): boolean;
    stopVideoStream(droneId: string): boolean;
    private updateTelemetry;
    getDrone(droneId: string): SimulatedDrone | undefined;
    getAllDrones(): SimulatedDrone[];
    getComplianceReport(): any;
    isSimulationActive(): boolean;
}
//# sourceMappingURL=simulation-manager.d.ts.map