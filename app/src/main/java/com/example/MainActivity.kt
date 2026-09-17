package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.AiChatScreen
import com.example.ui.AuthScreen
import com.example.ui.ClientsScreen
import com.example.ui.DashboardScreen
import com.example.ui.MoreServicesScreen
import com.example.ui.ProductsScreen
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CosmicPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.MyApplicationTheme

enum class MainTab(val title: String, val iconRes: Int, val tag: String) {
    DASHBOARD("Métricas", R.drawable.ic_nav_dashboard, "tab_dashboard"),
    PRODUCTS("Estoque", R.drawable.ic_nav_products, "tab_products"),
    CLIENTS("Clientes", R.drawable.ic_nav_clients, "tab_clients"),
    MORE("Mais", R.drawable.ic_nav_more, "tab_more")
}

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val crmVm: CrmViewModel by viewModels()
    private val aiChatVm: AiChatViewModel by viewModels()
    private val supportChatVm: SupportChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Dynamic initialization of Firebase services using custom supplied keys
        FirebaseService.initialize(applicationContext)
        
        // Execute check current user
        authViewModel.checkCurrentUser()

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val authState by authViewModel.authState.collectAsState()
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "auth",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = innerPadding.calculateTopPadding())
                    ) {
                        composable("auth") {
                            AuthScreen(
                                viewModel = authViewModel,
                                onAuthSuccess = { uid ->
                                    // Initialize view models with merchant context
                                    crmVm.loadMerchantData(uid)
                                    aiChatVm.setMerchantId(uid)
                                    
                                    // Route safely to the multi-tab layout
                                    navController.navigate("main") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("main") {
                            val activeUid = FirebaseService.getCurrentMerchantId()
                            if (activeUid == null) {
                                // Redirect to login if user not authenticated
                                LaunchedEffect(Unit) {
                                    navController.navigate("auth") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            } else {
                                // Main Tab-based Companion view Hub
                                MainHubScreen(
                                    merchantId = activeUid,
                                    crmVm = crmVm,
                                    aiChatVm = aiChatVm,
                                    supportChatVm = supportChatVm,
                                    onLogout = {
                                        authViewModel.signOut()
                                        navController.navigate("auth") {
                                            popUpTo("main") { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }
                    
                    // Simple routing checkpoint on current logged-in user
                    LaunchedEffect(authState) {
                        if (authState is AuthState.Success) {
                            val uid = (authState as AuthState.Success).uid
                            crmVm.loadMerchantData(uid)
                            aiChatVm.setMerchantId(uid)
                            navController.navigate("main") {
                                popUpTo("auth") { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Extra definitions

@Composable
fun MainHubScreen(
    merchantId: String,
    crmVm: CrmViewModel,
    aiChatVm: AiChatViewModel,
    supportChatVm: SupportChatViewModel,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MainTab.DASHBOARD) }
    val crmState by crmVm.uiState.collectAsState()
    val totalSales by crmVm.totalSalesValue.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = BorderDark.copy(alpha = 0.8f))
                    .navigationBarsPadding()
                    .testTag("main_bottom_nav"),
                tonalElevation = 0.dp
            ) {
                MainTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                painter = painterResource(id = tab.iconRes),
                                contentDescription = tab.title,
                                tint = if (isSelected) CosmicPrimary else TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedTextColor = CosmicPrimary,
                            indicatorColor = CosmicPrimary.copy(alpha = 0.14f),
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Render Selected Tab contents
            when (selectedTab) {
                MainTab.DASHBOARD -> {
                    DashboardScreen(
                        state = crmState,
                        totalSales = totalSales,
                        onSignOutClick = onLogout
                    )
                }
                MainTab.PRODUCTS -> {
                    ProductsScreen(
                        state = crmState,
                        viewModel = crmVm
                    )
                }
                MainTab.CLIENTS -> {
                    ClientsScreen(
                        state = crmState,
                        crmViewModel = crmVm,
                        chatViewModel = supportChatVm,
                        merchantId = merchantId
                    )
                }
                MainTab.MORE -> {
                    MoreServicesScreen(
                        crmState = crmState,
                        crmVm = crmVm,
                        supportChatVm = supportChatVm,
                        totalSales = totalSales,
                        merchantId = merchantId,
                        onSignOutClick = onLogout
                    )
                }
            }
        }
    }
}
