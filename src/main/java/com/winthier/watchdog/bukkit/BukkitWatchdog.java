package com.winthier.watchdog.bukkit;

import com.winthier.watchdog.Watchdog;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class BukkitWatchdog extends Watchdog {
    private final BukkitWatchdogPlugin plugin;
    
    @Override
    protected void shutdown() {
        plugin.getServer().shutdown();
    }

    @Override
    protected void command(String command) {
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
    }
}
