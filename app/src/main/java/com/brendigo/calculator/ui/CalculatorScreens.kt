package com.brendigo.calculator.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.brendigo.calculator.core.CalculatorEngine
import com.brendigo.calculator.data.CalculatorState
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable fun CalculatorPage(app:CalculatorState,p:BrandPalette,onKey:(String)->Unit) {
    var secondMode by remember { mutableStateOf(false) }
    val scientific = app.activeMode == "Scientific"
    Column(Modifier.fillMaxSize().padding(horizontal=24.dp)) {
        Row(Modifier.fillMaxWidth().padding(top=5.dp,bottom=12.dp),
            horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            listOf("Standard","Scientific","Programmer").forEach { mode ->
                val selected=app.activeMode==mode
                Box(Modifier.weight(1f)
                    .background(if(selected)p.accent else p.surface,RoundedCornerShape(22.dp))
                    .clickable {app.activeMode=mode}.padding(vertical=11.dp),
                    contentAlignment=Alignment.Center) {
                    Text(mode,color=if(selected)p.onAccent else p.muted,
                        fontSize=12.sp,fontWeight=FontWeight.SemiBold)
                }
            }
        }
        if(app.activeMode=="Programmer") {
            ProgrammerPanel(p)
        } else {
            BrandCard(p,Modifier.fillMaxWidth().weight(if(scientific)2.8f else 2.4f)) {
                Box(Modifier.fillMaxSize()) {
                    Column(Modifier.align(Alignment.CenterEnd),
                        horizontalAlignment=Alignment.End) {
                        Text(app.expression.ifBlank { "0" },color=p.muted,fontSize=24.sp,
                            textAlign=TextAlign.End,maxLines=2)
                        Spacer(Modifier.height(if(scientific) 12.dp else 18.dp))
                        val result=if(app.expression.isBlank())"0" else app.calculate()
                        Text(if(app.thousands) CalculatorEngine.grouped(result) else result,
                            color=if(result=="Error")Color(0xFFFF978A) else p.text,
                            fontSize=if(result.length>12)39.sp else 54.sp,
                            fontWeight=FontWeight.Bold,maxLines=2,textAlign=TextAlign.End)
                    }
                    if (!scientific) Text("Instant result",color=p.accent,fontSize=12.sp,
                        modifier=Modifier.align(Alignment.BottomStart))
                }
            }
            Spacer(Modifier.height(if(scientific) 10.dp else 13.dp))
            if (scientific) {
                val extra=listOf(
                    listOf("DEG","RAD","2nd","π","e"),
                    if(secondMode) listOf("asin","acos","atan","log","ln")
                    else listOf("sin","cos","tan","log","ln"),
                    listOf("x²","xʸ","√","n!","1/x"))
                val selected=buildSet {
                    add(if(app.degrees)"DEG" else "RAD")
                    if(secondMode)add("2nd")
                }
                extra.forEach { row ->
                    KeypadRow(row,p,Modifier.weight(0.95f),{ key ->
                        when (key) {
                            "DEG" -> app.updateDegrees(true)
                            "RAD" -> app.updateDegrees(false)
                            "2nd" -> secondMode = !secondMode
                            "√" -> onKey("sqrt")
                            else -> onKey(key)
                        }
                    },true,selected)
                    Spacer(Modifier.height(6.dp))
                }
            }
            val rows=if(scientific) listOf(
                listOf("AC","(",")","÷"),
                listOf("7","8","9","×"),
                listOf("4","5","6","−"),
                listOf("1","2","3","+"),
                listOf("0",".","⌫","="))
            else listOf(
                listOf("AC","( )","%","÷"),
                listOf("7","8","9","×"),
                listOf("4","5","6","−"),
                listOf("1","2","3","+"),
                listOf("0",".","⌫","="))
            rows.forEachIndexed { idx,row ->
                KeypadRow(row,p,Modifier.weight(if(scientific)0.9f else 1f),onKey,false)
                if(idx<rows.lastIndex) Spacer(Modifier.height(if(scientific) 6.dp else 9.dp))
            }
            Spacer(Modifier.height(if(scientific) 10.dp else 20.dp))
        }
    }
}
@Composable private fun KeypadRow(row:List<String>,p:BrandPalette,modifier:Modifier,
    onKey:(String)->Unit,scientific:Boolean,selected:Set<String> = emptySet()) {
    Row(modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)) {
        row.forEach { label ->
            val primary=label=="="
            val active=label in selected
            val operator=label in listOf("÷","×","−","+","AC") || active
            Box(Modifier.weight(1f).fillMaxHeight().background(
                if(primary)p.accent else if(operator)p.surface2 else p.surface,BrandShape)
                .clickable {onKey(label)}.padding(3.dp),
                contentAlignment=Alignment.Center) {
                Text(label,color=if(primary)p.onAccent else if(operator)p.accent else p.text,
                    fontSize=if(scientific)17.sp else if(label.length>2)21.sp else 29.sp,
                    fontWeight=FontWeight.SemiBold,maxLines=1)
            }
        }
    }
}

