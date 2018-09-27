#!/bin/bash

nexus_s=3434BF8B3CE900EC


adb -s $nexus_s root
adb -s $nexus_s shell "echo 800000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq"
adb -s $nexus_s shell "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq"
adb -s $nexus_s shell "echo 800000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq"
adb -s $nexus_s shell "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq"

echo "\n--- CPU0 Stats Before EXP \n"
adb -s $nexus_s shell "cat /sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state"

adb -s $nexus_s shell "time /data/local/tmp/papa/jpapabench.exe" > output-nexus-s.raw 

echo "\n--- CPU0 Stats AFTER EXP \n"
adb -s $nexus_s shell "cat /sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state"

