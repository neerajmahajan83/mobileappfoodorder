package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
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

    var showCartSheet by remember { mutableStateOf(false) }
    var isAdminMode by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val activeOrderCount = orders.count { it.status != "DELIVERED" && it.status != "CANCELLED" }
    val cartCount = cartItems.values.sumOf { it.quantity }

    // If admin mode is toggled, switch between customer screens and admin screen
    val effectiveScreen = if (isAdminMode) "admin" else currentScreen

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopHeader(
                simulatedTime = simulatedTime,
                isAdminMode = isAdminMode,
                cartItemCount = cartCount,
                onToggleAdminMode = {
                    isAdminMode = it
                    if (it) viewModel.setScreen("admin") else viewModel.setScreen("home")
                },
                onSelectTimePreset = { viewModel.setSimulatedPreset(it) },
                onCartClicked = { showCartSheet = true }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = effectiveScreen == "home",
                    onClick = {
                        isAdminMode = false
                        viewModel.setScreen("home")
                    },
                    icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Menu") },
                    label = { Text("Menu") },
                    modifier = Modifier.testTag("nav_menu")
                )

                NavigationBarItem(
                    selected = effectiveScreen == "tracking",
                    onClick = {
                        isAdminMode = false
                        viewModel.setScreen("tracking")
                    },
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
                    selected = effectiveScreen == "pass",
                    onClick = {
                        isAdminMode = false
                        viewModel.setScreen("pass")
                    },
                    icon = { Icon(Icons.Default.CardMembership, contentDescription = "Pass") },
                    label = { Text("Meal Pass") },
                    modifier = Modifier.testTag("nav_pass")
                )

                NavigationBarItem(
                    selected = effectiveScreen == "support",
                    onClick = {
                        isAdminMode = false
                        viewModel.setScreen("support")
                    },
                    icon = { Icon(Icons.Default.HeadsetMic, contentDescription = "Support") },
                    label = { Text("Support") },
                    modifier = Modifier.testTag("nav_support")
                )

                NavigationBarItem(
                    selected = effectiveScreen == "admin",
                    onClick = {
                        isAdminMode = true
                        viewModel.setScreen("admin")
                    },
                    icon = { Icon(Icons.Default.Kitchen, contentDescription = "Kitchen") },
                    label = { Text("Kitchen") },
                    modifier = Modifier.testTag("nav_kitchen")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (effectiveScreen) {
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