@Composable private fun ProgrammerPanel(p:BrandPalette) {
    // Each base represents the same underlying integer. All calculations stay local.
    var base by remember { mutableIntStateOf(16) }
    var input by remember { mutableStateOf("109") }
    var preview by remember { mutableStateOf("HEX FF + 0A") }
    var pending by remember { mutableStateOf<String?>(null) }
    var stored by remember { mutableStateOf<BigInteger?>(null) }
    var newOperand by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val number = remember(input, base) {
        runCatching { BigInteger(input, base) }.getOrNull()
    }
    val options = listOf(16 to "HEX", 10 to "DEC", 8 to "OCT", 2 to "BIN")
    val keypad = listOf(
        listOf("AND", "OR", "XOR", "NOT"),
        listOf("SHL", "SHR", "MOD", "±"),
        listOf("A", "B", "C", "D"),
        listOf("E", "F", "7", "8"),
        listOf("9", "4", "5", "6"),
        listOf("1", "2", "3", "+"),
        listOf("0", "⌫", "AC", "=")
    )

    fun keyPressed(key: String) {
        error = null
        when (key) {
            "AC" -> {
                input = "0"; preview = ""; pending = null
                stored = null; newOperand = true
            }
            "⌫" -> {
                input = input.dropLast(1).ifEmpty { "0" }
                newOperand = false
                preview = ""
            }
            "±" -> {
                if (number != null) input = number.negate().toString(base).uppercase(Locale.US)
                preview = ""
            }
            "NOT" -> {
                if (number != null) {
                    val original = input
                    input = number.not().toString(base).uppercase(Locale.US)
                    preview = "NOT $original"
                    newOperand = true
                }
            }
            "AND", "OR", "XOR", "SHL", "SHR", "MOD", "+" -> {
                if (number != null) {
                    stored = number
                    pending = key
                    preview = "${number.toString(base).uppercase(Locale.US)} $key"
                    newOperand = true
                }
            }
            "=" -> {
                val lhs = stored
                val rhs = number
                val operation = pending
                if (lhs != null && rhs != null && operation != null) {
                    val answer: BigInteger? = when (operation) {
                        "AND" -> lhs.and(rhs)
                        "OR" -> lhs.or(rhs)
                        "XOR" -> lhs.xor(rhs)
                        "+" -> lhs.add(rhs)
                        "MOD" -> if (rhs > BigInteger.ZERO) lhs.mod(rhs) else null
                        "SHL" -> if (rhs >= BigInteger.ZERO && rhs <= BigInteger.valueOf(1024))
                            lhs.shiftLeft(rhs.toInt()) else null
                        "SHR" -> if (rhs >= BigInteger.ZERO && rhs <= BigInteger.valueOf(1024))
                            lhs.shiftRight(rhs.toInt()) else null
                        else -> null
                    }
                    if (answer == null) {
                        error = "Invalid operand"
                    } else {
                        preview = "${lhs.toString(base).uppercase(Locale.US)} $operation ${rhs.toString(base).uppercase(Locale.US)}"
                        input = answer.toString(base).uppercase(Locale.US)
                        stored = null; pending = null; newOperand = true
                    }
                }
            }
            else -> {
                val digit = key.singleOrNull()?.digitToIntOrNull(16)
                if (digit != null && digit < base) {
                    val updated = if (newOperand || input == "0") key else input + key
                    if (updated.length <= 64) {
                        input = updated
                        newOperand = false
                        if (pending == null) preview = ""
                    }
                }
            }
        }
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BrandCard(p, Modifier.fillMaxWidth().weight(2.4f)) {
            Box(Modifier.fillMaxSize()) {
                Text(if (error != null) error!! else preview,
                    color = p.muted, fontSize = 18.sp, maxLines = 2,
                    textAlign = TextAlign.End, modifier = Modifier.align(Alignment.TopEnd))
                Text(number?.toString(base)?.uppercase(Locale.US) ?: "Error",
                    color = if (error != null) Color(0xFFFF978A) else p.text,
                    fontSize = if (input.length > 10) 31.sp else 52.sp,
                    fontWeight = FontWeight.Bold, maxLines = 2,
                    textAlign = TextAlign.End, modifier = Modifier.align(Alignment.BottomEnd))
            }
        }
        Row(Modifier.fillMaxWidth().weight(1.05f),
            horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            options.forEach { (option, label) ->
                val value = number?.toString(option)?.uppercase(Locale.US) ?: "—"
                Column(
                    Modifier.weight(1f).fillMaxHeight()
                        .background(if (base == option) p.surface2 else p.surface, BrandShape)
                        .clickable {
                            if (number != null) {
                                input = number.toString(option).uppercase(Locale.US)
                                base = option
                                newOperand = true
                                preview = ""
                            }
                        }.padding(horizontal = 4.dp, vertical = 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(label, color = p.accent, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(value, color = p.text, fontWeight = FontWeight.SemiBold,
                        fontSize = if (value.length > 10) 9.sp else 14.sp, maxLines = 1)
                }
            }
        }
        keypad.forEach { row ->
            Row(Modifier.fillMaxWidth().weight(0.9f),
                horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                row.forEach { key ->
                    val digit = key.singleOrNull()?.digitToIntOrNull(16)
                    val enabled = digit == null || digit < base
                    val primary = key == "="
                    val operator = key in listOf("AND", "OR", "XOR", "NOT", "SHL", "SHR", "MOD", "±", "+")
                    Box(Modifier.weight(1f).fillMaxHeight()
                        .background(
                            if (primary) p.accent
                            else if (operator) p.surface2
                            else p.surface, BrandShape
                        )
                        .clickable(enabled = enabled) { keyPressed(key) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(key, fontSize = if (key.length > 2) 17.sp else 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (!enabled) p.stroke
                                else if (primary) p.onAccent
                                else if (operator) p.accent else p.text)
                    }
                }
            }
        }
        Spacer(Modifier.height(7.dp))
    }
}

@Composable fun HistoryPage(app:CalculatorState,p:BrandPalette,favoritesOnly:Boolean) {
    var search by remember {mutableStateOf("")}
    var clearPrompt by remember {mutableStateOf(false)}
    val clipboard= LocalClipboardManager.current
    Column(Modifier.fillMaxSize()) {
        PageTitle(if(favoritesOnly)"Favorites" else "History",p,
            if(favoritesOnly)"Saved calculations" else "Your past calculations")
        BrandTextInput("Search history",search,{search=it},p,Modifier.padding(horizontal=24.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal=24.dp,vertical=12.dp),
            horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            BrandButton("All",p,onClick={app.activePage="History"},Modifier.weight(1f),primary=!favoritesOnly)
            BrandButton("★ Favorites",p,onClick={app.activePage="Favorites"},Modifier.weight(1f),primary=favoritesOnly)
        }
        val filtered=app.history.withIndex().filter { (_,e) ->
            (!favoritesOnly || e.starred) &&
                (e.expression.contains(search,true)||e.result.contains(search,true))
        }
        if(filtered.isEmpty()) {
            Column(Modifier.weight(1f).fillMaxWidth(),verticalArrangement=Arrangement.Center,
                horizontalAlignment=Alignment.CenterHorizontally) {
                Text(if(favoritesOnly)"☆" else "◷",color=p.accent,fontSize=48.sp)
                Text(if(favoritesOnly)"No favorites yet" else "No calculations yet",
                    color=p.text,fontSize=19.sp,fontWeight=FontWeight.SemiBold)
                Text("Your calculations stay on this device.",color=p.muted,fontSize=13.sp)
            }
        } else LazyColumn(Modifier.weight(1f).padding(horizontal=24.dp),
            verticalArrangement=Arrangement.spacedBy(9.dp)) {
            itemsIndexed(filtered,key={_,item->item.value.time}) { _, indexed ->
                val e=indexed.value
                BrandCard(p,Modifier.fillMaxWidth().clickable {app.useHistory(e)}) {
                    Text(e.expression,color=p.muted,fontSize=16.sp,maxLines=2)
                    Spacer(Modifier.height(5.dp))
                    Text("= "+e.result,color=p.accent,fontSize=24.sp,fontWeight=FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Text(SimpleDateFormat("MMM d, yyyy  HH:mm",Locale.US).format(Date(e.time)),
                            color=p.muted,fontSize=11.sp,modifier=Modifier.weight(1f))
                        Text(if(e.starred)"★" else "☆",color=p.accent,fontSize=24.sp,
                            modifier=Modifier.clickable {app.toggleFavorite(indexed.index)}.padding(end=16.dp))
                        Text("Copy",color=p.muted,fontSize=13.sp,
                            modifier=Modifier.clickable {clipboard.setText(AnnotatedString(e.result))})
                    }
                }
            }
        }
        if(!favoritesOnly && app.history.isNotEmpty()) {
            BrandButton("Clear History",p,onClick={clearPrompt=true},
                Modifier.fillMaxWidth().padding(24.dp))
        }
    }
    if(clearPrompt) AlertDialog(onDismissRequest={clearPrompt=false},
        title={Text("Clear history?")},text={Text("This permanently removes all saved calculations.")},
        confirmButton={TextButton(onClick={app.clearHistory();clearPrompt=false}){Text("Clear")}},
        dismissButton={TextButton(onClick={clearPrompt=false}){Text("Cancel")}})
}
