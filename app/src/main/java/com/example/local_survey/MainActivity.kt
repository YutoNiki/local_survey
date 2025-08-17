package com.example.local_survey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image


// --- Navigation Routes ---
object AppRoutes {
    const val SURVEY = "survey"
    const val LOGS = "logs"
    const val PASSWORD_PROTECTED_LOGS = "password_protected_logs"
}

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val sharedPref = newBase.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val language = sharedPref.getString("app_language", "ja") ?: "ja"
        super.attachBaseContext(ContextUtils.updateLocale(newBase, language))
    }

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
            Toast.makeText(context, context.getString(R.string.delete_logs), Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Log.e("FileDelete", "Error deleting file", e)
            Toast.makeText(context, context.getString(R.string.delete_logs), Toast.LENGTH_SHORT).show()
            false
        }
    } else {
        Log.d("FileDelete", "Log file not found, nothing to delete.")
        Toast.makeText(context, context.getString(R.string.no_logs_found), Toast.LENGTH_SHORT).show()
        false
    }
}

fun shareCsv(context: Context) {
    val file = File(context.filesDir, LOG_FILE_NAME)
    if (!file.exists()) {
        Log.e("FileShare", "Log file not found!")
        Toast.makeText(context, context.getString(R.string.no_logs_found), Toast.LENGTH_SHORT).show()
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
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_logs)))
}


// --- Screens ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreen(navController: NavController) {
    var buttonsEnabled by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf(R.string.how_was_experience) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = LocalActivity.current

    var expanded by remember { mutableStateOf(false) }
    val languages = listOf("en", "ja")
    val languageNames = mapOf("en" to stringResource(R.string.english), "ja" to stringResource(R.string.japanese))
    val currentLanguage = remember { mutableStateOf(context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("app_language", "ja") ?: "ja") }
    
    // Pre-fetch rating strings
    val verySatisfiedText = stringResource(R.string.very_satisfied)
    val satisfiedText = stringResource(R.string.satisfied)
    val unsatisfiedText = stringResource(R.string.unsatisfied)
    val veryUnsatisfiedText = stringResource(R.string.very_unsatisfied)

    val onButtonClick: (String) -> Unit = { rating ->
        if (buttonsEnabled) {
            writeToCsv(context, rating)
            buttonsEnabled = false
            message = R.string.thank_you_feedback
            scope.launch {
                delay(3000)
                buttonsEnabled = true
                message = R.string.how_was_experience
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    // Language Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.wrapContentSize(Alignment.TopEnd)
                    ) {
                        TextField(
                            value = languageNames[currentLanguage.value] ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.language)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            languages.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(languageNames[lang] ?: "") },
                                    onClick = {
                                        currentLanguage.value = lang
                                        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                        with(sharedPref.edit()) {
                                            putString("app_language", lang)
                                            apply()
                                        }
                                        activity?.recreate() // Recreate activity to apply language change
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(onClick = { navController.navigate(AppRoutes.PASSWORD_PROTECTED_LOGS) }) {
                        Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.view_logs))
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
                text = stringResource(message),
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 64.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SurveyButton(text = "😊", rating = verySatisfiedText, enabled = buttonsEnabled, onClick = onButtonClick)
                SurveyButton(text = "🙂", rating = satisfiedText, enabled = buttonsEnabled, onClick = onButtonClick)
                SurveyButton(text = "😐", rating = unsatisfiedText, enabled = buttonsEnabled, onClick = onButtonClick)
                SurveyButton(text = "😠", rating = veryUnsatisfiedText, enabled = buttonsEnabled, onClick = onButtonClick)
            }
            // Banner Image
            Spacer(modifier = Modifier.height(32.dp)) // Adjust spacing as needed
            Image(
                painter = painterResource(id = R.drawable.opma_banner),
                contentDescription = "OPMA Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp) // Adjust height as needed
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordScreen(navController: NavController) {
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val correctPassword = "1988" // 仮のパスワード。実際には安全な方法で管理してください。
    val incorrectPasswordMessage = stringResource(R.string.incorrect_password)
    val submitText = stringResource(R.string.submit)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.enter_password)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.view_logs))
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
                label = { Text(stringResource(R.string.password)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (password == correctPassword) {
                        navController.navigate(AppRoutes.LOGS)
                    } else {
                        Toast.makeText(context, incorrectPasswordMessage, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(submitText)
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
    
    // Pre-fetch dialog strings
    val deleteLogsConfirmationText = stringResource(R.string.delete_logs_confirmation)
    val yesText = stringResource(R.string.yes)
    val noText = stringResource(R.string.no)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.survey_logs)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.view_logs))
                    }
                },
                actions = {
                    IconButton(onClick = { shareCsv(context) }) {
                        Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.share_logs))
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete_logs))
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
                Text(stringResource(R.string.no_logs_found))
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
            title = { Text(stringResource(R.string.delete_logs)) },
            text = { Text(deleteLogsConfirmationText) },
            confirmButton = {
                Button(onClick = {
                    if (deleteCsv(context)) {
                        logEntries = emptyList() // Update UI immediately
                    }
                    showDeleteDialog = false
                }) {
                    Text(yesText)
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text(noText)
                }
            }
        )
    }
}


// --- Reusable Components & Previews ---

@Composable
fun SurveyButton(text: String, rating: String, enabled: Boolean, onClick: (String) -> Unit) {
    Surface(
        onClick = { onClick(rating) },
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
        modifier = Modifier
            .width(150.dp)
            .height(120.dp)
            .padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = text, fontSize = 60.sp) // フォントサイズを大きく
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = rating, fontSize = 12.sp)
        }
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