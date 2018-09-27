#!/bin/bash

nexus_5=0665c9430060f535


adb -s $nexus_5 shell su -c "stop mpdecision"
adb -s $nexus_5 shell su -c "mount -o rw,remount /system"
adb -s $nexus_5 shell su -c "mv /system/bin/mpdecision /system/bin/mpdecision-bak"
adb -s $nexus_5 shell su -c "chmod 777 /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq"
adb -s $nexus_5 shell su -c "chmod 777 /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq"

adb -s $nexus_5 shell su -c "/data/local/tmp/nexus-5-ondevice.sh"

adb -s $nexus_5 shell su -c "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq"
adb -s $nexus_5 shell su -c "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq"

echo "\n--- CPU0 Stats Before EXP \n"
adb -s $nexus_5 shell su -c "cat /sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state"

adb -s $nexus_5 shell su -c "time /data/local/tmp/ci/ci-nexus-5.exe" > output-nexus-5.raw 

echo "\n--- CPU0 Stats AFTER EXP \n"
adb -s $nexus_5 shell su -c "cat /sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state"

