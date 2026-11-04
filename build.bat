@echo off
title Fluxion Derleyici
echo Fluxion .exe derleniyor...
py -m PyInstaller --noconsole --onefile --name "Fluxion" --add-data "gui;gui" --add-binary "ffmpeg.exe;." --add-binary "aria2c.exe;." --clean -y main.py
echo.
echo Derleme tamamlandi! Dosya dist/Fluxion.exe konumunda hazir.
pause
