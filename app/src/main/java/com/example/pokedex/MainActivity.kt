package com.example.pokedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.pokedex.ui.components.AppDrawer
import com.example.pokedex.ui.screens.AbilityDexScreen
import com.example.pokedex.ui.screens.AccountProfileSheet
import com.example.pokedex.ui.screens.EmailLoginScreen
import com.example.pokedex.ui.screens.HomeScreen
import com.example.pokedex.ui.screens.ItemDexScreen
import com.example.pokedex.ui.screens.LocationDexScreen
import com.example.pokedex.ui.screens.MoveDexScreen
import com.example.pokedex.ui.screens.NatureDexScreen
import com.example.pokedex.ui.screens.PokemonDetailScreen
import com.example.pokedex.ui.screens.TeamBuilderScreen
import com.example.pokedex.ui.screens.TypeDexScreen
import com.example.pokedex.ui.theme.PokeDexTheme
import com.example.pokedex.viewmodel.PokemonViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {
            PokeDexTheme {
                PokeDexMainScreen()
            }
        }
    }
}

@Composable
fun PokeDexMainScreen() {
    val auth = remember { FirebaseAuth.getInstance() }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentRoute by remember { mutableStateOf("pokedex") }
    var routeAfterLogin by remember { mutableStateOf("pokedex") }
    var selectedPokemonId by remember { mutableStateOf<Int?>(null) }

    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var showProfileSheet by remember { mutableStateOf(false) }

    val pokemonViewModel: PokemonViewModel = viewModel()

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }

        auth.addAuthStateListener(listener)

        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                closeDrawer = {
                    scope.launch {
                        drawerState.close()
                    }
                },
                onNavigate = { route ->
                    currentRoute = route
                    selectedPokemonId = null
                },
                isLoggedIn = currentUser != null,
                userEmail = currentUser?.email,
                onSignInClick = {
                    scope.launch {
                        drawerState.close()
                    }

                    routeAfterLogin = currentRoute
                    currentRoute = "email_login"
                    selectedPokemonId = null
                },
                onProfileClick = {
                    scope.launch {
                        drawerState.close()
                    }

                    if (currentUser != null) {
                        showProfileSheet = true
                    } else {
                        routeAfterLogin = currentRoute
                        currentRoute = "email_login"
                        selectedPokemonId = null
                    }
                }
            )
        }
    ) {
        if (selectedPokemonId != null) {
            PokemonDetailScreen(
                pokemonId = selectedPokemonId!!,
                onBack = {
                    selectedPokemonId = null
                },
                onNavigateToPokemon = { nextId ->
                    selectedPokemonId = nextId
                },
                onNavigateHome = {
                    selectedPokemonId = null
                    currentRoute = "pokedex"
                },
                viewModel = pokemonViewModel
            )
        } else {
            when (currentRoute) {
                "pokedex" -> {
                    HomeScreen(
                        onOpenMenu = {
                            scope.launch {
                                drawerState.open()
                            }
                        },
                        onPokemonClick = { id ->
                            selectedPokemonId = id
                        },
                        viewModel = pokemonViewModel,
                        isLoggedIn = currentUser != null
                    )
                }

                "team" -> {
                    TeamBuilderScreen(
                        onOpenMenu = {
                            scope.launch {
                                drawerState.open()
                            }
                        },
                        viewModel = pokemonViewModel,
                        isLoggedIn = currentUser != null,
                        userEmail = currentUser?.email,
                        onRequireLogin = {
                            routeAfterLogin = "team"
                            currentRoute = "email_login"
                        }
                    )
                }

                "email_login" -> {
                    EmailLoginScreen(
                        onLoginSuccess = {
                            currentUser = auth.currentUser
                            currentRoute = routeAfterLogin
                        },
                        onBack = {
                            currentRoute = routeAfterLogin
                        }
                    )
                }

                "items" -> {
                    ItemDexScreen(
                        onOpenMenu = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }

                "locations" -> {
                    LocationDexScreen(
                        onOpenMenu = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }

                "moves" -> {
                    MoveDexScreen(
                        onOpenMenu = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }

                "abilities" -> {
                    AbilityDexScreen(
                        onOpenMenu = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }

                "natures" -> {
                    NatureDexScreen(
                        onOpenMenu = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }

                "types" -> {
                    TypeDexScreen(
                        onOpenMenu = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }

                else -> {
                    HomeScreen(
                        onOpenMenu = {
                            scope.launch {
                                drawerState.open()
                            }
                        },
                        onPokemonClick = { id ->
                            selectedPokemonId = id
                        },
                        viewModel = pokemonViewModel,
                        isLoggedIn = currentUser != null
                    )
                }
            }
        }
    }

    if (showProfileSheet) {
        AccountProfileSheet(
            user = currentUser,
            onDismiss = {
                showProfileSheet = false
            },
            onSignOut = {
                auth.signOut()
                currentUser = null
                showProfileSheet = false
                currentRoute = "pokedex"
            },
            onDeleteAccount = {
                currentUser?.delete()
                    ?.addOnCompleteListener {
                        auth.signOut()
                        currentUser = null
                        showProfileSheet = false
                        currentRoute = "pokedex"
                    }
            }
        )
    }
}