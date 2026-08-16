package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ChatMessage
import com.example.data.model.DiscordMedia
import com.example.data.model.EventLog
import com.example.data.model.GameState
import com.example.data.model.PatchNote
import com.example.data.model.PlayerData
import com.example.data.model.SectorStats
import com.example.data.model.ShopItem
import com.example.data.model.ShopTheme
import com.example.data.model.ShopTitle
import com.example.data.model.User
import com.example.data.model.VotePoll
import com.example.data.model.WheelSegment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DeadFestRepository {

    // SharedPreferences for local configuration persistence
    private var sharedPreferences: SharedPreferences? = null

    // Firebase instances with safe lazy initialization
    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (_: Throwable) {
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (_: Throwable) {
            null
        }
    }

    private val repoScope = CoroutineScope(Dispatchers.IO)
    private var usersListener: ListenerRegistration? = null
    private var eventsListener: ListenerRegistration? = null
    private var gameStateListener: ListenerRegistration? = null
    private var chatsListener: ListenerRegistration? = null
    private var patchNotesListener: ListenerRegistration? = null
    private var mediaListener: ListenerRegistration? = null

    // Reactive State Holders - Pure Real Live Backend
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _recentEvents = MutableStateFlow<List<EventLog>>(emptyList())
    val recentEvents: StateFlow<List<EventLog>> = _recentEvents.asStateFlow()

    private val _mediaFeed = MutableStateFlow<List<DiscordMedia>>(emptyList())
    val mediaFeed: StateFlow<List<DiscordMedia>> = _mediaFeed.asStateFlow()

    private val _patchNotes = MutableStateFlow<List<PatchNote>>(emptyList())
    val patchNotes: StateFlow<List<PatchNote>> = _patchNotes.asStateFlow()

    private val _votePolls = MutableStateFlow<List<VotePoll>>(emptyList())
    val votePolls: StateFlow<List<VotePoll>> = _votePolls.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Low-end / Performance mode is ON by default
    private val _performanceMode = MutableStateFlow(true)
    val performanceMode: StateFlow<Boolean> = _performanceMode.asStateFlow()

    init {
        setupFirestoreListeners()
        restoreAuthSession()
    }

    fun initPreferences(context: Context) {
        try {
            val prefs = context.getSharedPreferences("deadfest_prefs", Context.MODE_PRIVATE)
            sharedPreferences = prefs
            val savedMode = prefs.getBoolean("low_end_performance_mode", true)
            _performanceMode.value = savedMode
        } catch (_: Exception) {}
    }

    fun setPerformanceMode(enabled: Boolean) {
        _performanceMode.value = enabled
        try {
            sharedPreferences?.edit()?.putBoolean("low_end_performance_mode", enabled)?.apply()
        } catch (_: Exception) {}
    }

    private fun restoreAuthSession() {
        repoScope.launch {
            try {
                val currentFbUser = auth?.currentUser
                if (currentFbUser != null) {
                    val uid = currentFbUser.uid
                    val doc = firestore?.collection("users")?.document(uid)?.get()?.await()
                    if (doc != null && doc.exists()) {
                        val parsedUser = parseUserFromDoc(doc.id, doc.data ?: emptyMap())
                        _currentUser.value = parsedUser
                        checkAndApplyDailyBonus(parsedUser)
                    } else {
                        // User exists in auth but no doc yet
                        val newUser = User(
                            uid = uid,
                            email = currentFbUser.email ?: "survivor@deadfest.net",
                            displayName = currentFbUser.displayName ?: currentFbUser.email?.substringBefore("@") ?: "Survivor",
                            admin = currentFbUser.email?.contains("admin", ignoreCase = true) == true,
                            playerData = PlayerData(
                                avatar = "⚡",
                                color = "#7C4DFF",
                                coins = 150,
                                shield = false,
                                title = "Survivor",
                                ownedThemes = listOf("default", "retro_green", "amber_alert"),
                                selectedTheme = "default"
                            )
                        )
                        _currentUser.value = newUser
                        syncUserToFirestore(newUser)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun setupFirestoreListeners() {
        val db = firestore ?: return

        // 1. Listen to Real "users" collection
        usersListener = db.collection("users")
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener
                val parsedList = snapshots.documents.mapNotNull { doc ->
                    try {
                        parseUserFromDoc(doc.id, doc.data ?: emptyMap())
                    } catch (_: Exception) {
                        null
                    }
                }
                if (parsedList.isNotEmpty()) {
                    _allUsers.value = parsedList
                    // Also refresh current active user if present in list
                    val currentUid = _currentUser.value?.uid
                    if (currentUid != null) {
                        parsedList.find { it.uid == currentUid }?.let { liveUser ->
                            _currentUser.value = liveUser
                        }
                    }
                }
            }

        // 2. Listen to Real "gameState" collection
        gameStateListener = db.collection("gameState").document("central")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                try {
                    val grandTotal = snapshot.getLong("grandTotal")?.toInt() ?: _gameState.value.grandTotal
                    val econMult = (snapshot.get("economyMultiplier") as? Number)?.toDouble() ?: _gameState.value.economyMultiplier
                    val marketEnabled = snapshot.getBoolean("marketEnabled") ?: _gameState.value.marketEnabled
                    val wheelEnabled = snapshot.getBoolean("wheelEnabled") ?: _gameState.value.wheelEnabled
                    val discount = snapshot.getLong("flashSaleDiscount")?.toInt() ?: _gameState.value.flashSaleDiscount
                    val isFlash = snapshot.getBoolean("flashSaleActive") ?: _gameState.value.flashSaleActive
                    val broadcastText = snapshot.getString("featuredVideoText") ?: _gameState.value.featuredVideoText
                    val videoUrl = snapshot.getString("featuredVideoUrl") ?: _gameState.value.featuredVideoUrl
                    val rawGames = snapshot.get("games") as? List<*>
                    val gamesList = rawGames?.mapNotNull { it?.toString() } ?: _gameState.value.games
                    val rawLocked = snapshot.get("lockedGames") as? List<*>
                    val lockedList = rawLocked?.mapNotNull { it?.toString() } ?: _gameState.value.lockedGames

                    val rawModes = snapshot.get("sectorModes") as? Map<*, *>
                    val modesMap = if (rawModes != null) {
                        rawModes.mapNotNull { (k, v) ->
                            if (k != null && v != null) k.toString() to v.toString() else null
                        }.toMap()
                    } else {
                        _gameState.value.sectorModes
                    }

                    _gameState.value = _gameState.value.copy(
                        grandTotal = grandTotal,
                        economyMultiplier = econMult,
                        marketEnabled = marketEnabled,
                        wheelEnabled = wheelEnabled,
                        flashSaleActive = isFlash,
                        flashSaleDiscount = discount,
                        featuredVideoText = broadcastText,
                        featuredVideoUrl = videoUrl,
                        games = gamesList,
                        lockedGames = lockedList,
                        sectorModes = modesMap
                    )
                } catch (_: Exception) {}
            }

        // 3. Listen to Real "eventLog" collection
        eventsListener = db.collection("eventLog")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener
                val list = snapshots.documents.mapNotNull { doc ->
                    try {
                        EventLog(
                            id = doc.id,
                            category = doc.getString("category") ?: "death",
                            message = doc.getString("message") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            userUid = doc.getString("userUid"),
                            attackerUid = doc.getString("attackerUid"),
                            targetUid = doc.getString("targetUid"),
                            sector = doc.getString("sector"),
                            amount = doc.getLong("amount")?.toInt(),
                            kills = doc.getLong("kills")?.toInt()
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                _recentEvents.value = list
            }

        // 4. Listen to Real "messages" collection
        chatsListener = db.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limit(100)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener
                val list = snapshots.documents.mapNotNull { doc ->
                    try {
                        ChatMessage(
                            id = doc.id,
                            chatId = doc.getString("chatId") ?: "global",
                            senderId = doc.getString("senderId") ?: "",
                            senderName = doc.getString("senderName") ?: "Survivor",
                            text = doc.getString("text") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                _chatMessages.value = list
            }

        // 5. Listen to Real "patchNotes" collection
        patchNotesListener = db.collection("patchNotes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener
                val list = snapshots.documents.mapNotNull { doc ->
                    try {
                        PatchNote(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            content = doc.getString("content") ?: "",
                            author = doc.getString("author") ?: "HQ",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            versionTag = doc.getString("versionTag") ?: "v1.0"
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                _patchNotes.value = list
            }

        // 6. Listen to Real "discordMedia" collection
        mediaListener = db.collection("discordMedia")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener
                val list = snapshots.documents.mapNotNull { doc ->
                    try {
                        val rawLikedBy = doc.get("likedBy") as? List<*>
                        val likedByList = rawLikedBy?.mapNotNull { it?.toString() } ?: emptyList()
                        DiscordMedia(
                            id = doc.id,
                            type = doc.getString("type") ?: "Image",
                            url = doc.getString("url") ?: "",
                            caption = doc.getString("caption") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            authorName = doc.getString("authorName") ?: "Comms",
                            likes = doc.getLong("likes")?.toInt() ?: likedByList.size,
                            likedBy = likedByList
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                _mediaFeed.value = list
            }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseUserFromDoc(uid: String, data: Map<String, Any>): User {
        val displayName = data["displayName"]?.toString() ?: "Survivor"
        val email = data["email"]?.toString() ?: ""
        val admin = data["admin"] as? Boolean ?: false

        val pDataRaw = data["playerData"] as? Map<String, Any> ?: emptyMap()
        val avatar = pDataRaw["avatar"]?.toString() ?: "⚡"
        val color = pDataRaw["color"]?.toString() ?: "#7C4DFF"
        val coins = (pDataRaw["coins"] as? Number)?.toInt() ?: 150
        val shield = pDataRaw["shield"] as? Boolean ?: false
        val title = pDataRaw["title"]?.toString() ?: "Survivor"
        val ownedThemes = (pDataRaw["ownedThemes"] as? List<*>)?.mapNotNull { it?.toString() } ?: listOf("default")
        val ownedTitles = (pDataRaw["ownedTitles"] as? List<*>)?.mapNotNull { it?.toString() } ?: listOf("Survivor")
        val selectedTheme = pDataRaw["selectedTheme"]?.toString() ?: "default"

        val gamesMap = mutableMapOf<String, SectorStats>()
        val gamesRaw = pDataRaw["games"] as? Map<String, Any>
        if (gamesRaw != null) {
            for ((key, value) in gamesRaw) {
                if (value is Map<*, *>) {
                    val deaths = (value["deaths"] as? Number)?.toInt() ?: 0
                    val kills = (value["kills"] as? Number)?.toInt() ?: 0
                    gamesMap[key] = SectorStats(deaths = deaths, kills = kills)
                } else if (value is Number) {
                    gamesMap[key] = SectorStats(deaths = value.toInt(), kills = 0)
                }
            }
        }

        val lastLoginDate = pDataRaw["lastLoginDate"]?.toString()
        val lastReadPatchNoteId = pDataRaw["lastReadPatchNoteId"]?.toString()

        return User(
            uid = uid,
            email = email,
            displayName = displayName,
            admin = admin,
            playerData = PlayerData(
                avatar = avatar,
                color = color,
                coins = coins,
                shield = shield,
                title = title,
                ownedThemes = ownedThemes,
                ownedTitles = ownedTitles,
                selectedTheme = selectedTheme,
                games = gamesMap,
                lastLoginDate = lastLoginDate,
                lastReadPatchNoteId = lastReadPatchNoteId
            )
        )
    }

    // -------------------------------------------------------------
    // Authentication Operations
    // -------------------------------------------------------------

    suspend fun loginWithEmail(email: String, pass: String): Result<User> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || pass.length < 6) {
            return Result.failure(IllegalArgumentException("INVALID CREDENTIALS: Password must be at least 6 characters."))
        }

        try {
            val authInstance = auth
            if (authInstance != null) {
                val authResult = authInstance.signInWithEmailAndPassword(trimmedEmail, pass).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val uid = firebaseUser.uid
                    val doc = firestore?.collection("users")?.document(uid)?.get()?.await()
                    val user = if (doc != null && doc.exists()) {
                        parseUserFromDoc(uid, doc.data ?: emptyMap())
                    } else {
                        User(
                            uid = uid,
                            email = trimmedEmail,
                            displayName = firebaseUser.displayName ?: trimmedEmail.substringBefore("@"),
                            admin = trimmedEmail.contains("admin", ignoreCase = true),
                            playerData = PlayerData(
                                avatar = "⚡",
                                color = "#7C4DFF",
                                coins = 150,
                                shield = false,
                                title = "Survivor",
                                ownedThemes = listOf("default", "retro_green", "amber_alert"),
                                selectedTheme = "default"
                            )
                        )
                    }
                    updateUserInternal(user)
                    _currentUser.value = user
                    checkAndApplyDailyBonus(user)
                    syncUserToFirestore(user)
                    return Result.success(user)
                }
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        // Fallback for mock/local offline testing
        val user = User(
            uid = "usr_" + trimmedEmail.hashCode().toString().takeLast(6),
            email = trimmedEmail,
            displayName = trimmedEmail.substringBefore("@"),
            admin = trimmedEmail.contains("admin", ignoreCase = true),
            playerData = PlayerData(avatar = "⚡", color = "#7C4DFF", coins = 150)
        )
        updateUserInternal(user)
        _currentUser.value = user
        return Result.success(user)
    }

    suspend fun registerWithEmail(email: String, pass: String, customCallsign: String? = null): Result<User> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@") || pass.length < 6) {
            return Result.failure(IllegalArgumentException("INVALID PARAMS: Valid email and min 6-char passphrase required."))
        }

        val name = if (!customCallsign.isNullOrBlank()) customCallsign.trim() else trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }

        try {
            val authInstance = auth
            if (authInstance != null) {
                val authResult = authInstance.createUserWithEmailAndPassword(trimmedEmail, pass).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val uid = firebaseUser.uid
                    val newUser = User(
                        uid = uid,
                        email = trimmedEmail,
                        displayName = name,
                        admin = trimmedEmail.contains("admin", ignoreCase = true),
                        playerData = PlayerData(
                            avatar = "⚡",
                            color = "#7C4DFF",
                            coins = 150,
                            shield = false,
                            title = "Survivor",
                            ownedThemes = listOf("default", "retro_green", "amber_alert"),
                            selectedTheme = "default"
                        )
                    )
                    updateUserInternal(newUser)
                    _currentUser.value = newUser
                    checkAndApplyDailyBonus(newUser)
                    syncUserToFirestore(newUser)
                    return Result.success(newUser)
                }
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val newUser = User(
            uid = "usr_" + UUID.randomUUID().toString().take(8),
            email = trimmedEmail,
            displayName = name,
            admin = trimmedEmail.contains("admin", ignoreCase = true),
            playerData = PlayerData(avatar = "⚡", color = "#7C4DFF", coins = 150)
        )
        updateUserInternal(newUser)
        _currentUser.value = newUser
        return Result.success(newUser)
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        val trimmed = email.trim()
        if (trimmed.isEmpty() || !trimmed.contains("@")) {
            return Result.failure(IllegalArgumentException("Please provide a valid survivor email address."))
        }
        return try {
            auth?.sendPasswordResetEmail(trimmed)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun googleSignIn(accountName: String = "Survivor Operative"): Result<User> {
        val email = "${accountName.lowercase().replace(" ", "_")}@google.uplink"
        val newUser = User(
            uid = "goog_" + UUID.randomUUID().toString().take(8),
            email = email,
            displayName = accountName,
            admin = false,
            playerData = PlayerData(
                avatar = "🛰️",
                color = "#00F0FF",
                coins = 150,
                shield = false,
                title = "Survivor",
                ownedThemes = listOf("default"),
                selectedTheme = "default"
            )
        )
        updateUserInternal(newUser)
        _currentUser.value = newUser
        checkAndApplyDailyBonus(newUser)
        syncUserToFirestore(newUser)
        return Result.success(newUser)
    }

    fun logout() {
        try {
            auth?.signOut()
        } catch (_: Exception) {}
        _currentUser.value = null
    }

    fun switchUser(user: User) {
        _currentUser.value = user
    }

    private fun syncUserToFirestore(user: User) {
        repoScope.launch {
            try {
                val db = firestore ?: return@launch
                val gamesData = user.playerData.games.mapValues { entry ->
                    mapOf(
                        "deaths" to entry.value.deaths,
                        "kills" to entry.value.kills
                    )
                }
                val playerDataMap = hashMapOf<String, Any>(
                    "avatar" to user.playerData.avatar,
                    "color" to user.playerData.color,
                    "coins" to user.playerData.coins,
                    "shield" to user.playerData.shield,
                    "title" to user.playerData.title,
                    "ownedThemes" to user.playerData.ownedThemes,
                    "ownedTitles" to user.playerData.ownedTitles,
                    "selectedTheme" to user.playerData.selectedTheme,
                    "games" to gamesData,
                    "lastLoginDate" to (user.playerData.lastLoginDate ?: "")
                )

                val userDocMap = hashMapOf<String, Any>(
                    "uid" to user.uid,
                    "email" to user.email,
                    "displayName" to user.displayName,
                    "admin" to user.admin,
                    "playerData" to playerDataMap,
                    "updatedAt" to System.currentTimeMillis()
                )
                db.collection("users").document(user.uid).set(userDocMap, SetOptions.merge())
            } catch (_: Exception) {}
        }
    }

    // -------------------------------------------------------------
    // Daily Login Bonus
    // -------------------------------------------------------------

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    fun checkAndApplyDailyBonus(user: User): Boolean {
        val today = getTodayDateString()
        if (user.playerData.lastLoginDate != today) {
            val bonusCoins = 50
            val updatedUser = user.copy(
                playerData = user.playerData.copy(
                    coins = user.playerData.coins + bonusCoins,
                    lastLoginDate = today
                )
            )
            updateUserInternal(updatedUser)
            syncUserToFirestore(updatedUser)
            logEvent(
                EventLog(
                    id = "bonus_" + UUID.randomUUID().toString().take(6),
                    category = "wheel",
                    message = "${user.displayName} collected daily uplink bonus (+50 Coins)",
                    userUid = user.uid,
                    amount = bonusCoins
                )
            )
            return true
        }
        return false
    }

    // -------------------------------------------------------------
    // Casualty & Kill Reporting
    // -------------------------------------------------------------

    fun reportCasualty(sector: String): Result<Unit> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("No active survivor authenticated."))
        if (_gameState.value.lockedGames.contains(sector)) {
            return Result.failure(IllegalStateException("SECTOR LOCKED: Quarantined zone."))
        }

        // Increment user sector deaths
        val currentStats = user.playerData.games[sector] ?: SectorStats()
        val updatedGames = user.playerData.games.toMutableMap()
        updatedGames[sector] = currentStats.copy(deaths = currentStats.deaths + 1)

        val coinsEarned = (5 * _gameState.value.economyMultiplier).toInt().coerceAtLeast(1)
        val updatedUser = user.copy(
            playerData = user.playerData.copy(
                games = updatedGames,
                coins = user.playerData.coins + coinsEarned
            )
        )
        updateUserInternal(updatedUser)
        syncUserToFirestore(updatedUser)

        val newTotal = _gameState.value.grandTotal + 1
        _gameState.value = _gameState.value.copy(grandTotal = newTotal)

        repoScope.launch {
            try {
                firestore?.collection("gameState")?.document("central")?.set(
                    mapOf("grandTotal" to newTotal),
                    SetOptions.merge()
                )
            } catch (_: Exception) {}
        }

        logEvent(
            EventLog(
                id = "death_" + UUID.randomUUID().toString().take(6),
                category = "death",
                message = "${user.displayName} reported casualty in $sector (+1 death, +$coinsEarned coins)",
                userUid = user.uid,
                sector = sector,
                amount = coinsEarned
            )
        )
        return Result.success(Unit)
    }

    fun reportKill(sector: String): Result<Unit> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("No active survivor authenticated."))
        if (_gameState.value.lockedGames.contains(sector)) {
            return Result.failure(IllegalStateException("SECTOR LOCKED: Quarantined zone."))
        }

        val currentStats = user.playerData.games[sector] ?: SectorStats()
        val updatedGames = user.playerData.games.toMutableMap()
        updatedGames[sector] = currentStats.copy(kills = currentStats.kills + 1)

        val coinsEarned = (10 * _gameState.value.economyMultiplier).toInt().coerceAtLeast(1)
        val updatedUser = user.copy(
            playerData = user.playerData.copy(
                games = updatedGames,
                coins = user.playerData.coins + coinsEarned
            )
        )
        updateUserInternal(updatedUser)
        syncUserToFirestore(updatedUser)

        logEvent(
            EventLog(
                id = "kill_" + UUID.randomUUID().toString().take(6),
                category = "kill",
                message = "${user.displayName} confirmed kill in $sector (+1 kill, +$coinsEarned coins)",
                userUid = user.uid,
                sector = sector,
                kills = 1,
                amount = coinsEarned
            )
        )
        return Result.success(Unit)
    }

    fun toggleLikeMedia(mediaId: String) {
        val user = _currentUser.value ?: return
        val mediaItem = _mediaFeed.value.find { it.id == mediaId } ?: return
        val isLiked = mediaItem.likedBy.contains(user.uid)
        val updatedLikedBy = if (isLiked) mediaItem.likedBy - user.uid else mediaItem.likedBy + user.uid
        val updatedMedia = mediaItem.copy(
            likedBy = updatedLikedBy,
            likes = updatedLikedBy.size
        )
        _mediaFeed.value = _mediaFeed.value.map { if (it.id == mediaId) updatedMedia else it }

        repoScope.launch {
            try {
                firestore?.collection("discordMedia")?.document(mediaId)?.set(
                    mapOf(
                        "likedBy" to updatedLikedBy,
                        "likes" to updatedLikedBy.size
                    ),
                    SetOptions.merge()
                )
            } catch (_: Exception) {}
        }
    }

    // -------------------------------------------------------------
    // Black Market / Shop Actions
    // -------------------------------------------------------------

    fun getItemFinalPrice(basePrice: Int): Int {
        return if (_gameState.value.flashSaleActive) {
            val discountFactor = (100 - _gameState.value.flashSaleDiscount).coerceIn(10, 100)
            ((basePrice * discountFactor) / 100).coerceAtLeast(1)
        } else {
            basePrice
        }
    }

    fun buyShopItem(item: ShopItem, targetUserUid: String? = null, reviveSector: String? = null): Result<String> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("SURVIVOR UNAUTHORIZED"))
        if (!_gameState.value.marketEnabled) {
            return Result.failure(IllegalStateException("BLACK MARKET OFFLINE: Terminal trading suspended."))
        }
        val price = getItemFinalPrice(item.basePrice)
        if (user.playerData.coins < price) {
            return Result.failure(IllegalStateException("INSUFFICIENT FUNDS: Required $price Coins, available ${user.playerData.coins} Coins."))
        }

        when (item.id) {
            "shield" -> {
                val updatedUser = user.copy(
                    playerData = user.playerData.copy(
                        coins = user.playerData.coins - price,
                        shield = true
                    )
                )
                updateUserInternal(updatedUser)
                syncUserToFirestore(updatedUser)
                logEvent(
                    EventLog(
                        id = "shd_" + UUID.randomUUID().toString().take(6),
                        category = "wheel",
                        message = "${user.displayName} deployed Tactical Kinetic Shield.",
                        userUid = user.uid
                    )
                )
                return Result.success("Tactical Kinetic Shield deployed. Next hostile curse will be deflected.")
            }

            "curse" -> {
                if (targetUserUid == null) {
                    return Result.failure(IllegalArgumentException("TARGET REQUIRED: Select target survivor."))
                }
                val target = _allUsers.value.find { it.uid == targetUserUid }
                    ?: return Result.failure(IllegalArgumentException("Target survivor not found."))

                val updatedUser = user.copy(
                    playerData = user.playerData.copy(
                        coins = user.playerData.coins - price
                    )
                )
                updateUserInternal(updatedUser)
                syncUserToFirestore(updatedUser)

                if (target.playerData.shield) {
                    val updatedTarget = target.copy(
                        playerData = target.playerData.copy(shield = false)
                    )
                    updateUserInternal(updatedTarget)
                    syncUserToFirestore(updatedTarget)
                    logEvent(
                        EventLog(
                            id = "crs_blk_" + UUID.randomUUID().toString().take(6),
                            category = "curse_blocked",
                            message = "${user.displayName}'s Bio-Curse DEFLECTED by ${target.displayName}'s Shield!",
                            attackerUid = user.uid,
                            targetUid = target.uid
                        )
                    )
                    return Result.success("Bio-Curse deflected! ${target.displayName}'s shield shattered.")
                } else {
                    val primarySector = target.playerData.games.keys.firstOrNull() ?: _gameState.value.games.firstOrNull() ?: "Sector 01 - Red Wastelands"
                    val currentStats = target.playerData.games[primarySector] ?: SectorStats()
                    val targetGames = target.playerData.games.toMutableMap()
                    targetGames[primarySector] = currentStats.copy(deaths = currentStats.deaths + 1)
                    val updatedTarget = target.copy(
                        playerData = target.playerData.copy(games = targetGames)
                    )
                    updateUserInternal(updatedTarget)
                    syncUserToFirestore(updatedTarget)

                    val newGrandTotal = _gameState.value.grandTotal + 1
                    _gameState.value = _gameState.value.copy(grandTotal = newGrandTotal)

                    repoScope.launch {
                        try {
                            firestore?.collection("gameState")?.document("central")?.set(
                                mapOf("grandTotal" to newGrandTotal),
                                SetOptions.merge()
                            )
                        } catch (_: Exception) {}
                    }

                    logEvent(
                        EventLog(
                            id = "crs_suc_" + UUID.randomUUID().toString().take(6),
                            category = "curse_success",
                            message = "${user.displayName} cursed ${target.displayName} (+1 Casualty in $primarySector)!",
                            attackerUid = user.uid,
                            targetUid = target.uid,
                            sector = primarySector
                        )
                    )
                    return Result.success("Bio-Curse struck ${target.displayName} (+1 casualty recorded).")
                }
            }

            "revive" -> {
                if (reviveSector == null) {
                    return Result.failure(IllegalArgumentException("SECTOR REQUIRED: Select sector to purge casualty."))
                }
                val currentStats = user.playerData.games[reviveSector] ?: SectorStats()
                if (currentStats.deaths <= 0) {
                    return Result.failure(IllegalArgumentException("NO CASUALTIES: $reviveSector has 0 casualties."))
                }
                val updatedGames = user.playerData.games.toMutableMap()
                updatedGames[reviveSector] = currentStats.copy(deaths = currentStats.deaths - 1)
                val updatedUser = user.copy(
                    playerData = user.playerData.copy(
                        coins = user.playerData.coins - price,
                        games = updatedGames
                    )
                )
                updateUserInternal(updatedUser)
                syncUserToFirestore(updatedUser)
                logEvent(
                    EventLog(
                        id = "rev_" + UUID.randomUUID().toString().take(6),
                        category = "revive",
                        message = "${user.displayName} used Defibrillator in $reviveSector (-1 death)",
                        userUid = user.uid,
                        sector = reviveSector
                    )
                )
                return Result.success("Defibrillator discharged. -1 Casualty purged from $reviveSector.")
            }

            else -> {
                val updatedUser = user.copy(
                    playerData = user.playerData.copy(
                        coins = user.playerData.coins - price
                    )
                )
                updateUserInternal(updatedUser)
                syncUserToFirestore(updatedUser)
                logEvent(
                    EventLog(
                        id = "buy_" + UUID.randomUUID().toString().take(6),
                        category = "wheel",
                        message = "${user.displayName} bought ${item.name}.",
                        userUid = user.uid
                    )
                )
                return Result.success("${item.name} acquired.")
            }
        }
    }

    fun buyShopTitle(title: ShopTitle): Result<String> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("SURVIVOR UNAUTHORIZED"))
        if (user.playerData.ownedTitles.contains(title.name)) {
            val updatedUser = user.copy(
                playerData = user.playerData.copy(title = title.name)
            )
            updateUserInternal(updatedUser)
            syncUserToFirestore(updatedUser)
            return Result.success("Equipped Title: ${title.name}")
        }
        val price = getItemFinalPrice(title.price)
        if (user.playerData.coins < price) {
            return Result.failure(IllegalStateException("INSUFFICIENT FUNDS: Required $price Coins, available ${user.playerData.coins} Coins."))
        }
        val updatedTitles = (user.playerData.ownedTitles + title.name).distinct()
        val updatedUser = user.copy(
            playerData = user.playerData.copy(
                coins = user.playerData.coins - price,
                ownedTitles = updatedTitles,
                title = title.name
            )
        )
        updateUserInternal(updatedUser)
        syncUserToFirestore(updatedUser)
        logEvent(
            EventLog(
                id = "ttl_" + UUID.randomUUID().toString().take(6),
                category = "wheel",
                message = "${user.displayName} acquired Title '${title.name}'.",
                userUid = user.uid
            )
        )
        return Result.success("Equipped Title: ${title.name}")
    }

    fun buyShopTheme(theme: ShopTheme): Result<String> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("SURVIVOR UNAUTHORIZED"))
        if (user.playerData.ownedThemes.contains(theme.id)) {
            val updatedUser = user.copy(
                playerData = user.playerData.copy(selectedTheme = theme.id)
            )
            updateUserInternal(updatedUser)
            syncUserToFirestore(updatedUser)
            return Result.success("Activated Theme: ${theme.name}")
        }
        val price = getItemFinalPrice(theme.price)
        if (user.playerData.coins < price) {
            return Result.failure(IllegalStateException("INSUFFICIENT FUNDS: Required $price Coins, available ${user.playerData.coins} Coins."))
        }
        val updatedThemes = (user.playerData.ownedThemes + theme.id).distinct()
        val updatedUser = user.copy(
            playerData = user.playerData.copy(
                coins = user.playerData.coins - price,
                ownedThemes = updatedThemes,
                selectedTheme = theme.id
            )
        )
        updateUserInternal(updatedUser)
        syncUserToFirestore(updatedUser)
        logEvent(
            EventLog(
                id = "thm_" + UUID.randomUUID().toString().take(6),
                category = "wheel",
                message = "${user.displayName} unlocked Theme '${theme.name}'.",
                userUid = user.uid
            )
        )
        return Result.success("Activated Theme: ${theme.name}")
    }

    // -------------------------------------------------------------
    // Wheel of Fate Actions
    // -------------------------------------------------------------

    fun applyWheelOutcome(segment: WheelSegment): String {
        val user = _currentUser.value ?: return "No active survivor."
        var msg = ""
        var updatedUser = user

        when (segment.action) {
            "coins_50" -> {
                updatedUser = user.copy(
                    playerData = user.playerData.copy(coins = user.playerData.coins + 50)
                )
                msg = "+50 COINS CREDITED"
            }
            "coins_100" -> {
                updatedUser = user.copy(
                    playerData = user.playerData.copy(coins = user.playerData.coins + 100)
                )
                msg = "+100 COINS CREDITED"
            }
            "coins_250" -> {
                updatedUser = user.copy(
                    playerData = user.playerData.copy(coins = user.playerData.coins + 250)
                )
                msg = "+250 COINS CREDITED"
            }
            "coins_500" -> {
                updatedUser = user.copy(
                    playerData = user.playerData.copy(coins = user.playerData.coins + 500)
                )
                msg = "🔥 JACKPOT! +500 COINS CREDITED!"
            }
            "shield" -> {
                updatedUser = user.copy(
                    playerData = user.playerData.copy(shield = true)
                )
                msg = "🛡️ TACTICAL SHIELD ACTIVATED"
            }
            "death_plus_1" -> {
                val sec = _gameState.value.games.firstOrNull() ?: "Sector 01 - Red Wastelands"
                val currentStats = user.playerData.games[sec] ?: SectorStats()
                val gm = user.playerData.games.toMutableMap()
                gm[sec] = currentStats.copy(deaths = currentStats.deaths + 1)
                updatedUser = user.copy(
                    playerData = user.playerData.copy(games = gm)
                )
                val newTotal = _gameState.value.grandTotal + 1
                _gameState.value = _gameState.value.copy(grandTotal = newTotal)
                repoScope.launch {
                    try {
                        firestore?.collection("gameState")?.document("central")?.set(
                            mapOf("grandTotal" to newTotal),
                            SetOptions.merge()
                        )
                    } catch (_: Exception) {}
                }
                msg = "💀 FATE STRUCK: +1 CASUALTY IN $sec"
            }
            "bankrupt" -> {
                updatedUser = user.copy(
                    playerData = user.playerData.copy(coins = 0)
                )
                msg = "💥 BANKRUPT: ALL COINS LOST!"
            }
            else -> {
                updatedUser = user.copy(
                    playerData = user.playerData.copy(coins = user.playerData.coins + 75)
                )
                msg = "🎁 MYSTERY REWARD: +75 COINS"
            }
        }

        updateUserInternal(updatedUser)
        syncUserToFirestore(updatedUser)
        logEvent(
            EventLog(
                id = "whl_" + UUID.randomUUID().toString().take(6),
                category = "wheel",
                message = "${user.displayName} spun the Wheel: ${segment.label}",
                userUid = user.uid
            )
        )
        return msg
    }

    // -------------------------------------------------------------
    // Social: Coin Transfer, Messaging, Voting
    // -------------------------------------------------------------

    fun transferCoins(recipientUid: String, amount: Int): Result<String> {
        val sender = _currentUser.value ?: return Result.failure(IllegalStateException("SURVIVOR UNAUTHORIZED"))
        if (amount <= 0) return Result.failure(IllegalArgumentException("Amount must be greater than 0."))
        if (sender.playerData.coins < amount) {
            return Result.failure(IllegalStateException("INSUFFICIENT BALANCE: You have ${sender.playerData.coins} Coins."))
        }
        if (sender.uid == recipientUid) {
            return Result.failure(IllegalArgumentException("Cannot transfer coins to your own account."))
        }
        val recipient = _allUsers.value.find { it.uid == recipientUid }
            ?: return Result.failure(IllegalArgumentException("Recipient survivor not found."))

        val updatedSender = sender.copy(
            playerData = sender.playerData.copy(coins = sender.playerData.coins - amount)
        )
        val updatedRecipient = recipient.copy(
            playerData = recipient.playerData.copy(coins = recipient.playerData.coins + amount)
        )

        updateUserInternal(updatedSender)
        updateUserInternal(updatedRecipient)
        syncUserToFirestore(updatedSender)
        syncUserToFirestore(updatedRecipient)

        logEvent(
            EventLog(
                id = "xfr_" + UUID.randomUUID().toString().take(6),
                category = "transfer",
                message = "${sender.displayName} sent $amount Coins to ${recipient.displayName}",
                attackerUid = sender.uid,
                targetUid = recipient.uid,
                amount = amount
            )
        )
        return Result.success("Transferred $amount Coins to ${recipient.displayName}.")
    }

    fun castVote(pollId: String, option: String): Result<Unit> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("SURVIVOR UNAUTHORIZED"))
        val poll = _votePolls.value.find { it.id == pollId }
            ?: return Result.failure(IllegalArgumentException("Poll not found."))
        if (poll.closed) return Result.failure(IllegalStateException("POLL CLOSED"))

        val updatedVotes = poll.votes.toMutableMap()
        updatedVotes[user.uid] = option
        val updatedPoll = poll.copy(votes = updatedVotes)

        _votePolls.value = _votePolls.value.map {
            if (it.id == pollId) updatedPoll else it
        }
        return Result.success(Unit)
    }

    fun getChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    fun sendChatMessage(recipientUid: String, text: String): Result<ChatMessage> {
        val sender = _currentUser.value ?: return Result.failure(IllegalStateException("SURVIVOR UNAUTHORIZED"))
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return Result.failure(IllegalArgumentException("Message cannot be empty."))

        val chatId = getChatId(sender.uid, recipientUid)
        val msgId = "msg_" + UUID.randomUUID().toString().take(8)
        val message = ChatMessage(
            id = msgId,
            chatId = chatId,
            senderId = sender.uid,
            senderName = sender.displayName,
            text = trimmed,
            timestamp = System.currentTimeMillis()
        )
        _chatMessages.value = _chatMessages.value + message

        repoScope.launch {
            try {
                firestore?.collection("messages")?.document(msgId)?.set(
                    mapOf(
                        "id" to msgId,
                        "chatId" to chatId,
                        "senderId" to sender.uid,
                        "senderName" to sender.displayName,
                        "text" to trimmed,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            } catch (_: Exception) {}
        }

        return Result.success(message)
    }

    fun markPatchNoteRead(id: String) {
        val user = _currentUser.value ?: return
        val updated = user.copy(
            playerData = user.playerData.copy(lastReadPatchNoteId = id)
        )
        updateUserInternal(updated)
        syncUserToFirestore(updated)
    }

    fun updateProfile(displayName: String, avatar: String, colorHex: String, themeId: String): Result<Unit> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("SURVIVOR UNAUTHORIZED"))
        val trimmedName = displayName.trim().ifEmpty { user.displayName }
        val updated = user.copy(
            displayName = trimmedName,
            playerData = user.playerData.copy(
                avatar = avatar,
                color = colorHex,
                selectedTheme = themeId
            )
        )
        updateUserInternal(updated)
        syncUserToFirestore(updated)
        return Result.success(Unit)
    }

    // -------------------------------------------------------------
    // Admin Console Actions (Cloud Sync)
    // -------------------------------------------------------------

    fun toggleMarket(enabled: Boolean) {
        _gameState.value = _gameState.value.copy(marketEnabled = enabled)
        repoScope.launch {
            try {
                firestore?.collection("gameState")?.document("central")?.set(
                    mapOf("marketEnabled" to enabled),
                    SetOptions.merge()
                )
            } catch (_: Exception) {}
        }
    }

    fun toggleWheel(enabled: Boolean) {
        _gameState.value = _gameState.value.copy(wheelEnabled = enabled)
        repoScope.launch {
            try {
                firestore?.collection("gameState")?.document("central")?.set(
                    mapOf("wheelEnabled" to enabled),
                    SetOptions.merge()
                )
            } catch (_: Exception) {}
        }
    }

    fun updateBroadcast(text: String, videoUrl: String, videoId: String) {
        _gameState.value = _gameState.value.copy(
            featuredVideoText = text,
            featuredVideoUrl = videoUrl,
            featuredVideoId = videoId
        )
        repoScope.launch {
            try {
                firestore?.collection("gameState")?.document("central")?.set(
                    mapOf(
                        "featuredVideoText" to text,
                        "featuredVideoUrl" to videoUrl,
                        "featuredVideoId" to videoId
                    ),
                    SetOptions.merge()
                )
            } catch (_: Exception) {}
        }
    }

    fun triggerFlashSale(discountPercent: Int) {
        val discount = discountPercent.coerceIn(5, 75)
        _gameState.value = _gameState.value.copy(
            flashSaleActive = true,
            flashSaleDiscount = discount
        )
        repoScope.launch {
            try {
                firestore?.collection("gameState")?.document("central")?.set(
                    mapOf(
                        "flashSaleActive" to true,
                        "flashSaleDiscount" to discount
                    ),
                    SetOptions.merge()
                )
            } catch (_: Exception) {}
        }
    }

    fun stopFlashSale() {
        _gameState.value = _gameState.value.copy(flashSaleActive = false)
        repoScope.launch {
            try {
                firestore?.collection("gameState")?.document("central")?.set(
                    mapOf("flashSaleActive" to false),
                    SetOptions.merge()
                )
            } catch (_: Exception) {}
        }
    }

    fun updateEconomyMultiplier(multiplier: Double) {
        val clean = ((multiplier * 10).toInt() / 10.0).coerceIn(0.5, 10.0)
        _gameState.value = _gameState.value.copy(economyMultiplier = clean)
        repoScope.launch {
            try {
                firestore?.collection("gameState")?.document("central")?.set(
                    mapOf("economyMultiplier" to clean),
                    SetOptions.merge()
                )
            } catch (_: Exception) {}
        }
    }

    fun setSectorMode(sector: String, mode: String) {
        val updatedModes = _gameState.value.sectorModes.toMutableMap()
        updatedModes[sector] = mode
        _gameState.value = _gameState.value.copy(sectorModes = updatedModes)
        repoScope.launch {
            try {
                firestore?.collection("gameState")?.document("central")?.set(
                    mapOf("sectorModes" to updatedModes),
                    SetOptions.merge()
                )
            } catch (_: Exception) {}
        }
    }

    fun addSector(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty() && !_gameState.value.games.contains(trimmed)) {
            val updated = _gameState.value.games + trimmed
            _gameState.value = _gameState.value.copy(games = updated)
            repoScope.launch {
                try {
                    firestore?.collection("gameState")?.document("central")?.set(
                        mapOf("games" to updated),
                        SetOptions.merge()
                    )
                } catch (_: Exception) {}
            }
        }
    }

    fun toggleLockSector(name: String) {
        val currentLocked = _gameState.value.lockedGames
        val updated = if (currentLocked.contains(name)) {
            currentLocked - name
        } else {
            currentLocked + name
        }
        _gameState.value = _gameState.value.copy(lockedGames = updated)
        repoScope.launch {
            try {
                firestore?.collection("gameState")?.document("central")?.set(
                    mapOf("lockedGames" to updated),
                    SetOptions.merge()
                )
            } catch (_: Exception) {}
        }
    }

    fun deleteSector(name: String) {
        val updatedGames = _gameState.value.games - name
        val updatedLocked = _gameState.value.lockedGames - name
        _gameState.value = _gameState.value.copy(
            games = updatedGames,
            lockedGames = updatedLocked
        )
        repoScope.launch {
            try {
                firestore?.collection("gameState")?.document("central")?.set(
                    mapOf(
                        "games" to updatedGames,
                        "lockedGames" to updatedLocked
                    ),
                    SetOptions.merge()
                )
            } catch (_: Exception) {}
        }
    }

    fun addShopItem(item: ShopItem) {
        _gameState.value = _gameState.value.copy(
            shopItems = _gameState.value.shopItems + item
        )
    }

    fun deleteShopItem(id: String) {
        _gameState.value = _gameState.value.copy(
            shopItems = _gameState.value.shopItems.filterNot { it.id == id }
        )
    }

    fun addShopTitle(title: ShopTitle) {
        _gameState.value = _gameState.value.copy(
            shopTitles = _gameState.value.shopTitles + title
        )
    }

    fun deleteShopTitle(name: String) {
        _gameState.value = _gameState.value.copy(
            shopTitles = _gameState.value.shopTitles.filterNot { it.name == name }
        )
    }

    fun addShopTheme(theme: ShopTheme) {
        _gameState.value = _gameState.value.copy(
            shopThemes = _gameState.value.shopThemes + theme
        )
    }

    fun deleteShopTheme(id: String) {
        _gameState.value = _gameState.value.copy(
            shopThemes = _gameState.value.shopThemes.filterNot { it.id == id }
        )
    }

    fun createPatchNote(title: String, content: String, author: String, version: String) {
        val pnId = "pn_" + UUID.randomUUID().toString().take(6)
        val pn = PatchNote(
            id = pnId,
            title = title.trim(),
            content = content.trim(),
            author = author.trim().ifEmpty { "Command HQ" },
            versionTag = version.trim().ifEmpty { "v1.0.0" },
            timestamp = System.currentTimeMillis()
        )
        _patchNotes.value = listOf(pn) + _patchNotes.value
        repoScope.launch {
            try {
                firestore?.collection("patchNotes")?.document(pnId)?.set(
                    mapOf(
                        "id" to pnId,
                        "title" to pn.title,
                        "content" to pn.content,
                        "author" to pn.author,
                        "versionTag" to pn.versionTag,
                        "timestamp" to pn.timestamp
                    )
                )
            } catch (_: Exception) {}
        }
    }

    fun deletePatchNote(id: String) {
        _patchNotes.value = _patchNotes.value.filterNot { it.id == id }
        repoScope.launch {
            try {
                firestore?.collection("patchNotes")?.document(id)?.delete()
            } catch (_: Exception) {}
        }
    }

    fun createPoll(title: String, desc: String, options: List<String>) {
        val poll = VotePoll(
            id = "poll_" + UUID.randomUUID().toString().take(6),
            title = title.trim(),
            description = desc.trim(),
            options = options.map { it.trim() }.filter { it.isNotEmpty() },
            timestamp = System.currentTimeMillis()
        )
        _votePolls.value = listOf(poll) + _votePolls.value
    }

    fun toggleClosePoll(id: String) {
        _votePolls.value = _votePolls.value.map {
            if (it.id == id) it.copy(closed = !it.closed) else it
        }
    }

    fun deletePoll(id: String) {
        _votePolls.value = _votePolls.value.filterNot { it.id == id }
    }

    fun adjustPlayerCoins(uid: String, delta: Int) {
        val target = _allUsers.value.find { it.uid == uid } ?: return
        val newCoins = (target.playerData.coins + delta).coerceAtLeast(0)
        val updated = target.copy(
            playerData = target.playerData.copy(coins = newCoins)
        )
        updateUserInternal(updated)
        syncUserToFirestore(updated)
    }

    fun togglePlayerAdmin(uid: String) {
        val target = _allUsers.value.find { it.uid == uid } ?: return
        val updated = target.copy(admin = !target.admin)
        updateUserInternal(updated)
        syncUserToFirestore(updated)
    }

    fun wipePlayerData(uid: String) {
        val target = _allUsers.value.find { it.uid == uid } ?: return
        val updated = target.copy(
            playerData = PlayerData(
                avatar = target.playerData.avatar,
                color = target.playerData.color,
                coins = 0,
                shield = false,
                title = "Survivor",
                ownedThemes = listOf("default"),
                ownedTitles = listOf("Survivor"),
                selectedTheme = "default",
                games = emptyMap(),
                lastLoginDate = target.playerData.lastLoginDate
            )
        )
        updateUserInternal(updated)
        syncUserToFirestore(updated)
    }

    fun purgeMedia() {
        _mediaFeed.value = emptyList()
    }

    fun resetEventData() {
        _gameState.value = _gameState.value.copy(
            grandTotal = 0,
            lockedGames = emptyList()
        )
        _allUsers.value = _allUsers.value.map { user ->
            user.copy(
                playerData = user.playerData.copy(
                    coins = 150,
                    shield = false,
                    games = emptyMap()
                )
            )
        }
        _currentUser.value = _allUsers.value.find { it.uid == _currentUser.value?.uid }
        _recentEvents.value = emptyList()
    }

    // -------------------------------------------------------------
    // Helper Internal Operations
    // -------------------------------------------------------------

    private fun updateUserInternal(updatedUser: User) {
        _allUsers.value = if (_allUsers.value.any { it.uid == updatedUser.uid }) {
            _allUsers.value.map { if (it.uid == updatedUser.uid) updatedUser else it }
        } else {
            _allUsers.value + updatedUser
        }
        if (_currentUser.value?.uid == updatedUser.uid) {
            _currentUser.value = updatedUser
        }
    }

    private fun logEvent(event: EventLog) {
        _recentEvents.value = (listOf(event) + _recentEvents.value).take(100)
        repoScope.launch {
            try {
                firestore?.collection("eventLog")?.document(event.id)?.set(
                    mapOf(
                        "id" to event.id,
                        "category" to event.category,
                        "message" to event.message,
                        "timestamp" to event.timestamp,
                        "userUid" to (event.userUid ?: ""),
                        "attackerUid" to (event.attackerUid ?: ""),
                        "targetUid" to (event.targetUid ?: ""),
                        "sector" to (event.sector ?: ""),
                        "amount" to (event.amount ?: 0)
                    )
                )
            } catch (_: Exception) {}
        }
    }
}
