class Calculator {
    fun add(a: Int, b: Int): Int = a + b
    
    fun subtract(a: Int, b: Int): Int = a - b
    
    fun multiply(a: Int, b: Int): Int = a * b
    
    fun divide(a: Int, b: Int): Int {
        if (b == 0) {
            throw IllegalArgumentException("Division by zero is not allowed")
        }
        return a / b
    }
    
    fun power(base: Int, exponent: Int): Int {
        if (exponent < 0) {
            throw IllegalArgumentException("Negative exponents are not supported")
        }
        var result = 1
        repeat(exponent) {
            result *= base
        }
        return result
    }
}
