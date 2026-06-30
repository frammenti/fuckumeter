package dev.frammenti.fuckumeter

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import dev.frammenti.fuckumeter.widget.AppWidgetReceiver
import kotlinx.coroutines.launch

@Composable
fun AddWidgetButton() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    Button(
        onClick = {
            coroutineScope.launch {
                GlanceAppWidgetManager(context)
                    .requestPinGlanceAppWidget(
                        receiver = AppWidgetReceiver::class.java
                    )
            }
        },
        modifier = Modifier.padding(30.dp),
    ) {
        Text("Add Widget")
    }
}
