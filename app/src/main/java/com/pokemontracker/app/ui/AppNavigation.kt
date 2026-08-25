package com.pokemontracker.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pokemontracker.app.PokemonTrackerApp
import com.pokemontracker.app.data.CardRepository
import com.pokemontracker.app.ui.screens.CardDetailScreen
import com.pokemontracker.app.ui.screens.CardEditScreen
import com.pokemontracker.app.ui.screens.CardListScreen
import com.pokemontracker.app.ui.screens.OverviewScreen

private object Routes {
    const val HOME = "home"
    const val ADD = "edit"
    const val EDIT = "edit/{cardId}"
    const val DETAIL = "detail/{cardId}"
    const val ARG_CARD_ID = "cardId"

    fun edit(id: Long) = "edit/$id"
    fun detail(id: Long) = "detail/$id"
}

@Composable
private fun repository(): CardRepository =
    (LocalContext.current.applicationContext as PokemonTrackerApp).repository

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val repo = repository()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            val vm: CollectionViewModel = viewModel(
                factory = viewModelFactory { initializer { CollectionViewModel(repo) } },
            )
            HomeScreen(
                viewModel = vm,
                onAddCard = { navController.navigate(Routes.ADD) },
                onCardClick = { id -> navController.navigate(Routes.detail(id)) },
            )
        }

        composable(Routes.ADD) {
            val vm: CardEditViewModel = viewModel(
                factory = viewModelFactory { initializer { CardEditViewModel(repo, null) } },
            )
            CardEditScreen(
                viewModel = vm,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.EDIT,
            arguments = listOf(navArgument(Routes.ARG_CARD_ID) { type = NavType.LongType }),
        ) { entry ->
            val cardId = entry.arguments?.getLong(Routes.ARG_CARD_ID) ?: return@composable
            val vm: CardEditViewModel = viewModel(
                factory = viewModelFactory { initializer { CardEditViewModel(repo, cardId) } },
            )
            CardEditScreen(
                viewModel = vm,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument(Routes.ARG_CARD_ID) { type = NavType.LongType }),
        ) { entry ->
            val cardId = entry.arguments?.getLong(Routes.ARG_CARD_ID) ?: return@composable
            val vm: CardDetailViewModel = viewModel(
                factory = viewModelFactory { initializer { CardDetailViewModel(repo, cardId) } },
            )
            CardDetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Routes.edit(id)) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    viewModel: CollectionViewModel,
    onAddCard: () -> Unit,
    onCardClick: (Long) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedTab == 0) "Übersicht" else "Meine Sammlung") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Dashboard, contentDescription = null) },
                    label = { Text("Übersicht") },
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.Style, contentDescription = null) },
                    label = { Text("Sammlung") },
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCard) {
                Icon(Icons.Filled.Add, contentDescription = "Karte hinzufügen")
            }
        },
    ) { padding ->
        when (selectedTab) {
            0 -> OverviewScreen(viewModel = viewModel, modifier = Modifier.padding(padding))
            else -> CardListScreen(
                viewModel = viewModel,
                onCardClick = onCardClick,
                modifier = Modifier.padding(padding),
            )
        }
    }
}
