package me.tecc.uhccoreplus.scenarios;

import com.gmail.val59000mc.events.UhcTimeEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.Option;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class PowerSwapListener extends ScenarioListener {
    @Option
    private long duration = 5 * 60;

    private long timeUntilNextSwap;

    public void onEnable() {
        timeUntilNextSwap = duration;
    }

    @EventHandler
    public void onUhcTime(UhcTimeEvent event) {
        if (!getGameManager().getPvp()) return;
        if (getGameManager().getGameState() == GameState.DEATHMATCH) return;

        timeUntilNextSwap--;
        if (timeUntilNextSwap == 0) {
            timeUntilNextSwap = duration;
            try {
                String result = swap();
                broadcast(result);
            } catch (Exception e) {
                broadcast("&eSomething went wrong whilst trying to swap health.");
                e.printStackTrace();
            }
            return;
        }
        if (timeUntilNextSwap == 1) {
            broadcast("&fSwapping health in &e&l1 &fsecond.");
            return;
        }
        if (timeUntilNextSwap <= 10) {
            broadcast("&fSwapping health in &e&l" + timeUntilNextSwap + " seconds.");
            return;
        }
        if (timeUntilNextSwap % 60 == 0) {
            if (timeUntilNextSwap / 60 == 1)
                broadcast("&fSwapping health in &e&l1&f minute.");
            else broadcast("&fSwapping health in &e&l" + (timeUntilNextSwap / 60) + "&f minutes.");
        }
    }

    private void broadcast(String message) {
        getGameManager().broadcastInfoMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private String swap() throws UhcPlayerNotOnlineException {
        Set<UhcPlayer> players = getPlayersManager().getAllPlayingPlayers();

        double lowestHealth = 0;
        List<UhcPlayer> lowest = null;
        double highestHealth = 0;
        List<UhcPlayer> highest = null;

        for (UhcPlayer p : players) {
            if (!p.isOnline()) continue; // safeguard

            if (lowest == null) {
                lowest = mutableList(p);
                lowestHealth = p.getPlayer().getHealth();
            }
            if (highest == null) {
                highest = mutableList(p);
                highestHealth = p.getPlayer().getHealth();
                continue;
            }

            if (p.getPlayer().getHealth() == lowestHealth) {
                lowest.add(p);
            }
            if (p.getPlayer().getHealth() < lowestHealth) {
                lowest.clear();
                lowest.add(p);
                lowestHealth = p.getPlayer().getHealth();
            }

            if (p.getPlayer().getHealth() == highestHealth) {
                highest.add(p);
            }
            if (p.getPlayer().getHealth() > highestHealth) {
                highest.clear();
                highest.add(p);
                highestHealth = p.getPlayer().getHealth();
            }
        }

        assert lowest != null : "Lowest is null";
        // intellij is scary smart, and knows that if lowest is not null then highest cannot be null
        if (highest.size() > 1) return "More than 2 players have the greatest health, " + highestHealth + ".";
        UhcPlayer highestPlayer = highest.get(0);
        UhcPlayer lowestPlayer = lowest.get(RandomUtils.nextInt(lowest.size()));

        if (highestPlayer.getUuid() == lowestPlayer.getUuid()) return "what? just tell him/me that it's wrong";

        highestPlayer.getPlayer().setHealth(lowestHealth);
        lowestPlayer.getPlayer().setHealth(highestHealth);

        return "&fSwapped " + highestPlayer.getDisplayName() + "&f's health with " + lowestPlayer.getDisplayName() + "&f's.";
    }

    @SafeVarargs
    private final <T> List<T> mutableList(T... ts) {
        return new ArrayList<>(Arrays.asList(ts));
    }
}
