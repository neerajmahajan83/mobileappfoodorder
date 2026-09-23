package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.CartSheet
import com.example.ui.components.TopHeader
import com.example.ui.screens.*
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PreBiteViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: PreBiteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PreBiteApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreBiteApp(viewModel: PreBiteViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val simulatedTime by viewModel.simulatedTime.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedSlot by viewModel.selectedMealSlot.collectAsState()
    val activePass by viewModel.activeSubscription.collectAsState()
    val isLateExpress = viewModel.isLateExpressActive
    val deskNote by viewModel.deliveryDeskNote.collectAsState()
    val orders by viewModel.allOrders.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val savedAddresses by viewModel.userAddresses.collectAsState()

    var showCartSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val activeOrderCount = orders.count { it.status != "DELIVERED" && it.status != "CANCELLED" }
    val pendingPaymentCount = orders.count { it.paymentStatus == "PENDING_VERIFICATION" }
    val cartCount = cartItems.values.sumOf { it.quantity }
    val isAdmin = currentUser?.role == "ADMIN" || currentScreen == "admin"

    // Back handler to navigate back to Home if in another screen
    BackHandler(enabled = currentScreen != "home" && currentScreen != "login") {
        viewModel.setScreen("home")
    }

    if (!isLoggedIn || currentScreen == "login") {
        AuthScreen(
            viewModel = viewModel,
            onAuthSuccess = {
                // Screen is handled in viewModel.login/register
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopHeader(
                simulatedTime = simulatedTime,
                isAdminMode = isAdmin,
                cartItemCount = cartCount,
                currentUser = currentUser,
                onToggleAdminMode = {
                    if (it) viewModel.setScreen("admin") else viewModel.setScreen("home")
                },
                onSelectTimePreset = { viewModel.setSimulatedPreset(it) },
                onCartClicked = { showCartSheet = true },
                onProfileClicked = { viewModel.setScreen("profile") },
                onWebPortalClicked = { viewModel.setScreen("web_portal") }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == "home",
                    onClick = { viewModel.setScreen("home") },
                    icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Menu") },
                    label = { Text("Menu") },
                    modifier = Modifier.testTag("nav_menu")
                )

                NavigationBarItem(
                    selected = currentScreen == "tracking",
                    onClick = { viewModel.setScreen("tracking") },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (activeOrderCount > 0) {
                                    Badge(containerColor = CoralPrimary) {
                                        Text("$activeOrderCount")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = "Batches")
                        }
                    },
                    label = { Text("Batches") },
                    modifier = Modifier.testTag("nav_batches")
                )

                NavigationBarItem(
                    selected = currentScreen == "web_portal",
                    onClick = { viewModel.setScreen("web_portal") },
                    icon = { Icon(Icons.Default.Language, contentDescription = "Web Site") },
                    label = { Text("Web Order") },
                    modifier = Modifier.testTag("nav_web_portal")
                )

                NavigationBarItem(
                    selected = currentScreen == "admin",
                    onClick = { viewModel.setScreen("admin") },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pendingPaymentCount > 0) {
                                    Badge(containerColor = CoralPrimary) {
                                        Text("$pendingPaymentCount")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Kitchen, contentDescription = "Admin")
                        }
                    },
                    label = { Text("Admin") },
                    modifier = Modifier.testTag("nav_kitchen")
                )

                NavigationBarItem(
                    selected = currentScreen == "profile",
                    onClick = { viewModel.setScreen("profile") },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "home" -> DashboardScreen(
                    viewModel = viewModel,
                    onOpenCart = { showCartSheet = true }
                )
                "tracking" -> TrackingScreen(
                    viewModel = viewModel,
                    onNavigateToSupport = { viewModel.setScreen("support") }
                )
                "pass" -> PassesScreen(viewModel = viewModel)
                "support" -> SupportScreen(
                    viewModel = viewModel,
                    onNavigateToTracking = { viewModel.setScreen("tracking") }
                )
                "admin" -> AdminPanelScreen(viewModel = viewModel)
                "profile" -> ProfileScreen(
                    viewModel = viewModel,
                    onLogout = { viewModel.setScreen("login") }
                )
                "web_portal" -> WebPortalView(
                    viewModel = viewModel,
                    onNavigateToAdmin = { viewModel.setScreen("admin") }
                )
                else -> DashboardScreen(
                    viewModel = viewModel,
                    onOpenCart = { showCartSheet = true }
                )
            }
        }

        // Checkout Bottom Sheet
        if (showCartSheet) {
            CartSheet(
                cartItems = cartItems,
                selectedSlot = selectedSlot,
                subtotal = viewModel.cartSubtotal,
                deliveryFee = viewModel.cartDeliveryFee,
                lateFee = viewModel.cartLateFee,
                discount = viewModel.cartDiscount,
                total = viewModel.cartTotal,
                isLateExpress = isLateExpress,
                deliveryDeskNote = deskNote,
                activePass = activePass,
                savedAddresses = savedAddresses,
                onUpdateDeskNote = { viewModel.updateDeliveryDeskNote(it) },
                onAddToCart = { viewModel.addToCart(it) },
                onRemoveFromCart = { viewModel.removeFromCart(it) },
                onDismiss = { showCartSheet = false },
                onPlaceOrder = { paymentMethod ->
                    viewModel.placeOrder(paymentMethod) { orderId ->
                        showCartSheet = false
                        viewModel.setScreen("tracking")
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("🎉 Order #$orderId Locked into $selectedSlot Batch!")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "PreBite Scheduled Meals for $name!", modifier = modifier)
}
