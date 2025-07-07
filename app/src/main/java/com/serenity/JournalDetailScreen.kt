package com.serenity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.gson.Gson
import com.serenity.data.Journal
import java.com.serenity.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JournalDetailScreen(
    journal: Journal, 
    onBack: () -> Unit
) {
    var showJournalExpanded by remember { mutableStateOf(false) }
    var showSummaryPopup by remember { mutableStateOf(false) }
    var showMoodPopup by remember { mutableStateOf(false) }
    var showInsightsPopup by remember { mutableStateOf(false) }
    var showActivitiesPopup by remember { mutableStateOf(false) }
    var showPsyEmotionsPopup by remember { mutableStateOf(false) }
    var showReasonsPopup by remember { mutableStateOf(false) }
    var showTriggersPopup by remember { mutableStateOf(false) }
    var showInterpretationPopup by remember { mutableStateOf(false) }
    var showPeoplePopup by remember { mutableStateOf(false) }
    var showMomentsPopup by remember { mutableStateOf(false) }
    var showPlacesPopup by remember { mutableStateOf(false) }

    val analysis = remember(journal.analysisJson) {
        try {
            Gson().fromJson(journal.analysisJson, AiAnalysis::class.java)
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFE2E8F0)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Journal Entry",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                )
            }

            // Journal title, date, and content
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = journal.title ?: "Untitled",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        ),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
                            .format(Date(journal.timestamp)),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF64748B)
                        ),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (showJournalExpanded) (journal.content ?: "") else {
                            val content = journal.content ?: ""
                            if (content.length > 200) {
                                content.take(200) + "..."
                            } else {
                                content
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFF334155),
                            lineHeight = 28.sp
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    if ((journal.content?.length ?: 0) > 200) {
                        TextButton(
                            onClick = { showJournalExpanded = !showJournalExpanded },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = if (showJournalExpanded) "Show less" else "Read more",
                                color = Color(0xFF6366F1),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Analysis buttons
            if (analysis != null) {
                // Summary button
                AnalysisButton(
                    title = "Summary",
                    icon = painterResource(R.drawable.summary),
                    preview = (analysis.summary ?: "").take(50) + if ((analysis.summary?.length ?: 0) > 50) "..." else "",
                    onClick = { showSummaryPopup = true },
                    color = Color(0xFF6366F1)
                )

                // Mood button
                AnalysisButton(
                    title = "Mood Landscape",
                    icon = painterResource(R.drawable.mood),
                    preview = "${analysis.mood?.size ?: 0} emotions detected",
                    onClick = { showMoodPopup = true },
                    color = Color(0xFF8B5CF6)
                )

                // Insights button
                AnalysisButton(
                    title = "Key Insights",
                    icon = painterResource(R.drawable.bulb),
                    preview = "${analysis.insights?.size ?: 0} insights",
                    onClick = { showInsightsPopup = true },
                    color = Color(0xFF06B6D4)
                )

                // Activities button
                AnalysisButton(
                    title = "Suggested Activities",
                    icon = painterResource(R.drawable.recommend),
                    preview = "${analysis.activities?.size ?: 0} activities",
                    onClick = { showActivitiesPopup = true },
                    color = Color(0xFF10B981)
                )

                // Psychological Emotions button
                AnalysisButton(
                    title = "Psychological Emotions",
                    icon = painterResource(R.drawable.psychology),
                    preview = "${analysis.emotions_psychological?.size ?: 0} emotions",
                    onClick = { showPsyEmotionsPopup = true },
                    color = Color(0xFF6366F1)
                )

                // Reasons button
                AnalysisButton(
                    title = "Reasons Behind Emotions",
                    icon = painterResource(R.drawable.bulb),
                    preview = "${analysis.reasons_behind_emotions?.size ?: 0} reasons",
                    onClick = { showReasonsPopup = true },
                    color = Color(0xFFF59E42)
                )

                // Triggers button
                AnalysisButton(
                    title = "Emotional Triggers",
                    icon = painterResource(R.drawable.bulb),
                    preview = "${analysis.triggers?.size ?: 0} triggers",
                    onClick = { showTriggersPopup = true },
                    color = Color(0xFFEF4444)
                )

                // Psychological Interpretation button
                AnalysisButton(
                    title = "Psychological Interpretation",
                    icon = painterResource(R.drawable.psychology),
                    preview = (analysis.psychological_interpretation ?: "").take(50) + if ((analysis.psychological_interpretation?.length ?: 0) > 50) "..." else "",
                    onClick = { showInterpretationPopup = true },
                    color = Color(0xFF8B5CF6)
                )

                // People button
                AnalysisButton(
                    title = "People Mentioned",
                    icon = painterResource(R.drawable.people),
                    preview = "${analysis.people?.size ?: 0} people",
                    onClick = { showPeoplePopup = true },
                    color = Color(0xFF06B6D4)
                )

                // Moments button
                AnalysisButton(
                    title = "Key Moments",
                    icon = painterResource(R.drawable.place),
                    preview = "${analysis.moments?.size ?: 0} moments",
                    onClick = { showMomentsPopup = true },
                    color = Color(0xFF10B981)
                )

                // Places button
                AnalysisButton(
                    title = "Places Mentioned",
                    icon = painterResource(R.drawable.place),
                    preview = "${analysis.places?.size ?: 0} places",
                    onClick = { showPlacesPopup = true },
                    color = Color(0xFFF59E42)
                )
            } else {
                // No analysis available
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No insights available for this journal entry.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color(0xFF64748B)
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Popups
        if (analysis != null) {
            if (showSummaryPopup) {
                AnalysisPopup(
                    title = "Summary",
                    icon = painterResource(R.drawable.summary),
                    onDismiss = { showSummaryPopup = false },
                    color = Color(0xFF6366F1)
                ) {
                    Text(
                        text = analysis.summary ?: "",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFF334155),
                            lineHeight = 24.sp
                        )
                    )
                }
            }

            if (showMoodPopup) {
                AnalysisPopup(
                    title = "Mood Landscape",
                    icon = painterResource(R.drawable.mood),
                    onDismiss = { showMoodPopup = false },
                    color = Color(0xFF8B5CF6)
                ) {
                    FlowRow(
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        analysis.mood?.entries?.sortedByDescending { it.value }?.forEach { (emotion, value) ->
                            MoodChip(emotion, value)
                        }
                    }
                }
            }

            if (showInsightsPopup) {
                AnalysisPopup(
                    title = "Key Insights",
                    icon = painterResource(R.drawable.bulb),
                    onDismiss = { showInsightsPopup = false },
                    color = Color(0xFF06B6D4)
                ) {
                    analysis.insights?.forEach { insight ->
                        SimpleInsightItem(insight)
                    }
                }
            }

            if (showActivitiesPopup) {
                AnalysisPopup(
                    title = "Suggested Activities",
                    icon = painterResource(R.drawable.recommend),
                    onDismiss = { showActivitiesPopup = false },
                    color = Color(0xFF10B981)
                ) {
                    analysis.activities?.forEach { activity ->
                        SimpleActivityItem(activity)
                    }
                }
            }

            if (showPsyEmotionsPopup) {
                AnalysisPopup(
                    title = "Psychological Emotions",
                    icon = painterResource(R.drawable.psychology),
                    onDismiss = { showPsyEmotionsPopup = false },
                    color = Color(0xFF6366F1)
                ) {
                    analysis.emotions_psychological?.forEach { emotion ->
                        SimpleInsightItem(emotion)
                    }
                }
            }

            if (showReasonsPopup) {
                AnalysisPopup(
                    title = "Reasons Behind Emotions",
                    icon = painterResource(R.drawable.bulb),
                    onDismiss = { showReasonsPopup = false },
                    color = Color(0xFFF59E42)
                ) {
                    analysis.reasons_behind_emotions?.forEach { (emotion, reason) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = emotion ?: "",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF1E293B),
                                    fontWeight = FontWeight.SemiBold
                                ),
                                softWrap = true
                            )
                            Text(
                                text = reason ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF64748B),
                                    lineHeight = 22.sp
                                ),
                                modifier = Modifier.padding(start = 16.dp, top = 6.dp),
                                softWrap = true
                            )
                        }
                    }
                }
            }

            if (showTriggersPopup) {
                AnalysisPopup(
                    title = "Emotional Triggers",
                    icon = painterResource(R.drawable.place),
                    onDismiss = { showTriggersPopup = false },
                    color = Color(0xFFEF4444)
                ) {
                    analysis.triggers?.forEach { (emotion, trigger) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = emotion ?: "",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF1E293B),
                                    fontWeight = FontWeight.SemiBold
                                ),
                                softWrap = true
                            )
                            Text(
                                text = trigger ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF64748B),
                                    lineHeight = 22.sp
                                ),
                                modifier = Modifier.padding(start = 16.dp, top = 6.dp),
                                softWrap = true
                            )
                        }
                    }
                }
            }

            if (showInterpretationPopup) {
                AnalysisPopup(
                    title = "Psychological Interpretation",
                    icon = painterResource(R.drawable.psychology),
                    onDismiss = { showInterpretationPopup = false },
                    color = Color(0xFF8B5CF6)
                ) {
                    Text(
                        text = analysis.psychological_interpretation ?: "",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFF334155),
                            lineHeight = 24.sp
                        )
                    )
                }
            }

            if (showPeoplePopup) {
                AnalysisPopup(
                    title = "People",
                    icon = painterResource(R.drawable.people),
                    onDismiss = { showPeoplePopup = false },
                    color = Color(0xFF06B6D4)
                ) {
                    FlowRow(
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        analysis.people?.forEach { person ->
                            PersonChip(person)
                        }
                    }
                }
            }

            if (showMomentsPopup) {
                AnalysisPopup(
                    title = "Moments",
                    icon = painterResource(R.drawable.place),
                    onDismiss = { showMomentsPopup = false },
                    color = Color(0xFF10B981)
                ) {
                    analysis.moments?.forEach { moment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF1F5F9)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = moment.context ?: "",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF1E293B),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = moment.description ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF64748B)
                                    )
                                )
                                Text(
                                    text = moment.emotion ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF6366F1),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (showPlacesPopup) {
                AnalysisPopup(
                    title = "Places",
                    icon = painterResource(R.drawable.place),
                    onDismiss = { showPlacesPopup = false },
                    color = Color(0xFFF59E42)
                ) {
                    FlowRow(
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        analysis.places?.forEach { place ->
                            SuggestionChip(place) {}
                        }
                    }
                }
            }
        }
    }
}

