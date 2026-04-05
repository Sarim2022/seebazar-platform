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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import com.google.firebase.auth.FirebaseAuth
import com.homeshop.seebazar.data.MarketplaceData
import com.google.firebase.firestore.Source
import com.homeshop.seebazar.data.UserFirestore
import com.homeshop.seebazar.data.UserLocationPrefs
import com.homeshop.seebazar.data.UserProfilePrefs
import com.homeshop.seebazar.data.VendorFirestoreSync
import com.homeshop.seebazar.data.VendorLocationPrefs
import com.homeshop.seebazar.data.VendorPrefs
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.homeshop.seebazar.forgot.SeebazarForgotScreen
import com.homeshop.seebazar.servicehome.VendorHome
import com.homeshop.seebazar.servicehome.smallcompose.ShopDetailsScreen
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
//        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    val marketplace = remember { MarketplaceData() }
                    val logout: (String) -> Unit = remember(navController) {
                        { homeRoute ->
                            val auth = FirebaseAuth.getInstance()
                            val uid = auth.currentUser?.uid
                            val ctx = this@MainActivity
                            val finish = {
                                auth.signOut()
                                VendorPrefs.clear(ctx)
                                VendorLocationPrefs.clear(ctx)
                                UserLocationPrefs.clear(ctx)
                                UserProfilePrefs.clear(ctx)
                                marketplace.resetForUserSession()
                                navController.navigate("login") {
                                    popUpTo(homeRoute) { inclusive = true }
                                }
                            }
                            if (uid != null) {
                                UserFirestore.usersCollection().document(uid)
                                    .update(UserFirestore.FIELD_IS_LOGIN, false)
                                    .addOnCompleteListener { finish() }
                            } else {
                                finish()
                            }
                        }
                    }

                    val vendorAccountDeletedPurge: () -> Unit = remember(navController) {
                        {
                            val auth = FirebaseAuth.getInstance()
                            val ctx = this@MainActivity
                            auth.signOut()
                            VendorPrefs.clear(ctx)
                            VendorLocationPrefs.clear(ctx)
                            UserLocationPrefs.clear(ctx)
                            UserProfilePrefs.clear(ctx)
                            marketplace.resetForUserSession()
                            navController.navigate("login") {
                                popUpTo("vendor_home") { inclusive = true }
                            }
                        }
                    }

                    // Navigation Host
                    NavHost(
                        navController = navController,
                        startDestination = "bootstrap",
                    ) {
                        composable("bootstrap") {
                            val ctx = LocalContext.current
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                            LaunchedEffect(Unit) {
                                val user = FirebaseAuth.getInstance().currentUser
                                if (user == null) {
                                    navController.navigate("login") {
                                        popUpTo("bootstrap") { inclusive = true }
                                    }
                                    return@LaunchedEffect
                                }
                                UserFirestore.usersCollection().document(user.uid).get(Source.SERVER)
                                    .addOnCompleteListener { task ->
                                        val snap = task.result
                                        if (!task.isSuccessful || snap == null || !snap.exists()) {
                                            navController.navigate("login") {
                                                popUpTo("bootstrap") { inclusive = true }
                                            }
                                            return@addOnCompleteListener
                                        }
                                        val type = snap.getString(UserFirestore.FIELD_TYPE).orEmpty()
                                        val isVendor = type.equals("Service", ignoreCase = true)
                                        val bootName = snap.getString(UserFirestore.FIELD_NAME).orEmpty()
                                        val bootEmail = snap.getString(UserFirestore.FIELD_EMAIL).orEmpty()
                                        UserProfilePrefs.persist(
                                            ctx,
                                            user.uid,
                                            bootName,
                                            bootEmail,
                                            type.ifBlank { if (isVendor) "Service" else "User" },
                                        )
                                        if (isVendor) {
                                            VendorFirestoreSync.applySnapshot(snap, marketplace)
                                            VendorPrefs.persist(
                                                ctx,
                                                user.uid,
                                                bootName,
                                                bootEmail,
                                                marketplace,
                                            )
                                        } else {
                                            marketplace.resetForUserSession()
                                            VendorPrefs.clear(ctx)
                                            VendorLocationPrefs.clear(ctx)
                                        }
                                        val dest = if (isVendor) "vendor_home" else "user_home"
                                        navController.navigate(dest) {
                                            popUpTo("bootstrap") { inclusive = true }
                                        }
                                    }
                            }
                        }

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
                                marketplace = marketplace,
                                onRegisterClick = { navController.navigate("signup") },
                                onForgotPasswordClick = { navController.navigate("forgot") },
                                onLoginSuccess = { isVendor ->
                                    val dest = if (isVendor) "vendor_home" else "user_home"
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
                                onRegisterComplete = { navController.popBackStack() },
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
                                onLogout = { logout("user_home") },
                            )
                        }
                        composable("vendor_home") {
                            VendorHome(
                                marketplace = marketplace,
                                onNavigateToShopDetails = {
                                    navController.navigate("shop_details")
                                },
                                onLogout = { logout("vendor_home") },
                                onVendorAccountDeleted = vendorAccountDeletedPurge,
                            )
                        }
                        composable("shop_details") {
                            val shopCtx = LocalContext.current
                            ShopDetailsScreen(
                                shops = marketplace.shopList,
                                shopIndex = 0,
                                onBack = { navController.popBackStack() },
                                onPersistVendor = {
                                    FirebaseAuth.getInstance().currentUser?.uid?.let { vUid ->
                                        VendorFirestoreSync.pushVendorMarketplace(vUid, marketplace, shopCtx)
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
