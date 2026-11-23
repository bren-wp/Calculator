package com.brendigo.calculator.core

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

/** Offline expression evaluator: never executes arbitrary code or sends calculations anywhere. */
object CalculatorEngine {
    fun calculate(expression: String, degrees: Boolean = true): String = try {
        if (expression.length > 256) "Error" else if (expression.isBlank()) "0" else {
            val parser = Parser(expression, degrees)
            val result = parser.evaluate()
            if (!result.isFinite()) "Error" else format(result)
        }
    } catch (_: ArithmeticException) { "Error" }
      catch (_: IllegalArgumentException) { "Error" }

    fun format(value: Double): String {
        if (!value.isFinite()) return "Error"
        if (abs(value) < 1.0e-12) return "0"
        val bd = BigDecimal.valueOf(value).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros()
        return if (bd.precision() > 16 || bd.scale() < -8) {
            String.format(Locale.US, "%.8g", value)
        } else bd.toPlainString()
    }

    fun grouped(value: String): String {
        val n = value.toDoubleOrNull() ?: return value
        if (!n.isFinite()) return value
        val f = DecimalFormat("#,##0.##########", DecimalFormatSymbols(Locale.US))
        return f.format(n)
    }

    private class Parser(source: String, val degrees: Boolean) {
        private val s = source.replace("×", "*").replace("÷", "/")
            .replace("−", "-").replace("π", "pi").replace("√", "sqrt")
            .replace(" of ", "*").replace(" ", "")
        private var i = 0

        fun evaluate(): Double {
            val n = sum()
            if (i != s.length) throw IllegalArgumentException("Unexpected token")
            return n
        }
        private fun eat(ch: Char): Boolean {
            if (i < s.length && s[i] == ch) { i++; return true }
            return false
        }
        private fun sum(): Double {
            var v = product()
            while (true) v = when {
                eat('+') -> v + product()
                eat('-') -> v - product()
                else -> return v
            }
        }
        private fun product(): Double {
            var v = signed()
            while (true) v = when {
                eat('*') -> v * signed()
                eat('/') -> {
                    val d = signed()
                    if (d == 0.0) throw ArithmeticException("Divide by zero")
                    v / d
                }
                else -> return v
            }
        }
        private fun signed(): Double = when {
            eat('+') -> signed()
            eat('-') -> -signed()
            else -> power()
        }
        private fun power(): Double {
            val base = postfix()
            return if (eat('^')) base.pow(signed()) else base
        }
        private fun postfix(): Double {
            var v = atom()
            while (true) v = when {
                eat('%') -> v / 100
                eat('!') -> factorial(v)
                else -> return v
            }
        }
        private fun factorial(v: Double): Double {
            if (v < 0 || v > 170 || v != floor(v)) throw ArithmeticException("Invalid factorial")
            var result = 1.0
            for (n in 2..v.toInt()) result *= n
            return result
        }
        private fun atom(): Double {
            if (eat('(')) {
                val v = sum()
                if (!eat(')')) throw IllegalArgumentException("Missing )")
                return v
            }
            val start = i
            while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
            if (i > start) return s.substring(start,i).toDoubleOrNull()
                ?: throw IllegalArgumentException("Number")
            while (i < s.length && s[i].isLetter()) i++
            if (i == start) throw IllegalArgumentException("Unexpected token")
            val fn = s.substring(start, i).lowercase(Locale.US)
            if (fn == "pi") return PI
            if (fn == "e") return E
            val arg = if (eat('(')) {
                val a = sum()
                if (!eat(')')) throw IllegalArgumentException("Missing )")
                a
            } else signed()
            val rad = if (degrees) Math.toRadians(arg) else arg
            return when(fn) {
                "sin" -> sin(rad)
                "cos" -> cos(rad)
                "tan" -> tan(rad)
                "asin" -> if (degrees) Math.toDegrees(asin(arg)) else asin(arg)
                "acos" -> if (degrees) Math.toDegrees(acos(arg)) else acos(arg)
                "atan" -> if (degrees) Math.toDegrees(atan(arg)) else atan(arg)
                "log" -> log10(arg)
                "ln" -> ln(arg)
                "sqrt" -> sqrt(arg)
                "abs" -> abs(arg)
                "exp" -> exp(arg)
                else -> throw IllegalArgumentException("Unknown function")
            }
        }
    }
}

object FinancialMath {
    fun monthlyPayment(principal: Double, annualRatePercent: Double, years: Int): Double {
        if (principal < 0 || annualRatePercent < 0 || years <= 0) return Double.NaN
        val months = years * 12
        val rate = annualRatePercent / 1200.0
        return if (rate == 0.0) principal / months
        else principal * rate / (1.0 - (1.0 + rate).pow(-months))
    }
    fun tip(total: Double, percent: Double, people: Int): Pair<Double,Double> {
        if (total < 0 || percent < 0 || people <= 0) return Double.NaN to Double.NaN
        val gratuity = total * percent / 100.0
        return gratuity to (total + gratuity) / people
    }
    fun discounted(price: Double, percent: Double): Double =
        price * (1.0 - percent / 100.0)
    fun vat(net: Double, rate: Double): Pair<Double,Double> =
        (net * rate / 100.0) to (net * (1.0 + rate / 100.0))
}

object UnitMath {
    data class UnitEntry(val label: String, val toBase: Double, val offset: Double = 0.0)
    val categories = linkedMapOf(
        "Length" to listOf(UnitEntry("Meter (m)",1.0), UnitEntry("Kilometer (km)",1000.0),
            UnitEntry("Centimeter (cm)",0.01), UnitEntry("Mile (mi)",1609.344),
            UnitEntry("Foot (ft)",0.3048), UnitEntry("Inch (in)",0.0254)),
        "Weight" to listOf(UnitEntry("Kilogram (kg)",1.0), UnitEntry("Gram (g)",0.001),
            UnitEntry("Pound (lb)",0.45359237), UnitEntry("Ounce (oz)",0.028349523125)),
        "Area" to listOf(UnitEntry("Square meter (m²)",1.0), UnitEntry("Square kilometer (km²)",1e6),
            UnitEntry("Hectare (ha)",10000.0), UnitEntry("Square foot (ft²)",0.09290304),
            UnitEntry("Acre (ac)",4046.8564224)),
        "Volume" to listOf(UnitEntry("Liter (L)",1.0), UnitEntry("Milliliter (mL)",0.001),
            UnitEntry("Gallon (US)",3.785411784), UnitEntry("Cubic meter (m³)",1000.0)),
        "Temperature" to listOf(UnitEntry("Celsius (°C)",1.0,273.15),
            UnitEntry("Fahrenheit (°F)",5.0/9.0,459.67),
            UnitEntry("Kelvin (K)",1.0,0.0)),
        "Speed" to listOf(UnitEntry("Meter/second",1.0), UnitEntry("Kilometer/hour",1/3.6),
            UnitEntry("Mile/hour",0.44704), UnitEntry("Knot",0.514444)),
        "Time" to listOf(UnitEntry("Second",1.0),UnitEntry("Minute",60.0),
            UnitEntry("Hour",3600.0),UnitEntry("Day",86400.0)),
        "Data" to listOf(UnitEntry("Byte",1.0),UnitEntry("Kilobyte",1000.0),
            UnitEntry("Megabyte",1e6),UnitEntry("Gigabyte",1e9))
    )
    fun convert(value: Double, from: UnitEntry, to: UnitEntry) =
        (value + from.offset) * from.toBase / to.toBase - to.offset
}
