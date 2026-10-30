package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AddCrewScreen
import com.example.ui.screens.AddCustomerScreen
import com.example.ui.screens.AdMobBanner
import com.example.ui.screens.CrewHomeScreen
import com.example.ui.screens.CustomerDetailsScreen
import com.example.ui.screens.EditCrewScreen
import com.example.ui.screens.EditCustomerScreen
import com.example.ui.screens.EditEventScreen
import com.example.ui.screens.EventBookingScreen
import com.example.ui.screens.EventDetailsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StudioBottomBar

import com.example.ui.theme.MarutiPhotoStudioTheme
import com.example.ui.theme.StudioDarkText
import com.example.ui.theme.StudioGreenSuccess
import com.example.ui.theme.StudioRedPrimary
import com.example.ui.theme.StudioSecondaryGray
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.StudioViewModel

class LocalizedActivityContext(
    base: ComponentActivity,
    private val localizedResources: android.content.res.Resources
) : android.content.ContextWrapper(base), androidx.activity.result.ActivityResultRegistryOwner {
    override val activityResultRegistry: androidx.activity.result.ActivityResultRegistry
        get() = (baseContext as ComponentActivity).activityResultRegistry

    override fun getResources(): android.content.res.Resources = localizedResources
}

class MainActivity : ComponentActivity() {
    private val viewModel: StudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.util.AdMobManager.initialize(applicationContext)

        setContent {
            val currentLang by viewModel.language.collectAsState()

            // Request Notification permission gracefully via Compose launcher after initial layout
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                ) { /* Handled gracefully */ }

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            // Create localized context cleanly without mutating global window resources via deprecated updateConfiguration
            val localizedContext = remember(currentLang) {
                val locale = java.util.Locale(currentLang.code)
                java.util.Locale.setDefault(locale)
                val config = android.content.res.Configuration(resources.configuration).apply {
                    setLocale(locale)
                    setLayoutDirection(locale)
                }
                val confContext = createConfigurationContext(config)
                LocalizedActivityContext(this@MainActivity, confContext.resources)
            }

            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalConfiguration provides localizedContext.resources.configuration,
                androidx.compose.ui.platform.LocalContext provides localizedContext,
                androidx.activity.compose.LocalActivityResultRegistryOwner provides this@MainActivity
            ) {
                MarutiPhotoStudioTheme {
                    MarutiAppContent(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun MarutiAppContent(
    viewModel: StudioViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentLang by viewModel.language.collectAsState()
    val strings = remember(currentLang) { com.example.ui.strings.AppStrings.get(currentLang) }

    // Global Root BackHandler: handles system back on any non-Home screen safely
    BackHandler(enabled = currentScreen !is Screen.Home && currentScreen !is Screen.Splash) {
        viewModel.navigateBack()
    }

    // Collect user toast/status messages across the app
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            if (message.isNotBlank()) {
                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Determine current active tab index for bottom navigation:
    // 0: Home, 1: Event Booking, 2: Crew, 3: Settings
    val activeTabIndex = when (currentScreen) {
        is Screen.Home, is Screen.AddCustomer, is Screen.EditCustomer, is Screen.CustomerDetails -> 0
        is Screen.EventBookingFlow, is Screen.EventDetails, is Screen.EditEvent -> 1
        is Screen.CrewHome, is Screen.AddCrew, is Screen.EditCrew -> 2
        is Screen.Settings, is Screen.PrivacyPolicy -> 3
        else -> 0
    }

    val isAuthOrSplashScreen = currentScreen is Screen.Splash || currentScreen is Screen.SignIn
    val isFormScreen = when (currentScreen) {
        is Screen.AddCustomer, is Screen.EditCustomer, is Screen.EventBookingFlow, is Screen.EditEvent, is Screen.AddCrew, is Screen.EditCrew, is Screen.PrivacyPolicy -> true
        else -> false
    }

    val isAdMobBannerEnabled by viewModel.isAdMobBannerEnabled.collectAsState()
    val isAdMobTestMode by viewModel.isAdMobTestMode.collectAsState()
    val configuredBannerId by viewModel.bannerAdUnitId.collectAsState()
    val effectiveBannerId = remember(isAdMobTestMode, configuredBannerId) {
        viewModel.getEffectiveBannerAdUnitId()
    }

    Scaffold(
        bottomBar = {
            if (!isAuthOrSplashScreen) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Google AdMob Banner above Bottom Bar
                    if (isAdMobBannerEnabled) {
                        AdMobBanner(
                            adUnitId = effectiveBannerId,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    StudioBottomBar(
                        currentTabIndex = activeTabIndex,
                        strings = strings,
                        onTabSelected = { index ->
                            viewModel.onBottomNavSelected(index)
                        }
                    )
                }
            }
        },
        containerColor = Color.White,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            when (val screen = currentScreen) {
                is Screen.Splash -> {
                    SplashScreen(
                        strings = strings,
                        onSplashFinished = {
                            if (viewModel.currentUser.value == null) {
                                viewModel.navigateTo(Screen.SignIn, clearStack = true)
                            } else {
                                viewModel.navigateTo(Screen.Home, clearStack = true)
                            }
                        }
                    )
                }

                is Screen.SignIn -> {
                    SignInScreen(
                        authManager = viewModel.authManager,
                        onSignInSuccess = {
                            viewModel.onUserSignedIn()
                        },
                        onBack = null
                    )
                }


                is Screen.Home -> {
                    HomeScreen(viewModel = viewModel)
                }

                is Screen.AddCustomer -> {
                    AddCustomerScreen(viewModel = viewModel)
                }

                is Screen.EditCustomer -> {
                    EditCustomerScreen(customerId = screen.customerId, viewModel = viewModel)
                }

                is Screen.CustomerDetails -> {
                    CustomerDetailsScreen(customerId = screen.customerId, viewModel = viewModel)
                }

                is Screen.EventBookingFlow -> {
                    EventBookingScreen(
                        preselectedCustomerId = screen.preselectedCustomerId,
                        viewModel = viewModel
                    )
                }

                is Screen.EventDetails -> {
                    EventDetailsScreen(eventId = screen.eventId, viewModel = viewModel)
                }

                is Screen.EditEvent -> {
                    EditEventScreen(eventId = screen.eventId, viewModel = viewModel)
                }

                is Screen.CrewHome -> {
                    CrewHomeScreen(viewModel = viewModel)
                }

                is Screen.AddCrew -> {
                    AddCrewScreen(viewModel = viewModel)
                }

                is Screen.EditCrew -> {
                    EditCrewScreen(crewId = screen.crewId, viewModel = viewModel)
                }

                is Screen.Settings -> {
                    SettingsScreen(
                        viewModel = viewModel
                    )
                }

                is Screen.PrivacyPolicy -> {
                    PrivacyPolicyScreen(viewModel = viewModel)
                }
            }
        }
    }
}
