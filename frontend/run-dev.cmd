@echo off
REM Dev launcher for the SAMS frontend (Vite). Used by .claude/launch.json.
setlocal
cd /d "%~dp0"
call npm run dev
