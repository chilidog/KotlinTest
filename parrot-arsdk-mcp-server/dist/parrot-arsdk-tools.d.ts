import { Tool } from '@modelcontextprotocol/sdk/types.js';
import { ParrotDevice } from './bluetooth-manager.js';
export declare class ParrotARSDKTools {
    private bluetoothManager;
    private telemetryProcessor;
    constructor();
    /**
     * Get all available MCP tools for Parrot ARSDK
     */
    getTools(): Tool[];
    /**
     * List available Parrot drones
     */
    private getListDevicesTool;
    /**
     * Connect to a specific Parrot drone
     */
    private getConnectTool;
    /**
     * Disconnect from a Parrot drone
     */
    private getDisconnectTool;
    /**
     * Get real-time telemetry from drone
     */
    private getTelemetryTool;
    /**
     * Send piloting commands to drone
     */
    private getSendPilotingCommandTool;
    /**
     * Start video streaming from drone
     */
    private getStartVideoStreamTool;
    /**
     * Stop video streaming from drone
     */
    private getStopVideoStreamTool;
    /**
     * Execute MCP tool call
     */
    executeTool(name: string, arguments_: Record<string, any>): Promise<any>;
    private executeListDevices;
    private executeConnect;
    private executeDisconnect;
    private executeGetTelemetry;
    private executeSendPilotingCommand;
    private executeStartVideoStream;
    private executeStopVideoStream;
    /**
     * Get connected devices for status monitoring
     */
    getConnectedDevices(): ParrotDevice[];
    /**
     * Export government compliance report
     */
    exportComplianceReport(): string;
}
//# sourceMappingURL=parrot-arsdk-tools.d.ts.map