package com.winthier.watchdog;

import java.io.File;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

@Plugin(name = "Watchdog", version = "0.1")
@Description("Stop the server when it hangs")
@ApiVersion(ApiVersion.Target.v1_13)
@Author("StarTux")
@Website("https://cavetale.com")
@Commands(@Command(name = "watchdog",
                   desc = "Admin interface",
                   aliases = {},
                   permission = "watchdog.watchdog",
                   usage = "/watchdog stop\n/watchdog tick\n/watchdog start"))
@Permissions(@Permission(name = "watchdog.watchdog",
                         desc = "Use /watchdog",
                         defaultValue = PermissionDefault.OP))
public final class Watchdog extends JavaPlugin implements Runnable {
    private int pid;
    private volatile int idleSeconds;
    private volatile boolean disableWatchdog;
    static final int LIMIT = 180;

    @Override
    public void onEnable() {
        getServer().getScheduler().runTaskTimer(this, () -> idleSeconds = 0, 20L, 20L);
        try {
            String[] path = new File("/proc/self").getCanonicalPath().split(File.separator);
            pid = Integer.parseInt(path[path.length - 1]);
            info("My PID is " + pid);
        } catch (Exception e) {
            warn("Cannot parse pid");
            e.printStackTrace();
        }
        new Thread(this).start();
    }

    @Override
    public void run() {
        info("Watchdog thread started");
        while (!disableWatchdog) {
            try {
                loop();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        info("Watchdog thread stopped");
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (args.length != 1) return false;
        switch (args[0]) {
        case "stop":
            disableWatchdog = true;
            sender.sendMessage("Watchdog thread stopped");
            break;
        case "tick":
            idleSeconds = 0;
            sender.sendMessage("Watchdog thread ticked");
            break;
        case "start":
            if (disableWatchdog) {
                disableWatchdog = false;
                new Thread(this).start();
                sender.sendMessage("Watchdog thread started again");
            } else {
                sender.sendMessage("Watchdog already running");
            }
            break;
        default:
            return false;
        }
        return true;
    }

    void info(String message) {
        System.out.println("[winthier.Watchdog] " + message);
    }

    void warn(String message) {
        System.err.println("[winthier.Watchdog] " + message);
    }

    void logExec(String command) throws Exception {
        info("Running process: " + command);
        Runtime.getRuntime().exec(command);
    }

    void loop() throws Exception {
        final int seconds = idleSeconds;
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        idleSeconds += 1;
        if (seconds > 0 && seconds % 60 == 0) {
            int minutes = seconds / 60;
            if (minutes > 1) {
                warn("Nothing happened for " + minutes + " minutes");
            } else if (minutes == 1) {
                warn("Nothing happened for a minute.");
            }
        }
        switch (seconds) {
        case LIMIT:
            if (pid > 0) {
                logExec("kill " + pid);
            }
            break;
        case LIMIT + 30:
            warn("Calling Runtime.halt()...");
            Runtime.getRuntime().halt(1);
            break;
        case LIMIT + 60:
            if (pid > 0) {
                logExec("kill -KILL " + pid);
            }
            break;
        case LIMIT + 120:
            warn("I give up.");
        default:
            break;
        }
    }
}
