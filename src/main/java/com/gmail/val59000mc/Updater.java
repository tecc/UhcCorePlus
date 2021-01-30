package com.gmail.val59000mc;

import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.utils.FileUtils;
import com.gmail.val59000mc.utils.TimeUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vdurmont.semver4j.Semver;
import me.tecc.uhccoreplus.util.UCPLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.function.Consumer;

public class Updater extends Thread implements Listener {
    private static final UCPLogger logger = UCPLogger.global();
    private static final String LATEST_RELEASE = "https://api.github.com/repos/tecc/UhcCorePlus/releases";
    private final Plugin plugin;
    private Version currentVersion, newestVersion;
    private boolean hasPendingUpdate;
    private String jarDownloadUrl;

    public Updater(Plugin plugin) {
        this.plugin = plugin;
        hasPendingUpdate = false;
        start();
    }

    @Override
    public void run() {
        while (!hasPendingUpdate && plugin.isEnabled()) {
            try {
                runVersionCheck();
                sleep(false);
            } catch (Exception ex) {
                Bukkit.getLogger().severe("[UhcCore] Failed to check for updates!");
                ex.printStackTrace();
                sleep(true);
            }
        }
    }

    private void sleep(boolean failedLastCheck) {
        if (hasPendingUpdate) {
            return;
        }

        long time = (failedLastCheck ? 5 : 30) * TimeUtils.MINUTE;

        try {
            sleep(time);
        } catch (InterruptedException ex) {
            Bukkit.getLogger().severe("[UhcCore] Update thread stopped!");
            ex.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        if (player.isOp()) {
            sendUpdateMessage(player::sendMessage);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (!e.getMessage().equalsIgnoreCase("/uhccore update")) {
            return;
        }
        e.setCancelled(true);

        Player player = e.getPlayer();
        GameManager gm = GameManager.getGameManager();

        if (gm.getGameState() == GameState.PLAYING || gm.getGameState() == GameState.DEATHMATCH) {
            player.sendMessage(ChatColor.RED + "You can not update the plugin during games as it will restart your server.");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Updating plugin ...");

        try {
            updatePlugin(true);
        } catch (Exception ex) {
            player.sendMessage(ChatColor.RED + "Failed to update plugin, check console for more info.");
            ex.printStackTrace();
        }
    }

    private void runVersionCheck() throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(LATEST_RELEASE).openConnection();

        // Add headers
        connection.setRequestMethod("GET");
        connection.addRequestProperty("Accept", "application/json");
        connection.addRequestProperty("User-Agent", "UhcCore:" + UhcCore.getPlugin().getDescription().getVersion());

        connection.connect();

        JsonParser jp = new JsonParser();
        JsonObject root = jp.parse(new InputStreamReader(connection.getInputStream()))
                .getAsJsonArray().get(0)
                .getAsJsonObject();

        newestVersion = new Version(root.get("tag_name").getAsString(), root.get("prerelease").getAsBoolean());
        currentVersion = new Version(plugin.getDescription().getVersion());

        if (!newestVersion.isNewerThan(currentVersion)) {
            return; // Already on the newest or newer version
        }

        hasPendingUpdate = true;

        for (JsonElement jsonElement : root.get("assets").getAsJsonArray()) {
            JsonObject asset = jsonElement.getAsJsonObject();

            if (asset.get("name").getAsString().endsWith(".jar")) {
                jarDownloadUrl = asset.get("browser_download_url").getAsString();
                break;
            }
        }

        if (jarDownloadUrl == null) {
            Bukkit.getLogger().severe("Jar download URL not found!");
        }

        // New version is available, register player join listener so we can notify admins.
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        sendUpdateMessage(logger::info);
    }

    private void sendUpdateMessage(Consumer<String> out) {
        out.accept("A new version of UhcCorePlus is available! (" + currentVersion + " -> " + newestVersion + ").");
        out.accept("Do /update to download & update the plugin.");
        out.accept("WARNING: This will restart the server.");
    }

    private void updatePlugin(boolean restart) throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(jarDownloadUrl).openConnection();
        connection.connect();

        File newPluginFile = new File("plugins/UhcCore-" + newestVersion + ".jar");
        File oldPluginFile = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());

        InputStream in = connection.getInputStream();
        FileOutputStream out = new FileOutputStream(newPluginFile);

        // Copy in to out
        int read;
        byte[] bytes = new byte[1024];

        while ((read = in.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }

        out.flush();
        out.close();
        in.close();
        connection.disconnect();

        Bukkit.getLogger().info("[UhcCore] New plugin version downloaded.");

        if (!newPluginFile.equals(oldPluginFile)) {
            FileUtils.scheduleFileForDeletion(oldPluginFile);
            Bukkit.getLogger().info("[UhcCore] Old plugin version will be deleted on next startup.");
        }

        if (restart) {
            Bukkit.getLogger().info("[UhcCore] Restarting to finish plugin update.");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
        }
    }

    public void runAutoUpdate() {
        // Auto update is disabled.
        if (!GameManager.getGameManager().getConfig().get(MainConfig.AUTO_UPDATE)) {
            return;
        }

        // No pending update.
        if (!hasPendingUpdate) {
            return;
        }

        Bukkit.getLogger().info("[UhcCore] Running auto update.");
        try {
            updatePlugin(false);
        } catch (Exception ex) {
            Bukkit.getLogger().warning("[UhcCore] Failed to update plugin!");
            ex.printStackTrace();
        }
    }

    private static class Version {

        private final Semver semver;
        private boolean prerelease;

        private Version(String version) {
            this(version, false);
            this.prerelease = isSemverPrerelease();
        }
        private Version(String version, boolean prerelease) {
            if (version.startsWith("v"))
                version = version.substring(1);
            this.semver = new Semver(version);
            this.prerelease = prerelease;
        }

        public boolean isSemverPrerelease() {
            String suffix = semver.getSuffixTokens()[0];
            switch (suffix.toUpperCase()) {
                case "ALPHA":
                case "BETA":
                    return false;
                case "STABLE":
                default:
                    if (!suffix.equalsIgnoreCase("STABLE"))
                        logger.warn("Version has invalid version suffix " + suffix.toUpperCase() + ".");
                    return true;
            }
        }

        public boolean isPrerelease() {
            return prerelease || isSemverPrerelease();
        }

        public boolean equals(Version version) {
            return this.semver.equals(version.semver);
        }

        @Override
        public String toString() {
            return semver.toString();
        }

        @Contract("null -> false")
        private boolean isNewerThan(Version version) {
            if (version == null) return false;
            return version.semver.isGreaterThan(version.semver);
        }
    }

}