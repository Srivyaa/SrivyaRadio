package com.app.srivyaradio.ui.screens.countries

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.srivyaradio.ui.MainViewModel
import com.app.srivyaradio.utils.countryList

@Composable
fun ManageCountriesScreen(mainViewModel: MainViewModel) {
    val (name, setName) = remember { mutableStateOf("") }
    val (code, setCode) = remember { mutableStateOf("") }
    val userCountries = mainViewModel.getCountryListForUI().filter { it !in countryList }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
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
