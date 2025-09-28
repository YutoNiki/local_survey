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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.local_survey.ui.screens.LogScreen
import com.example.local_survey.ui.theme.Local_surveyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.compose.LocalActivity
import android.content.res.Configuration


import java.util.Locale

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

// Map rating to standardized Japanese format
fun standardizeRating(rating: String): String {
    return when (rating.lowercase().trim()) {
        "very satisfied", "大変満足" -> "大変満足"
        "satisfied", "満足" -> "満足"
        "neutral", "普通" -> "普通"
        "unsatisfied", "不満" -> "不満"
        "very unsatisfied", "大変不満" -> "大変不満"
        else -> rating // fallback to original if no match
    }
}

fun writeToCsv(context: Context, userType: String, rating: String) {
    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    val file = File(context.filesDir, LOG_FILE_NAME)
    val standardizedRating = standardizeRating(rating)
    val line = "$timestamp,$userType,$standardizedRating\n"
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
    val context = LocalContext.current
    var buttonsEnabled by remember { mutableStateOf(true) }
    var userTypeSelected by remember { mutableStateOf<String?>(null) }
    var locale by remember { mutableStateOf(context.resources.configuration.locales[0]) }
    val scope = rememberCoroutineScope()

    val localizedContext = remember(locale) {
        val newConfig = android.content.res.Configuration(context.resources.configuration)
        newConfig.setLocale(locale)
        context.createConfigurationContext(newConfig)
    }

    CompositionLocalProvider(LocalContext provides localizedContext) {
        val messageText = if (userTypeSelected == null) {
            stringResource(R.string.please_select_user_type)
        } else if (!buttonsEnabled) {
            stringResource(R.string.thank_you_feedback)
        } else {
            stringResource(R.string.how_was_experience)
        }

        val onRatingClick: (String) -> Unit = { ratingForLog ->
            if (buttonsEnabled && userTypeSelected != null) {
                writeToCsv(context, userTypeSelected!!, ratingForLog)
                buttonsEnabled = false
                scope.launch {
                    delay(2000)
                    userTypeSelected = null // Reset for next user
                    locale = context.resources.configuration.locales[0] // Reset locale
                    buttonsEnabled = true
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    navigationIcon = {
                        if (userTypeSelected != null) { // Only show back button when a user type is selected
                            IconButton(onClick = {
                                userTypeSelected = null // Reset user type selection
                                locale = context.resources.configuration.locales[0] // Reset locale to default
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back_button_description)
                                )
                            }
                        } else {
                            // Empty spacer to prevent invisible clickable area
                            Spacer(modifier = Modifier.size(0.dp))
                        }
                    },
                    actions = {
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
                    text = messageText,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(bottom = 64.dp)
                )

                if (userTypeSelected == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = {
                            userTypeSelected = "日本人"
                            locale = Locale("ja")
                        }, modifier = Modifier.width(200.dp).height(100.dp)) {
                            Text(stringResource(R.string.user_type_japanese), fontSize = 24.sp)
                        }
                        Button(onClick = {
                            userTypeSelected = "Foreigner"
                            locale = Locale("en")
                        }, modifier = Modifier.width(200.dp).height(100.dp)) {
                            Text(stringResource(R.string.user_type_foreigner), fontSize = 24.sp)
                        }
                    }
                } else {
                    // Pre-fetch rating strings within the localized context
                    val verySatisfiedText = stringResource(R.string.very_satisfied)
                    val satisfiedText = stringResource(R.string.satisfied)
                    val neutralText = stringResource(R.string.neutral)
                    val unsatisfiedText = stringResource(R.string.unsatisfied)
                    val veryUnsatisfiedText = stringResource(R.string.very_unsatisfied)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SurveyButton(text = "😊", rating = verySatisfiedText, ratingForLog = "大変満足", enabled = buttonsEnabled, onClick = onRatingClick)
                        SurveyButton(text = "🙂", rating = satisfiedText, ratingForLog = "満足", enabled = buttonsEnabled, onClick = onRatingClick)
                        SurveyButton(text = "😐", rating = neutralText, ratingForLog = "普通", enabled = buttonsEnabled, onClick = onRatingClick)
                        SurveyButton(text = "😕", rating = unsatisfiedText, ratingForLog = "不満", enabled = buttonsEnabled, onClick = onRatingClick)
                        SurveyButton(text = "😠", rating = veryUnsatisfiedText, ratingForLog = "大変不満", enabled = buttonsEnabled, onClick = onRatingClick)
                    }
                }

                // Banner Text
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "岡山県立美術館",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
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

// --- Reusable Components & Previews ---

@Composable
fun SurveyButton(text: String, rating: String, ratingForLog: String, enabled: Boolean, onClick: (String) -> Unit) {
    Surface(
        onClick = { onClick(ratingForLog) },
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