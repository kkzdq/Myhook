package com.myhook.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScopeManager.ensureRegistered()
        setContent {
            MyHookTheme {
                MyHookApp(this)
            }
        }
    }
}
