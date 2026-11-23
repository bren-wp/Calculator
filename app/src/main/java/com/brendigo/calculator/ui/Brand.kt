package com.brendigo.calculator.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

data class BrandPalette(val background:Color,val surface:Color,val surface2:Color,
    val stroke:Color,val text:Color,val muted:Color,val accent:Color,val onAccent:Color)

fun palette(mode:String) = when(mode) {
    "Light" -> BrandPalette(Color(0xFFF1F5F5),Color.White,Color(0xFFE3EDEE),
        Color(0xFFCFDDE0),Color(0xFF132A34),Color(0xFF58707B),Color(0xFF079F88),Color.White)
    "AMOLED" -> BrandPalette(Color.Black,Color(0xFF101010),Color(0xFF1D1D1D),
        Color(0xFF333333),Color.White,Color(0xFFAAAAAA),Color(0xFF53E4C5),Color(0xFF092921))
    else -> BrandPalette(Color(0xFF101D26),Color(0xFF1B2D38),Color(0xFF263C48),
        Color(0xFF34505C),Color(0xFFF3F8FA),Color(0xFFA0B2BD),Color(0xFF53E4C5),Color(0xFF092921))
}

val BrandShape = RoundedCornerShape(15.dp)

@Composable fun BrandLogo(modifier:Modifier=Modifier, p:BrandPalette) {
    Column(modifier.aspectRatio(1f).background(p.surface2,RoundedCornerShape(9.dp)).padding(3.dp)) {
        listOf(listOf("+","−"),listOf("×","=")).forEachIndexed { row, data ->
            Row(Modifier.weight(1f)) {
                data.forEachIndexed { col, label ->
                    Box(Modifier.weight(1f).fillMaxHeight().padding(1.dp).background(
                        if(row==1 && col==1) p.accent else p.surface,RoundedCornerShape(3.dp)),
                        contentAlignment=Alignment.Center) {
                        Text(label,color=if(row==1 && col==1) p.onAccent else p.text,
                            fontSize=17.sp,fontWeight=FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable fun PageTitle(title:String, p:BrandPalette, subtitle:String?=null) {
    Column(Modifier.fillMaxWidth().padding(start=24.dp,end=24.dp,top=12.dp,bottom=16.dp)) {
        Text(title,color=p.text,fontSize=29.sp,fontWeight=FontWeight.Bold)
        if (subtitle != null) Text(subtitle,color=p.muted,fontSize=13.sp)
    }
}

@Composable fun BrandCard(p:BrandPalette, modifier:Modifier=Modifier, content:@Composable ColumnScope.()->Unit) {
    Column(modifier.background(p.surface,BrandShape)
        .border(0.6.dp,p.stroke,BrandShape).padding(16.dp),content=content)
}

@Composable fun BrandButton(label:String,p:BrandPalette,onClick:()->Unit,modifier:Modifier=Modifier,
    primary:Boolean=false,muted:Boolean=false,enabled:Boolean=true) {
    Box(modifier.background(if(primary) p.accent else if(muted) p.surface2 else p.surface,BrandShape)
        .border(if(primary)0.dp else 0.5.dp,p.stroke,BrandShape)
        .clickable(enabled=enabled,onClick=onClick).padding(horizontal=10.dp,vertical=12.dp),
        contentAlignment=Alignment.Center) {
        Text(label,color=if(primary) p.onAccent else p.text,fontSize=16.sp,
            fontWeight=FontWeight.SemiBold,textAlign=TextAlign.Center,maxLines=2)
    }
}

@Composable fun BrandTextInput(label:String,value:String,onChange:(String)->Unit,p:BrandPalette,
    modifier:Modifier=Modifier,singleLine:Boolean=true) {
    Column(modifier) {
        Text(label,color=p.muted,fontSize=12.sp,modifier=Modifier.padding(bottom=6.dp))
        OutlinedTextField(value=value,onValueChange=onChange,singleLine=singleLine,
            modifier=Modifier.fillMaxWidth(),shape=BrandShape,
            textStyle=androidx.compose.ui.text.TextStyle(color=p.text,fontSize=18.sp),
            colors=OutlinedTextFieldDefaults.colors(
                focusedBorderColor=p.accent, unfocusedBorderColor=p.stroke,
                focusedContainerColor=p.surface,unfocusedContainerColor=p.surface,
                cursorColor=p.accent,focusedTextColor=p.text,unfocusedTextColor=p.text))
    }
}

@Composable fun BrandOption(title:String, subtitle:String, p:BrandPalette, onClick:()->Unit, trailing:String="›") {
    Row(Modifier.fillMaxWidth().padding(horizontal=18.dp,vertical=5.dp)
        .background(p.surface,BrandShape).clickable(onClick=onClick)
        .padding(horizontal=16.dp,vertical=17.dp),verticalAlignment=Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title,color=p.text,fontSize=16.sp,fontWeight=FontWeight.SemiBold)
            if(subtitle.isNotBlank()) Text(subtitle,color=p.muted,fontSize=12.sp)
        }
        Text(trailing,color=p.accent,fontSize=20.sp)
    }
}

@Composable fun BrandToggle(label:String,description:String,p:BrandPalette,
    checked:Boolean,onCheckedChange:(Boolean)->Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal=18.dp,vertical=6.dp)
        .background(p.surface,BrandShape).padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label,color=p.text,fontSize=16.sp,fontWeight=FontWeight.Medium)
            Text(description,color=p.muted,fontSize=12.sp)
        }
        Switch(checked=checked,onCheckedChange=onCheckedChange,
            colors=SwitchDefaults.colors(checkedThumbColor=p.onAccent,checkedTrackColor=p.accent))
    }
}
