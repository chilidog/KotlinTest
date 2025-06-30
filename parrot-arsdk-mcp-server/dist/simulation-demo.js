#!/usr/bin/env node
/**
 * Simulation Demo for Parrot ARSDK MCP Server
 * Demonstrates government-grade drone control capabilities without physical hardware
 */
import { SimulationManager } from './simulation-manager.js';
class SimulationDemo {
    sim;
    connectedDrones = new Set();
    constructor() {
        this.sim = new SimulationManager();
        this.setupEventListeners();
    }
    setupEventListeners() {
        this.sim.on('simulationStarted', (data) => {
            console.log(`✅ Simulation started with ${data.droneCount} drones`);
        });
        this.sim.on('dronesDiscovered', (drones) => {
            console.log(`📡 Discovered ${drones.length} Parrot drones via Bluetooth`);
        });
        this.sim.on('droneConnected', (drone) => {
            this.connectedDrones.add(drone.id);
            console.log(`🔗 Connected to ${drone.name} (Security: ${drone.compliance.securityLevel})`);
        });
        this.sim.on('videoStreamStarted', (data) => {
            console.log(`📹 Video stream started: ${data.resolution} @ ${data.fps}fps (Encrypted: ${data.encrypted})`);
        });
        this.sim.on('telemetryUpdate', (data) => {
            const drone = this.sim.getDrone(data.droneId);
            if (drone && this.connectedDrones.has(data.droneId)) {
                // Only log telemetry periodically to avoid spam
                if (Math.random() < 0.1) { // 10% chance each update
                    console.log(`📊 ${drone.name}: Lat ${data.telemetry.latitude.toFixed(6)}, ` +
                        `Lon ${data.telemetry.longitude.toFixed(6)}, ` +
                        `Alt ${data.telemetry.altitude.toFixed(1)}m, ` +
                        `Battery ${data.battery.percentage.toFixed(1)}%`);
                }
            }
        });
    }
    async runDemo() {
        console.log('🚁 PARROT ARSDK MCP SERVER - GOVERNMENT SIMULATION DEMO');
        console.log('='.repeat(60));
        console.log('🎯 Purpose: Demonstrate Blue sUAS compliant drone control capabilities');
        console.log('🏛️  Target: Government and defense contractors');
        console.log('🔒 Features: Secure Bluetooth, encrypted video, compliance tracking');
        console.log('='.repeat(60));
        console.log();
        // Step 1: Start simulation
        console.log('🚀 STEP 1: Initialize Simulation Environment');
        this.sim.startSimulation();
        await this.delay(2000);
        // Step 2: Scan for drones
        console.log('\n🔍 STEP 2: Bluetooth Drone Discovery');
        const discoveredDrones = this.sim.scanForDrones();
        await this.delay(3000);
        // Step 3: Display compliance analysis
        console.log('\n🛡️  STEP 3: Compliance Analysis');
        const compliance = this.sim.getComplianceReport();
        console.log(`📋 Total Drones: ${compliance.totalDrones}`);
        console.log(`🟢 Blue sUAS Approved: ${compliance.blueUASApproved}/${compliance.totalDrones}`);
        console.log(`🏛️  NDAA Compliant: ${compliance.ndaaCompliant}/${compliance.totalDrones}`);
        console.log(`🔒 High Security: ${compliance.highSecurityLevel}/${compliance.totalDrones}`);
        console.log(`📊 Compliance Rate: ${compliance.compliancePercentage}%`);
        await this.delay(3000);
        // Step 4: Connect to government-approved drone
        console.log('\n🔗 STEP 4: Connect to Blue sUAS Approved Drone');
        const govDrone = discoveredDrones.find(d => d.compliance.blueUAS);
        if (govDrone) {
            console.log(`🎯 Targeting: ${govDrone.name} (${govDrone.model})`);
            this.sim.connectToDrone(govDrone.id);
            await this.delay(3000);
            // Step 5: Start video stream
            console.log('\n📹 STEP 5: Secure Video Streaming');
            this.sim.startVideoStream(govDrone.id);
            await this.delay(2000);
            // Step 6: Monitor telemetry
            console.log('\n📊 STEP 6: Real-time Telemetry Monitoring');
            console.log('(Monitoring for 10 seconds...)');
            await this.delay(10000);
            // Step 7: Connect to additional drone for comparison
            console.log('\n🔄 STEP 7: Multi-Drone Management');
            const commercialDrone = discoveredDrones.find(d => d.model === 'ANAFI_AI');
            if (commercialDrone) {
                console.log(`🎯 Connecting to commercial drone: ${commercialDrone.name}`);
                this.sim.connectToDrone(commercialDrone.id);
                await this.delay(3000);
                this.sim.startVideoStream(commercialDrone.id);
                await this.delay(2000);
            }
            // Step 8: Demonstrate security comparison
            console.log('\n🔐 STEP 8: Security Level Comparison');
            this.displaySecurityComparison(discoveredDrones);
            await this.delay(5000);
            // Step 9: Mission simulation
            console.log('\n🎯 STEP 9: Mission Simulation (15 seconds)');
            console.log('📍 Simulating patrol mission around government facilities...');
            await this.delay(15000);
            // Step 10: Clean shutdown
            console.log('\n🛑 STEP 10: Mission Complete - Secure Shutdown');
            for (const droneId of this.connectedDrones) {
                const drone = this.sim.getDrone(droneId);
                if (drone) {
                    console.log(`📱 Stopping video stream: ${drone.name}`);
                    this.sim.stopVideoStream(droneId);
                    await this.delay(1000);
                    console.log(`🔌 Disconnecting: ${drone.name}`);
                    this.sim.disconnectDrone(droneId);
                    await this.delay(1000);
                }
            }
        }
        this.sim.stopSimulation();
        console.log('\n✅ SIMULATION COMPLETE');
        console.log('='.repeat(60));
        console.log('🎉 Successfully demonstrated government-grade drone control capabilities');
        console.log('💼 Ready for SBIR Phase II deployment');
        console.log('📞 Contact ControlStation team for integration support');
        console.log('='.repeat(60));
    }
    displaySecurityComparison(drones) {
        console.log('🔒 SECURITY COMPARISON MATRIX:');
        console.log('┌─────────────────────────┬──────────┬──────────┬──────────┬──────────┐');
        console.log('│ Drone Model             │ Blue sUAS│ NDAA     │ FIPS     │ Security │');
        console.log('├─────────────────────────┼──────────┼──────────┼──────────┼──────────┤');
        drones.forEach(drone => {
            const name = drone.name.padEnd(23);
            const blueUAS = drone.compliance.blueUAS ? '    ✅   ' : '    ❌   ';
            const ndaa = drone.compliance.ndaaCompliant ? '    ✅   ' : '    ❌   ';
            const fips = drone.compliance.fipsCompliant ? '    ✅   ' : '    ❌   ';
            const security = drone.compliance.securityLevel.padEnd(8);
            console.log(`│ ${name} │${blueUAS}│${ndaa}│${fips}│ ${security} │`);
        });
        console.log('└─────────────────────────┴──────────┴──────────┴──────────┴──────────┘');
        console.log('🎯 Recommendation: Use Blue sUAS approved drones for government missions');
    }
    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
}
// Main execution
async function main() {
    try {
        const demo = new SimulationDemo();
        await demo.runDemo();
        process.exit(0);
    }
    catch (error) {
        console.error('❌ Simulation failed:', error);
        process.exit(1);
    }
}
// Handle graceful shutdown
process.on('SIGINT', () => {
    console.log('\n🛑 Received SIGINT, shutting down gracefully...');
    process.exit(0);
});
process.on('SIGTERM', () => {
    console.log('\n🛑 Received SIGTERM, shutting down gracefully...');
    process.exit(0);
});
if (import.meta.url === `file://${process.argv[1]}`) {
    main();
}
//# sourceMappingURL=simulation-demo.js.map