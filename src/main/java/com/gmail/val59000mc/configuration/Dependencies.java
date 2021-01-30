package com.gmail.val59000mc.configuration;

import com.gmail.val59000mc.utils.ProtocolUtils;
import me.tecc.uhccoreplus.util.UCPLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class Dependencies {
    private static final UCPLogger logger = UCPLogger.global();

    // dependencies
    private static boolean worldEditLoaded;
    private static boolean vaultLoaded;
    private static boolean protocolLibLoaded;

    public static void loadWorldEdit() {
        Plugin wePlugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (wePlugin == null || !wePlugin.getClass().getName().equals("com.sk89q.worldedit.bukkit.WorldEditPlugin")) {
            logger.warn("WorldEdit plugin not found, there will be no support of schematics.");
            worldEditLoaded = false;
        } else {
            logger.info("Hooked into WorldEdit plugin.");
            worldEditLoaded = true;
        }
    }

    public static void loadVault() {
        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
        if (vault == null || !vault.getClass().getName().equals("net.milkbowl.vault.Vault")) {
            logger.warn("Vault plugin not found, there will be no support of economy rewards.");
            vaultLoaded = false;
            return;
        }

        logger.info("Hooked into Vault plugin.");
        vaultLoaded = true;

        VaultManager.setupEconomy();
    }

    public static void loadProtocolLib() {
        Plugin protocolLib = Bukkit.getPluginManager().getPlugin("ProtocolLib");
        if (protocolLib == null || !protocolLib.getClass().getName().equals("com.comphenix.protocol.ProtocolLib")) {
            logger.warn("ProtocolLib plugin not found.");
            protocolLibLoaded = false;
            return;
        }

        logger.info("Hooked into ProtocolLib plugin.");
        protocolLibLoaded = true;

        try {
            ProtocolUtils.register();
        } catch (Exception ex) {
            protocolLibLoaded = false;
            logger.error("Failed to load ProtocolLib, are you using the right version?");
            ex.printStackTrace();
        }
    }

    public static boolean getWorldEditLoaded() {
        return worldEditLoaded;
    }

    public static boolean getVaultLoaded() {
        return vaultLoaded;
    }

    public static boolean getProtocolLibLoaded() {
        return protocolLibLoaded;
    }

}
