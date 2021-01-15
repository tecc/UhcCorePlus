package com.gmail.val59000mc.scenarios.scenariolisteners;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.events.UhcGameStateChangedEvent;
import com.gmail.val59000mc.events.UhcTimeEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class PowerSwapListener extends ScenarioListener {
    private long duration = 2 * 60;
    private long timeUntilNextSwap = duration;

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
            broadcast("&fSwapping health in &e&l" + (timeUntilNextSwap / 60) + "&f minutes");
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
                lowest = Arrays.asList(p);
                lowestHealth = p.getPlayer().getHealth();
            }
            if (highest == null) {
                highest = Arrays.asList(p);
                highestHealth = p.getPlayer().getHealth();
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

        if (highest.size() > 1) return "More than 2 players have the greatest health, " + highestHealth + ".";
        UhcPlayer highestPlayer = highest.get(0);
        UhcPlayer lowestPlayer = lowest.get(RandomUtils.nextInt(lowest.size()));

        if (highestPlayer.getUuid() == lowestPlayer.getUuid()) return "what? just tell him/me that it's wrong";

        highestPlayer.getPlayer().setHealth(lowestHealth);
        lowestPlayer.getPlayer().setHealth(highestHealth);

        return "&fSwapped " + highestPlayer.getDisplayName() + "&f's health with " + lowestPlayer.getDisplayName() + "&f's.";
    }
}
