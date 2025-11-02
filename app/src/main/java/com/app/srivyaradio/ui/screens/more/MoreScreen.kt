package com.app.srivyaradio.ui.screens.more

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SecurityUpdateGood
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.SecurityUpdateGood
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.app.srivyaradio.R
import com.app.srivyaradio.ui.MainViewModel
import com.app.srivyaradio.ui.components.PremiumCard
import com.app.srivyaradio.ui.components.TextAlertDialog
import com.app.srivyaradio.ui.components.ThemeSelectionDialog
import com.app.srivyaradio.utils.ThemeMode
import androidx.navigation.NavController
import androidx.compose.material3.RadioButton
import com.app.srivyaradio.ui.navigation.NavigationItem
import com.app.srivyaradio.ui.navigation.Screen

@Composable
fun MoreScreen(
    mainViewModel: MainViewModel,
    navController: NavController
) {
    var openThemeDialog by remember { mutableStateOf(false) }
    var openChangelogDialog by remember { mutableStateOf(false) }
    var openDefaultScreenDialog by remember { mutableStateOf(false) }

    val themeOptions = listOf(ThemeMode.AUTO, ThemeMode.DARK, ThemeMode.LIGHT)
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    LazyColumn {
        item {
            PremiumCard(isPremium = mainViewModel.isPremium) {
                mainViewModel.onPurchase()
            }
            ListItem(headlineContent = {
                Text(text = "Default start screen")
            }, leadingContent = {
                Icon(Icons.Outlined.Settings, contentDescription = null)
            }, modifier = Modifier.clickable {
                openDefaultScreenDialog = true
            })
            ListItem(headlineContent = {
                Text(text = "Recently played")
            }, leadingContent = {
                Icon(painterResource(id = R.drawable.ic_radio_outlined), contentDescription = null)
            }, modifier = Modifier.clickable { navController.navigate(Screen.RECENTS.name) })
            ListItem(headlineContent = {
                Text(text = "Manage queue")
            }, leadingContent = {
                Icon(painterResource(id = R.drawable.ic_radio_filled), contentDescription = null)
            },modifier = Modifier.clickable { navController.navigate(Screen.QUEUE.name) })
            ListItem(headlineContent = {
                Text(text = "Manage countries")
            }, leadingContent = {
                Icon(Icons.Outlined.Settings, contentDescription = null)
            },modifier = Modifier.clickable { navController.navigate(Screen.MANAGE_COUNTRIES.name) })

            OutlinedButton(
                onClick = {
                    try {
                        uriHandler.openUri("https://docs.google.com/forms/d/e/1FAIpQLSejDu1XaN-iCQ_8wlwbfducJyZeQtwrXodn3G_x3OLpPFW8UA/viewform")
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "No app found to handle this action",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text("Submit radio station")
            }
            ListItem(headlineContent = {
                Text(text = "Update changelog")
            }, leadingContent = {
                Icon(Icons.Outlined.SecurityUpdateGood, contentDescription = null)
            }, modifier = Modifier.clickable {
                openChangelogDialog = true
            })
        }
    }
    when {
        openThemeDialog -> {
            ThemeSelectionDialog(
                onDismiss = { openThemeDialog = false },
                onSubmit = { mainViewModel.setTheme(it) },
                themeOptions = themeOptions,
                initialTheme = mainViewModel.appTheme
            )
        }

        openDefaultScreenDialog -> {
            var selection by remember { mutableStateOf(mainViewModel.getDefaultScreen() ?: (if (mainViewModel.hasSaved) NavigationItem.Favorites.route else NavigationItem.Discover.route)) }
            com.app.srivyaradio.ui.components.CustomAlertDialog(
                title = "Default start screen",
                confirmText = "Apply",
                dismissText = "Cancel",
                icon = Icons.Outlined.Settings,
                onDismiss = { openDefaultScreenDialog = false },
                onConfirm = {
                    mainViewModel.setDefaultScreen(selection)
                    openDefaultScreenDialog = false
                }
            ) {
                val options = listOf(
                    NavigationItem.Favorites.route to NavigationItem.Favorites.routeName,
                    NavigationItem.Discover.route to NavigationItem.Discover.routeName,
                    Screen.RECENTS.name to Screen.RECENTS.routeName
                )
                androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxWidth()) {
                    options.forEach { (route, label) ->
                        androidx.compose.foundation.layout.Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selection = route }
                        ) {
                            RadioButton(selected = selection == route, onClick = { selection = route })
                            Text(text = label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }

        openChangelogDialog -> {
            TextAlertDialog(
                onDismissRequest = { openChangelogDialog = false },
                onConfirmation = { openChangelogDialog = false },
                dialogTitle = "App changelog",
                dialogText = "Completely rebuilt the app with Jetpack Compose.",
                icon = Icons.Default.SecurityUpdateGood
            )
        }
    }
}

