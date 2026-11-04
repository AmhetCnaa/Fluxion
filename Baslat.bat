@echo off
title Fluxion
cd /d "%~dp0"
if exist "Fluxion.exe" (
    start "" "Fluxion.exe"
) else (
    py main.py
)
