package com.example.pokedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import com.example.pokedex.ui.components.AppDrawer
import com.example.pokedex.ui.screens.HomeScreen
import com.example.pokedex.ui.screens.TeamBuilderScreen // Đã thêm Import màn hình TeamBuilder
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

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AppDrawer(
                            currentRoute = currentRoute,
                            closeDrawer = { scope.launch { drawerState.close() } },
                            onNavigate = { route -> currentRoute = route }
                        )
                    }
                ) {

                    when (currentRoute) {
                        "pokedex" -> {
                            HomeScreen(onOpenMenu = { scope.launch { drawerState.open() } })
                        }
                        "team" -> {
                            // Gọi màn hình Team Builder khi bấm vào menu
                            TeamBuilderScreen(onOpenMenu = { scope.launch { drawerState.open() } })
                        }
                        else -> {
                            // Màn hình mặc định dự phòng
                            HomeScreen(onOpenMenu = { scope.launch { drawerState.open() } })
                        }
                    }
                    // ==========================================
                }
            }
        }
    }
}