package com.brendigo.calculator.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.brendigo.calculator.data.CalculatorState

@Composable private fun SettingsScroll(content:@Composable ColumnScope.()->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        .padding(bottom=16.dp),content=content)
}

@Composable fun SettingsPage(app:CalculatorState,p:BrandPalette) {
    SettingsScroll {
        PageTitle("Settings",p,"Make Calculator your own")
        BrandOption("Appearance","Themes and color schemes",p,{app.activePage="Appearance"},app.theme)
        BrandOption("Calculation Preferences","Angles, results and number formatting",p,
            {app.activePage="Calculation Preferences"})
        BrandToggle("Vibration","Haptic feedback on key press",p,app.vibration,app::setVibration)
        BrandToggle("Sound","Key press sounds",p,app.sounds,app::setSounds)
        BrandOption("Quick Access","Open Calculator with fewer taps",p,{app.activePage="Quick Access"})
        BrandOption("Widgets","Calculator at a glance",p,{app.activePage="Widgets"})
        BrandOption("Privacy & Data","Manage your locally stored history",p,{app.activePage="Privacy & Data"})
        BrandOption("About","Discover more about Calculator",p,{app.activePage="About"})
    }
}

@Composable fun AppearancePage(app:CalculatorState,p:BrandPalette) {
    SettingsScroll {
        PageTitle("Appearance",p,"Choose the look that feels right")
        listOf(
            Triple("Dark","Subtle surfaces and balanced contrast",Color(0xFF101D26)),
            Triple("Light","Fresh, open and easy to read",Color(0xFFF1F5F5)),
            Triple("AMOLED","Pure black with vibrant accents",Color.Black)
        ).forEach { (name,description,swatch) ->
            Row(Modifier.fillMaxWidth().padding(horizontal=24.dp,vertical=7.dp)
                .background(p.surface,BrandShape).clickable {app.setTheme(name)}
                .padding(18.dp),verticalAlignment=Alignment.CenterVertically) {
                Box(Modifier.size(64.dp).background(swatch,RoundedCornerShape(12.dp))
                    .border(1.dp,p.stroke,RoundedCornerShape(12.dp)),
                    contentAlignment=Alignment.Center) {
                    Text("=",color=Color(0xFF53E4C5),fontSize=32.sp,fontWeight=FontWeight.Bold)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(name,color=p.text,fontSize=17.sp,fontWeight=FontWeight.Bold)
                    Text(description,color=p.muted,fontSize=12.sp)
                }
                Text(if(app.theme==name)"●" else "○",color=p.accent,fontSize=25.sp)
            }
        }
    }
}

