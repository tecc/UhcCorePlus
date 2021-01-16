package me.tecc.uhccoreplus.addons;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.players.PlayersManager;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.scenarios.ScenarioManager;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public abstract class Addon {
    private boolean enabled = false;
    @NotNull
    public final String id;
    protected final Logger logger;

    public Addon() {
        AddonDescription config = getAddonDescription();
        if (!config.contains("id"))
            throw new RuntimeException("No ID found in addon description!");
        //noinspection ConstantConditions
        id = config.getString("id");
        logger = Logger.getLogger("UCPA_" + id);
        logger.setParent(UhcCore.getPlugin().getLogger());
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

    public void onEnable() {
        logger.info("onEnable: " + id);
    }

    public void onDisable() {
        logger.info("onDisable: " + id);
    }

    public List<Scenario> getScenarios() {
        return Collections.emptyList();
    }

    @NotNull
    public AddonDescription getAddonDescription() {
        return AddonManager.getAddonManager().getAddonConfig(this.getClass());
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
}
