# 🚁 ControlStation Project - Agent Mode Quick Reference

## 🤖 FOR AGENT MODE SESSIONS

**If you are an Agent Mode session directed to explore this project for
WiFiLink 2 integration:**

### 📍 You Are Here

This is a **sophisticated hybrid drone control system** with working
WebSocket + MAVLink communication that needs WiFiLink 2 video streaming integration.

### 🎯 Your Mission

Add WiFiLink 2 video streaming as a third protocol while preserving all existing functionality.

### 📚 Required Reading (Start Here)

1. **AGENT_CONTINUATION.md** ← Quick start and context
2. **docs/COPILOT_AGENT_INSTRUCTIONS.md** ← Complete implementation guide
3. **docs/WIFILINK2_INTEGRATION_PLAN.md** ← Technical architecture details

### ⚡ Quick Context

- **Status**: Production-ready WebSocket+MAVLink system operational
- **Environment**: WSL:CachyOS (high-performance Linux)
- **Architecture**: Protocol adapter pattern with coroutine concurrency
- **Goal**: Triple protocol support (WebSocket + MAVLink + Video)

### 🔍 Project Structure

```text
/workspaces/KotlinTest/
├── pom.xml                              ← Enhanced with MAVLink deps
├── src/main/kotlin/SimpleControlStation.kt ← Working hybrid demo
├── AGENT_CONTINUATION.md                ← Start reading here
├── docs/COPILOT_AGENT_INSTRUCTIONS.md  ← Implementation guide
├── docs/WIFILINK2_INTEGRATION_PLAN.md  ← Architecture details
└── docs/AGENT_PROMPT_TEMPLATES.md      ← Development patterns
```

### 🚀 Immediate Actions

1. **Read documentation** to understand current architecture
2. **Preserve existing functionality** (WebSocket+MAVLink must keep working)
3. **Add video dependencies** to pom.xml following patterns
4. **Implement WiFiLink2Adapter** using established protocol patterns
5. **Test on WSL:CachyOS** environment (user's primary platform)

### ✅ Success Criteria

- All existing demos still work (WebSocket, MAVLink, Unified modes)
- New video integration operational
- Cross-platform compatibility maintained
- Real-time performance achieved

**START WITH**: Read AGENT_CONTINUATION.md for complete context and instructions.
