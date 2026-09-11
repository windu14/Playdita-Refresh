# 🎮 RetroDroid Studio — Next-Gen Android Retro Console

<p align="center">
  <img src="https://raw.githubusercontent.com/google/material-design-icons/master/png/action/sports_esports/materialicons/48dp/2x/baseline_sports_esports_black_48dp.png" width="96" height="96" alt="RetroDroid Studio Logo" />
</p>

<p align="center">
  <strong>The ultimate, modern, all-in-one retro gaming emulator for Android.</strong><br/>
  Crafted with Jetpack Compose & Material Design 3, powered by Libretro and mGBA.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Architecture-Clean%20%2F%20Coroutines-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Core-Libretro%20%2B%20mGBA-E53935?style=for-the-badge" alt="Libretro" />
  <img src="https://img.shields.io/badge/Cheat%20Engine-Experimental-FF9800?style=for-the-badge" alt="Experimental Cheats" />
</p>

---

## ✨ Overview

**RetroDroid Studio** is a reimagined, high-performance retro gaming application built from the ground up to deliver a premium, console-grade experience on Android devices. Featuring a unified library manager, butter-smooth Material 3 navigation animations, an experimental native GBA cheat engine with auto-sanitization, live GLSL retro display shaders, and deep virtual controller customization.

---

## 🚀 Key Features

### ⚡ GBA Cheat Engine *(Experimental)*
- **Direct Memory Patching**: Integrates seamlessly with mGBA's native libretro engine for real-time memory write execution every video frame.
- **Anti-Freeze Smart Filter**: Automatically identifies and safely skips legacy Master Codes (`[M]`, `0000...`, `9...`) that cause emulator crashes and breakpoint halts.
- **Multi-Format Parsing**: Full support for CodeBreaker (`XXXXXXXX YYYY`), GameShark SP, GameShark v1/v2 (`XXXXXXXX YYYYYYYY`), and direct RAM addresses (`03XXXXXX:YY`).
- **One-Tap Presets**: Instant tested cheats for classics like **Super Mario Advance 4: Super Mario Bros. 3** *(Invincibility, Super Mario Form, Infinite Lives, Tanooki Suit, Stop Timer)*, **Super Mario World**, and **Pokémon**.

### 🎨 Live Retro Shaders & Display Filters
- **Hardware-Accelerated GLSL**: Instant, live switching during active gameplay without reloading or lagging.
- **Authentic Retro Profiles**:
  - **CRT Scanlines**: Emulates classic curved cathode-ray tube phosphor lines for nostalgia.
  - **LCD Grid**: Pixel grid simulation tailored for GBA, GBC, and handheld consoles.
  - **Sharp Pixel**: Pure integer nearest-neighbor scaling for ultra-crisp sprite edges.
  - **Smooth (Bilinear)**: Softened texture filtering ideal for 3D polygon-based systems.

### 🕹️ Fully Customizable Virtual Touch Controls
- **Skin Profiles**: Modern Glass, Minimalist Matte, Retro Classic, and Cyberpunk styles.
- **RGB Color Wheel**: Choose any custom tint and opacity with real-time preview.
- **Haptic Feedback**: Responsive tactile vibration tuned for modern vibration motors.
- **Custom Button Placement**: On-the-fly interactive control positioning and scaling.

### 🖼️ Console Backgrounds & Screen Layout
- **Custom Handheld Wallpapers**: Split-screen and full-screen background artwork for an authentic console shell feel.
- **Ergonomic Positioning**: Adjust display scale (50%–120%) and shift screen alignment (Top, Center, Bottom) for comfortable one-handed or dual-thumb play.

### 🌊 Silky Smooth UI Transitions
- **Fluid Screen Choreography**: Slide-and-fade navigation across menus, modal sheets, and settings.
- **Material 3 Dynamic Theming**: Adapts to device wallpaper and system light/dark mode seamlessly.

### 💾 High-Reliability Save States & Persistence
- **Auto-Save & Auto-Resume**: Never lose your progress when switching apps or receiving calls.
- **Visual Save Slots**: Quick snapshot previews for instant state identification.
- **Local Room Database**: Fast, indexed catalog of your ROM collection with box art and metadata.

---

## 🕹️ Supported Systems & Cores

