package com.brendigo.calculator.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.brendigo.calculator.core.*
import com.brendigo.calculator.data.CalculatorState
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.*

@Composable private fun ScrollScreen(content:@Composable ColumnScope.()->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        .padding(horizontal=24.dp,vertical=6.dp),
        verticalArrangement=Arrangement.spacedBy(14.dp),content=content)
}
@Composable private fun Hint(text:String,p:BrandPalette) =
    Text(text,color=p.muted,fontSize=13.sp)
@Composable private fun Answer(label:String,value:String,p:BrandPalette) {
    BrandCard(p,Modifier.fillMaxWidth()) {
        Text(label,color=p.muted,fontSize=13.sp)
        Spacer(Modifier.height(9.dp))
        Text(value,color=p.accent,fontSize=31.sp,fontWeight=FontWeight.Bold,
            maxLines=3)
    }
}
private fun num(text:String)=text.toDoubleOrNull()
private fun out(n:Double) = if(n.isFinite()) CalculatorEngine.grouped(CalculatorEngine.format(n)) else "Enter valid values"
@Composable private fun PickRow(items:List<String>,selected:String,p:BrandPalette,onPick:(String)->Unit) {
    Column(verticalArrangement=Arrangement.spacedBy(7.dp)) {
        items.chunked(3).forEach { row ->
            Row(horizontalArrangement=Arrangement.spacedBy(7.dp)) {
                row.forEach { option ->
                    BrandButton(option,p,{onPick(option)},Modifier.weight(1f),primary=option==selected)
                }
            }
        }
    }
}

