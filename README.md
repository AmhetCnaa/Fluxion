<div align="center">

```
  ███████╗██╗     ██╗   ██╗██╗  ██╗██╗ ██████╗ ███╗   ██╗
  ██╔════╝██║     ██║   ██║╚██╗██╔╝██║██╔═══██╗████╗  ██║
  █████╗  ██║     ██║   ██║ ╚███╔╝ ██║██║   ██║██╔██╗ ██║
  ██╔══╝  ██║     ██║   ██║ ██╔██╗ ██║██║   ██║██║╚██╗██║
  ██║     ███████╗╚██████╔╝██╔╝ ██╗██║╚██████╔╝██║ ╚████║
  ╚═╝     ╚══════╝ ╚═════╝ ╚═╝  ╚═╝╚═╝ ╚═════╝ ╚═╝  ╚═══╝
```

### Ultra-Fast Lossless Media Stream Engine

*Engineered with a dark obsidian studio aesthetic, 16-channel accelerated stream extraction, unthrottled YouTube challenge solvers, and hardware-accelerated fluid UI physics.*

---

[![Platform](https://img.shields.io/badge/Platform-Windows%2010%20%7C%2011-09090b?style=for-the-badge&logo=windows&logoColor=white)](https://github.com/AmhetCnaa/Fluxion)
[![Engine](https://img.shields.io/badge/Downloader-16x%20Aria2%20Parallel-18181c?style=for-the-badge)](https://github.com/AmhetCnaa/Fluxion)
[![Muxer](https://img.shields.io/badge/Muxer-FFmpeg%206.1%20Lossless-09090b?style=for-the-badge&logo=ffmpeg&logoColor=white)](https://github.com/AmhetCnaa/Fluxion)
[![GUI](https://img.shields.io/badge/GUI-Edge%20WebView2%20Studio-18181c?style=for-the-badge)](https://github.com/AmhetCnaa/Fluxion)
[![License](https://img.shields.io/badge/License-MIT-09090b?style=for-the-badge)](https://github.com/AmhetCnaa/Fluxion)

</div>

---

## ⚡ Overview

**Fluxion** is a modern, standalone desktop media engine built for creators, audiophiles, and power users who need instant, unthrottled media extraction without clutter, intrusive ads, or command-line friction.

Unlike generic downloaders throttled by YouTube's cipher and limited by single-threaded TCP connections, Fluxion combines an **embedded Node.js EJS challenge solver** with an optimized **16-channel parallel aria2c pipeline**, delivering download speeds up to **30–50+ MB/s** on high-bandwidth connections.

---

## 💎 Design Philosophy & Micro-Interactions

Designed around the modern studio aesthetic popularized by **Linear**, **Raycast**, and **Apple Pro Tools**:

- **Dark Obsidian Canvas:** Deep `#09090b` architectural dot-grid backdrop with ambient top illumination.
- **Hardware-Accelerated Gliding Pill:** Ultra-smooth spring-eased transition (`cubic-bezier(0.18, 0.89, 0.32, 1.08)`) between MP4 Video and MP3 Audio modes.
- **Radial Mouse-Tracking Spotlight:** Cards dynamically calculate cursor proximity to render subtle, tactile specular highlights.
- **Fluid Accordion Geometry:** Media metadata inspection and live telemetry drawers expand smoothly using CSS Grid `0fr -> 1fr` transitions without layout shifts.
- **Bilingual Internationalization (i18n):** Native instant switching between **English (Default)** and **Turkish (TR)** with persistent local preferences.

---

## 🚀 Performance Benchmark

| Benchmark Target (130 MB 1080p60 Video) | Average Speed | Total Transfer Time |
| :--- | :---: | :---: |
| **Fluxion (16-ch Aria2 + EJS Unthrottle)** | **~32.6 MB/s** | **~4.0 seconds** ⚡ |
| Standard yt-dlp (Single Thread) | ~1.2 MB/s (Throttled) | ~108.0 seconds |
| Common Web-based Downloaders | ~600 KB/s | ~215.0 seconds |

---

## 🏗️ Architecture Pipeline

```text
[ User URL ]
     │
     ▼
[ Automatic Platform Resolver ] (YouTube, Instagram, TikTok, X, Twitch...)
     │
     ├─► [ Node.js EJS Challenge Solver ] ───► Bypass player cipher throttling
     │
     ├─► [ 16-Channel Aria2 Pipeline ]   ───► 16x concurrent TCP connections
     │
     ▼
[ Lossless Stream Assembler (FFmpeg 6.1) ]
     ├─► MP4: Highest available resolution + best audio stream muxing
     └─► MP3: Lossless audio extraction + 320 kbps high-bitrate encoding
     │
     ▼
[ Instant Explorer Reveal ] (Direct folder select in Windows Explorer)
```

---

## ⌨️ Quickstart & Hotkeys

| Action | Shortcut / Interaction |
| :--- | :--- |
| **Instant Paste & Inspect** | Click `Paste` button or press <kbd>Ctrl</kbd> + <kbd>V</kbd> |
| **Instant Download** | Press <kbd>Enter</kbd> inside the URL field |
| **Toggle Output Format** | Click `MP4 Video` or `MP3 Audio` with fluid gliding pill |
| **Language Selection** | Click `EN` or `TR` in the top right header pill |
| **Reveal Downloaded Media** | Click `Show in Folder` on the completion card |

---

## 🛠️ Installation from Source

### Prerequisites:
1. **Python 3.10+** (64-bit recommended)
2. **Node.js** (Optional, but recommended for solving YouTube n-sig challenges)
3. **FFmpeg & Aria2** (Automatically discovered from project directory or system `PATH`)

```bash
# Clone the repository
git clone https://github.com/AmhetCnaa/Fluxion.git
cd Fluxion

# Install Python dependencies
pip install -r requirements.txt

# Run Fluxion
python main.py
# or double-click Start.bat
```

---

## 📦 Compiling Standalone `.exe`

Fluxion can be bundled into a single zero-dependency Windows executable (`Fluxion.exe`) with no terminal popups:

```bash
# Run the automated build script
build.bat
```

Or manually via PyInstaller:
```bash
py -m PyInstaller --noconsole --onefile --name "Fluxion" --add-data "gui;gui" --add-binary "ffmpeg.exe;." --add-binary "aria2c.exe;." --clean -y main.py
```

The resulting binary will be generated inside the `dist/Fluxion.exe` directory.

---

## 🛡️ License

Distributed under the MIT License. Developed for educational, personal, and research workflows. Respect content creator terms and platform guidelines.
