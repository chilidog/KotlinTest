import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class SystemConfigTest {
    
    @BeforeEach
    fun resetConfig() {
        // Reset to default before each test
        SystemConfig.osType = "Ubuntu"
    }
    
    @Test
    fun `test default OS type is Ubuntu`() {
        assertEquals("Ubuntu", SystemConfig.osType)
        assertTrue(SystemConfig.isUbuntu())
        assertFalse(SystemConfig.isCachyOS())
        assertFalse(SystemConfig.isAlpine())
    }
    
    @Test
    fun `test setting OS type to CachyOS`() {
        SystemConfig.osType = "CachyOS"
        assertEquals("CachyOS", SystemConfig.osType)
        assertTrue(SystemConfig.isCachyOS())
        assertFalse(SystemConfig.isUbuntu())
        assertFalse(SystemConfig.isAlpine())
    }
    
    @Test
    fun `test setting OS type to Alpine`() {
        SystemConfig.osType = "Alpine"
        assertEquals("Alpine", SystemConfig.osType)
        assertTrue(SystemConfig.isAlpine())
        assertFalse(SystemConfig.isUbuntu())
        assertFalse(SystemConfig.isCachyOS())
    }
    
    @Test
    fun `test case insensitive OS type setting`() {
        SystemConfig.osType = "cachyos"
        assertTrue(SystemConfig.isCachyOS())
        
        SystemConfig.osType = "UBUNTU"
        assertTrue(SystemConfig.isUbuntu())
        
        SystemConfig.osType = "alpine"
        assertTrue(SystemConfig.isAlpine())
    }
    
    @Test
    fun `test invalid OS type keeps current value`() {
        val originalValue = SystemConfig.osType
        SystemConfig.osType = "InvalidOS"
        assertEquals(originalValue, SystemConfig.osType)
    }
    
    @Test
    fun `test switching between all OS types`() {
        // Start with default Ubuntu
        assertTrue(SystemConfig.isUbuntu())
        
        // Switch to CachyOS
        SystemConfig.osType = "CachyOS"
        assertTrue(SystemConfig.isCachyOS())
        assertFalse(SystemConfig.isUbuntu())
        assertFalse(SystemConfig.isAlpine())
        
        // Switch to Alpine
        SystemConfig.osType = "Alpine"
        assertTrue(SystemConfig.isAlpine())
        assertFalse(SystemConfig.isUbuntu())
        assertFalse(SystemConfig.isCachyOS())
        
        // Switch back to Ubuntu
        SystemConfig.osType = "Ubuntu"
        assertTrue(SystemConfig.isUbuntu())
        assertFalse(SystemConfig.isCachyOS())
        assertFalse(SystemConfig.isAlpine())
    }
    
    @Test
    fun `test getSupportedOSes returns all three options`() {
        val supportedOSes = SystemConfig.getSupportedOSes()
        assertEquals(3, supportedOSes.size)
        assertTrue(supportedOSes.contains("CachyOS"))
        assertTrue(supportedOSes.contains("Ubuntu"))
        assertTrue(supportedOSes.contains("Alpine"))
    }
}
