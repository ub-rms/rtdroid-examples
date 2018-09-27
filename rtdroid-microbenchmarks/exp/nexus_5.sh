#!/bin/bash

nexus_5=0665c9430060f535

execution_array=(25 30 35 40 45) 
#execution_array=(5 10 15 20 25 30 35 40 45 50) 
#execution_array=(50) 

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


#adb -s $nexus_5 shell su -c "/data/local/tmp/microbenchmark/microbenchmark.exe RT_MSG COMP 1" &> msg_base_line.raw

for num in "${execution_array[@]}"
do
    adb -s $nexus_5 shell su -c "/data/local/tmp/microbenchmark/microbenchmark.exe RT_MSG MULTI_MSG ${num}" &> data_msg/msg_multi-sender_${num}.raw 
done

#for num in "${execution_array[@]}"
#do
#    adb -s $nexus_5 shell su -c "/data/local/tmp/microbenchmark/microbenchmark.exe RT_MSG COMP ${num}" &> data_msg/msg_comp_${num}.raw 
#done
#
#for num in "${execution_array[@]}"
#do
#    adb -s $nexus_5 shell su -c "/data/local/tmp/microbenchmark/microbenchmark.exe RT_MSG GC ${num}" &> data_msg/msg_gc_${num}.raw 
#done
#
######################################################### Intent tests ########################################################################
#for num in "${execution_array[@]}"
#do
#    adb -s $nexus_5 shell su -c "/data/local/tmp/microbenchmark/microbenchmark.exe RT_INTENT MULTI_INTENT ${num}" &> data_intent/msg_multi-sender_${num}.raw 
#done
#
#for num in "${execution_array[@]}"
#do
#    adb -s $nexus_5 shell su -c "/data/local/tmp/microbenchmark/microbenchmark.exe RT_INTENT COMP ${num}" &> data_intent/msg_comp_${num}.raw 
#done
#
#for num in "${execution_array[@]}"
#do
#    adb -s $nexus_5 shell su -c "/data/local/tmp/microbenchmark/microbenchmark.exe RT_INTENT GC ${num}" &> data_intent/msg_gc_${num}.raw 
#done
#




echo "\n--- CPU0 Stats AFTER EXP \n"
adb -s $nexus_5 shell su -c "cat /sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state"

