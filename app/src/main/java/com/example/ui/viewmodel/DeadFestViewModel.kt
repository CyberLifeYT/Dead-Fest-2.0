package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatMessage
import com.example.data.model.DiscordMedia
import com.example.data.model.EventLog
import com.example.data.model.GameState
import com.example.data.model.PatchNote
import com.example.data.model.ShopItem
import com.example.data.model.ShopTheme
import com.example.data.model.ShopTitle
import com.example.data.model.User
import com.example.data.model.VotePoll
import com.example.data.model.WheelSegment
import com.example.data.repository.DeadFestRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppNavTab(val title: String, val icon: String, val badge: String? = null) {
    DASHBOARD("DASHBOARD", "🏠"),
    PLAYERS("SURVIVORS", "👥"),
    SHOP("BLACK MARKET", "🛒"),
    WHEEL("FATE WHEEL", "🎡"),
    MORE("COMM HUB", "➕"),
    SETTINGS("SYSTEM", "⚙️"),
    ADMIN("OVERSEER", "🛡️")
}

enum class MoreSubScreen(val title: String) {
    HUB("COMMUNICATION HUB"),
    MEDIA("MEDIA FEED"),
    VOTING("SECTOR VOTING"),
    TRANSFER("COIN TRANSFER"),
    MESSAGES("SECURE COMMS"),
    PATCH_NOTES("PATCH LOGS"),
    ARCHIVES("TERMINAL ARCHIVES"),
    DOWNLOADS("SYSTEM DOWNLINK")
}

data class ToastMessage(
    val message: String,
    val isError: Boolean = false,
    val id: Long = System.currentTimeMillis()
)

