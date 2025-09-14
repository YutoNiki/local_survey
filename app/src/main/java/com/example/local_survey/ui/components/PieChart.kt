package com.example.local_survey.ui.components

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.local_survey.R
import com.example.local_survey.ui.theme.Amber400
import com.example.local_survey.ui.theme.Green400
import com.example.local_survey.ui.theme.LightGreen400
import com.example.local_survey.ui.theme.Red400

fun calculateSatisfactionDataByGroup(logEntries: List<String>): Map<String, Map<String, Int>> {
    val groups = listOf("日本人", "Foreigner")
    val ratings = listOf("大変満足", "満足", "普通", "不満", "大変不満")

    // Initialize the map with all groups and ratings
    val satisfactionCounts = groups.associateWith {
        ratings.associateWith { 0 }.toMutableMap()
    }.toMutableMap()

    logEntries.forEach { entry ->
        val parts = entry.split(",")
        if (parts.size > 2) {
            val group = parts[1].trim()
            val rating = parts[2].trim()
            if (satisfactionCounts.containsKey(group) && satisfactionCounts[group]!!.containsKey(rating)) {
                satisfactionCounts[group]!![rating] = satisfactionCounts[group]!!.getValue(rating) + 1
            }
        }
    }
    return satisfactionCounts
}

@Composable
fun SatisfactionPieChart(data: Map<String, Int>) {
    val total = data.values.sum()
    if (total == 0) {
        Text(stringResource(R.string.no_data_available), modifier = Modifier.padding(16.dp))
        return
    }

    val totalFloat = total.toFloat()

    val sortedData = data.entries.sortedBy { entry ->
        when (entry.key) {
            "大変満足" -> 0
            "満足" -> 1
            "普通" -> 2
            "不満" -> 3
            "大変不満" -> 4
            else -> 5
        }
    }

    val slices = sortedData.map { (label, count) ->
        PieSlice(label, count.toFloat() / totalFloat, count)
    }

    val colors = listOf(
        Green400,      // 大変満足
        LightGreen400, // 満足
        Color.Gray,    // 普通
        Amber400,      // 不満
        Red400         // 大変不満
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.total_respondents, total),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Canvas(modifier = Modifier.size(200.dp)) {
            var startAngle = -90f
            slices.forEachIndexed { index, slice ->
                val sweepAngle = slice.percentage * 360f
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset.Zero,
                    size = size
                )
                startAngle += sweepAngle
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Legend
        Column(horizontalAlignment = Alignment.Start) {
            slices.forEachIndexed { index, slice ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier
                        .size(16.dp)
                        .background(colors[index % colors.size]))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${slice.label}: ${slice.count} (${"%.1f".format(slice.percentage * 100)}%)")
                }
            }
        }
    }
}

data class PieSlice(val label: String, val percentage: Float, val count: Int)
