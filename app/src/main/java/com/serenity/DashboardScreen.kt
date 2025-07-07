package com.serenity

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.serenity.data.Journal
import java.text.SimpleDateFormat
import java.util.*

// Import Person from JournalViewModel
import java.com.serenity.R


// Data class for mood data point
data class MoodDataPoint(
    val date: Long,
    val mood: String,
    val value: Int
)

@Composable
fun DashboardScreen(
    journals: List<Journal>,
    onBack: () -> Unit
) {
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
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
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
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Stats Overview
                StatsOverviewCard(journals)
                
                // Mood Trends
                MoodTrendsCard(journals)
                
                // People Mentioned
                PeopleCard(journals)
                
                // Places Visited
                PlacesCard(journals)
                
                // Recent Activity
                RecentActivityCard(journals)
            }
        }
    }
}

@Composable
private fun StatsOverviewCard(journals: List<Journal>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.dashboard),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF6366F1)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = painterResource(R.drawable.book),
                    value = journals.size.toString(),
                    label = "Entries",
                    color = Color(0xFF6366F1)
                )
                
                StatItem(
                    icon = painterResource(R.drawable.mood),
                    value = journals.count { it.analysisJson != null }.toString(),
                    label = "Analyzed",
                    color = Color(0xFF8B5CF6)
                )
                
                StatItem(
                    icon = painterResource(R.drawable.people),
                    value = getUniquePeopleCount(journals).toString(),
                    label = "People",
                    color = Color(0xFF10B981)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: Painter,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = color
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF64748B)
            )
        )
    }
}

@Composable
private fun MoodTrendsCard(journals: List<Journal>) {
    val moodData = getMoodTrendsData(journals)
    var isExpanded by remember { mutableStateOf(true) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(bottom = 16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.mood),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF8B5CF6)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Mood Trends",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    imageVector = if (isExpanded) {
                        Icons.Filled.KeyboardArrowUp
                    } else {
                        Icons.Filled.ArrowDropDown
                    },
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            if (isExpanded) {
                if (moodData.isNotEmpty()) {
                    // Mood Sparklines
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val moodTypes = moodData.map { it.mood }.distinct()
                        moodTypes.forEach { moodType ->
                            val moodPoints = moodData.filter { it.mood == moodType }
                                .sortedBy { it.date }
                            
                            MoodSparkline(
                                moodName = moodType,
                                dataPoints = moodPoints,
                                color = getMoodColor(moodType)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No mood data available yet.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF64748B)
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun MoodSparkline(
    moodName: String,
    dataPoints: List<MoodDataPoint>,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = moodName.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF475569)
                )
            )
            
            Text(
                text = "${dataPoints.size} entries",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF64748B)
                )
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Sparkline chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        ) {
            if (dataPoints.isNotEmpty()) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val width = size.width
                    val height = size.height
                    val maxValue = dataPoints.maxOfOrNull { it.value } ?: 100
                    val minValue = dataPoints.minOfOrNull { it.value } ?: 0
                    val valueRange = maxValue - minValue
                    
                    if (valueRange > 0) {
                        val path = Path()
                        val pointWidth = width / (dataPoints.size - 1)
                        
                        dataPoints.forEachIndexed { index, point ->
                            val x = index * pointWidth
                            val normalizedValue = (point.value - minValue) / valueRange.toFloat()
                            val y = height - (normalizedValue * height)
                            
                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }
                        
                        // Draw the line
                        drawPath(
                            path = path,
                            color = color,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                        
                        // Draw points
                        dataPoints.forEachIndexed { index, point ->
                            val x = index * pointWidth
                            val normalizedValue = (point.value - minValue) / valueRange.toFloat()
                            val y = height - (normalizedValue * height)
                            
                            drawCircle(
                                color = color,
                                radius = 3.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }
        }
        
        // Current average
        val average = dataPoints.map { it.value }.average()
        Text(
            text = "Average: ${average.toInt()}%",
            style = MaterialTheme.typography.bodySmall.copy(
                color = color,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun PeopleCard(journals: List<Journal>) {
    val people = getPeopleData(journals)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.people),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF10B981)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "People Mentioned",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                )
            }
            
            if (people.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    people.take(10).forEach { person ->
                        PersonChip(person)
                    }
                }
            } else {
                Text(
                    text = "No people mentioned yet.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF64748B)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun PersonChip(person: AiPerson) {
    val relationshipColor = when (person.relationship?.lowercase()) {
        "positive" -> Color(0xFF10B981) // Green
        "negative" -> Color(0xFFEF4444) // Red
        else -> Color(0xFF6B7280) // Gray
    }
    
    Card(
        modifier = Modifier
            .shadow(2.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = person.name.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF1E293B),
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )
            if (person.relationship?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = person.relationship.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = relationshipColor,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PlacesCard(journals: List<Journal>) {
    val places = getPlacesData(journals)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.place),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFFF59E0B)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Places Visited",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                )
            }
            
            if (places.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    places.take(10).forEach { place ->
                        PlaceChip(place)
                    }
                }
            } else {
                Text(
                    text = "No places mentioned yet.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF64748B)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PlaceChip(place: String) {
    Card(
        modifier = Modifier
            .shadow(2.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEF3C7)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = place,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF92400E),
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun RecentActivityCard(journals: List<Journal>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.book),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF6366F1)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                )
            }
            
            if (journals.isNotEmpty()) {
                journals.take(5).forEach { journal ->
                    RecentActivityItem(journal)
                    if (journal != journals.take(5).last()) {
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            } else {
                Text(
                    text = "No journal entries yet.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF64748B)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun RecentActivityItem(journal: Journal) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.book),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color(0xFF6366F1)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = journal.title ?: "Untitled",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E293B)
                )
            )
            
            Text(
                text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(Date(journal.timestamp)),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF64748B)
                )
            )
        }
        
        if (journal.analysisJson != null) {
            Icon(
                painter = painterResource(R.drawable.summary),
                contentDescription = "Analyzed",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF10B981)
            )
        }
    }
}

