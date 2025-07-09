package com.serenity.ui.screen

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.serenity.ui.viewmodel.SignInState
import com.serenity.ui.viewmodel.SignInViewModel
import java.com.serenity.ui.theme.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.content.Intent

@Composable
fun SignInScreen(
    viewModel: SignInViewModel = hiltViewModel(),
    navController: NavController = rememberNavController()
) {
    val loginState by viewModel.signInState.collectAsState()
    val fallbackSignInIntent by viewModel.fallbackSignInIntent.collectAsState()
    val context = LocalContext.current

    // Animated gradient
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient"
    )

    // Floating animation for the logo
    val floatingAnimation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

    // Google Sign-In launcher

    // Google Sign-In fallback launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.result
                val idToken = account?.idToken
                if (idToken != null) {
                    viewModel.signInWithGoogleIdToken(idToken) { success, error ->
                        // Optionally handle result
                    }
                } else {
                    // Optionally show error
                }
            } catch (e: Exception) {
                // Optionally show error
            }
        }
        viewModel.clearFallbackSignInIntent()
    }

    // Launch fallback intent if available
    LaunchedEffect(fallbackSignInIntent) {
        fallbackSignInIntent?.let {
            googleSignInLauncher.launch(it)
        }
    }

    LaunchedEffect(loginState) {
        when (loginState) {
            is SignInState.Success -> {
                navController.navigate("main") {
                    popUpTo("signIn") { inclusive = true }
                }
            }
            is SignInState.Error -> {
                Log.e("SignInScreen", (loginState as SignInState.Error).message)
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SerenityPrimary.copy(alpha = 0.1f),
                        SerenitySecondary.copy(alpha = 0.05f),
                        SerenityBackground
                    )
                )
            )
    ) {
        // Animated background circles
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-50 + animatedProgress * 100).dp, y = (-100 + animatedProgress * 50).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SerenityAccent1.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(100.dp)
                )
        )
        
        Box(
            modifier = Modifier
                .size(150.dp)
                .offset(x = (300 - animatedProgress * 100).dp, y = (600 + animatedProgress * 50).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SerenityAccent2.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(75.dp)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Logo
            Box(
                modifier = Modifier
                    .offset(y = floatingAnimation.dp)
                    .size(120.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(60.dp),
                        spotColor = SerenityPrimary.copy(alpha = 0.3f)
                    )
                    .background(
                        brush = PrimaryGradient,
                        shape = RoundedCornerShape(60.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Serenity Logo",
                    modifier = Modifier.size(60.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Title with gradient text
            Text(
                text = "Serenity",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = SerenityPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Your Personal Journal Companion",
                style = MaterialTheme.typography.bodyLarge,
                color = SerenityOnSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Reflect • Grow • Heal",
                style = MaterialTheme.typography.bodyMedium,
                color = SerenitySecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(48.dp))

            val isLoading = loginState is SignInState.Loading
            
            // Beautiful Sign-In Button
            Button(
                onClick = {
                    viewModel.startGoogleSignIn(context)
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = SerenityPrimary.copy(alpha = 0.3f)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = if (isLoading) {
                                Brush.linearGradient(
                                    colors = listOf(
                                        SerenityOnSurfaceVariant.copy(alpha = 0.3f),
                                        SerenityOnSurfaceVariant.copy(alpha = 0.2f)
                                    )
                                )
                            } else {
                                PrimaryGradient
                            },
                            shape = RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = SerenityPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Sign in with Google",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            if (loginState is SignInState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = ErrorColor.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = (loginState as SignInState.Error).message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ErrorColor,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // Features preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeatureChip(
                    icon = "✨",
                    text = "AI Insights",
                    color = SerenityAccent1
                )
                FeatureChip(
                    icon = "💭",
                    text = "Smart Chat",
                    color = SerenityAccent2
                )
                FeatureChip(
                    icon = "🔒",
                    text = "Secure",
                    color = SerenityAccent3
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "By signing in, you agree to our Terms of Service and Privacy Policy",
                style = MaterialTheme.typography.bodySmall,
                color = SerenityOnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun FeatureChip(
    icon: String,
    text: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = color.copy(alpha = 0.2f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = SerenityOnSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
