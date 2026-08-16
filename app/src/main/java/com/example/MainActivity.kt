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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CrtScanlineOverlay
import com.example.ui.components.TerminalBottomNav
import com.example.ui.components.TerminalHeader
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BootScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MoreHubScreen
import com.example.ui.screens.PlayersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.WheelScreen
import com.example.ui.theme.DeadFestAppTheme
import com.example.ui.theme.TerminalTheme
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.DeadFestViewModel
import com.example.ui.viewmodel.MoreSubScreen
import com.example.ui.viewmodel.ToastMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: DeadFestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initPreferences(applicationContext)
        enableEdgeToEdge()

        setContent {
            val currentUser by viewModel.currentUser.collectAsState()
            val themeId = currentUser?.playerData?.selectedTheme ?: "default"

            DeadFestAppTheme(themeId = themeId) {
                DeadFestApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun DeadFestApp(viewModel: DeadFestViewModel) {
    val theme = TerminalTheme.current

    val isBooting by viewModel.isBooting.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val recentEvents by viewModel.recentEvents.collectAsState()
    val mediaFeed by viewModel.mediaFeed.collectAsState()
    val patchNotes by viewModel.patchNotes.collectAsState()
    val votePolls by viewModel.votePolls.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val performanceMode by viewModel.performanceMode.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()

    val activeTab by viewModel.activeTab.collectAsState()
    val activeMoreSubScreen by viewModel.activeMoreSubScreen.collectAsState()
    val activeChatRecipient by viewModel.activeChatRecipient.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val showBroadcastModal by viewModel.showBroadcastModal.collectAsState()

    // Modals
    val showCurseModal by viewModel.showCurseTargetModal.collectAsState()
    val showReviveModal by viewModel.showReviveSectorModal.collectAsState()

    // Fate Wheel State
    val isWheelSpinning by viewModel.isWheelSpinning.collectAsState()
    val wheelRotation by viewModel.wheelSpinRotation.collectAsState()
    val wheelResult by viewModel.wheelSpinResult.collectAsState()
    val wheelCooldown by viewModel.wheelCooldownRemaining.collectAsState()

    // Toast banner state
    var currentToast by remember { mutableStateOf<ToastMessage?>(null) }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { toast ->
            currentToast = toast
            delay(3200)
            if (currentToast?.id == toast.id) {
                currentToast = null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.bgDark)
    ) {
        if (isBooting) {
            BootScreen()
        } else if (currentUser == null) {
            AuthScreen(
                onLogin = { email, pass -> viewModel.login(email, pass) },
                onRegister = { email, pass, callsign -> viewModel.register(email, pass, callsign) },
                onGoogleSignIn = { viewModel.googleSignIn() },
                onForgotPassword = { email -> viewModel.sendPasswordReset(email) },
                isLoading = isAuthLoading,
                performanceMode = performanceMode
            )
        } else {
            val user = currentUser!!
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = theme.bgDark,
                topBar = {
                    TerminalHeader(
                        eyebrow = when (activeTab) {
                            AppNavTab.DASHBOARD -> "SECTOR TELEMETRY"
                            AppNavTab.PLAYERS -> "SURVIVOR ROSTER"
                            AppNavTab.MARKET -> "MARKET"
                            AppNavTab.WHEEL -> "SURVIVAL PROBABILITY"
                            AppNavTab.COMMS -> "INTEL & COMMS"
                            AppNavTab.SETTINGS -> "SYSTEM CONFIG"
                        },
                        title = when (activeTab) {
                            AppNavTab.DASHBOARD -> "DEAD-FEST TERMINAL"
                            AppNavTab.PLAYERS -> "SURVIVORS"
                            AppNavTab.MARKET -> "BLACK MARKET"
                            AppNavTab.WHEEL -> "WHEEL OF FATE"
                            AppNavTab.COMMS -> if (activeMoreSubScreen == MoreSubScreen.HUB) "INTEL HUB" else activeMoreSubScreen.title
                            AppNavTab.SETTINGS -> if (activeMoreSubScreen == MoreSubScreen.ADMIN) "OVERSEER CONSOLE" else "SETTINGS"
                        },
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.triggerRefresh() }
                    )
                },
                bottomBar = {
                    TerminalBottomNav(
                        activeTab = activeTab,
                        onTabSelected = { viewModel.selectTab(it) },
                        isAdmin = user.admin
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    CrtScanlineOverlay(alpha = 0.03f, enabled = !performanceMode)

                    when (activeTab) {
                        AppNavTab.DASHBOARD -> {
                            DashboardScreen(
                                user = user,
                                gameState = gameState,
                                recentEvents = recentEvents,
                                onReportCasualty = { viewModel.reportCasualty(it) },
                                onReportKill = { viewModel.reportKill(it) },
                                onNavigateToShop = { viewModel.selectTab(AppNavTab.MARKET) },
                                showBroadcast = showBroadcastModal,
                                onDismissBroadcast = { viewModel.dismissBroadcast() }
                            )
                        }

                        AppNavTab.PLAYERS -> {
                            PlayersScreen(
                                users = allUsers,
                                currentUser = user,
                                onOpenChat = { targetUser ->
                                    viewModel.selectTab(AppNavTab.COMMS)
                                    viewModel.navigateMore(MoreSubScreen.MESSAGES, targetUser)
                                },
                                onOpenTransfer = { targetUser ->
                                    viewModel.selectTab(AppNavTab.COMMS)
                                    viewModel.navigateMore(MoreSubScreen.TRANSFER, targetUser)
                                }
                            )
                        }

                        AppNavTab.MARKET -> {
                            ShopScreen(
                                user = user,
                                gameState = gameState,
                                allUsers = allUsers,
                                onBuyItem = { viewModel.buyItem(it) },
                                onExecuteCurse = { viewModel.executeCursePurchase(it) },
                                onExecuteRevive = { viewModel.executeRevivePurchase(it) },
                                onBuyTitle = { viewModel.buyTitle(it) },
                                onBuyTheme = { viewModel.buyTheme(it) },
                                showCurseModal = showCurseModal,
                                onDismissCurseModal = { viewModel.showCurseTargetModal.value = false },
                                showReviveModal = showReviveModal,
                                onDismissReviveModal = { viewModel.showReviveSectorModal.value = false },
                                isWheelSpinning = isWheelSpinning,
                                wheelRotation = wheelRotation,
                                wheelResult = wheelResult,
                                wheelCooldown = wheelCooldown,
                                onSpinWheel = { viewModel.spinWheel() },
                                onAcknowledgeWheelResult = { viewModel.acknowledgeWheelResult() }
                            )
                        }

                        AppNavTab.WHEEL -> {
                            WheelScreen(
                                user = user,
                                gameState = gameState,
                                isSpinning = isWheelSpinning,
                                rotationAngle = wheelRotation,
                                cooldownRemaining = wheelCooldown,
                                resultSegment = wheelResult,
                                onSpin = { viewModel.spinWheel() },
                                onAcknowledgeResult = { viewModel.acknowledgeWheelResult() }
                            )
                        }

                        AppNavTab.COMMS -> {
                            MoreHubScreen(
                                currentSubScreen = activeMoreSubScreen,
                                currentUser = user,
                                allUsers = allUsers,
                                gameState = gameState,
                                recentEvents = recentEvents,
                                mediaFeed = mediaFeed,
                                patchNotes = patchNotes,
                                votePolls = votePolls,
                                chatMessages = chatMessages,
                                activeChatRecipient = activeChatRecipient,
                                onNavigateSubScreen = { subScreen, chatRecipient ->
                                    viewModel.navigateMore(subScreen, chatRecipient)
                                },
                                onTransferCoins = { recipientUid, amt ->
                                    viewModel.transferCoins(recipientUid, amt)
                                },
                                onCastVote = { pollId, opt ->
                                    viewModel.castVote(pollId, opt)
                                },
                                onSendMessage = { recipientUid, text ->
                                    viewModel.sendChatMessage(recipientUid, text)
                                },
                                onMarkPatchNoteRead = { viewModel.markPatchNoteRead(it) },
                                onToggleLikeMedia = { viewModel.toggleLikeMedia(it) }
                            )
                        }

                        AppNavTab.SETTINGS -> {
                            if (activeMoreSubScreen == MoreSubScreen.ADMIN && user.admin) {
                                AdminScreen(
                                    gameState = gameState,
                                    allUsers = allUsers,
                                    patchNotes = patchNotes,
                                    votePolls = votePolls,
                                    onToggleMarket = { viewModel.toggleMarket(it) },
                                    onToggleWheel = { viewModel.toggleWheel(it) },
                                    onUpdateBroadcast = { text, url, id -> viewModel.updateBroadcast(text, url, id) },
                                    onTriggerFlashSale = { viewModel.triggerFlashSale(it) },
                                    onStopFlashSale = { viewModel.stopFlashSale() },
                                    onUpdateEconomyMultiplier = { viewModel.updateEconomyMultiplier(it) },
                                    onSetSectorMode = { sec, mode -> viewModel.setSectorMode(sec, mode) },
                                    onAddSector = { viewModel.addSector(it) },
                                    onToggleLockSector = { viewModel.toggleLockSector(it) },
                                    onDeleteSector = { viewModel.deleteSector(it) },
                                    onAddShopItem = { viewModel.addShopItem(it) },
                                    onDeleteShopItem = { viewModel.deleteShopItem(it) },
                                    onAddShopTitle = { viewModel.addShopTitle(it) },
                                    onDeleteShopTitle = { viewModel.deleteShopTitle(it) },
                                    onAddShopTheme = { viewModel.addShopTheme(it) },
                                    onDeleteShopTheme = { viewModel.deleteShopTheme(it) },
                                    onCreatePatchNote = { t, c, a, v -> viewModel.createPatchNote(t, c, a, v) },
                                    onDeletePatchNote = { viewModel.deletePatchNote(it) },
                                    onCreatePoll = { t, d, o -> viewModel.createPoll(t, d, o) },
                                    onToggleClosePoll = { viewModel.toggleClosePoll(it) },
                                    onDeletePoll = { viewModel.deletePoll(it) },
                                    onAdjustCoins = { uid, delta -> viewModel.adjustPlayerCoins(uid, delta) },
                                    onToggleAdmin = { viewModel.togglePlayerAdmin(it) },
                                    onWipeUser = { viewModel.wipePlayerData(it) },
                                    onPurgeMedia = { viewModel.purgeMedia() },
                                    onResetEventData = { viewModel.resetEventData() },
                                    onBack = { viewModel.navigateMore(MoreSubScreen.HUB) }
                                )
                            } else {
                                SettingsScreen(
                                    currentUser = user,
                                    allUsers = allUsers,
                                    performanceMode = performanceMode,
                                    onTogglePerformanceMode = { viewModel.togglePerformanceMode(it) },
                                    onUpdateProfile = { name, avatar, color, themeChoice ->
                                        viewModel.updateProfile(name, avatar, color, themeChoice)
                                    },
                                    onSwitchUser = { viewModel.switchUser(it) },
                                    onLogout = { viewModel.logout() },
                                    onOpenAdmin = {
                                        viewModel.navigateMore(MoreSubScreen.ADMIN)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Custom Cyber Toast Notification Overlay
        AnimatedVisibility(
            visible = currentToast != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 44.dp, start = 16.dp, end = 16.dp)
        ) {
            currentToast?.let { toast ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(14.dp), spotColor = if (toast.isError) theme.error else theme.primary),
                    color = theme.surface1.copy(alpha = 0.98f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (toast.isError) theme.error else theme.primary,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (toast.isError) "⚠️" else "⚡",
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = toast.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (toast.isError) theme.error else theme.textLight,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