class DeadFestViewModel(
    private val repository: DeadFestRepository = DeadFestRepository()
) : ViewModel() {

    // Boot uplink state
    private val _isBooting = MutableStateFlow(true)
    val isBooting: StateFlow<Boolean> = _isBooting.asStateFlow()

    private val _bootProgress = MutableStateFlow(0f)
    val bootProgress: StateFlow<Boolean> = _isBooting.asStateFlow()

    // Navigation state
    private val _activeTab = MutableStateFlow(AppNavTab.DASHBOARD)
    val activeTab: StateFlow<AppNavTab> = _activeTab.asStateFlow()

    private val _activeMoreSubScreen = MutableStateFlow(MoreSubScreen.HUB)
    val activeMoreSubScreen: StateFlow<MoreSubScreen> = _activeMoreSubScreen.asStateFlow()

    // Selected target chat user
    private val _activeChatRecipient = MutableStateFlow<User?>(null)
    val activeChatRecipient: StateFlow<User?> = _activeChatRecipient.asStateFlow()

    // Refreshing animation
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Toast notifications
    private val _toastEvent = MutableSharedFlow<ToastMessage>()
    val toastEvent: SharedFlow<ToastMessage> = _toastEvent.asSharedFlow()

    // Daily Bonus Modal Alert
    private val _dailyBonusAwarded = MutableStateFlow(false)
    val dailyBonusAwarded: StateFlow<Boolean> = _dailyBonusAwarded.asStateFlow()

    // Featured Emergency Broadcast Modal
    private val _showBroadcastModal = MutableStateFlow(false)
    val showBroadcastModal: StateFlow<Boolean> = _showBroadcastModal.asStateFlow()
    private val dismissedBroadcasts = mutableSetOf<String>()

    // Modal Sheet States
    val showReportModal = MutableStateFlow(false)
    val showCurseTargetModal = MutableStateFlow(false)
    val showReviveSectorModal = MutableStateFlow(false)
    val showDossierModal = MutableStateFlow<User?>(null)
    val pendingCurseItem = MutableStateFlow<ShopItem?>(null)
    val pendingReviveItem = MutableStateFlow<ShopItem?>(null)

    // Wheel spin state
    val isWheelSpinning = MutableStateFlow(false)
    val wheelSpinRotation = MutableStateFlow(0f)
    val wheelSpinResult = MutableStateFlow<WheelSegment?>(null)
    val wheelCooldownRemaining = MutableStateFlow(0)

    // Data streams from repository
    val currentUser: StateFlow<User?> = repository.currentUser
    val allUsers: StateFlow<List<User>> = repository.allUsers
    val gameState: StateFlow<GameState> = repository.gameState
    val recentEvents: StateFlow<List<EventLog>> = repository.recentEvents
    val mediaFeed: StateFlow<List<DiscordMedia>> = repository.mediaFeed
    val patchNotes: StateFlow<List<PatchNote>> = repository.patchNotes
    val votePolls: StateFlow<List<VotePoll>> = repository.votePolls
    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
    val isOnline: StateFlow<Boolean> = repository.isOnline

    init {
        // Boot sequence simulation
        viewModelScope.launch {
            delay(1200)
            _isBooting.value = false
            checkBroadcast()
        }
    }

    fun triggerRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(600)
            _isRefreshing.value = false
            showToast("TERMINAL SYNC COMPLETE // FEED UPDATED")
        }
    }

    fun selectTab(tab: AppNavTab) {
        _activeTab.value = tab
        if (tab != AppNavTab.MORE) {
            _activeMoreSubScreen.value = MoreSubScreen.HUB
            _activeChatRecipient.value = null
        }
    }

    fun navigateMore(subScreen: MoreSubScreen, chatRecipient: User? = null) {
        _activeMoreSubScreen.value = subScreen
        _activeChatRecipient.value = chatRecipient
    }

    fun showToast(msg: String, isError: Boolean = false) {
        viewModelScope.launch {
            _toastEvent.emit(ToastMessage(msg, isError))
        }
    }

    fun dismissDailyBonus() {
        _dailyBonusAwarded.value = false
    }

    fun dismissBroadcast() {
        _showBroadcastModal.value = false
        val id = gameState.value.featuredVideoId
        if (id.isNotEmpty()) {
            dismissedBroadcasts.add(id)
        }
    }

    private fun checkBroadcast() {
        val gs = gameState.value
        if (gs.featuredVideoText.isNotEmpty() && !dismissedBroadcasts.contains(gs.featuredVideoId)) {
            _showBroadcastModal.value = true
        }
    }

    // -------------------------------------------------------------
    // Auth
    // -------------------------------------------------------------

    fun login(email: String, pass: String) {
        val result = repository.loginWithEmail(email, pass)
        result.onSuccess {
            showToast("ACCESS GRANTED: Welcome, Survivor ${it.displayName}")
            checkBroadcast()
        }.onFailure {
            showToast(it.message ?: "Authentication failed", isError = true)
        }
    }

    fun register(email: String, pass: String) {
        val result = repository.registerWithEmail(email, pass)
        result.onSuccess {
            showToast("TERMINAL INITIALIZED: Survivor Profile Created")
            checkBroadcast()
        }.onFailure {
            showToast(it.message ?: "Registration failed", isError = true)
        }
    }

    fun googleSignIn() {
        val result = repository.googleSignIn("Agent Delta")
        result.onSuccess {
            showToast("SATELLITE UPLINK CONNECTED: Welcome ${it.displayName}")
            checkBroadcast()
        }.onFailure {
            showToast("Satellite uplink failed", isError = true)
        }
    }

    fun logout() {
        repository.logout()
        showToast("TERMINAL SESSION TERMINATED")
    }

    fun switchUser(user: User) {
        repository.switchUser(user)
        showToast("SWAPPED ACTIVE TERMINAL // ${user.displayName}")
    }

    // -------------------------------------------------------------
    // Casualty Report
    // -------------------------------------------------------------

    fun reportCasualty(sector: String) {
        val result = repository.reportCasualty(sector)
        result.onSuccess {
            showReportModal.value = false
            showToast("CASUALTY RECORDED: +1 in $sector (+5 Coins Bounty)")
        }.onFailure {
            showToast(it.message ?: "Failed to log casualty", isError = true)
        }
    }

    // -------------------------------------------------------------
    // Black Market Purchases
    // -------------------------------------------------------------

    fun buyItem(item: ShopItem) {
        when (item.id) {
            "curse" -> {
                pendingCurseItem.value = item
                showCurseTargetModal.value = true
            }
            "revive" -> {
                pendingReviveItem.value = item
                showReviveSectorModal.value = true
            }
            else -> {
                val result = repository.buyShopItem(item)
                result.onSuccess {
                    showToast(it)
                }.onFailure {
                    showToast(it.message ?: "Transaction rejected", isError = true)
                }
            }
        }
    }

    fun executeCursePurchase(targetUid: String) {
        val item = pendingCurseItem.value ?: return
        val result = repository.buyShopItem(item, targetUserUid = targetUid)
        showCurseTargetModal.value = false
        pendingCurseItem.value = null
        result.onSuccess {
            showToast(it)
        }.onFailure {
            showToast(it.message ?: "Curse deployment failed", isError = true)
        }
    }

    fun executeRevivePurchase(sector: String) {
        val item = pendingReviveItem.value ?: return
        val result = repository.buyShopItem(item, reviveSector = sector)
        showReviveSectorModal.value = false
        pendingReviveItem.value = null
        result.onSuccess {
            showToast(it)
        }.onFailure {
            showToast(it.message ?: "Revive sequence failed", isError = true)
        }
    }

    fun buyTitle(title: ShopTitle) {
        val result = repository.buyShopTitle(title)
        result.onSuccess {
            showToast(it)
        }.onFailure {
            showToast(it.message ?: "Title purchase failed", isError = true)
        }
    }

    fun buyTheme(theme: ShopTheme) {
        val result = repository.buyShopTheme(theme)
        result.onSuccess {
            showToast(it)
        }.onFailure {
            showToast(it.message ?: "Theme unlock failed", isError = true)
        }
    }

    // -------------------------------------------------------------
    // Wheel of Fate
    // -------------------------------------------------------------

    fun spinWheel() {
        if (isWheelSpinning.value || wheelCooldownRemaining.value > 0) return
        val items = gameState.value.wheelItems
        if (items.isEmpty()) return

        isWheelSpinning.value = true
        wheelSpinResult.value = null

        // Calculate weighted random pick
        val totalWeight = items.sumOf { it.weight }
        val randomNum = (1..totalWeight).random()
        var accumulated = 0
        var pickedIndex = 0
        for (i in items.indices) {
            accumulated += items[i].weight
            if (randomNum <= accumulated) {
                pickedIndex = i
                break
            }
        }
        val chosenSegment = items[pickedIndex]

        viewModelScope.launch {
            val segmentAngle = 360f / items.size
            val targetSegmentCenter = pickedIndex * segmentAngle + (segmentAngle / 2f)
            // 5 full rotations + offset
            val totalSpinsDegrees = 360f * 5 + (360f - targetSegmentCenter)
            wheelSpinRotation.value = wheelSpinRotation.value + totalSpinsDegrees

            delay(3200)
            isWheelSpinning.value = false
            wheelSpinResult.value = chosenSegment

            // 5-second cooldown
            wheelCooldownRemaining.value = 5
            while (wheelCooldownRemaining.value > 0) {
                delay(1000)
                wheelCooldownRemaining.value -= 1
            }
        }
    }

    fun acknowledgeWheelResult() {
        val segment = wheelSpinResult.value ?: return
        val outcomeMsg = repository.applyWheelOutcome(segment)
        wheelSpinResult.value = null
        showToast(outcomeMsg)
    }

    // -------------------------------------------------------------
    // Social / More Hub
    // -------------------------------------------------------------

    fun transferCoins(recipientUid: String, amount: Int) {
        val result = repository.transferCoins(recipientUid, amount)
        result.onSuccess {
            showToast(it)
        }.onFailure {
            showToast(it.message ?: "Transfer failed", isError = true)
        }
    }

    fun castVote(pollId: String, option: String) {
        val result = repository.castVote(pollId, option)
        result.onSuccess {
            showToast("VOTE SUBMITTED & LOGGED IN NEURAL MESH")
        }.onFailure {
            showToast(it.message ?: "Vote failed", isError = true)
        }
    }

    fun sendChatMessage(recipientUid: String, text: String) {
        val result = repository.sendChatMessage(recipientUid, text)
        result.onSuccess {
            // instant scroll or update
        }.onFailure {
            showToast(it.message ?: "Failed to transmit message", isError = true)
        }
    }

    fun markPatchNoteRead(id: String) {
        repository.markPatchNoteRead(id)
    }

    fun updateProfile(displayName: String, avatar: String, colorHex: String, themeId: String) {
        val result = repository.updateProfile(displayName, avatar, colorHex, themeId)
        result.onSuccess {
            showToast("SURVIVOR ID UPDATED // HARDWARE SYNCED")
        }.onFailure {
            showToast(it.message ?: "Update failed", isError = true)
        }
    }

    // -------------------------------------------------------------
    // Admin Controls
    // -------------------------------------------------------------

    fun toggleMarket(enabled: Boolean) {
        repository.toggleMarket(enabled)
        showToast("MARKET STATUS: ${if (enabled) "ENABLED" else "DISABLED"}")
    }

    fun toggleWheel(enabled: Boolean) {
        repository.toggleWheel(enabled)
        showToast("FATE WHEEL: ${if (enabled) "ENABLED" else "DISABLED"}")
    }

    fun updateBroadcast(text: String, url: String, id: String) {
        repository.updateBroadcast(text, url, id)
        showToast("SYSTEM BROADCAST TRANSMITTED ACROSS ALL UNITS")
    }

    fun triggerFlashSale(discount: Int) {
        repository.triggerFlashSale(discount)
        showToast("FLASH SALE ACTIVATED: $discount% DISCOUNT APPLIED")
    }

    fun stopFlashSale() {
        repository.stopFlashSale()
        showToast("FLASH SALE CONCLUDED")
    }

    fun addSector(name: String) {
        repository.addSector(name)
        showToast("SECTOR REGISTERED: $name")
    }

    fun toggleLockSector(name: String) {
        repository.toggleLockSector(name)
        showToast("SECTOR QUARANTINE STATUS TOGGLED")
    }

    fun deleteSector(name: String) {
        repository.deleteSector(name)
        showToast("SECTOR DECOMMISSIONED")
    }

    fun addShopItem(item: ShopItem) {
        repository.addShopItem(item)
        showToast("TACTICAL ITEM ADDED TO CATALOG")
    }

    fun deleteShopItem(id: String) {
        repository.deleteShopItem(id)
        showToast("ITEM EXPUNGED FROM INVENTORY")
    }

    fun addShopTitle(title: ShopTitle) {
        repository.addShopTitle(title)
        showToast("TITLE ADDED")
    }

    fun deleteShopTitle(name: String) {
        repository.deleteShopTitle(name)
        showToast("TITLE REMOVED")
    }

    fun addShopTheme(theme: ShopTheme) {
        repository.addShopTheme(theme)
        showToast("THEME ADDED")
    }

    fun deleteShopTheme(id: String) {
        repository.deleteShopTheme(id)
        showToast("THEME REMOVED")
    }

    fun createPatchNote(title: String, content: String, author: String, version: String) {
        repository.createPatchNote(title, content, author, version)
        showToast("PATCH LOG BROADCASTED")
    }

    fun deletePatchNote(id: String) {
        repository.deletePatchNote(id)
        showToast("PATCH LOG EXPUNGED")
    }

    fun createPoll(title: String, desc: String, options: List<String>) {
        repository.createPoll(title, desc, options)
        showToast("COUNCIL POLL OPENED")
    }

    fun toggleClosePoll(id: String) {
        repository.toggleClosePoll(id)
        showToast("POLL STATUS TOGGLED")
    }

    fun deletePoll(id: String) {
        repository.deletePoll(id)
        showToast("POLL DELETED")
    }

    fun adjustPlayerCoins(uid: String, delta: Int) {
        repository.adjustPlayerCoins(uid, delta)
        showToast("SURVIVOR BALANCE ADJUSTED BY $delta COINS")
    }

    fun togglePlayerAdmin(uid: String) {
        repository.togglePlayerAdmin(uid)
        showToast("OVERSEER PRIVILEGES TOGGLED")
    }

    fun wipePlayerData(uid: String) {
        repository.wipePlayerData(uid)
        showToast("SURVIVOR TELEMETRY PURGED")
    }

    fun purgeMedia() {
        repository.purgeMedia()
        showToast("DISCORD MEDIA FEED PURGED")
    }

    fun resetEventData() {
        repository.resetEventData()
        showToast("CENTRAL EVENT MATRIX & CASUALTIES RESET")
    }
}
