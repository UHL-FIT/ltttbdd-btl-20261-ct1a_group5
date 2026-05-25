package com.example.pokedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.pokedex.ui.components.AppDrawer
import com.example.pokedex.ui.screens.HomeScreen
import com.example.pokedex.ui.screens.TeamBuilderScreen
import com.example.pokedex.ui.screens.PokemonDetailScreen
import com.example.pokedex.ui.screens.ItemDexScreen
import com.example.pokedex.ui.screens.LocationDexScreen
import com.example.pokedex.ui.screens.MoveDexScreen
import com.example.pokedex.ui.screens.TypeDexScreen
import com.example.pokedex.ui.screens.AbilityDexScreen
import com.example.pokedex.ui.screens.NatureDexScreen
import com.example.pokedex.viewmodel.PokemonViewModel
import com.example.pokedex.ui.theme.PokeDexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokeDexTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                var currentRoute by remember { mutableStateOf("pokedex") }

                var selectedPokemonId by remember { mutableStateOf<Int?>(null) }

                val pokemonViewModel: PokemonViewModel = viewModel()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AppDrawer(
                            currentRoute = currentRoute,
                            closeDrawer = { scope.launch { drawerState.close() } },
                            onNavigate = { route ->
                                currentRoute = route
                                selectedPokemonId = null
                            }
                        )
                    }
                ) {
                    if (selectedPokemonId != null) {
                        PokemonDetailScreen(
                            pokemonId = selectedPokemonId!!,
                            onBack = { selectedPokemonId = null },
                            onNavigateToPokemon = { nextId -> selectedPokemonId = nextId },
                            onNavigateHome = {
                                selectedPokemonId = null
                                currentRoute = "pokedex" // Force về trang chủ cho an toàn
                            },
                            viewModel = pokemonViewModel
                        )
                    } else {
                        // --- 2. ĐIỀU HƯỚNG THÔNG MINH ---
                        when (currentRoute) {
                            "pokedex" -> {
                                HomeScreen(
                                    onOpenMenu = { scope.launch { drawerState.open() } },
                                    onPokemonClick = { id -> selectedPokemonId = id },
                                    viewModel = pokemonViewModel
                                )
                            }
                            "team" -> {
                                TeamBuilderScreen(
                                    onOpenMenu = { scope.launch { drawerState.open() } },
                                    viewModel = pokemonViewModel
                                )
                            }
                            "items" -> {
                                ItemDexScreen(
                                    onOpenMenu = { scope.launch { drawerState.open() } }
                                )
                            }
                            "locations" -> {
                                LocationDexScreen(
                                    onOpenMenu = { scope.launch { drawerState.open() } }
                                )
                            }
                            "moves" -> {
                                MoveDexScreen(
                                    onOpenMenu = { scope.launch { drawerState.open() } }
                                )
                            }

                            "abilities" -> {
                                AbilityDexScreen(
                                    onOpenMenu = { scope.launch { drawerState.open() } }
                                )
                            }

                            "natures" -> {
                                NatureDexScreen(
                                    onOpenMenu = { scope.launch { drawerState.open() } }
                                )
                        }
                            "types" -> {
                                TypeDexScreen(
                                    onOpenMenu = { scope.launch { drawerState.open() } }
                                )
                            }
                            else -> {
                                HomeScreen(
                                    onOpenMenu = { scope.launch { drawerState.open() } },
                                    onPokemonClick = { id -> selectedPokemonId = id },
                                    viewModel = pokemonViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}