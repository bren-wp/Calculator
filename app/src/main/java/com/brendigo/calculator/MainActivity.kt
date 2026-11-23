package com.brendigo.calculator

import android.os.Bundle
import android.media.AudioManager
import android.media.ToneGenerator
import android.content.pm.ApplicationInfo
import com.brendigo.calculator.data.HistoryEntry
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.brendigo.calculator.data.CalculatorState
import com.brendigo.calculator.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val app = CalculatorState(applicationContext)
        val requestedMode = intent.getStringExtra("mode")
        if (requestedMode in listOf("Standard","Scientific","Programmer")) {
            app.activeMode = requestedMode!!
        }
        // Non-production image comparison entry points. Never shown in user-facing navigation.
        if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            val preview = intent.getStringExtra("preview_screen")
            val pages = setOf("History","Favorites","Tools","Unit Converter",
                "Currency Converter","Percentage","Tip & Split","Loan Planner",
                "Discount","VAT & Tax","Date Calculator","Graphing",
                "Settings","Appearance","Calculation Preferences",
                "Quick Access","Privacy & Data","About","Widgets")
            if (preview in pages) app.activePage = preview!!
            if (preview=="Scientific Calculator") app.activeMode = "Scientific"
            if (preview=="Programmer Calculator") app.activeMode = "Programmer"
            if (preview=="Light Theme") app.updateTheme("Light")
            else if (preview=="AMOLED Theme") app.updateTheme("AMOLED")
            else app.updateTheme("Dark")
            if (preview in listOf("History","Favorites")) {
                app.history.clear()
                app.history.addAll(listOf(
                    HistoryEntry("128×8.5","1088",System.currentTimeMillis(),true),
                    HistoryEntry("(50+25)×4","300",System.currentTimeMillis()-60000),
                    HistoryEntry("sqrt(144)","12",System.currentTimeMillis()-120000)))
            }
            if (preview=="Empty History") { app.activePage="History";app.history.clear() }
            if (preview=="Error State") app.expression="1÷0"
            if (preview=="Standard Calculator" || preview in listOf("Light Theme","AMOLED Theme"))
                app.expression="128×8.5"
            if (preview=="Scientific Calculator") app.expression="sin(30)+2^2"
        }
        setContent {
            val p = palette(app.theme)
            SideEffect {
                window.statusBarColor = p.background.toArgb()
                window.navigationBarColor = p.background.toArgb()
                WindowInsetsControllerCompat(window,window.decorView)
                    .isAppearanceLightStatusBars = app.theme=="Light"
            }
            val colors = if (app.theme == "Light") lightColorScheme(
                primary=p.accent, onPrimary=p.onAccent, background=p.background,
                onBackground=p.text, surface=p.surface, onSurface=p.text,
                surfaceVariant=p.surface2, onSurfaceVariant=p.muted)
            else darkColorScheme(primary=p.accent, onPrimary=p.onAccent,
                background=p.background, onBackground=p.text, surface=p.surface,
                onSurface=p.text, surfaceVariant=p.surface2, onSurfaceVariant=p.muted)
            MaterialTheme(colorScheme=colors) { CalculatorRoot(app) }
        }
    }
}

@Composable private fun CalculatorRoot(app:CalculatorState) {
    val p = palette(app.theme)
    val haptics = LocalHapticFeedback.current
    val tone = remember { ToneGenerator(AudioManager.STREAM_MUSIC,20) }
    DisposableEffect(tone) { onDispose { tone.release() } }
    BackHandler(app.activePage!="Calculator") { app.activePage="Calculator" }
    Column(Modifier.fillMaxSize().background(p.background)
        .statusBarsPadding().navigationBarsPadding()) {
        BrandTopBar(app,p)
        Box(Modifier.weight(1f)) {
            when(app.activePage) {
                "Calculator" -> CalculatorPage(app,p) {
                    if(app.vibration) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if(app.sounds) tone.startTone(ToneGenerator.TONE_PROP_BEEP,25)
                    app.press(it)
                }
                "History" -> HistoryPage(app,p,false)
                "Favorites" -> HistoryPage(app,p,true)
                "Tools" -> ToolsPage(app,p)
                "Settings" -> SettingsPage(app,p)
                "Appearance" -> AppearancePage(app,p)
                "Calculation Preferences" -> PreferencesPage(app,p)
                "Privacy & Data" -> PrivacyPage(app,p)
                "Quick Access" -> QuickAccessPage(app,p)
                "About" -> AboutPage(app,p)
                "Unit Converter" -> UnitConverterPage(p)
                "Currency Converter" -> CurrencyPage(p)
                "Percentage" -> PercentagePage(p)
                "Tip & Split" -> TipPage(p)
                "Loan Planner" -> LoanPage(p)
                "Discount" -> DiscountPage(p)
                "VAT & Tax" -> VatPage(p)
                "Date Calculator" -> DatePage(p)
                "Graphing" -> GraphPage(p)
                "Widgets" -> WidgetsPage(p)
                else -> ToolsPage(app,p)
            }
        }
        BrandBottomNavigation(app,p)
    }
    if (app.errorMessage != null) {
        AlertDialog(onDismissRequest={app.errorMessage=null},
            title={Text("Calculation error")},text={Text(app.errorMessage.orEmpty())},
            confirmButton={TextButton(onClick={app.errorMessage=null}){Text("OK")}})
    }
}