// Helper functions
private fun getMoodTrendsData(journals: List<Journal>): List<MoodDataPoint> {
    val moodData = mutableListOf<MoodDataPoint>()
    
    journals.forEach { journal ->
        if (journal.analysisJson != null) {
            try {
                val analysis = Gson().fromJson(journal.analysisJson, AiAnalysis::class.java)
                analysis?.mood?.forEach { (mood, value) ->
                    moodData.add(MoodDataPoint(journal.timestamp, mood ?: "", value))
                }
            } catch (e: Exception) {
                // Ignore parsing errors
            }
        }
    }
    
    return moodData.sortedBy { it.date }
}

private fun getMoodColor(mood: String): Color {
    return when (mood.lowercase()) {
        "happy", "joy", "excited" -> Color(0xFF10B981) // Green
        "sad", "depressed", "melancholy" -> Color(0xFF3B82F6) // Blue
        "angry", "frustrated", "irritated" -> Color(0xFFEF4444) // Red
        "anxious", "worried", "stressed" -> Color(0xFFF59E0B) // Orange
        "calm", "peaceful", "relaxed" -> Color(0xFF8B5CF6) // Purple
        "neutral", "okay", "fine" -> Color(0xFF6B7280) // Gray
        else -> Color(0xFF8B5CF6) // Default purple
    }
}

private fun getPeopleData(journals: List<Journal>): List<AiPerson> {
    val peopleMap = mutableMapOf<String, AiPerson>()
    
    journals.forEach { journal ->
        if (journal.peopleJson != null) {
            try {
                val people: List<AiPerson> = Gson().fromJson(journal.peopleJson, object : TypeToken<List<AiPerson>>() {}.type)
                people.forEach { person ->
                    // Use name as key to avoid duplicates
                    if (!peopleMap.containsKey(person.name ?: "")) {
                        peopleMap[person.name ?: ""] = person
                    }
                }
            } catch (e: Exception) {
                // Try parsing as the old format (list of strings)
                try {
                    val peopleNames: List<String> = Gson().fromJson(journal.peopleJson, object : TypeToken<List<String>>() {}.type)
                    peopleNames.forEach { name ->
                        if (!peopleMap.containsKey(name)) {
                            peopleMap[name] = AiPerson(name, "neutral", "")
                        }
                    }
                } catch (e2: Exception) {
                    // Ignore parsing errors
                }
            }
        }
    }
    
    return peopleMap.values.toList()
}

private fun getPlacesData(journals: List<Journal>): List<String> {
    val placesSet = mutableSetOf<String>()
    
    journals.forEach { journal ->
        if (journal.placesJson != null) {
            try {
                val places: List<String> = Gson().fromJson(journal.placesJson, object : TypeToken<List<String>>() {}.type)
                placesSet.addAll(places)
            } catch (e: Exception) {
                // Ignore parsing errors
            }
        }
    }
    
    return placesSet.toList()
}

private fun getUniquePeopleCount(journals: List<Journal>): Int {
    return getPeopleData(journals).size
} 