// AnalysisButton composable
@Composable
private fun AnalysisButton(
    title: String,
    icon: Painter,
    preview: String,
    onClick: () -> Unit,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                )
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF64748B)
                    )
                )
            }
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "View details",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// AnalysisPopup composable
@Composable
private fun AnalysisPopup(
    title: String,
    icon: Painter,
    onDismiss: () -> Unit,
    color: Color,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = color,
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    content()
                }
            }
        }
    }
}

// MoodChip composable
@Composable
private fun MoodChip(emotion: String, value: Int) {
    val color = when {
        value >= 80 -> Color(0xFFFB7185)
        value >= 60 -> Color(0xFFF59E42)
        value >= 40 -> Color(0xFFFBBF24)
        value >= 20 -> Color(0xFF34D399)
        else -> Color(0xFF60A5FA)
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = emotion,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$value",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = color
                )
            )
        }
    }
}

@Composable
private fun SimpleInsightItem(insight: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF6366F1),
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(top = 2.dp, end = 12.dp)
        )
        
        Text(
            text = insight,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF475569),
                lineHeight = 24.sp
            ),
            modifier = Modifier.weight(1f),
            softWrap = true
        )
    }
}

@Composable
private fun SimpleActivityItem(activity: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "→",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF8B5CF6),
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(top = 2.dp, end = 12.dp)
        )
        
        Text(
            text = activity,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF475569),
                lineHeight = 24.sp
            ),
            modifier = Modifier.weight(1f),
            softWrap = true
        )
    }
}

