package com.talmudfinance.app.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.talmudfinance.app.R
import com.talmudfinance.app.TalmudFinanceApp
import com.talmudfinance.app.ui.home.HomeScreen
import com.talmudfinance.app.ui.home.HomeViewModel
import com.talmudfinance.app.ui.market.MarketScreen
import com.talmudfinance.app.ui.market.MarketViewModel
import com.talmudfinance.app.ui.talmud.TalmudScreen
import com.talmudfinance.app.ui.talmud.TalmudViewModel
import androidx.compose.ui.platform.LocalContext

sealed class Tab(val route: String, val labelRes: Int, val icon: ImageVector) {
    data object Home    : Tab("home",    R.string.tab_home,    Icons.Filled.Home)
    data object Market  : Tab("market",  R.string.tab_market,  Icons.Filled.ShowChart)
    data object Talmud  : Tab("talmud",  R.string.tab_talmud,  Icons.Filled.AutoStories)

    companion object {
        val all = listOf(Home, Market, Talmud)
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as TalmudFinanceApp

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination
            NavigationBar {
                Tab.all.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(id = tab.labelRes)) }
                    )
                }
            }
        }
    ) { padding: PaddingValues ->
        NavHost(
            navController = navController,
            startDestination = Tab.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Tab.Home.route) {
                val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(app))
                HomeScreen(viewModel = vm)
            }
            composable(Tab.Market.route) {
                val vm: MarketViewModel = viewModel(factory = MarketViewModel.factory(app))
                MarketScreen(viewModel = vm)
            }
            composable(Tab.Talmud.route) {
                val vm: TalmudViewModel = viewModel(factory = TalmudViewModel.factory(app))
                TalmudScreen(viewModel = vm)
            }
        }
    }
}