| Console | Core | Native Audio | Save States | Cheats |
| :--- | :--- | :---: | :---: | :---: |
| **Game Boy Advance (GBA)** | mGBA | ✅ Low-Latency | ✅ | ✅ *Experimental* |
| **Game Boy / Color (GB/GBC)** | Gambatte | ✅ Low-Latency | ✅ | Planned |
| **Super Nintendo (SNES)** | Snes9x | ✅ Low-Latency | ✅ | Planned |
| **Nintendo Entertainment System (NES)** | FCEUmm | ✅ Low-Latency | ✅ | Planned |
| **Nintendo DS (NDS)** | DeSmuME / MelonDS | ✅ Low-Latency | ✅ | — |
| **Nintendo 64 (N64)** | Mupen64Plus | ✅ Low-Latency | ✅ | — |
| **PlayStation (PSX)** | PCSX ReARMed | ✅ Low-Latency | ✅ | — |
| **PlayStation Portable (PSP)** | PPSSPP | ✅ Low-Latency | ✅ | — |
| **Sega Genesis / Mega Drive** | Genesis Plus GX | ✅ Low-Latency | ✅ | — |
| **Arcade** | FBNeo | ✅ Low-Latency | ✅ | — |

---

## 📖 GBA Cheat Guide & Best Practices

To ensure 100% stability and prevent game freezes when using cheats in mGBA:

1. **Do NOT use Master Codes (`Must Be On` / `[M]`)**:
   - In traditional hardware cartridges, Master Codes were required to bypass encryption chips.
   - mGBA does **not** need Master Codes; it injects cheats directly into virtual RAM.
   - Master Codes activate decryption seeds that scramble valid cheats into corrupt memory writes or trigger CPU breakpoint loops (freezing the game).
   - Our app automatically detects and skips Master Code lines for your safety.

2. **Supported Formats**:
   - **CodeBreaker**: `33003F64 0035` *(8 digits, space, 4 digits)*
   - **GameShark**: `XXXXXXXX YYYYYYYY` *(8 digits, space, 8 digits)*
   - **Direct RAM**: `03003F64:35` *(Auto-converted to CodeBreaker)*

3. **Recommended Super Mario Advance 4 Codes**:
   - **Invulnerability (Blink/Tembus Musuh)**: `33003F64 0035`
   - **Star Power Invincible**: `33002C2E 00FF`
   - **Always Super Mario (Big Mario)**: `33003F5F 0001`
   - **Fire Mario**: `33003F5F 0002`
   - **Racoon Mario (Ekor Terbang)**: `33003F5F 0003`
   - **Infinite Lives (98 Nyawa)**: `33002A6A 0062`
   - **Freeze Level Timer**: `33003D0E 0020`

---

## 🎨 Why Built-In GLSL Shaders?

Users often ask: *“Should we scan shaders from external files/folders, or use built-in shaders?”*

| Factor | Built-In GLSL Shaders (Current) | External Shader Scans (.slangp/.glslp) |
| :--- | :--- | :--- |
| **Stability** | 🟢 100% stable, zero crash risk | 🔴 Prone to GPU driver crashes & compilation errors |
| **Performance** | 🟢 Optimized for mobile OpenGL ES | 🔴 Multi-pass shaders cause severe FPS drops & heat |
| **Battery Life** | 🟢 Lightweight on mobile GPUs | 🔴 High thermal throttling |
| **Ease of Use** | 🟢 1-tap live preview during gameplay | 🔴 Complex configuration and texture file dependencies |

> **Recommendation**: Built-in optimized GLSL ES shaders provide the best balance of visual nostalgia, 60 FPS performance, and rock-solid stability.

---

## 🛠️ Architecture & Tech Stack

- **Language**: 100% Modern Kotlin
- **UI Toolkit**: Jetpack Compose with Material 3 (M3)
- **Asynchronous Architecture**: Kotlin Coroutines & `StateFlow`
- **Database**: Android Jetpack Room with SQLite
- **Emulation Engine**: LibretroDroid native JNI bridge with mGBA, Snes9x, and PCSX
- **Graphics Pipeline**: OpenGL ES 2.0 / GLSurfaceView with custom GLSL shaders

---

## 📜 License & Credits

- Built upon the open-source foundation of [Lemuroid](https://github.com/Swordfish90/Lemuroid) and [Libretro](https://www.libretro.com/).
- Licensed under the **GNU General Public License v3.0 (GPLv3)**.
- Emulation cores are copyrighted by their respective Libretro core authors.
