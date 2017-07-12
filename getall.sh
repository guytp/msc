#!/bin/bash
mkdir session_data 2>/dev/null
cd session_data
adb pull /storage/emulated/legacy/Documents
cd ..
