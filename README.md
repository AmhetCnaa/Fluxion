# ⚡ Fluxion — Ultra-Fast Media Stream Engine

<p align="center">
  <b>Modern, minimalist ve ultra-yüksek hızlı video & ses aktarım stüdyosu.</b><br>
  Tek tıkla YouTube, Instagram, TikTok, X, Twitch ve 1000'den fazla siteden en yüksek çözünürlükte video veya 320 kbps stüdyo sesi indirin.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Python-3.10+-3776AB?style=flat-square&logo=python&logoColor=white" />
  <img src="https://img.shields.io/badge/GUI-PyWebView%20%2F%20Edge%20WebView2-0078D7?style=flat-square" />
  <img src="https://img.shields.io/badge/Speed-16x%20Aria2%20Parallel-FF6B6B?style=flat-square" />
  <img src="https://img.shields.io/badge/Audio-FFmpeg%20320kbps-007808?style=flat-square&logo=ffmpeg&logoColor=white" />
  <img src="https://img.shields.io/badge/Platform-Windows%2010%2F11-00A4EF?style=flat-square&logo=windows&logoColor=white" />
</p>

---

## ✨ Özellikler

- **🎯 Tek Tıkla Yapıştır & İndir:** Panodaki bağlantıyı tek tıkla yapıştırın, video başlığını ve küçük resmini anında önizleyin.
- **🚀 16 Kanallı Paralel İndirme Hızı:** Standart YouTube hız sınırlamalarını aşan Node.js tabanlı EJS challenge çözücüsü ve 16 eşzamanlı TCP bağlantısı sunan `aria2c` motoru.
- **🎬 En Yüksek Kalite MP4:** 4K, 2K, 1080p 60fps video akışını en net ses akışıyla `ffmpeg` kullanarak kayıpsız birleştirir.
- **🎵 320 kbps MP3 Stüdyo Sesi:** Videolardan sesi otomatik ayıklayarak yüksek kaliteli MP3 formatına dönüştürür.
- **💎 Linear / Raycast Obsidian Tasarımı:**
  - `#09090b` mimari dot-grid arka plan ve stüdyo ambiyans ışığı.
  - Fare hareketini takip eden radyal spot ışıklı etkileşimli kartlar.
  - **Gliding Pill:** MP4 ile MP3 arasında yaylanma fiziğine (`cubic-bezier(0.18, 0.89, 0.32, 1.08)`) sahip donanım hızlandırmalı kayan kapsül animasyonu.
  - Akıcı CSS Grid `0fr -> 1fr` akordeon önizleme ve telemetri çekmecesi.
- **🖥️ Bağımsız Masaüstü Deneyimi:** Terminal veya konsol penceresi olmadan çalışan native WebView2 masaüstü uygulaması.

---

## 🛠️ Mimari & Teknoloji Yığını

- **Frontend:** HTML5, Modern CSS Variables, CSS Grid/Flexbox, Hardware Accelerated Transforms, Pure JS.
- **Desktop Shell:** `pywebview` (Microsoft Edge WebView2 motoru).
- **İndirme & Çözümleme:** `yt-dlp` + Node.js JS Runtime (EJS).
- **Hızlandırıcı:** `aria2c` (16 paralel kanal, 1MB parça boyutu).
- **Muxer / Transcoder:** `FFmpeg 6.1`.

---

## 🚀 Kurulum & Kaynaktan Çalıştırma

### Gereksinimler:
1. Python 3.10 veya üzeri
2. [Node.js](https://nodejs.org/) (YouTube n-challenge çözümü için önerilir)
3. [FFmpeg](https://ffmpeg.org/) ve [aria2](https://aria2.github.io/) (Sistem PATH'inde veya proje klasöründe)

### Adımlar:
```bash
# Depoyu klonlayın
git clone https://github.com/AmhetCnaa/Fluxion.git
cd Fluxion

# Gerekli kütüphaneleri yükleyin
pip install -r requirements.txt

# Uygulamayı başlatın
python main.py
```

---

## 📦 Tek Dosya (.exe) Haline Getirme

Uygulamayı konsol penceresi olmadan tek bir `.exe` haline derlemek için:

```bash
py -m PyInstaller --noconsole --onefile --name "Fluxion" --add-data "gui;gui" --add-binary "ffmpeg.exe;." --add-binary "aria2c.exe;." --clean -y main.py
```

Derlenen dosya `dist/Fluxion.exe` konumunda oluşturulacaktır.

---

## 📄 Lisans

Bu proje kişisel ve eğitim amaçlı kullanım için hazırlanmıştır. İlgili platformların kullanım koşullarına saygı gösteriniz.
