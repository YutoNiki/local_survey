package com.example.local_survey.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.local_survey.R
import java.text.SimpleDateFormat
import java.util.*

data class DailyResponseData(
    val date: String,
    val count: Int
)

@Composable
fun WeeklyResponseBarChart(
    logEntries: List<String>,
    modifier: Modifier = Modifier
) {
    val weeklyData = remember(logEntries) { calculateWeeklyResponseData(logEntries) }
    val maxCount = weeklyData.maxOfOrNull { it.count } ?: 1
    
    if (weeklyData.all { it.count == 0 }) {
        Box(
            modifier = modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_data_available),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.weekly_responses),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Chart area with bars and count labels
        Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            // Draw bars
            Canvas(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
            ) {
                val barWidth = size.width / weeklyData.size * 0.7f
                val barSpacing = size.width / weeklyData.size * 0.3f
                val chartHeight = size.height - 50.dp.toPx() // Leave space for count labels
                
                weeklyData.forEachIndexed { index, data ->
                    val barHeight = if (maxCount > 0) (data.count.toFloat() / maxCount) * chartHeight else 0f
                    val x = index * (barWidth + barSpacing) + barSpacing / 2
                    val y = size.height - barHeight - 25.dp.toPx()
                    
                    // Draw bar
                    drawRect(
                        color = Color(0xFF6200EE),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight)
                    )
                }
            }
            
            // Count labels overlay
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weeklyData.forEach { data ->
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (data.count > 0) {
                            Text(
                                text = data.count.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Date labels
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weeklyData.forEach { data ->
                Text(
                    text = data.date,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.total_responses_week, weeklyData.sumOf { it.count }),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun calculateWeeklyResponseData(logEntries: List<String>): List<DailyResponseData> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormat = SimpleDateFormat("M/d", Locale.getDefault())
    val calendar = Calendar.getInstance()
    
    // Get the past 7 days
    val weekDates = mutableListOf<String>()
    val weekDisplayDates = mutableListOf<String>()
    
    for (i in 6 downTo 0) {
        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_YEAR, -i)
        val dateStr = dateFormat.format(calendar.time)
        val displayDateStr = displayFormat.format(calendar.time)
        weekDates.add(dateStr)
        weekDisplayDates.add(displayDateStr)
    }
    
    // Count responses for each day
    val dailyCounts = mutableMapOf<String, Int>()
    weekDates.forEach { date ->
        dailyCounts[date] = 0
    }
    
    logEntries.forEach { entry ->
        val parts = entry.split(",")
        if (parts.isNotEmpty()) {
            try {
                val entryDate = parts[0].trim().split(" ")[0] // Extract date part (yyyy-MM-dd)
                if (dailyCounts.containsKey(entryDate)) {
                    dailyCounts[entryDate] = dailyCounts[entryDate]!! + 1
                }
            } catch (e: Exception) {
                // Skip invalid entries
            }
        }
    }
    
    return weekDates.mapIndexed { index, date ->
        DailyResponseData(
            date = weekDisplayDates[index],
            count = dailyCounts[date] ?: 0
        )
    }
}
