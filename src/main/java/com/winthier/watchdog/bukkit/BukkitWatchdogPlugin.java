package com.winthier.watchdog.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitWatchdogPlugin extends JavaPlugin {
    final BukkitWatchdog watchdog = new BukkitWatchdog(this);
    final BukkitTicker ticker = new BukkitTicker(this);
    
    @Override
    public void onEnable() {
        watchdog.enable();
        ticker.runTaskTimer(this, 1L, 1L);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
        if (args.length == 1 && args[0].equalsIgnoreCase("cancel")) {
            watchdog.cancel();
            sender.sendMessage("Watchdog thread cancelled");
        } else if (args.length == 1 && args[0].equalsIgnoreCase("tick")) {
            watchdog.somethingHappened();
            sender.sendMessage("Watchdog thread ticked");
        }
        return false;
    }
}
