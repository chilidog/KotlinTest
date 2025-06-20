import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.BeforeEach
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class CalculatorTest {
    
    private val calculator = Calculator()
    
    @Test
    fun `test addition`() {
        assertEquals(8, calculator.add(5, 3))
        assertEquals(0, calculator.add(-5, 5))
        assertEquals(-8, calculator.add(-3, -5))
    }
    
    @Test
    fun `test subtraction`() {
        assertEquals(2, calculator.subtract(5, 3))
        assertEquals(-10, calculator.subtract(-5, 5))
        assertEquals(2, calculator.subtract(-3, -5))
    }
    
    @Test
    fun `test multiplication`() {
        assertEquals(15, calculator.multiply(5, 3))
        assertEquals(-25, calculator.multiply(-5, 5))
        assertEquals(15, calculator.multiply(-3, -5))
        assertEquals(0, calculator.multiply(0, 5))
    }
    
    @Test
    fun `test division`() {
        assertEquals(5, calculator.divide(15, 3))
        assertEquals(-5, calculator.divide(-15, 3))
        assertEquals(2, calculator.divide(10, 5))
    }
    
    @Test
    fun `test division by zero throws exception`() {
        assertThrows<IllegalArgumentException> {
            calculator.divide(10, 0)
        }
    }
    
    @Test
    fun `test power function`() {
        assertEquals(1, calculator.power(5, 0))
        assertEquals(5, calculator.power(5, 1))
        assertEquals(25, calculator.power(5, 2))
        assertEquals(8, calculator.power(2, 3))
    }
    
    @Test
    fun `test power with negative exponent throws exception`() {
        assertThrows<IllegalArgumentException> {
            calculator.power(5, -1)
        }
    }
}

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
