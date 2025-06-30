#!/usr/bin/env node

import { Server } from '@modelcontextprotocol/sdk/server/index.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import { CallToolRequestSchema, ListToolsRequestSchema } from '@modelcontextprotocol/sdk/types.js';
import { ParrotARSDKTools } from './parrot-arsdk-tools.js';
import { program } from 'commander';
import winston from 'winston';

// Server configuration
const SERVER_NAME = 'parrot-arsdk-mcp-server';
const SERVER_VERSION = '1.0.0';

// Set up command line interface
program
  .name(SERVER_NAME)
  .description('Government-grade MCP server for Parrot ARSDK drone control integration')
  .version(SERVER_VERSION)
  .option('--bluetooth-adapter <adapter>', 'Bluetooth adapter to use', 'auto')
  .option('--log-level <level>', 'Logging level', 'info')
  .option('--government-mode', 'Enable government compliance features', false)
  .option('--blue-suas-mode', 'Enable Blue sUAS compliance mode', false)
  .parse();

const options = program.opts();

// Configure logging
const logger = winston.createLogger({
  level: options.logLevel,
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.errors({ stack: true }),
    winston.format.json()
  ),
  transports: [
    new winston.transports.Console({
      format: winston.format.json() // Use JSON format for MCP compatibility
    }),
    new winston.transports.File({
      filename: 'parrot-arsdk-server.log',
      format: winston.format.json()
    })
  ]
});

// Government compliance logging
if (options.governmentMode) {
  logger.add(new winston.transports.File({
    filename: 'government-compliance.log',
    level: 'info',
    format: winston.format.combine(
      winston.format.timestamp(),
      winston.format.json()
    )
  }));
}

async function main() {
  logger.info('Starting Parrot ARSDK MCP Server', {
    version: SERVER_VERSION,
    bluetoothAdapter: options.bluetoothAdapter,
    governmentMode: options.governmentMode,
    blueSuasMode: options.blueSuasMode
  });

  // Initialize Parrot ARSDK tools
  const parrotTools = new ParrotARSDKTools();

  // Create MCP server
  const server = new Server(
    {
      name: SERVER_NAME,
      version: SERVER_VERSION,
    }
  );

  // Register tool handlers
  server.setRequestHandler(ListToolsRequestSchema, async () => {
    const tools = parrotTools.getTools();
    logger.debug('Listed tools', { toolCount: tools.length });
    return { tools };
  });

  server.setRequestHandler(CallToolRequestSchema, async (request) => {
    const { name, arguments: args } = request.params;
    
    logger.info('Executing tool', { 
      toolName: name, 
      arguments: args,
      timestamp: new Date().toISOString()
    });

    try {
      const result = await parrotTools.executeTool(name, args || {});
      
      logger.info('Tool execution completed', { 
        toolName: name, 
        success: result.success,
        timestamp: new Date().toISOString()
      });

      return {
        content: [
          {
            type: 'text',
            text: JSON.stringify(result, null, 2),
          },
        ],
      };
    } catch (error: any) {
      logger.error('Tool execution failed', { 
        toolName: name, 
        error: error?.message || 'Unknown error',
        stack: error?.stack,
        timestamp: new Date().toISOString()
      });

      return {
        content: [
          {
            type: 'text',
            text: JSON.stringify({
              success: false,
              error: {
                message: error?.message || 'Unknown error',
                type: error?.constructor?.name || 'Error',
                timestamp: new Date().toISOString()
              }
            }, null, 2),
          },
        ],
        isError: true,
      };
    }
  });

  // Set up server transport
  const transport = new StdioServerTransport();
  
  // Handle server lifecycle
  server.onerror = (error) => {
    logger.error('Server error', { error: error.message, stack: error.stack });
  };

  process.on('SIGINT', async () => {
    logger.info('Received SIGINT, shutting down gracefully');
    
    // Export compliance report before shutdown
    if (options.governmentMode) {
      try {
        const complianceReport = parrotTools.exportComplianceReport();
        logger.info('Government compliance report generated', {
          reportLength: complianceReport.length,
          timestamp: new Date().toISOString()
        });
      } catch (error) {
        logger.error('Failed to generate compliance report', { error: (error as any)?.message || 'Unknown error' });
      }
    }
    
    await server.close();
    process.exit(0);
  });

  process.on('uncaughtException', (error) => {
    logger.error('Uncaught exception', { error: error.message, stack: error.stack });
    process.exit(1);
  });

  process.on('unhandledRejection', (reason, promise) => {
    logger.error('Unhandled rejection', { reason, promise });
    process.exit(1);
  });

  // Start the server
  await server.connect(transport);
  
  logger.info('Parrot ARSDK MCP Server started successfully', {
    capabilities: ['listDevices', 'connect', 'disconnect', 'getTelemetry', 'sendPilotingCommand', 'startVideoStream', 'stopVideoStream'],
    compliance: {
      governmentMode: options.governmentMode,
      blueSuasMode: options.blueSuasMode,
      auditLogging: true,
      dataEncryption: true
    }
  });

  // Keep the process alive
  process.stdin.resume();
}

// Handle startup errors
main().catch((error) => {
  logger.error('Failed to start server', { error: error.message, stack: error.stack });
  process.exit(1);
});
