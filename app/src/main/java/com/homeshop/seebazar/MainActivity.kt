package com.homeshop.seebazar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset
import com.homeshop.seebazar.data.MarketplaceData
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.homeshop.seebazar.forgot.SeebazarForgotScreen
import com.homeshop.seebazar.servicehome.VendorHome
import com.homeshop.seebazar.signup.SeebazarSignupScreen
import com.homeshop.seebazar.ui.SeebazarLoginScreen
import com.homeshop.seebazar.userhome.UserHome

private const val AuthNavMs = 320

private val authTweenFloat = tween<Float>(durationMillis = AuthNavMs)
private val authTweenSlide = tween<IntOffset>(durationMillis = AuthNavMs)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            splashScreenView.remove()
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    val marketplace = remember { MarketplaceData() }

                    // Navigation Host
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                    ) {

                        composable(
                            route = "login",
                            enterTransition = {
                                fadeIn(authTweenFloat) +
                                    slideInHorizontally(authTweenSlide) { w -> -w / 2 }
                            },
                            exitTransition = {
                                fadeOut(authTweenFloat) +
                                    slideOutHorizontally(authTweenSlide) { w -> -w / 3 }
                            },
                            popEnterTransition = {
                                fadeIn(authTweenFloat) +
                                    slideInHorizontally(authTweenSlide) { w -> -w / 2 }
                            },
                            popExitTransition = {
                                fadeOut(authTweenFloat) +
                                    slideOutHorizontally(authTweenSlide) { w -> w / 3 }
                            },
                        ) {
                            SeebazarLoginScreen(
                                onRegisterClick = { navController.navigate("signup") },
                                onForgotPasswordClick = { navController.navigate("forgot") },
                                onLoginSuccess = { isServiceTab ->
                                    val dest = if (isServiceTab) "vendor_home" else "user_home"
                                    navController.navigate(dest) {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                            )
                        }
                        composable(
                            route = "signup",
                            enterTransition = {
                                fadeIn(authTweenFloat) +
                                    slideInHorizontally(authTweenSlide) { w -> w }
                            },
                            exitTransition = {
                                fadeOut(authTweenFloat) +
                                    slideOutHorizontally(authTweenSlide) { w -> -w / 4 }
                            },
                            popEnterTransition = {
                                fadeIn(authTweenFloat) +
                                    slideInHorizontally(authTweenSlide) { w -> -w / 4 }
                            },
                            popExitTransition = {
                                fadeOut(authTweenFloat) +
                                    slideOutHorizontally(authTweenSlide) { w -> w }
                            },
                        ) {
                            SeebazarSignupScreen(
                                onLoginClick = { navController.popBackStack() },
                            )
                        }
                        composable(
                            route = "forgot",
                            enterTransition = {
                                fadeIn(authTweenFloat) +
                                    slideInHorizontally(authTweenSlide) { w -> w }
                            },
                            exitTransition = {
                                fadeOut(authTweenFloat) +
                                    slideOutHorizontally(authTweenSlide) { w -> -w / 4 }
                            },
                            popEnterTransition = {
                                fadeIn(authTweenFloat) +
                                    slideInHorizontally(authTweenSlide) { w -> -w / 4 }
                            },
                            popExitTransition = {
                                fadeOut(authTweenFloat) +
                                    slideOutHorizontally(authTweenSlide) { w -> w }
                            },
                        ) {
                            SeebazarForgotScreen(
                                onLoginClick = { navController.popBackStack() },
                            )
                        }
                        composable("user_home") {
                            UserHome(
                                marketplace = marketplace,
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("user_home") { inclusive = true }
                                    }
                                },
                            )
                        }
                        composable("vendor_home") {
                            VendorHome(
                                marketplace = marketplace,
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("vendor_home") { inclusive = true }
                                    }
                                },
                            )
                        }

                    }
                }
            }
        }
    }
}
