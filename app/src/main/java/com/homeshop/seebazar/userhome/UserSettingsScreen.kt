package com.homeshop.seebazar.userhome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.homeshop.seebazar.servicehome.smallcompose.SettingsRowItem

object UserSettingsMenuIds {
    const val Profile = "profile"
    const val MyStatus = "my_status"
    const val Terms = "terms"
    const val Privacy = "privacy"
    const val Logout = "logout"
}

private data class UserSettingsEntry(
    val id: String,
    val title: String,
    val isDestructive: Boolean = false,
)

private val userSettingsEntries = listOf(
    UserSettingsEntry(UserSettingsMenuIds.Profile, "Profile"),
    UserSettingsEntry(UserSettingsMenuIds.MyStatus, "My Status"),
    UserSettingsEntry(UserSettingsMenuIds.Terms, "T&C"),
    UserSettingsEntry(UserSettingsMenuIds.Privacy, "Privacy and Policy"),
    UserSettingsEntry(UserSettingsMenuIds.Logout, "Logout", isDestructive = true),
)

private val RowTextBlack = Color(0xFF000000)
private val WindowInsetsZero = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onMenuItemClick: (menuId: String) -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        contentWindowInsets = WindowInsetsZero,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsetsZero,
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        color = RowTextBlack,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = RowTextBlack,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = RowTextBlack,
                    navigationIconContentColor = RowTextBlack,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            items(
                items = userSettingsEntries,
                key = { it.id },
            ) { entry ->
                SettingsRowItem(
                    title = entry.title,
                    isDestructive = entry.isDestructive,
                    onClick = { onMenuItemClick(entry.id) },
                )
            }
        }
    }
}
