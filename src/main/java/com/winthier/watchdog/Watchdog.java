package com.winthier.watchdog;

import java.io.File;
import java.io.IOException;

public abstract class Watchdog {
    int pid = 0;
    volatile int idleSeconds;
    volatile boolean cancelled = false;
    static final int LIMIT = 120;

    protected abstract void shutdown();
    protected abstract void command(String command);

    public void somethingHappened() {
        idleSeconds = 0;
    }

    public void cancel() {
        cancelled = true;
    }

    void info(String message) {
        System.out.println("[Watchdog] " + message);
    }
    
    void warn(String message) {
        System.err.println("[Watchdog] " + message);
    }

    void logCommand(String command) {
        info("Running command: " + command);
        command(command);
    }
    
    void logExec(String command) throws Exception {
        info("Running process: " + command);
        Runtime.getRuntime().exec(command);
    }

    public void enable() {
        try {
            String[] path = new File("/proc/self").getCanonicalPath().split(File.separator);
            pid = Integer.parseInt(path[path.length - 1]);
            info("My PID is " + pid);
        } catch (Exception e) {
            warn("Cannot parse pid");
            e.printStackTrace();
        }
        new Thread() {
            @Override public void run() {
                info("Watchdog thread started");
                while (true) {
                    try {
                        if (!loop()) break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                info("Watchdog thread stopped");
            }
        }.start();
    }

    boolean loop() throws Exception {
        Thread.sleep(1000L);
        switch (idleSeconds++) {
        case 60:
            warn("Nothing happened for a minute. Starting to worry.");
            break;
        case LIMIT:
            warn("Trying to shutdown soon");
            logCommand("shutdown now");
            logCommand("as set now");
            break;
        case LIMIT + 60:
            logCommand("stop");
            break;
        case LIMIT + 120:
            warn("System.exit(1)");
            System.exit(1);
            break;
        case LIMIT + 125:
            logExec("kill " + pid);
            break;
        case LIMIT + 130:
            logExec("kill -KILL " + pid);
            break;
        case LIMIT + 135:
            logExec("killall java");
            break;
        case LIMIT + 140:
            logExec("killall -KILL java");
            break;
        case LIMIT + 150:
            warn("I give up.");
            return false;
        }
        return !cancelled;
    }
}
