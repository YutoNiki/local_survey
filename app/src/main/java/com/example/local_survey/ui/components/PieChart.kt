package com.example.local_survey.ui.components

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@Composable
fun calculateSatisfactionData(context: Context, logEntries: List<String>): Map<String, Int> {
    val verySatisfiedText = stringResource(R.string.very_satisfied)
    val satisfiedText = stringResource(R.string.satisfied)
    val unsatisfiedText = stringResource(R.string.unsatisfied)
    val veryUnsatisfiedText = stringResource(R.string.very_unsatisfied)

    val satisfactionCounts = mutableMapOf(
        verySatisfiedText to 0,
        satisfiedText to 0,
        unsatisfiedText to 0,
        veryUnsatisfiedText to 0
    )

    logEntries.forEach { entry ->
        val parts = entry.split(",")
        if (parts.size > 1) {
            val rating = parts[1].trim()
            when (rating) {
                verySatisfiedText -> satisfactionCounts[verySatisfiedText] = satisfactionCounts.getValue(verySatisfiedText) + 1
                satisfiedText -> satisfactionCounts[satisfiedText] = satisfactionCounts.getValue(satisfiedText) + 1
                unsatisfiedText -> satisfactionCounts[unsatisfiedText] = satisfactionCounts.getValue(unsatisfiedText) + 1
                veryUnsatisfiedText -> satisfactionCounts[veryUnsatisfiedText] = satisfactionCounts.getValue(veryUnsatisfiedText) + 1
            }
        }
    }
    return satisfactionCounts
}

@Composable
fun SatisfactionPieChart(data: Map<String, Int>) {
    val total = data.values.sum().toFloat()
    if (total == 0f) {
        Text(stringResource(R.string.no_logs_found), modifier = Modifier.padding(16.dp))
        return
    }

    val slices = data.map { (label, count) ->
        PieSlice(label, count.toFloat() / total, count)
    }

    val colors = listOf(
        Green400, // Green for Very Satisfied
        LightGreen400, // Light Green for Satisfied
        Amber400, // Amber for Unsatisfied
        Red400  // Red for Very Unsatisfied
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
        Canvas(modifier = Modifier.size(200.dp)) {
            var startAngle = 0f
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
