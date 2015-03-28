package com.winthier.watchdog.bukkit;

import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
class BukkitTicker extends BukkitRunnable {
    private final BukkitWatchdogPlugin plugin;
    
    @Override
    public void run() {
        plugin.watchdog.somethingHappened();
    }
}
