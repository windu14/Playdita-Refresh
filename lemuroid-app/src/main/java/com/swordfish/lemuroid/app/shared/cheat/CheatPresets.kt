package com.swordfish.lemuroid.app.shared.cheat

import com.swordfish.lemuroid.lib.library.db.entity.Game

data class CheatPreset(
    val title: String,
    val description: String,
    val code: String,
    val gameKeywords: List<String>,
)

object CheatPresets {

    private val ALL_PRESETS = listOf(
        // Super Mario Advance 4: Super Mario Bros. 3
        CheatPreset(
            title = "Invulnerability (Tembus Musuh)",
            description = "Mario berkedip tembus musuh dan rintangan tanpa mati",
            code = "33003F64 0035",
            gameKeywords = listOf("mario 4", "mario advance 4", "super mario bros. 3", "smb3", "sma4"),
        ),
        CheatPreset(
            title = "Infinite Star Power (Kekuatan Bintang)",
            description = "Bintang invicible aktif terus-menerus",
            code = "33002C2E 00FF",
            gameKeywords = listOf("mario 4", "mario advance 4", "super mario bros. 3", "smb3", "sma4"),
        ),
        CheatPreset(
            title = "Super Mario Form (Mario Besar)",
            description = "Mario selalu berukuran besar tanpa membuat game freeze",
            code = "33003F5F 0001",
            gameKeywords = listOf("mario 4", "mario advance 4", "super mario bros. 3", "smb3", "sma4"),
        ),
        CheatPreset(
            title = "Fire Mario Form (Mario Api)",
            description = "Mario berubah menjadi Mario Api yang bisa menembak api",
            code = "33003F5F 0002",
            gameKeywords = listOf("mario 4", "mario advance 4", "super mario bros. 3", "smb3", "sma4"),
        ),
        CheatPreset(
            title = "Racoon Mario Form (Mario Daun/Ekor)",
            description = "Mario berubah menjadi Tanooki/Racoon dengan ekor terbang",
            code = "33003F5F 0003",
            gameKeywords = listOf("mario 4", "mario advance 4", "super mario bros. 3", "smb3", "sma4"),
        ),
        CheatPreset(
            title = "Frog Mario Form (Mario Katak)",
            description = "Mario berubah bentuk katak untuk berenang cepat",
            code = "33003F5F 0004",
            gameKeywords = listOf("mario 4", "mario advance 4", "super mario bros. 3", "smb3", "sma4"),
        ),
        CheatPreset(
            title = "Tanooki Mario Form (Mario Patung)",
            description = "Mario kostum beruang Tanooki",
            code = "33003F5F 0005",
            gameKeywords = listOf("mario 4", "mario advance 4", "super mario bros. 3", "smb3", "sma4"),
        ),
        CheatPreset(
            title = "Hammer Mario Form (Mario Palu)",
            description = "Mario melempar palu menghancurkan semua musuh",
            code = "33003F5F 0006",
            gameKeywords = listOf("mario 4", "mario advance 4", "super mario bros. 3", "smb3", "sma4"),
        ),
        CheatPreset(
            title = "Infinite Lives (98 Nyawa)",
            description = "Jumlah nyawa Mario terkunci di 98",
            code = "33002A6A 0062",
            gameKeywords = listOf("mario 4", "mario advance 4", "super mario bros. 3", "smb3", "sma4"),
        ),
        CheatPreset(
            title = "Stop Level Timer (Waktu Berhenti)",
            description = "Waktu hitung mundur panggung tidak berkurang",
            code = "33003D0E 0020",
            gameKeywords = listOf("mario 4", "mario advance 4", "super mario bros. 3", "smb3", "sma4"),
        ),
        CheatPreset(
            title = "Always 98 Coins (98 Koin)",
            description = "Koin Mario selalu 98",
            code = "33002C58 0062",
            gameKeywords = listOf("mario 4", "mario advance 4", "super mario bros. 3", "smb3", "sma4"),
        ),
        CheatPreset(
            title = "P-Meter Always Full (Lari/Terbang Instan)",
            description = "Meteran lari P selalu maksimal sehingga bisa langsung terbang",
            code = "33003C9F 007F",
            gameKeywords = listOf("mario 4", "mario advance 4", "super mario bros. 3", "smb3", "sma4"),
        ),

        // Super Mario Advance 2: Super Mario World
        CheatPreset(
            title = "Invincible Star Power (SMW)",
            description = "Mario memiliki kekuatan bintang tanpa batas",
            code = "32000038 00FF",
            gameKeywords = listOf("mario 2", "mario advance 2", "super mario world", "smw"),
        ),
        CheatPreset(
            title = "Infinite Lives (SMW 99 Nyawa)",
            description = "Jumlah nyawa Mario terkunci di 99",
            code = "320000DB 0063",
            gameKeywords = listOf("mario 2", "mario advance 2", "super mario world", "smw"),
        ),
        CheatPreset(
            title = "Super Mario Form (SMW)",
            description = "Mario selalu berukuran besar",
            code = "32000019 0001",
            gameKeywords = listOf("mario 2", "mario advance 2", "super mario world", "smw"),
        ),
        CheatPreset(
            title = "Cape Mario Form (SMW Jubah)",
            description = "Mario selalu memakai jubah terbang",
            code = "32000019 0002",
            gameKeywords = listOf("mario 2", "mario advance 2", "super mario world", "smw"),
        ),

        // Pokemon Emerald / FireRed
        CheatPreset(
            title = "Infinite Money (Uang Maksimal)",
            description = "Uang tidak habis-habis di Pokemon FireRed/LeafGreen",
            code = "82003884 423F\n82003886 000F",
            gameKeywords = listOf("pokemon", "firered", "leafgreen", "emerald"),
        ),
        CheatPreset(
            title = "99 Master Balls in PC",
            description = "Ambil 99 Master Ball dari Item PC",
            code = "820257BC 0001\n820257BE 0063",
            gameKeywords = listOf("pokemon", "firered", "leafgreen"),
        ),
        CheatPreset(
            title = "Infinite Rare Candies in PC",
            description = "Ambil 99 Rare Candy untuk leveling instan",
            code = "820257BC 0044\n820257BE 0063",
            gameKeywords = listOf("pokemon", "firered", "leafgreen"),
        ),
    )

    fun getPresetsForGame(game: Game): List<CheatPreset> {
        val titleLower = game.title.lowercase()
        val fileLower = game.fileName.lowercase()
        val matched = ALL_PRESETS.filter { preset ->
            preset.gameKeywords.any { kw -> titleLower.contains(kw) || fileLower.contains(kw) }
        }
        return if (matched.isNotEmpty()) matched else ALL_PRESETS
    }
}
