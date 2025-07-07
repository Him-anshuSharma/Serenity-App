package com.serenity

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.com.serenity.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    journalViewModel: JournalViewModel = hiltViewModel(),
    signInViewModel: SignInViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val journals by journalViewModel.journals.collectAsState()
    val backupStatus by journalViewModel.backupStatus.collectAsState()
    
    // Check for backup on first load
    LaunchedEffect(Unit) {
        journalViewModel.checkForBackup()
    }

    // Animated background
    val infiniteTransition = rememberInfiniteTransition(label = "profile-background")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "profile-background"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SerenityBackground,
                        SerenitySurfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        // Animated background elements
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (300 - animatedProgress * 100).dp, y = (-50 + animatedProgress * 30).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SerenityAccent3.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(100.dp)
                )
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Beautiful Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(0.dp, 0.dp, 24.dp, 24.dp),
                        spotColor = SerenityTertiary.copy(alpha = 0.2f)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(0.dp, 0.dp, 24.dp, 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = AccentGradient,
                            shape = RoundedCornerShape(0.dp, 0.dp, 24.dp, 24.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .background(
                                        color = Color.White.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Text(
                                text = "Your Profile",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // User Info Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    spotColor = Color.White.copy(alpha = 0.2f)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.9f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(
                                            brush = PrimaryGradient,
                                            shape = RoundedCornerShape(30.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = "Profile",
                                        modifier = Modifier.size(32.dp),
                                        tint = Color.White
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column {
                                    Text(
                                        text = signInViewModel.getProfile()?.displayName ?: "User",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = SerenityOnSurface
                                    )
                                    Text(
                                        text = signInViewModel.getProfile()?.email ?: "user@example.com",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SerenityOnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stats Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Entries",
                    value = journals.size.toString(),
                    icon = Icons.Filled.Edit,
                    color = SerenityAccent1,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "This Month",
                    value = journals.count { 
                        val journalDate = Date(it.timestamp)
                        val currentDate = Date()
                        val calendar = Calendar.getInstance()
                        calendar.time = journalDate
                        val journalMonth = calendar.get(Calendar.MONTH)
                        val journalYear = calendar.get(Calendar.YEAR)
                        calendar.time = currentDate
                        val currentMonth = calendar.get(Calendar.MONTH)
                        val currentYear = calendar.get(Calendar.YEAR)
                        journalMonth == currentMonth && journalYear == currentYear
                    }.toString(),
                    icon = Icons.Filled.DateRange,
                    color = SerenityAccent2,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Backup Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = SerenityQuaternary.copy(alpha = 0.2f)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Backup & Sync",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = SerenityOnSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    when (backupStatus) {
                        is BackupStatus.Loading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = SerenityPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Processing...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SerenityOnSurfaceVariant
                                )
                            }
                        }
                        is BackupStatus.Success -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Success",
                                    tint = SuccessColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = (backupStatus as BackupStatus.Success).message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SuccessColor
                                )
                            }
                        }
                        is BackupStatus.Error -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = "Error",
                                    tint = ErrorColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = (backupStatus as BackupStatus.Error).message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ErrorColor
                                )
                            }
                        }
                        is BackupStatus.BackupAvailable -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Backup Available",
                                    tint = InfoColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Backup available from ${
                                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(
                                            Date(
                                                (backupStatus as BackupStatus.BackupAvailable).lastBackupTime)
                                        )}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = InfoColor
                                )
                            }
                        }
                        null -> {
                            Text(
                                text = "No backup data available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SerenityOnSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { journalViewModel.backupData() },
                            modifier = Modifier.weight(1f).background(
                                brush = PrimaryGradient,
                                shape = RoundedCornerShape(12.dp)
                            ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    ,
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Backup Data",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        
                        Button(
                            onClick = { journalViewModel.restoreData() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SerenitySurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Restore Data",
                                color = SerenityOnSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Logout Button
            Button(
                onClick = { signInViewModel.signOut() },
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(ErrorColor, ErrorColor.copy(alpha = 0.8f))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = ErrorColor.copy(alpha = 0.2f)
                    )
                    ,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        ,
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Logout",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = color.copy(alpha = 0.2f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SerenityOnSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = SerenityOnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Helper for chips
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: Dp = 0.dp,
    crossAxisSpacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val rows = mutableListOf<MutableList<Placeable>>()
        val rowHeights = mutableListOf<Int>()
        var currentRow = mutableListOf<Placeable>()
        var currentWidth = 0
        var maxHeight = 0
        placeables.forEach { placeable ->
            if (currentWidth + placeable.width > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                rowHeights.add(maxHeight)
                currentRow = mutableListOf()
                currentWidth = 0
                maxHeight = 0
            }
            currentRow.add(placeable)
            currentWidth += placeable.width + mainAxisSpacing.roundToPx()
            maxHeight = maxOf(maxHeight, placeable.height)
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowHeights.add(maxHeight)
        }
        val height = rowHeights.sum() + crossAxisSpacing.roundToPx() * (rowHeights.size - 1)
        layout(constraints.maxWidth, height) {
            var y = 0
            rows.forEachIndexed { rowIndex, row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + mainAxisSpacing.roundToPx()
                }
                y += rowHeights[rowIndex] + crossAxisSpacing.roundToPx()
            }
        }
    }
} 