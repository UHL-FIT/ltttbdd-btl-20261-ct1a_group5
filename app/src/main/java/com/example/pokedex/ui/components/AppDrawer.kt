package com.example.pokedex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppDrawer(
    currentRoute: String = "pokedex",
    closeDrawer: () -> Unit,
    onNavigate: (String) -> Unit,
    isLoggedIn: Boolean = false,
    userEmail: String? = null,
    onSignInClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        modifier = Modifier.width(300.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PokéDex",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE3350D)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF334F6A))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (isLoggedIn) {
                            userEmail ?: "Signed in trainer"
                        } else {
                            "Welcome, trainer"
                        },
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row {
                        OutlinedButton(
                            onClick = {
                                onProfileClick()
                            },
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "PROFILE",
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (!isLoggedIn) {
                            OutlinedButton(
                                onClick = {
                                    onSignInClick()
                                },
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "SIGN IN",
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }
                        } else {
                            Text(
                                text = "SIGNED IN",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            DrawerMenuItem(
                icon = Icons.Default.CatchingPokemon,
                label = "Pokédex",
                isSelected = currentRoute == "pokedex",
                onClick = {
                    onNavigate("pokedex")
                    closeDrawer()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.FlashOn,
                label = "Move Dex",
                isSelected = currentRoute == "moves",
                onClick = {
                    onNavigate("moves")
                    closeDrawer()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.AutoAwesome,
                label = "Ability Dex",
                isSelected = currentRoute == "abilities",
                onClick = {
                    onNavigate("abilities")
                    closeDrawer()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Backpack,
                label = "Item Dex",
                isSelected = currentRoute == "items",
                onClick = {
                    onNavigate("items")
                    closeDrawer()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.LocationOn,
                label = "Location Dex",
                isSelected = currentRoute == "locations",
                onClick = {
                    onNavigate("locations")
                    closeDrawer()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Label,
                label = "Type Dex",
                isSelected = currentRoute == "types",
                onClick = {
                    onNavigate("types")
                    closeDrawer()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Eco,
                label = "Nature Dex",
                isSelected = currentRoute == "natures",
                onClick = {
                    onNavigate("natures")
                    closeDrawer()
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                thickness = 1.dp,
                color = Color.LightGray
            )

            DrawerMenuItem(
                icon = Icons.Default.Groups,
                label = "Team Builder",
                isSelected = currentRoute == "team",
                onClick = {
                    onNavigate("team")
                    closeDrawer()
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                thickness = 1.dp,
                color = Color.LightGray
            )

            DrawerMenuItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = currentRoute == "settings",
                onClick = {
                    onNavigate("settings")
                    closeDrawer()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tintColor = if (isSelected) Color(0xFFE3350D) else Color(0xFF334F6A)

    NavigationDrawerItem(
        label = {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = tintColor
            )
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tintColor
            )
        },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Color(0xFFF5F5F5),
            unselectedContainerColor = Color.Transparent
        ),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
    )
}