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
     * - Direct RAM / VBA: "XXXXXXXX:YYYY" (8 hex address, colon, 4 hex value = 13 chars)
     */
    val normalizedLines: List<String>
        get() {
            return code
                .split("\n", "\r", "+", ";")
                .mapNotNull { normalizeLine(it) }
        }

    /**
     * Joined format using '+' separator, which is the canonical Libretro multiline cheat format.
     */
    val formattedCode: String
        get() = normalizedLines.joinToString("+")

    companion object {
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
                        // 8-bit RAM write (e.g. 02025ABC:01 -> CodeBreaker 32025ABC 0001)
                        if (addr.startsWith("02")) {
                            "32${addr.substring(2)} 00$value"
                        } else if (addr.startsWith("03")) {
                            "33${addr.substring(2)} 00$value"
                        } else {
                            "$addr:00$value"
                        }
                    }
                    4 -> "$addr:$value" // Standard 13-character VBA format recognized by mGBA
                    8 -> "$addr $value" // 17-char GameShark/ActionReplay
                    else -> "$addr:$value"
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
                        } else if (addr.startsWith("03")) {
                            "33${addr.substring(2)} 00$value"
                        } else {
                            "$addr:00$value"
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

