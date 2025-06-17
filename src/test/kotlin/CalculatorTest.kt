import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

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
