package com.example.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "Survivor",
    val admin: Boolean = false,
    val playerData: PlayerData = PlayerData()
)

data class PlayerData(
    val avatar: String = "💀",
    val color: String = "#FF5252",
    val coins: Int = 150,
    val shield: Boolean = false,
    val title: String = "Survivor",
    val ownedThemes: List<String> = listOf("default"),
    val ownedTitles: List<String> = listOf("Survivor"),
    val selectedTheme: String = "default",
    val games: Map<String, SectorDeaths> = emptyMap(),
    val lastLoginDate: String? = null,
    val lastReadPatchNoteId: String? = null
) {
    val totalDeaths: Int
        get() = games.values.sumOf { it.deaths }
}

data class SectorDeaths(
    val deaths: Int = 0
)

data class GameState(
    val grandTotal: Int = 14382,
    val games: List<String> = listOf(
        "Sector 01 - Red Wastelands",
        "Sector 04 - Core Reactor",
        "Sector 07 - Neo-Haven",
        "Sector 09 - Sub-Levels",
        "Sector X - Bio-Lab Vault"
    ),
    val lockedGames: List<String> = listOf("Sector X - Bio-Lab Vault"),
    val marketEnabled: Boolean = true,
    val wheelEnabled: Boolean = true,
    val shopItems: List<ShopItem> = listOf(
        ShopItem(
            id = "shield",
            icon = "🛡️",
            name = "Tactical Shield",
            desc = "Deploy kinetic field. Absorbs 1 lethal curse attack from any hostile survivor.",
            basePrice = 150
        ),
        ShopItem(
            id = "curse",
            icon = "☣️",
            name = "Bio-Curse Beacon",
            desc = "Target any survivor terminal to shatter their shield or inflict +1 fatal casualty.",
            basePrice = 250
        ),
        ShopItem(
            id = "revive",
            icon = "💉",
            name = "Emergency Defibrillator",
            desc = "De-escalate casualty records. Purges 1 death count from a designated sector.",
            basePrice = 350
        ),
        ShopItem(
            id = "stimpack",
            icon = "⚡",
            name = "Neural Stimpack",
            desc = "High-frequency cyber combat booster with encrypted survivor telemetry.",
            basePrice = 100
        ),
        ShopItem(
            id = "decrypter",
            icon = "📟",
            name = "Sub-Net Decrypter",
            desc = "Classified tactical mainframe decoder used by high-tier operatives.",
            basePrice = 200
        )
    ),
    val shopTitles: List<ShopTitle> = listOf(
        ShopTitle("Ghost of Sector 7", 120),
        ShopTitle("Void Walker", 180),
        ShopTitle("Biohazard Operative", 240),
        ShopTitle("Reaper Elite", 320),
        ShopTitle("Cyber Overlord", 500),
        ShopTitle("Terminal Architect", 750)
    ),
    val shopThemes: List<ShopTheme> = listOf(
        ShopTheme("retro_green", "Matrix Phosphor", 200),
        ShopTheme("amber_alert", "Amber Alert", 250),
        ShopTheme("ghost_white", "Ghost Monolith", 300),
        ShopTheme("cyber_blue", "Cyber Cyan", 350),
        ShopTheme("blood_moon", "Blood Moon", 400),
        ShopTheme("toxic_hazard", "Toxic Hazard", 450),
        ShopTheme("sunset_strip", "Sunset Wasteland", 500)
    ),
    val wheelItems: List<WheelSegment> = listOf(
        WheelSegment(label = "+50 COINS", action = "coins_50", weight = 30, colorHex = "#FF3B30"),
        WheelSegment(label = "TACTICAL SHIELD", action = "shield", weight = 15, colorHex = "#5AC8FA"),
        WheelSegment(label = "+100 COINS", action = "coins_100", weight = 20, colorHex = "#FF9500"),
        WheelSegment(label = "+1 CASUALTY 💀", action = "death_plus_1", weight = 15, colorHex = "#AF52DE"),
        WheelSegment(label = "+250 COINS", action = "coins_250", weight = 10, colorHex = "#FFCC00"),
        WheelSegment(label = "BANKRUPT 💥", action = "bankrupt", weight = 5, colorHex = "#FF453A"),
        WheelSegment(label = "JACKPOT +500", action = "coins_500", weight = 5, colorHex = "#34C759")
    ),
    val flashSaleActive: Boolean = true,
    val flashSaleDiscount: Int = 30,
    val flashSaleEnd: String = "2026-08-16T23:59:59Z",
    val featuredVideoUrl: String = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    val featuredVideoId: String = "transmission_v09",
    val featuredVideoText: String = "EMERGENCY BROADCAST: Sector 04 core containment containment anomaly detected. All survivor units report casualties immediately."
)

data class ShopItem(
    val id: String = "",
    val icon: String = "📦",
    val name: String = "",
    val desc: String = "",
    val basePrice: Int = 100
)

data class ShopTitle(
    val name: String = "",
    val price: Int = 100
)

data class ShopTheme(
    val id: String = "",
    val name: String = "",
    val price: Int = 100
)

data class WheelSegment(
    val label: String = "",
    val action: String = "",
    val weight: Int = 10,
    val colorHex: String = "#FF3B30"
)

data class EventLog(
    val id: String = "",
    val message: String = "",
    val category: String = "death", // "death" | "curse_success" | "curse_blocked" | "revive" | "transfer" | "wheel"
    val userUid: String? = null,
    val attackerUid: String? = null,
    val targetUid: String? = null,
    val sector: String? = null,
    val amount: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class DiscordMedia(
    val id: String = "",
    val type: String = "Image", // "Image" | "Video"
    val url: String = "",
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val authorName: String = "Command HQ",
    val likes: Int = 12
)

data class PatchNote(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val author: String = "Overlord Terminal",
    val timestamp: Long = System.currentTimeMillis(),
    val versionTag: String = "v3.8.4"
)

data class VotePoll(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val options: List<String> = emptyList(),
    val votes: Map<String, String> = emptyMap(), // userId -> option
    val timestamp: Long = System.currentTimeMillis(),
    val closed: Boolean = false
)

data class ChatMessage(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
