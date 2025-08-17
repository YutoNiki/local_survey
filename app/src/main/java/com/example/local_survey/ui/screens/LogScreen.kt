package com.example.local_survey.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.local_survey.R
import com.example.local_survey.deleteCsv
import com.example.local_survey.readCsv
import com.example.local_survey.shareCsv
import com.example.local_survey.ui.components.SatisfactionPieChart
import com.example.local_survey.ui.components.calculateSatisfactionData
import com.example.local_survey.ui.components.WeeklyResponseBarChart

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
            val satisfactionData = calculateSatisfactionData(context, logEntries)
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Weekly response bar chart
                WeeklyResponseBarChart(
                    logEntries = logEntries,
                    modifier = Modifier.fillMaxWidth()
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                // Satisfaction pie chart
                SatisfactionPieChart(data = satisfactionData)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                // Raw log entries
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.survey_logs),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    logEntries.forEach { entry ->
                        Text(
                            text = entry,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        HorizontalDivider()
                    }
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