@Composable fun PreferencesPage(app:CalculatorState,p:BrandPalette) {
    SettingsScroll {
        PageTitle("Calculation Preferences",p,"Precision in every detail")
        Text("Angle unit",color=p.muted,fontSize=13.sp,
            modifier=Modifier.padding(horizontal=24.dp,vertical=9.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal=24.dp),
            horizontalArrangement=Arrangement.spacedBy(9.dp)) {
            BrandButton("Degrees",p,{app.setDegrees(true)},Modifier.weight(1f),primary=app.degrees)
            BrandButton("Radians",p,{app.setDegrees(false)},Modifier.weight(1f),primary=!app.degrees)
        }
        Spacer(Modifier.height(12.dp))
        BrandToggle("Digit grouping","Separate thousands with commas",p,app.thousands,app::setThousands)
        BrandCard(p,Modifier.padding(24.dp).fillMaxWidth()) {
            Text("Number examples",color=p.muted,fontSize=13.sp)
            Text(if(app.thousands)"1,234,567.89" else "1234567.89",
                color=p.accent,fontSize=25.sp,fontWeight=FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Text("Results are rounded to 10 decimal places for display.",
                color=p.muted,fontSize=12.sp)
        }
    }
}

@Composable fun PrivacyPage(app:CalculatorState,p:BrandPalette) {
    var deletePrompt by remember {mutableStateOf(false)}
    SettingsScroll {
        PageTitle("Privacy & Data",p,"Your calculations belong to you")
        BrandCard(p,Modifier.fillMaxWidth().padding(24.dp)) {
            Text("Private by design",color=p.text,fontSize=19.sp,fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("Calculations, favorites and preferences are saved only on this device. " +
                "Calculator does not require an account, analytics, location, or an internet connection.",
                color=p.muted,fontSize=14.sp)
        }
        BrandOption("Saved calculations",app.history.size.toString()+" entries",p,
            {app.activePage="History"})
        BrandOption("Delete history","Permanently remove all saved calculations",p,
            {deletePrompt=true},"×")
    }
    if(deletePrompt) AlertDialog(onDismissRequest={deletePrompt=false},
        title={Text("Delete all calculations?")},
        text={Text("This cannot be undone.")},
        confirmButton={TextButton(onClick={app.clearHistory();deletePrompt=false}){Text("Delete")}},
        dismissButton={TextButton(onClick={deletePrompt=false}){Text("Cancel")}})
}

@Composable fun QuickAccessPage(app:CalculatorState,p:BrandPalette) {
    SettingsScroll {
        PageTitle("Quick Access",p,"Your tools, one tap away")
        BrandCard(p,Modifier.fillMaxWidth().padding(horizontal=24.dp)) {
            Text("Make it yours",color=p.text,fontSize=21.sp,fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            Text("Pin Calculator to your home screen or app dock for instant access.",
                color=p.muted,fontSize=14.sp)
        }
        BrandOption("Standard Calculator","Everyday calculations",p,
            {app.activeMode="Standard";app.activePage="Calculator"})
        BrandOption("Scientific Calculator","Advanced math tools",p,
            {app.activeMode="Scientific";app.activePage="Calculator"})
        BrandOption("Programmer Calculator","Binary, hexadecimal and bitwise math",p,
            {app.activeMode="Programmer";app.activePage="Calculator"})
        BrandCard(p,Modifier.fillMaxWidth().padding(horizontal=24.dp)) {
            Text("Calculator app preference",color=p.text,fontSize=17.sp,fontWeight=FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text("Some device launchers let you choose which calculator opens for calculator actions. " +
                "This is controlled by your device, and may not be available everywhere.",
                color=p.muted,fontSize=13.sp)
        }
    }
}

@Composable fun AboutPage(app:CalculatorState,p:BrandPalette) {
    val uriHandler=LocalUriHandler.current
    SettingsScroll {
        Column(Modifier.fillMaxWidth().padding(top=42.dp,bottom=35.dp),
            horizontalAlignment=Alignment.CenterHorizontally) {
            BrandLogo(Modifier.size(112.dp),p)
            Spacer(Modifier.height(19.dp))
            Text("Calculator",color=p.text,fontSize=36.sp,fontWeight=FontWeight.Bold)
            Text("Beautifully simple. Powerfully precise.",
                color=p.muted,fontSize=14.sp)
            Spacer(Modifier.height(14.dp))
            Text("Version 1.0.0",color=p.accent,fontSize=15.sp)
        }
        BrandOption("Privacy","Your data stays on this device",p,{app.activePage="Privacy & Data"})
        BrandOption("Source & licenses","Open-source MIT license",p,{uriHandler.openUri("https://github.com/bren-wp/Calculator/blob/main/LICENSE")})
        Spacer(Modifier.height(60.dp))
        Text("© 2026 Calculator",color=p.muted,fontSize=12.sp,
            modifier=Modifier.fillMaxWidth(),textAlign=androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable fun WidgetsPage(p:BrandPalette) {
    SettingsScroll {
        PageTitle("Widgets",p,"Your favorite tool, always close")
        BrandCard(p,Modifier.fillMaxWidth().padding(horizontal=24.dp)) {
            Text("Home screen shortcut",color=p.text,fontSize=19.sp,fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Long-press an empty area of your home screen, choose Widgets, " +
                "then find Calculator. The widget opens the calculator with one tap.",
                color=p.muted,fontSize=14.sp)
        }
        Spacer(Modifier.height(12.dp))
        BrandCard(p,Modifier.fillMaxWidth().padding(horizontal=24.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) {
                BrandLogo(Modifier.size(64.dp),p)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Calculator",color=p.text,fontSize=18.sp,fontWeight=FontWeight.Bold)
                    Text("Tap to calculate",color=p.accent,fontSize=13.sp)
                }
            }
        }
    }
}
