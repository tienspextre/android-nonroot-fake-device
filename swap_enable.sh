#!/system/bin/sh
# swap_enable.sh - zRAM & CPU Turbo Optimization for Realme C2 (MT6765 Helio P22)
# Executed by init service swap_enable_init as root (u:r:rutilsdaemon:s0)

log -p i -t swap_enable "Initializing zRAM & CPU Turbo Booster..."

apply_perf() {
    # 1. zRAM Activation (1024 MB compressed RAM)
    if [ -f "/sys/block/zram0/disksize" ]; then
        # Only configure if not already active
        SWAP_ACTIVE=$(cat /proc/swaps 2>/dev/null | grep -c "zram0")
        if [ "$SWAP_ACTIVE" -eq 0 ]; then
            echo lz4 > /sys/block/zram0/comp_algorithm 2>/dev/null
            echo 1073741824 > /sys/block/zram0/disksize 2>/dev/null
            /system/bin/mkswap /dev/block/zram0 2>/dev/null
            /system/bin/swapon /dev/block/zram0 2>/dev/null
            log -p i -t swap_enable "zRAM 1GB enabled."
        fi
    fi

    # Virtual Memory tuning
    echo 100 > /proc/sys/vm/swappiness 2>/dev/null
    echo 0 > /proc/sys/vm/page-cluster 2>/dev/null
    echo 100 > /proc/sys/vm/vfs_cache_pressure 2>/dev/null
    if [ -d "/dev/memcg" ]; then
        echo 100 > /dev/memcg/memory.swappiness 2>/dev/null
        echo 100 > /dev/memcg/apps/memory.swappiness 2>/dev/null
        echo 100 > /dev/memcg/system/memory.swappiness 2>/dev/null
    fi

    # 2. Force all 8 CPU cores online
    for i in 0 1 2 3 4 5 6 7; do
        if [ -f "/sys/devices/system/cpu/cpu$i/online" ]; then
            echo 1 > "/sys/devices/system/cpu/cpu$i/online" 2>/dev/null
        fi
    done

    # 3. CPU Frequencies: MediaTek Helio P22 (policy0 Big Cluster, policy4 Little Cluster)
    # Big Cluster (Cortex-A53 2.0 GHz): floor at 1.62 GHz
    if [ -d "/sys/devices/system/cpu/cpufreq/policy0" ]; then
        echo performance > /sys/devices/system/cpu/cpufreq/policy0/scaling_governor 2>/dev/null
        echo 1617000 > /sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq 2>/dev/null
        echo 2001000 > /sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq 2>/dev/null
    fi

    # Little Cluster (Cortex-A53 1.5 GHz): floor at 1.10 GHz
    if [ -d "/sys/devices/system/cpu/cpufreq/policy4" ]; then
        echo performance > /sys/devices/system/cpu/cpufreq/policy4/scaling_governor 2>/dev/null
        echo 1102000 > /sys/devices/system/cpu/cpufreq/policy4/scaling_min_freq 2>/dev/null
        echo 1500000 > /sys/devices/system/cpu/cpufreq/policy4/scaling_max_freq 2>/dev/null
    fi

    # 4. Oppo Touchscreen Gaming Mode & I/O
    if [ -f "/proc/touchpanel/game_switch_enable" ]; then
        echo 1 > /proc/touchpanel/game_switch_enable 2>/dev/null
    fi
    for queue in /sys/block/mmcblk*/queue; do
        echo 512 > "$queue/read_ahead_kb" 2>/dev/null
    done

    log -p i -t swap_enable "Realme C2 Performance mode applied."
}

# Apply immediately
apply_perf

# Background watchdog to re-apply after full Android boot
(
    sleep 15
    apply_perf
    sleep 30
    apply_perf
) &
