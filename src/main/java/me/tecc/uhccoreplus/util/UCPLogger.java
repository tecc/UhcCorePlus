package me.tecc.uhccoreplus.util;

import com.gmail.val59000mc.UhcCore;
import me.tecc.uhccoreplus.addons.Addon;
import me.tecc.uhccoreplus.addons.AddonManager;

import java.util.function.Consumer;
import java.util.logging.Logger;

public class UCPLogger {
    private static final String PREFIX_FORMAT = "[" + UhcCore.getPlugin().getDescription().getName() + "]%s";
    private static final String LOG_FORMAT = "%s: %s";
    private Logger underlyingLogger = UhcCore.getPlugin().getLogger();
    private String prefix;

    private UCPLogger(String prefix) {
        if (prefix == null || prefix.isEmpty())
            prefix = "";
        else
            prefix = " " + prefix;
        this.prefix = String.format(PREFIX_FORMAT, prefix);
    }

    private void log(Consumer<String> logFunction, String message) {
        logFunction.accept(String.format(LOG_FORMAT, this.prefix, message));
    }
    // public logging functions

    /**
     * Logs an info message to the underlying logger.
     *
     * @param message The log message.
     */
    public void info(String message) {
        log(underlyingLogger::info, message);
    }
    /**
     * Logs a warning message to the underlying logger.
     *
     * @param message The log message.
     */
    public void warn(String message) {
        log(underlyingLogger::warning, message);
    }
    /**
     * Logs an error message to the underlying logger.
     *
     * @param message The log message.
     */
    public void error(String message) {
        log(underlyingLogger::severe, message);
    }

    // for compatibility purposes

    /**
     * @see #warn(String)
     * @deprecated Use {@link #warn(String)} instead
     */
    @Deprecated
    public void warning(String message) {
        warn(message);
    }


    public static UCPLogger global() {
        return new UCPLogger(null);
    }

    public static UCPLogger of(Class<? extends Addon> addonClass) {
        Addon addon = AddonManager.getAddonManager().getAddon(addonClass);
        if (addon == null) {
            UCPLogger global = global();
            global.warn("UCPLogger#of was called with null as parameter. Use UCPLogger.getGlobalLogger() instead.");
            return global;
        }

        String addonName = addon.getAddonDescription().name();
        return new UCPLogger(addonName);
    }
}
