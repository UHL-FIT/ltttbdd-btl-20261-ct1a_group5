package com.example.pokedex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountProfileSheet(
    user: FirebaseUser?,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    var backupEnabled by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE9EDF1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(74.dp)
                    )
                }

                Spacer(modifier = Modifier.width(22.dp))

                Column {
                    Text(
                        text = user?.displayName ?: "PokéDex Trainer",
                        color = Color(0xFF3E5F78),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = user?.email ?: "No email",
                        color = Color.Gray,
                        fontSize = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(34.dp))

            Row(
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "Account",
                    tint = Color(0xFF3E5F78),
                    modifier = Modifier.size(30.dp)
                )

                Spacer(modifier = Modifier.width(18.dp))

                Column {
                    Text(
                        text = "Account",
                        color = Color(0xFF3E5F78),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your PokéDex account is completely free, and holds your favorites, checklist, team builder data, settings and more. If you delete your account, we will not be able to save or restore your data.",
                        color = Color(0xFF9AA7B2),
                        fontSize = 17.sp,
                        lineHeight = 27.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onDeleteAccount,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFE23B3B)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete"
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "DELETE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF3E5F78)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Logout,
                        contentDescription = "Sign out"
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "SIGN OUT",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(34.dp))

            Row(
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.CloudQueue,
                    contentDescription = "Cloud",
                    tint = Color(0xFF3E5F78),
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(18.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Data Backup & Restore",
                            color = Color(0xFF3E5F78),
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        Switch(
                            checked = backupEnabled,
                            onCheckedChange = {
                                backupEnabled = it
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "When enabled, PokéDex will backup your data such as favorites, caught Pokémon and team builder data to the cloud. Sign in from any device to restore your data.",
                        color = Color(0xFF9AA7B2),
                        fontSize = 17.sp,
                        lineHeight = 27.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Latest backup: None",
                color = Color(0xFF7E93A4),
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { },
                    enabled = backupEnabled,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF3E5F78)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Backup,
                        contentDescription = "Restore"
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "RESTORE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = { },
                    enabled = backupEnabled,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF3E5F78)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Backup,
                        contentDescription = "Backup"
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "BACKUP",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (backupEnabled) {
                    "Data backup & restore is enabled."
                } else {
                    "Data backup & restore is disabled. Enable the feature by flipping the switch above."
                },
                color = Color(0xFF9AA7B2),
                fontSize = 17.sp,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}