@Composable fun ToolsPage(app:CalculatorState,p:BrandPalette) {
    val tools=listOf(
        Triple("Unit Converter","Length, weight, temperature and more","⇄"),
        Triple("Currency Converter","Convert with a rate you provide","$"),
        Triple("Percentage","Instant percentages","%"),
        Triple("Tip & Split","Share the bill with ease","÷"),
        Triple("Loan Planner","Monthly payment estimates","◈"),
        Triple("Discount","See savings and final prices","%↓"),
        Triple("VAT & Tax","Calculate tax and totals","+"),
        Triple("Date Calculator","Count days between dates","▦"),
        Triple("Graphing","Visualize mathematical functions","⌁"))
    ScrollScreen {
        Text("Tools",color=p.text,fontSize=28.sp,fontWeight=FontWeight.Bold)
        Hint("Everything you need, in one beautiful place.",p)
        tools.chunked(2).forEach { pair ->
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                pair.forEach { (name,description,symbol) ->
                    Column(Modifier.weight(1f).height(134.dp)
                        .background(p.surface,BrandShape).clickable {app.activePage=name}
                        .padding(15.dp)) {
                        Text(symbol,color=p.accent,fontSize=26.sp,fontWeight=FontWeight.Bold)
                        Spacer(Modifier.height(7.dp))
                        Text(name,color=p.text,fontSize=15.sp,fontWeight=FontWeight.SemiBold)
                        Text(description,color=p.muted,fontSize=11.sp,maxLines=2)
                    }
                }
                if(pair.size==1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
@Composable fun UnitConverterPage(p:BrandPalette) {
    var category by remember {mutableStateOf("Length")}
    var from by remember {mutableIntStateOf(0)}
    var to by remember {mutableIntStateOf(1)}
    var amount by remember {mutableStateOf("1")}
    val entries=UnitMath.categories.getValue(category)
    val computed=num(amount)?.let {UnitMath.convert(it,entries[from],entries[to])}
    ScrollScreen {
        PageTitle("Unit Converter",p,"Accurate everyday conversions")
        PickRow(UnitMath.categories.keys.toList(),category,p) {
            category=it;from=0;to=1
        }
        BrandTextInput("Value",amount,{amount=it},p)
        Hint("From",p)
        PickRow(entries.map {it.label},entries[from].label,p){selected->
            from=entries.indexOfFirst {it.label==selected}.coerceAtLeast(0)
        }
        Hint("To",p)
        PickRow(entries.map {it.label},entries[to].label,p){selected->
            to=entries.indexOfFirst {it.label==selected}.coerceAtLeast(0)
        }
        Answer("Converted result",computed?.let(::out) ?: "Enter a number",p)
        BrandButton("Swap units",p,{val old=from;from=to;to=old},Modifier.fillMaxWidth())
    }
}
@Composable fun CurrencyPage(p:BrandPalette) {
    var amount by remember {mutableStateOf("100")}
    var from by remember {mutableStateOf("EUR")}
    var to by remember {mutableStateOf("USD")}
    var rate by remember {mutableStateOf("1")}
    ScrollScreen {
        PageTitle("Currency Converter",p,"Your own exchange rate, no connection needed")
        Hint("Rates are entered manually. They are not live market quotes.",p)
        PickRow(listOf("EUR","USD","GBP","CHF","JPY","CAD"),from,p){from=it}
        BrandTextInput("Amount in "+from,amount,{amount=it},p)
        PickRow(listOf("EUR","USD","GBP","CHF","JPY","CAD"),to,p){to=it}
        BrandTextInput("1 "+from+" equals how many "+to+"?",rate,{rate=it},p)
        Answer("Converted amount",if(num(amount)!=null && num(rate)!=null &&
            num(rate)!!>0) out(num(amount)!!*num(rate)!!)+" "+to else "Enter a valid rate",p)
        BrandButton("Swap currencies",p,{
            val old=from;from=to;to=old
            val r=num(rate)
            if(r!=null && r!=0.0) rate=CalculatorEngine.format(1/r)
        },Modifier.fillMaxWidth())
    }
}
@Composable fun PercentagePage(p:BrandPalette) {
    var value by remember {mutableStateOf("860")}
    var percent by remember {mutableStateOf("15")}
    ScrollScreen {
        PageTitle("Percentage",p,"Calculate any percentage")
        BrandTextInput("Value",value,{value=it},p)
        BrandTextInput("Percentage (%)",percent,{percent=it},p)
        val x=num(value);val y=num(percent)
        Answer("Percentage of value",if(x!=null && y!=null)out(x*y/100) else "Enter valid values",p)
        Answer("Value plus percentage",if(x!=null && y!=null)out(x*(1+y/100)) else "—",p)
        Answer("Value minus percentage",if(x!=null && y!=null)out(x*(1-y/100)) else "—",p)
    }
}
@Composable fun TipPage(p:BrandPalette) {
    var total by remember {mutableStateOf("85")}
    var tip by remember {mutableStateOf("15")}
    var people by remember {mutableStateOf("2")}
    val result=FinancialMath.tip(num(total)?:-1.0,num(tip)?:-1.0,people.toIntOrNull()?:0)
    ScrollScreen {
        PageTitle("Tip & Split",p,"A fair split in seconds")
        BrandTextInput("Bill amount",total,{total=it},p)
        BrandTextInput("Tip (%)",tip,{tip=it},p)
        BrandTextInput("Number of people",people,{people=it},p)
        Answer("Total tip",out(result.first),p)
        Answer("Each person pays",out(result.second),p)
    }
}
@Composable fun LoanPage(p:BrandPalette) {
    var principal by remember {mutableStateOf("150000")}
    var rate by remember {mutableStateOf("4.5")}
    var years by remember {mutableStateOf("20")}
    val loan=num(principal)?:-1.0
    val length=years.toIntOrNull()?:0
    val payment=FinancialMath.monthlyPayment(loan,num(rate)?:-1.0,length)
    ScrollScreen {
        PageTitle("Loan Planner",p,"Plan payments with clarity")
        BrandTextInput("Loan amount",principal,{principal=it},p)
        BrandTextInput("Annual interest (%)",rate,{rate=it},p)
        BrandTextInput("Term (years)",years,{years=it},p)
        Answer("Monthly payment",out(payment),p)
        Answer("Total interest",out(payment*length*12-loan),p)
        Hint("Estimates only. Actual loan terms and fees may differ.",p)
    }
}
@Composable fun DiscountPage(p:BrandPalette) {
    var price by remember {mutableStateOf("200")}
    var discount by remember {mutableStateOf("15")}
    val x=num(price);val y=num(discount)
    ScrollScreen {
        PageTitle("Discount",p,"Know your savings instantly")
        BrandTextInput("Original price",price,{price=it},p)
        BrandTextInput("Discount (%)",discount,{discount=it},p)
        Answer("Final price",if(x!=null&&y!=null) out(FinancialMath.discounted(x,y)) else "—",p)
        Answer("You save",if(x!=null&&y!=null)out(x*y/100.0) else "—",p)
    }
}
@Composable fun VatPage(p:BrandPalette) {
    var net by remember {mutableStateOf("100")}
    var tax by remember {mutableStateOf("25")}
    val x=num(net);val y=num(tax)
    val result=if(x!=null&&y!=null)FinancialMath.vat(x,y) else null
    ScrollScreen {
        PageTitle("VAT & Tax",p,"Transparent totals")
        BrandTextInput("Amount before tax",net,{net=it},p)
        BrandTextInput("Tax rate (%)",tax,{tax=it},p)
        Answer("Tax amount",result?.let {out(it.first)} ?: "—",p)
        Answer("Total including tax",result?.let {out(it.second)} ?: "—",p)
        Hint("Enter the rate applicable to your transaction.",p)
    }
}
@Composable fun DatePage(p:BrandPalette) {
    var start by remember {mutableStateOf("2026-11-01")}
    var end by remember {mutableStateOf("2026-11-23")}
    val days=remember(start,end) {
        try {
            val fmt=SimpleDateFormat("yyyy-MM-dd",Locale.US)
            fmt.isLenient=false;fmt.timeZone=TimeZone.getTimeZone("UTC")
            val a=fmt.parse(start) ?: throw IllegalArgumentException()
            val b=fmt.parse(end) ?: throw IllegalArgumentException()
            (b.time-a.time)/86_400_000L
        } catch(_:Exception){null}
    }
    ScrollScreen {
        PageTitle("Date Calculator",p,"Count the days between dates")
        BrandTextInput("Start date (YYYY-MM-DD)",start,{start=it},p)
        BrandTextInput("End date (YYYY-MM-DD)",end,{end=it},p)
        Answer("Days between",days?.let {it.toString()+" days"} ?: "Use valid dates",p)
        Answer("Weeks between",days?.let{out(it/7.0)+" weeks"} ?: "—",p)
    }
}
@Composable fun GraphPage(p:BrandPalette) {
    var equation by remember {mutableStateOf("sin(x)")}
    val samples=remember(equation) {
        (0..240).mapNotNull { index ->
            val x=-10.0+20.0*index/240.0
            val expr=equation.replace(Regex("(?<![A-Za-z])x(?![A-Za-z])"),
                "("+CalculatorEngine.format(x)+")")
            val value=CalculatorEngine.calculate(expr,false).toDoubleOrNull()
            if(value!=null && value.isFinite()) (x to value) else null
        }
    }
    ScrollScreen {
        PageTitle("Graphing",p,"Explore functions visually")
        BrandTextInput("f(x)",equation,{equation=it},p)
        BrandCard(p,Modifier.fillMaxWidth()) {
            Canvas(Modifier.fillMaxWidth().height(310.dp)) {
                val w=size.width;val h=size.height
                drawLine(p.stroke,androidx.compose.ui.geometry.Offset(0f,h/2),
                    androidx.compose.ui.geometry.Offset(w,h/2),strokeWidth=2f)
                drawLine(p.stroke,androidx.compose.ui.geometry.Offset(w/2,0f),
                    androidx.compose.ui.geometry.Offset(w/2,h),strokeWidth=2f)
                var previous:androidx.compose.ui.geometry.Offset?=null
                samples.forEach { (x,y) ->
                    val pt=androidx.compose.ui.geometry.Offset(
                        ((x+10)/20*w).toFloat(),(h/2-y*h/20).toFloat())
                    if(previous!=null && pt.y in (-h)..(h*2) &&
                        previous!!.y in (-h)..(h*2)) drawLine(p.accent,previous!!,pt,strokeWidth=3f)
                    previous=pt
                }
            }
        }
        Hint("Use x, sin(x), cos(x), x^2, sqrt(x) and other supported functions.",p)
        if(samples.isEmpty()) Text("Check your function.",color=Color(0xFFFF978A))
    }
}
