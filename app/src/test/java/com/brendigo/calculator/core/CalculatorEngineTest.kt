package com.brendigo.calculator.core

import org.junit.Assert.*
import org.junit.Test

class CalculatorEngineTest {
    @Test fun precedence() {
        assertEquals("14", CalculatorEngine.calculate("2+3×4"))
        assertEquals("20", CalculatorEngine.calculate("(2+3)×4"))
        assertEquals("512", CalculatorEngine.calculate("2^3^2"))
        assertEquals("-4", CalculatorEngine.calculate("-2^2"))
    }
    @Test fun mathFunctions() {
        assertEquals("0.5", CalculatorEngine.calculate("sin(30)"))
        assertEquals("12", CalculatorEngine.calculate("sqrt(144)"))
        assertEquals("120", CalculatorEngine.calculate("5!"))
        assertEquals("129", CalculatorEngine.calculate("15% of 860"))
    }
    @Test fun invalidInputsAreSafe() {
        assertEquals("Error", CalculatorEngine.calculate("1/0"))
        assertEquals("Error", CalculatorEngine.calculate("abc"))
        assertEquals("Error", CalculatorEngine.calculate("sqrt(-2)"))
    }
    @Test fun financialTools() {
        assertEquals(100.0,FinancialMath.monthlyPayment(1200.0,0.0,1),0.000001)
        assertEquals(230.0,FinancialMath.discounted(200.0,-15.0),0.000001)
        assertEquals(30.0,FinancialMath.tip(200.0,15.0,2).first,0.00001)
    }
    @Test fun conversion() {
        val length = UnitMath.categories.getValue("Length")
        assertEquals(0.62137119,UnitMath.convert(1.0,length[1],length[3]),0.000001)
        val temp = UnitMath.categories.getValue("Temperature")
        assertEquals(32.0,UnitMath.convert(0.0,temp[0],temp[1]),0.00001)
    }
}
