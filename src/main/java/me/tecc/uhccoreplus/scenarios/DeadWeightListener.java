package me.tecc.uhccoreplus.scenarios;

import com.gmail.val59000mc.events.UhcPlayerKillEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.Option;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import me.tecc.uhccoreplus.util.UCPUtil;
import org.bukkit.event.EventHandler;

public class DeadWeightListener extends ScenarioListener {
    private static final String FORMAT = "&cAs you kill your &e%s &cvictim, your heart starts weighing down. \n&cYou become slower as a result.";

    /**
     * The initial amount subtracted for a kill.
     * <p>
     * Default: 0.01f
     */
    @Option
    public float subtractedSpeed = 0.01f;

    /**
     * Whether or not to subtract less speed after kills.
     */
    @Option
    public boolean lessEffectPerKill = true;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerKill(UhcPlayerKillEvent event) throws UhcPlayerNotOnlineException {
        UhcPlayer up = event.getKiller();
        up.getPlayer().setWalkSpeed(calculatePlayerSpeed(up.getKills()));
        String killCount;
        switch (up.getKills() % 10) {
            case 1:
                killCount = up.getKills() + "st";
                break;
            case 2:
                killCount = up.getKills() + "nd";
                break;
            case 3:
                killCount = up.getKills() + "rd";
                break;
            default:
                killCount = up.getKills() + "th";
                break;
        }
        up.sendMessage(UCPUtil.colourise(String.format(FORMAT, killCount)));
    }

    private float calculatePlayerSpeed(int kills) {
        if (!lessEffectPerKill) {
            return .2f - (kills * subtractedSpeed);
        }

        if (kills == 1) return .2f - subtractedSpeed;

        return calculatePlayerSpeed(kills - 1) - (subtractedSpeed / kills);
    }
}
