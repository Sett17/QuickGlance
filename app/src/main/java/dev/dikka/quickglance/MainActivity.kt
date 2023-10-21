package dev.dikka.quickglance

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.dikka.quickglance.ui.InputScreen
import dev.dikka.quickglance.ui.ReadScreen

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Preferences.init(this)

        setContent {
            val navController = rememberNavController()
            val currentBackStack by navController.currentBackStackEntryAsState()
            val currentDestination = currentBackStack?.destination
            val currentScreen =
                screens.find { it.route == currentDestination?.route } ?: Input

            val snackbarHostState = remember { SnackbarHostState() }

            val input = remember {
                mutableStateOf(
                    when (intent.action) {
                        Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
                        Intent.ACTION_PROCESS_TEXT -> intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)
                        else -> ""
                    } ?: ""
                )
            }

            MaterialTheme(
                colorScheme = dynamicDarkColorScheme(LocalContext.current)
            ) {
                Scaffold(
                    Modifier.background(MaterialTheme.colorScheme.background),
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.padding(16.dp)
                        )
                    },
                    topBar = {
                        TopAppBar(
                            modifier = Modifier.padding(0.dp),
                            title = {
                                Text(
                                    text = "Quick Glance",
                                    fontSize = 24.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            },
                            navigationIcon = {
                                AnimatedVisibility(
                                    currentScreen != Input,
                                    enter = expandHorizontally(tween(100)) + fadeIn(tween(50)),
                                    exit = shrinkHorizontally(tween(100)) + fadeOut(tween(50))
                                ) {
                                    IconButton(
                                        modifier = Modifier.width(48.dp),
                                        onClick = { navController.popBackStack() }) {
                                        Icon(
                                            Icons.Rounded.ArrowBack,
                                            contentDescription = "Back",
                                            tint = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Input.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(route = Input.route,
                            enterTransition = {
                                fadeIn(tween(100))
                            },
                            exitTransition = {
                                fadeOut(tween(100))
                            }) {
                            InputScreen(input, gotoRead = {
                                navController.navigateSingleTopTo(Read.route)
                            }, snackbarHostState = snackbarHostState)
                        }
                        composable(route = Read.route,
                            enterTransition = {
                                fadeIn(tween(100))
                            },
                            exitTransition = {
                                fadeOut(tween(100))
                            }) {
                            Column(
                                Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                            ) {
                                ReadScreen(
                                    input.value,
                                    snackbarHostState = snackbarHostState
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(
            this@navigateSingleTopTo.graph.findStartDestination().id
        ) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
