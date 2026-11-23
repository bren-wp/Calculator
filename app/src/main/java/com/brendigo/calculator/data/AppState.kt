package com.brendigo.calculator.data

import android.content.Context
import androidx.compose.runtime.*
import com.brendigo.calculator.core.CalculatorEngine
import org.json.JSONArray
import org.json.JSONObject

data class HistoryEntry(val expression: String, val result: String, val time: Long, val starred: Boolean = false)

class CalculatorState(context: Context) {
    private val prefs = context.getSharedPreferences("calculator_local", Context.MODE_PRIVATE)
    val history = mutableStateListOf<HistoryEntry>()
    var theme by mutableStateOf(prefs.getString("theme", "Dark") ?: "Dark")
        private set
    var degrees by mutableStateOf(prefs.getBoolean("degrees", true))
        private set
    var vibration by mutableStateOf(prefs.getBoolean("vibration", true))
        private set
    var sounds by mutableStateOf(prefs.getBoolean("sounds", false))
        private set
    var thousands by mutableStateOf(prefs.getBoolean("thousands", true))
        private set
    var activePage by mutableStateOf("Calculator")
    var activeMode by mutableStateOf("Standard")
    var expression by mutableStateOf("")
    var memory by mutableDoubleStateOf(0.0)
    var lastResult by mutableStateOf("0")
    var errorMessage by mutableStateOf<String?>(null)

    init {
        try {
            val list = JSONArray(prefs.getString("history","[]"))
            for (i in 0 until list.length()) {
                val e = list.getJSONObject(i)
                history.add(HistoryEntry(e.getString("expression"),e.getString("result"),
                    e.getLong("time"),e.optBoolean("starred",false)))
            }
        } catch (_: Exception) { history.clear() }
    }
    fun setTheme(value: String) { theme = value; prefs.edit().putString("theme",value).apply() }
    fun setDegrees(value: Boolean) { degrees = value; prefs.edit().putBoolean("degrees",value).apply() }
    fun setVibration(value: Boolean) { vibration = value; prefs.edit().putBoolean("vibration",value).apply() }
    fun setSounds(value: Boolean) { sounds = value; prefs.edit().putBoolean("sounds",value).apply() }
    fun setThousands(value: Boolean) { thousands = value; prefs.edit().putBoolean("thousands",value).apply() }
    private fun saveHistory() {
        val arr = JSONArray()
        history.forEach {
            arr.put(JSONObject().put("expression", it.expression).put("result", it.result)
                .put("time", it.time).put("starred",it.starred))
        }
        prefs.edit().putString("history",arr.toString()).apply()
    }
    fun clearHistory() { history.clear(); saveHistory() }
    fun toggleFavorite(index: Int) {
        if (index in history.indices) { history[index] = history[index].copy(starred=!history[index].starred); saveHistory() }
    }
    fun useHistory(entry: HistoryEntry) { expression=entry.expression; activeMode="Standard"; activePage="Calculator" }
    fun calculate(): String = CalculatorEngine.calculate(expression,degrees)
    fun press(key: String) {
        when (key) {
            "AC","C" -> { expression=""; lastResult="0"; errorMessage=null }
            "⌫" -> expression=expression.dropLast(1)
            "=" -> {
                val result = calculate()
                if (result != "Error" && expression.isNotBlank()) {
                    history.add(0, HistoryEntry(expression,result,System.currentTimeMillis()))
                    while (history.size > 500) history.removeAt(history.lastIndex)
                    saveHistory()
                    expression=result
                    lastResult=result
                } else { errorMessage="Unable to calculate this expression" }
            }
            "( )" -> {
                val open = expression.count { it=='(' }
                val closed = expression.count { it==')' }
                expression += if (open>closed && expression.lastOrNull() !in listOf('+','-','×','÷','(')) ")" else "("
            }
            "±" -> expression = if (expression.isBlank()) "-" else "-(" + expression + ")"
            "MR" -> expression += CalculatorEngine.format(memory)
            "MC" -> memory = 0.0
            "M+" -> memory += calculate().toDoubleOrNull() ?: 0.0
            "M−" -> memory -= calculate().toDoubleOrNull() ?: 0.0
            else -> {
                val fn = key in listOf("sin","cos","tan","asin","acos","atan","log","ln","sqrt","abs")
                if (key.length > 1 && fn) expression += "$key("
                else if (key == "x²") expression += "^2"
                else if (key == "xʸ") expression += "^"
                else if (key == "π") expression += "π"
                else if (key == "n!") expression += "!"
                else if (key == "1/x") expression = "1/(" + expression + ")"
                else if (key == "e") expression += "e"
                else if (key == "." && (expression.takeLastWhile { it.isDigit() || it=='.' }.contains('.'))) return
                else if (key == "0" && expression.isEmpty()) expression = "0"
                else expression += key
            }
        }
    }
}
