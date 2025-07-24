package com.example.local_survey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.local_survey.ui.theme.Local_surveyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// --- Navigation Routes ---
object AppRoutes {
    const val SURVEY = "survey"
    const val LOGS = "logs"
    const val PASSWORD_PROTECTED_LOGS = "password_protected_logs"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Local_surveyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppRoutes.SURVEY) {
        composable(AppRoutes.SURVEY) {
            SurveyScreen(navController = navController)
        }
        composable(AppRoutes.PASSWORD_PROTECTED_LOGS) {
            PasswordScreen(navController = navController)
        }
        composable(AppRoutes.LOGS) {
            LogScreen(navController = navController)
        }
    }
}

// --- File I/O ---
const val LOG_FILE_NAME = "survey_log.csv"

fun writeToCsv(context: Context, rating: String) {
    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    val file = File(context.filesDir, LOG_FILE_NAME)
    val line = "$timestamp,$rating\n"
    try {
        file.appendText(line)
        Log.d("FileWrite", "Successfully wrote: $line")
    } catch (e: Exception) {
        Log.e("FileWrite", "Error writing to file", e)
    }
}

fun readCsv(context: Context): List<String> {
    val file = File(context.filesDir, LOG_FILE_NAME)
    return if (file.exists()) {
        file.readLines().reversed() // Show newest first
    } else {
        emptyList()
    }
}

fun deleteCsv(context: Context): Boolean {
    val file = File(context.filesDir, LOG_FILE_NAME)
    return if (file.exists()) {
        try {
            file.delete()
            Log.d("FileDelete", "Successfully deleted: ${file.absolutePath}")
            Toast.makeText(context, "Logs deleted", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Log.e("FileDelete", "Error deleting file", e)
            Toast.makeText(context, "Error deleting logs", Toast.LENGTH_SHORT).show()
            false
        }
    } else {
        Log.d("FileDelete", "Log file not found, nothing to delete.")
        Toast.makeText(context, "No logs to delete", Toast.LENGTH_SHORT).show()
        false
    }
}

fun shareCsv(context: Context) {
    val file = File(context.filesDir, LOG_FILE_NAME)
    if (!file.exists()) {
        Log.e("FileShare", "Log file not found!")
        Toast.makeText(context, "No logs to share", Toast.LENGTH_SHORT).show()
        return
    }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Log File"))
}


// --- Screens ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreen(navController: NavController) {
    var buttonsEnabled by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("How was your experience?") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val onButtonClick: (String) -> Unit = { rating ->
        if (buttonsEnabled) {
            writeToCsv(context, rating)
            buttonsEnabled = false
            message = "Thank you for your feedback!"
            scope.launch {
                delay(5000)
                buttonsEnabled = true
                message = "How was your experience?"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Local Survey") },
                actions = {
                    IconButton(onClick = { navController.navigate(AppRoutes.PASSWORD_PROTECTED_LOGS) }) {
                        Icon(Icons.Filled.Menu, contentDescription = "View Logs")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 64.dp)
            )
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordScreen(navController: NavController) {
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val correctPassword = "1234" // 仮のパスワード。実際には安全な方法で管理してください。

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (password == correctPassword) {
                        navController.navigate(AppRoutes.LOGS)
                    } else {
                        Toast.makeText(context, "Incorrect Password", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(navController: NavController) {
    val context = LocalContext.current
    var logEntries by remember { mutableStateOf(readCsv(context)) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Survey Logs") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { shareCsv(context) }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share Logs")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Logs")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (logEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No logs found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                items(logEntries) { entry ->
                    Text(entry, modifier = Modifier.padding(vertical = 4.dp))
                    HorizontalDivider()
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Logs") },
            text = { Text("Are you sure you want to delete all survey logs? This action cannot be undone.") },
            confirmButton = {
                Button(onClick = {
                    if (deleteCsv(context)) {
                        logEntries = emptyList() // Update UI immediately
                    }
                    showDeleteDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}


// --- Reusable Components & Previews ---

@Composable
fun SurveyButton(text: String, rating: String, enabled: Boolean, onClick: (String) -> Unit) {
    Button(
        onClick = { onClick(rating) },
        enabled = enabled,
        modifier = Modifier
            .width(150.dp)
            .height(120.dp)
            .padding(8.dp)
    ) {
        Text(text = text, fontSize = 56.sp)
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=160")
@Composable
fun SurveyScreenPreview() {
    Local_surveyTheme {
        // Dummy NavController for preview
        val navController = rememberNavController()
        SurveyScreen(navController)
    }
}

@Preview(showBackground = true)
@Composable
fun PasswordScreenPreview() {
    Local_surveyTheme {
        val navController = rememberNavController()
        PasswordScreen(navController)
    }
}

@Preview(showBackground = true)
@Composable
fun LogScreenPreview() {
    Local_surveyTheme {
        val navController = rememberNavController()
        LogScreen(navController)
    }
}