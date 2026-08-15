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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
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
                onRegister = { email, pass -> viewModel.register(email, pass) },
                onGoogleSignIn = { viewModel.googleSignIn() }
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
                            AppNavTab.SHOP -> "BLACK MARKET PROTOCOL"
                            AppNavTab.WHEEL -> "FATE PROBABILITY MATRIX"
                            AppNavTab.MORE -> "SUB-NET ARCHIVES"
                            AppNavTab.SETTINGS -> "HARDWARE CONFIG"
                            AppNavTab.ADMIN -> "OVERSEER ROOT CONTROL"
                        },
                        title = when (activeTab) {
                            AppNavTab.DASHBOARD -> "DEAD-FEST TERMINAL"
                            AppNavTab.PLAYERS -> "ACTIVE SURVIVORS"
                            AppNavTab.SHOP -> "TACTICAL BLACK MARKET"
                            AppNavTab.WHEEL -> "WHEEL OF FATE"
                            AppNavTab.MORE -> if (activeMoreSubScreen == MoreSubScreen.HUB) "COMMUNICATION HUB" else activeMoreSubScreen.title
                            AppNavTab.SETTINGS -> "SURVIVOR SYSTEM"
                            AppNavTab.ADMIN -> "OVERSEER CONSOLE"
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
                    CrtScanlineOverlay(alpha = 0.04f)

                    when (activeTab) {
                        AppNavTab.DASHBOARD -> {
                            DashboardScreen(
                                user = user,
                                gameState = gameState,
                                recentEvents = recentEvents,
                                onReportCasualty = { viewModel.reportCasualty(it) },
                                onNavigateToShop = { viewModel.selectTab(AppNavTab.SHOP) },
                                showBroadcast = showBroadcastModal,
                                onDismissBroadcast = { viewModel.dismissBroadcast() }
                            )
                        }

                        AppNavTab.PLAYERS -> {
                            PlayersScreen(
                                users = allUsers,
                                currentUser = user,
                                onOpenChat = { targetUser ->
                                    viewModel.selectTab(AppNavTab.MORE)
                                    viewModel.navigateMore(MoreSubScreen.MESSAGES, targetUser)
                                },
                                onOpenTransfer = { targetUser ->
                                    viewModel.selectTab(AppNavTab.MORE)
                                    viewModel.navigateMore(MoreSubScreen.TRANSFER, targetUser)
                                }
                            )
                        }

                        AppNavTab.SHOP -> {
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
                                onDismissReviveModal = { viewModel.showReviveSectorModal.value = false }
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

                        AppNavTab.MORE -> {
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
                                onMarkPatchNoteRead = { viewModel.markPatchNoteRead(it) }
                            )
                        }

                        AppNavTab.SETTINGS -> {
                            SettingsScreen(
                                currentUser = user,
                                allUsers = allUsers,
                                onUpdateProfile = { name, avatar, color, themeChoice ->
                                    viewModel.updateProfile(name, avatar, color, themeChoice)
                                },
                                onSwitchUser = { viewModel.switchUser(it) },
                                onLogout = { viewModel.logout() }
                            )
                        }

                        AppNavTab.ADMIN -> {
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
                                onResetEventData = { viewModel.resetEventData() }
                            )
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
