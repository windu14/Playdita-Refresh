package com.swordfish.lemuroid.app.shared.cheat

import java.io.Serializable
import java.util.UUID

data class GbaCheat(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val code: String,
    val enabled: Boolean = true,
) : Serializable {

    /**
     * Normalizes a raw cheat code into individual lines formatted
     * exactly according to what mGBA's libretro parser expects:
     * - CodeBreaker / GameShark SP: "XXXXXXXX YYYY" (8 hex digits, 1 space, 4 hex digits = 13 chars)
     * - GameShark / Action Replay v3: "XXXXXXXX YYYYYYYY" (8 hex digits, 1 space, 8 hex digits = 17 chars)
     */
    val normalizedLines: List<String>
        get() {
            return code
                .split("\n", "\r", "+", ";")
                .mapNotNull { normalizeLine(it) }
        }

    /**
     * Executable lines passed to LibretroDroid.
     * Master codes (Game ID 0000... or Hook 9... / 1000... 0007) are intentionally
     * filtered out because mGBA directly writes memory every frame without needing Master Codes.
     * Passing Master Codes to mGBA triggers ROM breakpoints or decryption tables that cause
     * the emulator to freeze or corrupt memory writes!
     */
    val executableLines: List<String>
        get() {
            return normalizedLines.filterNot { isMasterCode(it) }
        }

    /**
     * Whether this cheat code includes Master Code lines that are safely skipped.
     */
    val hasMasterCodes: Boolean
        get() = normalizedLines.any { isMasterCode(it) }

    /**
     * Joined format using '+' separator, which is the canonical Libretro multiline cheat format.
     */
    val formattedCode: String
        get() = executableLines.joinToString("+")

    companion object {
        /**
         * Detects Master Codes (Enable Code / [M]) which cause freezes in mGBA.
         * - CodeBreaker Game ID / Master: starts with "0000" (e.g. 000021FA 000A)
         * - CodeBreaker Master Line 2: starts with "1000" and ends with "0007"
         * - CodeBreaker / GameShark Hook: starts with "9" (e.g. 9XXXXXXX YYYY)
         */
        fun isMasterCode(line: String): Boolean {
            val trimmed = line.trim().uppercase()
            if (trimmed.startsWith("0000") && (trimmed.length == 13 || trimmed.length == 14 || trimmed.length == 17)) return true
            if (trimmed.startsWith("1000") && trimmed.endsWith("0007")) return true
            if (trimmed.startsWith("9") && (trimmed.length == 13 || trimmed.length == 17)) return true
            return false
        }

        fun normalizeLine(rawLine: String): String? {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                return null
            }

            // Remove any internal extra whitespace
            val compact = line.replace("""\s+""".toRegex(), " ")

            // Check for VBA format: XXXXXXXX:YYYY or XXXXXXXX:YY
            val vbaMatch = Regex("""^([0-9a-fA-F]{8})\s*:\s*([0-9a-fA-F]{2,8})$""").matchEntire(compact)
            if (vbaMatch != null) {
                val addr = vbaMatch.groupValues[1].uppercase()
                val value = vbaMatch.groupValues[2].uppercase()
                return when (value.length) {
                    2 -> {
                        // 8-bit RAM write: convert to CodeBreaker 32... or 33...
                        if (addr.startsWith("02")) {
                            "32${addr.substring(2)} 00$value"
                        } else {
                            "33${addr.substring(2)} 00$value"
                        }
                    }
                    4 -> {
                        // 16-bit RAM write: convert to CodeBreaker 82... or 83...
                        if (addr.startsWith("02")) {
                            "82${addr.substring(2)} $value"
                        } else {
                            "83${addr.substring(2)} $value"
                        }
                    }
                    8 -> "$addr $value" // 17-char GameShark/ActionReplay
                    else -> {
                        val padded = value.padStart(4, '0').takeLast(4)
                        "33${addr.substring(2)} $padded"
                    }
                }
            }

            // Strip spaces, colons, dashes to test pure hex length
            val cleanHex = compact.replace("""[\s:\-]+""".toRegex(), "")
            if (cleanHex.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) {
                when (cleanHex.length) {
                    // 12 hex chars: CodeBreaker / GameShark SP -> "XXXXXXXX YYYY"
                    12 -> {
                        val part1 = cleanHex.substring(0, 8).uppercase()
                        val part2 = cleanHex.substring(8).uppercase()
                        return "$part1 $part2"
                    }
                    // 16 hex chars: GameShark / Action Replay -> "XXXXXXXX YYYYYYYY"
                    16 -> {
                        val part1 = cleanHex.substring(0, 8).uppercase()
                        val part2 = cleanHex.substring(8).uppercase()
                        return "$part1 $part2"
                    }
                    // 10 hex chars: 8-bit RAM write (e.g. 02025ABC01) -> CodeBreaker 32...
                    10 -> {
                        val addr = cleanHex.substring(0, 8).uppercase()
                        val value = cleanHex.substring(8).uppercase()
                        return if (addr.startsWith("02")) {
                            "32${addr.substring(2)} 00$value"
                        } else {
                            "33${addr.substring(2)} 00$value"
                        }
                    }
                }
            }

            // Space-separated fallback
            val parts = compact.split(" ")
            if (parts.size == 2) {
                val p1 = parts[0].uppercase()
                val p2 = parts[1].uppercase()
                if (p1.length == 8 && (p2.length == 4 || p2.length == 8)) {
                    return "$p1 $p2"
                }
            }

            return compact.uppercase()
        }
    }
}
