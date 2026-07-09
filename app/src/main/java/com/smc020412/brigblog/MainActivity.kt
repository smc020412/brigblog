package com.smc020412.brigblog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.smc020412.brigblog.ui.GameScreen
import com.smc020412.brigblog.ui.theme.BrigBlogTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            BrigBlogTheme {
                GameScreen()
            }
        }
    }
}