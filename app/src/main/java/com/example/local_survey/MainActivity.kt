package com.example.local_survey

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.local_survey.ui.theme.Local_surveyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Local_surveyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass the file writing function as a lambda
                    SurveyScreen(onRecordSurvey = ::writeToCsv)
                }
            }
        }
    }

    private fun writeToCsv(rating: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val fileName = "survey_log.csv"
        // Use applicationContext to get the correct files directory
        val file = File(applicationContext.filesDir, fileName)
        val line = "$timestamp,$rating\n"
        try {
            // Append the new line. Create the file if it doesn't exist.
            file.appendText(line)
            Log.d("FileWrite", "Successfully wrote: $line to ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("FileWrite", "Error writing to file", e)
        }
    }
}

@Composable
fun SurveyScreen(onRecordSurvey: (String) -> Unit) {
    var buttonsEnabled by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("How was your experience?") }
    val scope = rememberCoroutineScope()

    val onButtonClick: (String) -> Unit = { rating ->
        if (buttonsEnabled) {
            // 1. Record the survey
            onRecordSurvey(rating)

            // 2. Update UI and disable buttons
            buttonsEnabled = false
            message = "Thank you for your feedback!"

            // 3. Start 5-second cooldown
            scope.launch {
                delay(5000)
                buttonsEnabled = true
                message = "How was your experience?"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            fontSize = 32.sp, // Larger text for visibility
            modifier = Modifier.padding(bottom = 64.dp)
        )

        // Using a flexible Grid layout might be better for different screen sizes,
        // but for a fixed tablet view, a Row is fine.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SurveyButton(text = "😊", rating = "Very Satisfied", enabled = buttonsEnabled, onClick = onButtonClick)
            SurveyButton(text = "🙂", rating = "Satisfied", enabled = buttonsEnabled, onClick = onButtonClick)
            SurveyButton(text = "😐", rating = "Unsatisfied", enabled = buttonsEnabled, onClick = onButtonClick)
            SurveyButton(text = "😠", rating = "Very Unsatisfied", enabled = buttonsEnabled, onClick = onButtonClick)
        }
    }
}

@Composable
fun SurveyButton(text: String, rating: String, enabled: Boolean, onClick: (String) -> Unit) {
    Button(
        onClick = { onClick(rating) },
        enabled = enabled,
        modifier = Modifier
            .width(150.dp) // Wider buttons
            .height(120.dp) // Taller buttons
            .padding(8.dp)
    ) {
        Text(text = text, fontSize = 56.sp)
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=160") // Preview for a tablet
@Composable
fun SurveyScreenPreview() {
    Local_surveyTheme {
        // In preview, the lambda does nothing.
        SurveyScreen(onRecordSurvey = {})
    }
}
