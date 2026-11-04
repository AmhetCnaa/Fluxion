@echo off
title Fluxion Compiler
echo Compiling Fluxion into standalone executable...
py -m PyInstaller --noconsole --onefile --name "Fluxion" --add-data "gui;gui" --add-binary "ffmpeg.exe;." --add-binary "aria2c.exe;." --clean -y main.py
echo.
echo Build complete! Executable is ready at dist/Fluxion.exe
pause
