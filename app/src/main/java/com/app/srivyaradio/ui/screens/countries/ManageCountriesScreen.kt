package com.app.srivyaradio.ui.screens.countries

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.app.srivyaradio.ui.MainViewModel
import com.app.srivyaradio.utils.countryList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCountriesScreen(mainViewModel: MainViewModel, onBackClick: (() -> Unit)? = null) {
    val (name, setName) = remember { mutableStateOf("") }
    val (code, setCode) = remember { mutableStateOf("") }
    val userCountries = mainViewModel.getCountryListForUI().filter { it !in countryList }

    // Import launcher (CSV and common CSV MIME variants)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                mainViewModel.importCountriesFromUri(uri)
            }
        }
    )

    // Export CSV launcher
    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri -> if (uri != null) mainViewModel.exportCountriesToUri(uri) }
    )

    // Template launcher (CSV)
    val templateCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri -> if (uri != null) mainViewModel.saveTemplateToUri(uri) }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Countries") },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(12.dp)) {
        // Import/Export controls
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                val mimes = arrayOf(
                    "text/csv",
                    "text/comma-separated-values",
                    "application/csv",
                    "application/vnd.ms-excel",
                    "text/plain"
                )
                try {
                    importLauncher.launch(mimes)
                } catch (_: Exception) {
                    importLauncher.launch(arrayOf("*/*"))
                }
            }) { Text("Import CSV") }
            Button(onClick = {
                val filename = "countries.csv"
                exportCsvLauncher.launch(filename)
            }) { Text("Export CSV") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Button(onClick = {
                val filename = "countries-template.csv"
                templateCsvLauncher.launch(filename)
            }) { Text("Download Template (CSV)") }
        }

        // Status line
        mainViewModel.importExportMessage?.let { msg ->
            Text(msg, modifier = Modifier.padding(vertical = 8.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = name, onValueChange = setName, label = { Text("Name") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = code, onValueChange = setCode, label = { Text("Code") }, modifier = Modifier.weight(1f))
            Button(onClick = {
                mainViewModel.addUserCountry(name, code)
                setName("")
                setCode("")
            }) { Text("Add") }
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(userCountries) { (n, c) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text("$n ($c)", modifier = Modifier.weight(1f))
                    IconButton(onClick = { mainViewModel.removeUserCountry(n, c) }) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                }
            }
        }
    }
    }
}
