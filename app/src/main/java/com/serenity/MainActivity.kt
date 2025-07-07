package com.serenity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.com.serenity.ui.theme.SerenityTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        // Make the status bar transparent
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            SerenityTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MainContent()
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    val navController = rememberNavController()
    val signInViewModel: SignInViewModel = hiltViewModel()
    val user by signInViewModel.user.collectAsState()
    
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        NavHost(
            navController = navController,
            startDestination = if (user == null) "signIn" else "main"
        ) {
            composable("signIn") {
                SignInScreen(signInViewModel, navController)
            }
            composable("main") {
                JournalScreen()
            }
        }
    }
}