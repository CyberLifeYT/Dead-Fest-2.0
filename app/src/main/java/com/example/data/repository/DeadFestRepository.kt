package com.example.data.repository

import com.example.data.model.ChatMessage
import com.example.data.model.DiscordMedia
import com.example.data.model.EventLog
import com.example.data.model.GameState
import com.example.data.model.PatchNote
import com.example.data.model.PlayerData
import com.example.data.model.SectorDeaths
import com.example.data.model.ShopItem
import com.example.data.model.ShopTheme
import com.example.data.model.ShopTitle
import com.example.data.model.User
import com.example.data.model.VotePoll
import com.example.data.model.WheelSegment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DeadFestRepository {

    // Initial pre-seeded survivor players
    private val initialUsers = listOf(
        User(
            uid = "admin_overlord",
            email = "commander@deadfest.terminal",
            displayName = "Command_Overlord",
            admin = true,
            playerData = PlayerData(
                avatar = "☣️",
                color = "#FF3B30",
                coins = 1250,
                shield = true,
                title = "Terminal Architect",
                ownedThemes = listOf("default", "blood_moon", "retro_green", "cyber_blue"),
                ownedTitles = listOf("Survivor", "Terminal Architect", "Cyber Overlord"),
                selectedTheme = "default",
                games = mapOf(
                    "Sector 01 - Red Wastelands" to SectorDeaths(12),
                    "Sector 04 - Core Reactor" to SectorDeaths(24),
                    "Sector 07 - Neo-Haven" to SectorDeaths(5)
                ),
                lastLoginDate = "2026-08-15"
            )
        ),
        User(
            uid = "user_neon_reaper",
            email = "reaper@wasteland.net",
            displayName = "NeonReaper_99",
            admin = false,
            playerData = PlayerData(
                avatar = "💀",
                color = "#BF00FF",
                coins = 890,
                shield = false,
                title = "Reaper Elite",
                ownedThemes = listOf("default", "toxic_hazard"),
                ownedTitles = listOf("Survivor", "Reaper Elite"),
                selectedTheme = "toxic_hazard",
                games = mapOf(
                    "Sector 01 - Red Wastelands" to SectorDeaths(38),
                    "Sector 04 - Core Reactor" to SectorDeaths(19),
                    "Sector 09 - Sub-Levels" to SectorDeaths(42)
                )
            )
        ),
        User(
            uid = "user_cyber_valkyrie",
            email = "valk@neo-haven.org",
            displayName = "CyberValkyrie",
            admin = false,
            playerData = PlayerData(
                avatar = "⚡",
                color = "#00F0FF",
                coins = 620,
                shield = true,
                title = "Void Walker",
                ownedThemes = listOf("default", "cyber_blue"),
                ownedTitles = listOf("Survivor", "Void Walker"),
                selectedTheme = "cyber_blue",
                games = mapOf(
                    "Sector 07 - Neo-Haven" to SectorDeaths(14),
                    "Sector 09 - Sub-Levels" to SectorDeaths(8)
                )
            )
        ),
        User(
            uid = "user_ghost_walker",
            email = "ghost@terminal.io",
            displayName = "GhostWalker_X",
            admin = false,
            playerData = PlayerData(
                avatar = "🗡️",
                color = "#ECEFF1",
                coins = 480,
                shield = false,
                title = "Ghost of Sector 7",
                ownedThemes = listOf("default", "ghost_white"),
                ownedTitles = listOf("Survivor", "Ghost of Sector 7"),
                selectedTheme = "ghost_white",
                games = mapOf(
                    "Sector 01 - Red Wastelands" to SectorDeaths(7),
                    "Sector 07 - Neo-Haven" to SectorDeaths(31)
                )
            )
        ),
        User(
            uid = "user_amber_scout",
            email = "scout@amber.sector",
            displayName = "AmberScout_404",
            admin = false,
            playerData = PlayerData(
                avatar = "🤖",
                color = "#FFB300",
                coins = 310,
                shield = false,
                title = "Biohazard Operative",
                ownedThemes = listOf("default", "amber_alert"),
                ownedTitles = listOf("Survivor", "Biohazard Operative"),
                selectedTheme = "amber_alert",
                games = mapOf(
                    "Sector 04 - Core Reactor" to SectorDeaths(11)
                )
            )
        )
    )

    private val initialEvents = listOf(
        EventLog(
            id = "evt_1",
            category = "death",
            message = "NeonReaper_99 reported casualty in Sector 09 - Sub-Levels (+1 death)",
            sector = "Sector 09 - Sub-Levels",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 4
        ),
        EventLog(
            id = "evt_2",
            category = "curse_success",
            message = "Command_Overlord deployed Bio-Curse against GhostWalker_X (Casualty +1)",
            attackerUid = "admin_overlord",
            targetUid = "user_ghost_walker",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 18
        ),
        EventLog(
            id = "evt_3",
            category = "curse_blocked",
            message = "Hostile Bio-Curse deflected! CyberValkyrie's Tactical Shield consumed.",
            targetUid = "user_cyber_valkyrie",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 35
        ),
        EventLog(
            id = "evt_4",
            category = "wheel",
            message = "NeonReaper_99 spun the Wheel of Fate and scored JACKPOT +500 COINS!",
            amount = 500,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 52
        ),
        EventLog(
            id = "evt_5",
            category = "revive",
            message = "Command_Overlord activated Emergency Defibrillator in Sector 04 - Core Reactor (-1 death)",
            sector = "Sector 04 - Core Reactor",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 95
        ),
        EventLog(
            id = "evt_6",
            category = "transfer",
            message = "CyberValkyrie transferred 150 Coins to AmberScout_404",
            amount = 150,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 140
        )
    )

    private val initialMedia = listOf(
        DiscordMedia(
            id = "med_1",
            type = "Image",
            url = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=60",
            caption = "SURVEILLANCE CAM #04: Severe radiation flare detected at the Reactor containment barrier. Heavy casualties reported.",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
            authorName = "Sector 04 Beacon",
            likes = 47
        ),
        DiscordMedia(
            id = "med_2",
            type = "Image",
            url = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800&auto=format&fit=crop&q=60",
            caption = "UPLINK ARCHIVE: Re-established satellite link to Sub-Levels. Black market trader caravan spotted.",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 8,
            authorName = "Scout Drone-7",
            likes = 32
        ),
        DiscordMedia(
            id = "med_3",
            type = "Video",
            url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            caption = "TACTICAL BRIEFING: Biohazard containment protocol v3.8. All operatives review revised rules of engagement.",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
            authorName = "Overlord Command",
            likes = 89
        )
    )

    private val initialPatchNotes = listOf(
        PatchNote(
            id = "pn_384",
            versionTag = "v3.8.4",
            title = "Tactical Overhaul: Flash Sales & Shield Deflection",
            content = "1. Activated dynamic Black Market flash discounts with automatic countdown matrix.\n2. Tactical Shield now grants complete kinetic deflection against bio-curse orbital strikes.\n3. Wheel of Fate spin cooldown reduced to 5.0 seconds with enhanced jackpot probability.\n4. Added 8 high-contrast CRT monitor themes with distinct phosphor calibration.",
            author = "Terminal Architect",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 2
        ),
        PatchNote(
            id = "pn_380",
            versionTag = "v3.8.0",
            title = "Sub-Level Grid Expansion & Chat Protocol",
            content = "1. Sector 09 - Sub-Levels unlocked for survival squads.\n2. Point-to-point encrypted messaging mainframe deployed between survivors.\n3. Daily uplink bonus increased to 50 Coins per login cycle.\n4. Added dossier security records and sector statistics archive.",
            author = "Overlord Command",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7
        )
    )

    private val initialVotes = listOf(
        VotePoll(
            id = "poll_1",
            title = "Next Sector Containment Breach",
            description = "Which restricted quarantine zone should the Central Terminal unlock in the next cycle?",
            options = listOf("Sector X - Bio-Lab Vault", "Sector 13 - Orbital Crash Site", "Sector 02 - Toxic Floods"),
            votes = mapOf(
                "admin_overlord" to "Sector X - Bio-Lab Vault",
                "user_neon_reaper" to "Sector X - Bio-Lab Vault",
                "user_cyber_valkyrie" to "Sector 13 - Orbital Crash Site"
            )
        ),
        VotePoll(
            id = "poll_2",
            title = "Black Market Supply Drop Priority",
            description = "Authorize tactical item requisition for upcoming flash sale rotation.",
            options = listOf("Bio-Curse Beacon (+50% Power)", "Emergency Defibrillators (+2x Stock)", "Neural EMP Jammer"),
            votes = mapOf(
                "user_neon_reaper" to "Bio-Curse Beacon (+50% Power)",
                "user_ghost_walker" to "Emergency Defibrillators (+2x Stock)"
            )
        )
    )

    private val initialChats = listOf(
        ChatMessage(
            id = "msg_1",
            chatId = "admin_overlord_user_neon_reaper",
            senderId = "admin_overlord",
            senderName = "Command_Overlord",
            text = "Reaper, status on Sector 09 containment?",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 45
        ),
        ChatMessage(
            id = "msg_2",
            chatId = "admin_overlord_user_neon_reaper",
            senderId = "user_neon_reaper",
            senderName = "NeonReaper_99",
            text = "Heavy casualties at sub-level 4. Requesting coin transfer for Tactical Shield.",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 30
        )
    )

    // Reactive State Holders
    private val _currentUser = MutableStateFlow<User?>(initialUsers.first())
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(initialUsers)
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _recentEvents = MutableStateFlow<List<EventLog>>(initialEvents)
    val recentEvents: StateFlow<List<EventLog>> = _recentEvents.asStateFlow()

    private val _mediaFeed = MutableStateFlow<List<DiscordMedia>>(initialMedia)
    val mediaFeed: StateFlow<List<DiscordMedia>> = _mediaFeed.asStateFlow()

    private val _patchNotes = MutableStateFlow<List<PatchNote>>(initialPatchNotes)
    val patchNotes: StateFlow<List<PatchNote>> = _patchNotes.asStateFlow()

    private val _votePolls = MutableStateFlow<List<VotePoll>>(initialVotes)
    val votePolls: StateFlow<List<VotePoll>> = _votePolls.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(initialChats)
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // -------------------------------------------------------------
    // Authentication Operations
    // -------------------------------------------------------------

    fun loginWithEmail(email: String, pass: String): Result<User> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || pass.length < 6) {
            return Result.failure(IllegalArgumentException("INVALID CREDENTIALS: Password must be at least 6 chars."))
        }
        val existing = _allUsers.value.find { it.email.equals(trimmedEmail, ignoreCase = true) }
        val user = if (existing != null) {
            existing
        } else {
            val name = trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
            val newUser = User(
                uid = "user_" + UUID.randomUUID().toString().take(8),
                email = trimmedEmail,
                displayName = name,
                admin = trimmedEmail.contains("admin", ignoreCase = true),
                playerData = PlayerData(
                    avatar = "💀",
                    color = "#FF5252",
                    coins = 100,
                    shield = false,
                    title = "Survivor",
                    ownedThemes = listOf("default"),
                    selectedTheme = "default",
                    games = emptyMap(),
                    lastLoginDate = null
                )
            )
            _allUsers.value = _allUsers.value + newUser
            newUser
        }
        _currentUser.value = user
        checkAndApplyDailyBonus(user)
        return Result.success(user)
    }

    fun registerWithEmail(email: String, pass: String): Result<User> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@") || pass.length < 6) {
            return Result.failure(IllegalArgumentException("INVALID PARAMS: Valid email and min 6-char passphrase required."))
        }
        val existing = _allUsers.value.find { it.email.equals(trimmedEmail, ignoreCase = true) }
        if (existing != null) {
            _currentUser.value = existing
            checkAndApplyDailyBonus(existing)
            return Result.success(existing)
        }
        val name = trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        val newUser = User(
            uid = "user_" + UUID.randomUUID().toString().take(8),
            email = trimmedEmail,
            displayName = name,
            admin = trimmedEmail.contains("admin", ignoreCase = true),
            playerData = PlayerData(
                avatar = "💀",
                color = "#FF5252",
                coins = 100,
                shield = false,
                title = "Survivor",
                ownedThemes = listOf("default"),
                selectedTheme = "default",
                games = emptyMap(),
                lastLoginDate = null
            )
        )
        _allUsers.value = _allUsers.value + newUser
        _currentUser.value = newUser
        checkAndApplyDailyBonus(newUser)
        return Result.success(newUser)
    }

    fun googleSignIn(accountName: String = "Survivor Agent"): Result<User> {
        val email = "${accountName.lowercase().replace(" ", "_")}@google.uplink"
        val existing = _allUsers.value.find { it.email.equals(email, ignoreCase = true) }
        val user = if (existing != null) {
            existing
        } else {
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
                    selectedTheme = "default",
                    games = emptyMap(),
                    lastLoginDate = null
                )
            )
            _allUsers.value = _allUsers.value + newUser
            newUser
        }
        _currentUser.value = user
        checkAndApplyDailyBonus(user)
        return Result.success(user)
    }

    fun logout() {
        _currentUser.value = null
    }

    fun switchUser(user: User) {
        _currentUser.value = user
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
            logEvent(
                EventLog(
                    id = "bonus_" + UUID.randomUUID().toString().take(6),
                    category = "wheel",
                    message = "${user.displayName} established daily satellite uplink (+50 Daily Bonus Coins)",
                    userUid = user.uid,
                    amount = bonusCoins
                )
            )
            return true
        }
        return false
    }

    // -------------------------------------------------------------
    // Casualty Reporting
    // -------------------------------------------------------------

    fun reportCasualty(sector: String): Result<Unit> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("No active survivor authenticated."))
        if (_gameState.value.lockedGames.contains(sector)) {
            return Result.failure(IllegalStateException("SECTOR LOCKED: Casualties cannot be reported in quarantined zones."))
        }

        // Increment user sector deaths
        val currentSectorDeaths = user.playerData.games[sector]?.deaths ?: 0
        val updatedGames = user.playerData.games.toMutableMap()
        updatedGames[sector] = SectorDeaths(currentSectorDeaths + 1)

        // Award small survival bounty (+5 coins for reporting)
        val updatedUser = user.copy(
            playerData = user.playerData.copy(
                games = updatedGames,
                coins = user.playerData.coins + 5
            )
        )
        updateUserInternal(updatedUser)

        // Increment global grand total
        _gameState.value = _gameState.value.copy(
            grandTotal = _gameState.value.grandTotal + 1
        )

        // Log to eventLog
        logEvent(
            EventLog(
                id = "death_" + UUID.randomUUID().toString().take(6),
                category = "death",
                message = "${user.displayName} logged casualty in $sector (+1 death, +5 coins bounty)",
                userUid = user.uid,
                sector = sector
            )
        )
        return Result.success(Unit)
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
            return Result.failure(IllegalStateException("BLACK MARKET OFFLINE: Terminal trading suspended by Overseer."))
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
                logEvent(
                    EventLog(
                        id = "shd_" + UUID.randomUUID().toString().take(6),
                        category = "wheel",
                        message = "${user.displayName} acquired Tactical Kinetic Shield.",
                        userUid = user.uid
                    )
                )
                return Result.success("Tactical Kinetic Shield deployed. Next lethal curse deflected.")
            }

            "curse" -> {
                if (targetUserUid == null) {
                    return Result.failure(IllegalArgumentException("TARGET REQUIRED: Select survivor terminal to target."))
                }
                val target = _allUsers.value.find { it.uid == targetUserUid }
                    ?: return Result.failure(IllegalArgumentException("Target survivor not found."))

                // Deduct coins from attacker
                val updatedUser = user.copy(
                    playerData = user.playerData.copy(
                        coins = user.playerData.coins - price
                    )
                )
                updateUserInternal(updatedUser)

                if (target.playerData.shield) {
                    // Shield deflected!
                    val updatedTarget = target.copy(
                        playerData = target.playerData.copy(shield = false)
                    )
                    updateUserInternal(updatedTarget)
                    logEvent(
                        EventLog(
                            id = "crs_blk_" + UUID.randomUUID().toString().take(6),
                            category = "curse_blocked",
                            message = "${user.displayName}'s Bio-Curse was DEFLECTED by ${target.displayName}'s Tactical Shield!",
                            attackerUid = user.uid,
                            targetUid = target.uid
                        )
                    )
                    return Result.success("Bio-Curse deflected! ${target.displayName}'s Tactical Shield was shattered.")
                } else {
                    // Add 1 death to target's first sector or default sector
                    val primarySector = target.playerData.games.keys.firstOrNull() ?: _gameState.value.games.first()
                    val currentDeaths = target.playerData.games[primarySector]?.deaths ?: 0
                    val targetGames = target.playerData.games.toMutableMap()
                    targetGames[primarySector] = SectorDeaths(currentDeaths + 1)
                    val updatedTarget = target.copy(
                        playerData = target.playerData.copy(games = targetGames)
                    )
                    updateUserInternal(updatedTarget)

                    _gameState.value = _gameState.value.copy(
                        grandTotal = _gameState.value.grandTotal + 1
                    )

                    logEvent(
                        EventLog(
                            id = "crs_suc_" + UUID.randomUUID().toString().take(6),
                            category = "curse_success",
                            message = "${user.displayName} deployed Bio-Curse onto ${target.displayName} (+1 Casualty in $primarySector)!",
                            attackerUid = user.uid,
                            targetUid = target.uid,
                            sector = primarySector
                        )
                    )
                    return Result.success("Bio-Curse executed! +1 Casualty inflicted onto ${target.displayName}.")
                }
            }

            "revive" -> {
                if (reviveSector == null) {
                    return Result.failure(IllegalArgumentException("SECTOR REQUIRED: Select sector to purge casualty record."))
                }
                val sectorDeaths = user.playerData.games[reviveSector]?.deaths ?: 0
                if (sectorDeaths <= 0) {
                    return Result.failure(IllegalArgumentException("NO CASUALTIES: $reviveSector has 0 casualties."))
                }
                val updatedGames = user.playerData.games.toMutableMap()
                updatedGames[reviveSector] = SectorDeaths(sectorDeaths - 1)
                val updatedUser = user.copy(
                    playerData = user.playerData.copy(
                        coins = user.playerData.coins - price,
                        games = updatedGames
                    )
                )
                updateUserInternal(updatedUser)
                logEvent(
                    EventLog(
                        id = "rev_" + UUID.randomUUID().toString().take(6),
                        category = "revive",
                        message = "${user.displayName} activated Emergency Defibrillator in $reviveSector (-1 death)",
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
                logEvent(
                    EventLog(
                        id = "buy_" + UUID.randomUUID().toString().take(6),
                        category = "wheel",
                        message = "${user.displayName} purchased ${item.name}.",
                        userUid = user.uid
                    )
                )
                return Result.success("${item.name} acquired and registered to terminal.")
            }
        }
    }

    fun buyShopTitle(title: ShopTitle): Result<String> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("SURVIVOR UNAUTHORIZED"))
        if (user.playerData.ownedTitles.contains(title.name)) {
            // Already owned -> simply equip it
            val updatedUser = user.copy(
                playerData = user.playerData.copy(title = title.name)
            )
            updateUserInternal(updatedUser)
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
        logEvent(
            EventLog(
                id = "ttl_" + UUID.randomUUID().toString().take(6),
                category = "wheel",
                message = "${user.displayName} acquired and equipped Title '${title.name}'.",
                userUid = user.uid
            )
        )
        return Result.success("Purchased & Equipped Title: ${title.name}")
    }

    fun buyShopTheme(theme: ShopTheme): Result<String> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("SURVIVOR UNAUTHORIZED"))
        if (user.playerData.ownedThemes.contains(theme.id)) {
            // Already owned -> switch to it
            val updatedUser = user.copy(
                playerData = user.playerData.copy(selectedTheme = theme.id)
            )
            updateUserInternal(updatedUser)
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
        logEvent(
            EventLog(
                id = "thm_" + UUID.randomUUID().toString().take(6),
                category = "wheel",
                message = "${user.displayName} unlocked Terminal Theme '${theme.name}'.",
                userUid = user.uid
            )
        )
        return Result.success("Unlocked & Activated Theme: ${theme.name}")
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
                msg = "+50 COINS CREDITED TO SURVIVOR VAULT"
            }
            "coins_100" -> {
                updatedUser = user.copy(
                    playerData = user.playerData.copy(coins = user.playerData.coins + 100)
                )
                msg = "+100 COINS CREDITED TO SURVIVOR VAULT"
            }
            "coins_250" -> {
                updatedUser = user.copy(
                    playerData = user.playerData.copy(coins = user.playerData.coins + 250)
                )
                msg = "+250 COINS CREDITED TO SURVIVOR VAULT"
            }
            "coins_500" -> {
                updatedUser = user.copy(
                    playerData = user.playerData.copy(coins = user.playerData.coins + 500)
                )
                msg = "🔥 OVERLORD JACKPOT! +500 COINS CREDITED!"
            }
            "shield" -> {
                updatedUser = user.copy(
                    playerData = user.playerData.copy(shield = true)
                )
                msg = "🛡️ TACTICAL SHIELD ACTIVATED"
            }
            "death_plus_1" -> {
                val sec = _gameState.value.games.firstOrNull() ?: "Sector 01 - Red Wastelands"
                val curr = user.playerData.games[sec]?.deaths ?: 0
                val gm = user.playerData.games.toMutableMap()
                gm[sec] = SectorDeaths(curr + 1)
                updatedUser = user.copy(
                    playerData = user.playerData.copy(games = gm)
                )
                _gameState.value = _gameState.value.copy(
                    grandTotal = _gameState.value.grandTotal + 1
                )
                msg = "💀 FATE STRUCK: +1 CASUALTY RECORDED IN $sec"
            }
            "bankrupt" -> {
                val lostCoins = user.playerData.coins
                updatedUser = user.copy(
                    playerData = user.playerData.copy(coins = 0)
                )
                msg = "💥 CRITICAL BREACH: ALL $lostCoins COINS WIPED!"
            }
            else -> {
                updatedUser = user.copy(
                    playerData = user.playerData.copy(coins = user.playerData.coins + 75)
                )
                msg = "🎁 MYSTERY CRATE REWARD: +75 COINS AWARDED!"
            }
        }

        updateUserInternal(updatedUser)
        logEvent(
            EventLog(
                id = "whl_" + UUID.randomUUID().toString().take(6),
                category = "wheel",
                message = "${user.displayName} spun the Wheel of Fate: ${segment.label}",
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
            return Result.failure(IllegalArgumentException("Cannot transfer coins to your own terminal."))
        }
        val recipient = _allUsers.value.find { it.uid == recipientUid }
            ?: return Result.failure(IllegalArgumentException("Recipient survivor not found."))

        // Atomic update
        val updatedSender = sender.copy(
            playerData = sender.playerData.copy(coins = sender.playerData.coins - amount)
        )
        val updatedRecipient = recipient.copy(
            playerData = recipient.playerData.copy(coins = recipient.playerData.coins + amount)
        )

        _allUsers.value = _allUsers.value.map {
            when (it.uid) {
                updatedSender.uid -> updatedSender
                updatedRecipient.uid -> updatedRecipient
                else -> it
            }
        }
        _currentUser.value = updatedSender

        logEvent(
            EventLog(
                id = "xfr_" + UUID.randomUUID().toString().take(6),
                category = "transfer",
                message = "${sender.displayName} transferred $amount Coins to ${recipient.displayName}",
                attackerUid = sender.uid,
                targetUid = recipient.uid,
                amount = amount
            )
        )
        return Result.success("Successfully transferred $amount Coins to ${recipient.displayName}.")
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
        val message = ChatMessage(
            id = "msg_" + UUID.randomUUID().toString().take(8),
            chatId = chatId,
            senderId = sender.uid,
            senderName = sender.displayName,
            text = trimmed,
            timestamp = System.currentTimeMillis()
        )
        _chatMessages.value = _chatMessages.value + message
        return Result.success(message)
    }

    fun markPatchNoteRead(id: String) {
        val user = _currentUser.value ?: return
        val updated = user.copy(
            playerData = user.playerData.copy(lastReadPatchNoteId = id)
        )
        updateUserInternal(updated)
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
        return Result.success(Unit)
    }

    // -------------------------------------------------------------
    // Admin Console Actions
    // -------------------------------------------------------------

    fun toggleMarket(enabled: Boolean) {
        _gameState.value = _gameState.value.copy(marketEnabled = enabled)
    }

    fun toggleWheel(enabled: Boolean) {
        _gameState.value = _gameState.value.copy(wheelEnabled = enabled)
    }

    fun updateBroadcast(text: String, videoUrl: String, videoId: String) {
        _gameState.value = _gameState.value.copy(
            featuredVideoText = text,
            featuredVideoUrl = videoUrl,
            featuredVideoId = videoId
        )
    }

    fun triggerFlashSale(discountPercent: Int) {
        _gameState.value = _gameState.value.copy(
            flashSaleActive = true,
            flashSaleDiscount = discountPercent.coerceIn(5, 75)
        )
    }

    fun stopFlashSale() {
        _gameState.value = _gameState.value.copy(flashSaleActive = false)
    }

    fun addSector(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty() && !_gameState.value.games.contains(trimmed)) {
            _gameState.value = _gameState.value.copy(
                games = _gameState.value.games + trimmed
            )
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
    }

    fun deleteSector(name: String) {
        _gameState.value = _gameState.value.copy(
            games = _gameState.value.games - name,
            lockedGames = _gameState.value.lockedGames - name
        )
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
        val pn = PatchNote(
            id = "pn_" + UUID.randomUUID().toString().take(6),
            title = title.trim(),
            content = content.trim(),
            author = author.trim().ifEmpty { "Command HQ" },
            versionTag = version.trim().ifEmpty { "v3.9.0" },
            timestamp = System.currentTimeMillis()
        )
        _patchNotes.value = listOf(pn) + _patchNotes.value
    }

    fun deletePatchNote(id: String) {
        _patchNotes.value = _patchNotes.value.filterNot { it.id == id }
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
    }

    fun togglePlayerAdmin(uid: String) {
        val target = _allUsers.value.find { it.uid == uid } ?: return
        val updated = target.copy(admin = !target.admin)
        updateUserInternal(updated)
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
                    coins = 100,
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
        _allUsers.value = _allUsers.value.map {
            if (it.uid == updatedUser.uid) updatedUser else it
        }
        if (_currentUser.value?.uid == updatedUser.uid) {
            _currentUser.value = updatedUser
        }
    }

    private fun logEvent(event: EventLog) {
        _recentEvents.value = (listOf(event) + _recentEvents.value).take(100)
    }
}
