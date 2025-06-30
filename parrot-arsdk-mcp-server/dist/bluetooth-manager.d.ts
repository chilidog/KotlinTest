import { EventEmitter } from 'events';
export interface ParrotDevice {
    id: string;
    name: string;
    type: 'ANAFI' | 'ANAFI_USA' | 'ANAFI_AI' | 'BEBOP' | 'DISCO' | 'UNKNOWN';
    rssi: number;
    address: string;
    connected: boolean;
}
export interface TelemetryData {
    timestamp: number;
    position: {
        latitude: number;
        longitude: number;
        altitude: number;
    };
    attitude: {
        roll: number;
        pitch: number;
        yaw: number;
    };
    velocity: {
        x: number;
        y: number;
        z: number;
    };
    battery: {
        percentage: number;
        voltage: number;
    };
    gps: {
        satellites: number;
        fix: 'NO_FIX' | '2D' | '3D';
    };
    flightMode: string;
    armed: boolean;
}
export interface PilotingCommand {
    type: 'TAKEOFF' | 'LAND' | 'MOVE' | 'ROTATE' | 'EMERGENCY_STOP';
    parameters: {
        roll?: number;
        pitch?: number;
        yaw?: number;
        gaz?: number;
    };
}
export declare class BluetoothManager extends EventEmitter {
    private logger;
    private connectedDevices;
    private scanTimeout;
    private isScanning;
    constructor(scanTimeout?: number);
    /**
     * Scan for available Parrot drones via Bluetooth
     */
    listDevices(deviceTypes?: string[]): Promise<ParrotDevice[]>;
    /**
     * Connect to a specific Parrot drone
     */
    connect(deviceId: string, connectionTimeout?: number): Promise<boolean>;
    /**
     * Disconnect from a Parrot drone
     */
    disconnect(deviceId: string): Promise<boolean>;
    /**
     * Get real-time telemetry from connected drone
     */
    getTelemetry(deviceId: string): Promise<TelemetryData>;
    /**
     * Send piloting command to drone
     */
    sendPilotingCommand(deviceId: string, command: PilotingCommand): Promise<boolean>;
    /**
     * Start video streaming from drone
     */
    startVideoStream(deviceId: string): Promise<{
        streamUrl: string;
        port: number;
    }>;
    /**
     * Stop video streaming from drone
     */
    stopVideoStream(deviceId: string): Promise<boolean>;
    /**
     * Get list of connected devices
     */
    getConnectedDevices(): ParrotDevice[];
    /**
     * Check if device is connected
     */
    isDeviceConnected(deviceId: string): boolean;
}
//# sourceMappingURL=bluetooth-manager.d.ts.map