@Composable private fun BrandTopBar(app:CalculatorState,p:BrandPalette) {
    Row(Modifier.fillMaxWidth().height(76.dp).padding(horizontal=24.dp),
        verticalAlignment=Alignment.CenterVertically) {
        if (app.activePage !in listOf("Calculator","Tools","History","Settings")) {
            Text("‹",modifier=Modifier.clickable {
                app.activePage=when(app.activePage) {
                    "Favorites" -> "History"
                    "Appearance","Calculation Preferences","Privacy & Data","Quick Access","About","Widgets" -> "Settings"
                    else -> "Tools"
                }
            }.padding(end=14.dp),color=p.text,fontSize=32.sp)
        } else BrandLogo(Modifier.size(37.dp),p)
        Spacer(Modifier.width(12.dp))
        Text(if(app.activePage in listOf("Calculator","Tools","History","Settings")) "Calculator"
            else app.activePage,color=p.text,fontSize=23.sp,fontWeight=FontWeight.Bold,
            modifier=Modifier.weight(1f),maxLines=1)
        if(app.activePage=="Calculator") {
            Text("◷",fontSize=28.sp,color=p.muted,
                modifier=Modifier.clickable {app.activePage="History"}.padding(7.dp))
            Text("⚙",fontSize=22.sp,color=p.muted,
                modifier=Modifier.clickable {app.activePage="Settings"}.padding(7.dp))
        }
    }
}

@Composable private fun BrandBottomNavigation(app:CalculatorState,p:BrandPalette) {
    val tabs=listOf("Calculator","Tools","History","Settings")
    val icons=listOf(Icons.Outlined.Calculate, Icons.Outlined.GridView,
        Icons.Outlined.History,Icons.Outlined.Settings)
    Row(Modifier.fillMaxWidth().height(86.dp)
        .border(BorderStroke(0.5.dp,p.stroke)).padding(horizontal=10.dp,vertical=7.dp),
        verticalAlignment=Alignment.CenterVertically) {
        tabs.forEachIndexed { index,name ->
            val selected=app.activePage==name ||
                (index==1 && app.activePage in listOf("Unit Converter","Currency Converter","Percentage",
                    "Tip & Split","Loan Planner","Discount","VAT & Tax","Date Calculator","Graphing")) ||
                (index==2 && app.activePage=="Favorites") ||
                (index==3 && app.activePage in listOf("Appearance","Calculation Preferences",
                    "Quick Access","Privacy & Data","About","Widgets"))
            Column(Modifier.weight(1f).fillMaxHeight().background(
                    if(selected) p.surface else Color.Transparent,RoundedCornerShape(15.dp))
                .clickable {app.activePage=name}.padding(5.dp),
                horizontalAlignment=Alignment.CenterHorizontally,
                verticalArrangement=Arrangement.Center) {
                Icon(icons[index],contentDescription=name,
                    tint=if(selected) p.accent else p.muted,modifier=Modifier.size(23.dp))
                Spacer(Modifier.height(4.dp))
                Text(name,color=if(selected) p.accent else p.muted,fontSize=11.sp,
                    fontWeight=if(selected)FontWeight.Bold else FontWeight.Medium)
            }
        }
    }
}
