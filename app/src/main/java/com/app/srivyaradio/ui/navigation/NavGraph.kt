package com.app.srivyaradio.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.srivyaradio.ui.MainViewModel
import com.app.srivyaradio.ui.screens.discover.DiscoverScreen
import com.app.srivyaradio.ui.screens.favorites.FavoriteScreen
import com.app.srivyaradio.ui.screens.more.MoreScreen
import com.app.srivyaradio.ui.screens.recents.RecentsScreen
import com.app.srivyaradio.ui.screens.queue.QueueScreen
import com.app.srivyaradio.ui.screens.countries.ManageCountriesScreen

@Composable
fun NavGraph(navHostController: NavHostController, modifier: Modifier) {
    val mainViewModel: MainViewModel = viewModel()
    val initialRoute = mainViewModel.getStartDestinationRoute()

    NavHost(navController = navHostController, startDestination = initialRoute) {
        composable(NavigationItem.Favorites.route) {
            FavoriteScreen(mainViewModel)
        }
        composable(NavigationItem.Discover.route) {
            DiscoverScreen(mainViewModel)
        }
        composable(NavigationItem.More.route) {
            MoreScreen(mainViewModel = mainViewModel, navController = navHostController)
        }
        composable(Screen.RECENTS.name) {
            RecentsScreen(mainViewModel)
        }
        composable(Screen.QUEUE.name) {
            QueueScreen(mainViewModel)
        }
        composable(Screen.MANAGE_COUNTRIES.name) {
            ManageCountriesScreen(mainViewModel)
        }
    }
}