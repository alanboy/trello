@echo off
setlocal
set path=%path%;"%ProgramFiles(x86)%\WiX Toolset v3.10\bin"
candle trello-installer.wxs
light trello-installer.wixobj

