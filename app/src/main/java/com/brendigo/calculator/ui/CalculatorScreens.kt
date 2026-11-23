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
            BrandCard(p,Modifier.fillMaxWidth().weight(if(app.activeMode=="Scientific")0.72f else 1f)) {
                Box(Modifier.fillMaxSize(),contentAlignment=Alignment.BottomEnd) {
                    Column(horizontalAlignment=Alignment.End) {
                        Text(app.expression.ifBlank { "0" },color=p.muted,fontSize=24.sp,
                            textAlign=TextAlign.End,maxLines=2)
                        Spacer(Modifier.height(16.dp))
                        val result=if(app.expression.isBlank())"0" else app.calculate()
                        Text(if(app.thousands) CalculatorEngine.grouped(result) else result,
                            color=if(result=="Error")Color(0xFFFF978A) else p.text,
                            fontSize=if(result.length>12)39.sp else 51.sp,
                            fontWeight=FontWeight.Bold,maxLines=2,textAlign=TextAlign.End)
                        Spacer(Modifier.height(8.dp))
                        Text("Instant result",color=p.accent,fontSize=12.sp)
                    }
                }
            }
            Spacer(Modifier.height(13.dp))
            if (app.activeMode=="Scientific") {
                val extra=listOf(
                    listOf("sin","cos","tan","π"),
                    listOf("ln","log","sqrt","xʸ"),
                    listOf("x²","n!","1/x","e"))
                extra.forEach { row ->
                    KeypadRow(row,p,Modifier.weight(0.42f),onKey,true)
                    Spacer(Modifier.height(5.dp))
                }
            }
            val rows=listOf(
                listOf("AC","( )","%","÷"),
                listOf("7","8","9","×"),
                listOf("4","5","6","−"),
                listOf("1","2","3","+"),
                listOf("0",".","⌫","="))
            rows.forEachIndexed { idx,row ->
                KeypadRow(row,p,Modifier.weight(1f),onKey,false)
                if(idx<rows.lastIndex) Spacer(Modifier.height(9.dp))
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
@Composable private fun KeypadRow(row:List<String>,p:BrandPalette,modifier:Modifier,
    onKey:(String)->Unit,scientific:Boolean) {
    Row(modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)) {
        row.forEach { label ->
            val primary=label=="="
            val operator=label in listOf("÷","×","−","+","AC")
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
    var input by remember {mutableStateOf("255")}
    var base by remember {mutableIntStateOf(10)}
    var operand by remember {mutableStateOf("1")}
    var result by remember {mutableStateOf("")}
    val value=remember(input,base) {runCatching {BigInteger(input.trim(),base)}.getOrNull()}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Number systems",fontSize=20.sp,color=p.text,fontWeight=FontWeight.SemiBold)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(9.dp)) {
            listOf(2,8,10,16).forEach { n ->
                BrandButton(when(n){2->"BIN";8->"OCT";10->"DEC";else->"HEX"},p,
                    onClick={if(value!=null){input=value.toString(n).uppercase(Locale.US);base=n}},
                    Modifier.weight(1f),primary=base==n)
            }
        }
        Spacer(Modifier.height(18.dp))
        BrandTextInput("Input ("+when(base){2->"Binary";8->"Octal";10->"Decimal";else->"Hexadecimal"}+")",
            input,{input=it.uppercase(Locale.US)},p)
        Spacer(Modifier.height(15.dp))
        if(value==null) Text("Invalid digits for this base",color=Color(0xFFFF978A))
        listOf(16 to "HEX",10 to "DEC",8 to "OCT",2 to "BIN").forEach { (n,label) ->
            BrandCard(p,Modifier.fillMaxWidth().padding(bottom=8.dp)) {
                Text(label,color=p.accent,fontSize=12.sp)
                Text(value?.toString(n)?.uppercase(Locale.US) ?: "—",color=p.text,
                    fontSize=22.sp,fontWeight=FontWeight.SemiBold)
            }
        }
        BrandTextInput("Bitwise operand (decimal)",operand,{operand=it},p)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            listOf("AND","OR","XOR","NOT").forEach { op ->
                BrandButton(op,p,onClick={
                    val a=value;val b=operand.toBigIntegerOrNull()
                    result=if(a==null) "Invalid number" else when(op) {
                        "NOT" -> a.not().toString()
                        "AND" -> b?.let{a.and(it).toString()} ?: "Invalid operand"
                        "OR" -> b?.let{a.or(it).toString()} ?: "Invalid operand"
                        else -> b?.let{a.xor(it).toString()} ?: "Invalid operand"
                    }
                },Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(result,color=p.accent,fontSize=22.sp)
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
