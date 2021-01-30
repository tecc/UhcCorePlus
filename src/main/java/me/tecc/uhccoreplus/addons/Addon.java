package me.tecc.uhccoreplus.addons;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioManager;
import me.tecc.uhccoreplus.util.UCPLogger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public abstract class Addon {
    private boolean enabled = false;
    @NotNull
    public final String id;
    public final File addonFile;
    protected final UCPLogger logger = UCPLogger.of(this.getClass());

    public Addon() {
        AddonDescription config = getAddonDescription();
        if (!config.contains("id"))
            throw new RuntimeException("No ID found in addon description!");
        //noinspection ConstantConditions
        id = config.getString("id");
        addonFile = AddonManager.getAddonManager().getAddonFile(this.getClass());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public final void enable() {
        if (enabled) return;
        logger.info("Attempting to enable addon " + id);
        List<Scenario> scenarios = getScenarios();
        scenarios.forEach(GameManager.getGameManager().getScenarioManager()::registerScenario);
        onEnable();
        enabled = true;
        logger.info("Enabled addon " + id);
    }

    /**
     * Called when the addon is enabled.
     * The addon may be enabled at any point <i>after</i> the world has been generated.
     */
    public void onEnable() {
        logger.info("onEnable: " + id);
    }

    /**
     * Called when the addon is disabled.
     * The addon may be disabled at any point after it has been enabled.
     * Note that any scenarios registered by the plugin will automatically be disabled.
     */
    public void onDisable() {
        logger.info("onDisable: " + id);
    }

    public List<Scenario> getScenarios() {
        return Collections.emptyList();
    }

    @NotNull
    public AddonDescription getAddonDescription() {
        return AddonManager.getAddonManager().getAddonDescription(this.getClass());
    }

    public final void disable() {
        if (!enabled) return;
        onDisable();

        ScenarioManager sm = GameManager.getGameManager().getScenarioManager();
        for (Scenario s : getScenarios()) {
            sm.unregisterScenario(s);
        }
        logger.info("Disabled addon " + id);
    }

    // now for the utility methods
    public GameManager getGameManager() {
        return GameManager.getGameManager();
    }

    public PlayersManager getPlayersManager() {
        return getGameManager().getPlayersManager();
    }

    public YamlConfiguration getConfiguration() {
        try {
            return AddonManager.getAddonManager().getAddonConfiguration(this.getClass());
        } catch (Exception e) {
            logger.error("Something went wrong whilst trying to get addon configuration for addon " + id);
            e.printStackTrace();

            return new YamlConfiguration();
        }
    }

    public ClassLoader getClassLoader() {
        return AddonManager.getAddonManager().getClassLoader(this.getClass());
    }
}
