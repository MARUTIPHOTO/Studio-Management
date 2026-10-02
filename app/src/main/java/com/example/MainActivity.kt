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
import com.example.ui.screens.CrewHomeScreen
import com.example.ui.screens.CustomerDetailsScreen
import com.example.ui.screens.EditCrewScreen
import com.example.ui.screens.EditCustomerScreen
import com.example.ui.screens.EditEventScreen
import com.example.ui.screens.EventBookingScreen
import com.example.ui.screens.EventDetailsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StudioBottomBar
import com.example.ui.theme.MarutiPhotoStudioTheme
import com.example.ui.theme.StudioDarkText
import com.example.ui.theme.StudioGreenSuccess
import com.example.ui.theme.StudioRedPrimary
import com.example.ui.theme.StudioSecondaryGray
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.StudioViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: StudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarutiPhotoStudioTheme {
                MarutiAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MarutiAppContent(viewModel: StudioViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val strings = viewModel.strings

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
        is Screen.Settings -> 3
        else -> 0
    }

    val isSplashScreen = currentScreen is Screen.Splash

    Scaffold(
        bottomBar = {
            if (!isSplashScreen) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Small "Studio Management" ad/branding tag visible across all pages
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF7F7))
                            .padding(vertical = 3.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(StudioGreenSuccess)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Studio Management",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioDarkText
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(StudioRedPrimary)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "PRO",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• Smart Photography Solution",
                                fontSize = 10.sp,
                                color = StudioSecondaryGray
                            )
                        }
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
                            viewModel.navigateTo(Screen.Home, clearStack = true)
                        }
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
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
