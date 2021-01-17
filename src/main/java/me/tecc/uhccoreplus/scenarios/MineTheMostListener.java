package me.tecc.uhccoreplus.scenarios;

import com.gmail.val59000mc.events.UhcTimeEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerDoesntExistException;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.scenarios.Option;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import me.tecc.uhccoreplus.util.UCPUtil;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.*;

public class MineTheMostListener extends ScenarioListener {
    @Option
    private long interval = 10 * 60;
    private long timer = -1;

    private final HashMap<UUID, Long> blocksMinedByUUID = new HashMap<>();

    private UUID lastKill;

    @EventHandler
    public void onMineBlockEvent(BlockBreakEvent event) {
        UUID u = event.getPlayer().getUniqueId();
        long playerMinedBlocks = blocksMinedByUUID.get(u);
        blocksMinedByUUID.put(u, playerMinedBlocks + 1);
    }

    @EventHandler
    public void onTimeEvent(UhcTimeEvent event) {
        if (!getGameManager().getPvp()) return;
        if (timer == -1) timer = interval;

        timer--;

        if (timer == 0) {
            timer = interval;
            try {
                String result = killWorstMiner();
                broadcast(result);
            } catch (Exception e) {
                broadcast("&eSomething went wrong whilst trying to swap health.");
                e.printStackTrace();
            }
            return;
        }
        if (timer == 1) {
            broadcast("&fKilling a player in &4&l1 &fsecond.");
            return;
        }
        if (timer <= 10) {
            broadcast("&fKilling a player in &c&l" + timer + "&f seconds.");
            return;
        }
        if (timer <= 30 && timer % 10 == 0) {
            broadcast("&fKilling a player in &6&l" + timer + "&f seconds.");
        }
        if (timer % 60 == 0) {
            if (timer / 60 == 1)
                broadcast("&fKilling a player in &6&l1&f minute.");
            else broadcast("&fKilling a player in &e&l" + (timer / 60) + "&f minutes.");
        }
    }

    private void broadcast(String message) {
        getGameManager().broadcastInfoMessage(UCPUtil.colourise(message));
    }

    public String killWorstMiner() throws UhcPlayerDoesntExistException, UhcPlayerNotOnlineException {
        Optional<Map.Entry<UUID, Long>> maybeWorstPlayer = blocksMinedByUUID.entrySet().stream()
                .min(Comparator.comparingLong(Map.Entry::getValue));

        if (!maybeWorstPlayer.isPresent())
            return "No player to kill.";

        UhcPlayer player = getPlayersManager().getUhcPlayer(maybeWorstPlayer.get().getKey());
        long blocksMined = maybeWorstPlayer.get().getValue();
        blocksMinedByUUID.remove(player.getUuid());

        if (!player.isOnline()) {
            getPlayersManager().killOfflineUhcPlayer(player, player.getStoredItems());
        }

        double damage = player.getPlayer().getHealth();
        EntityDamageEvent cause = new EntityDamageEvent(player.getPlayer(), EntityDamageEvent.DamageCause.CUSTOM, damage);
        lastKill = player.getUuid();
        player.getPlayer().setLastDamageCause(cause);
        Bukkit.getServer().getPluginManager().callEvent(cause);
        return player.getDisplayName() + "&f had only mined &e" + blocksMined + "&f blocks.";
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        if (event.getEntity().getUniqueId() != lastKill)
            return;
        Player p = event.getEntity();
        String format = DEATH_MESSAGES[RandomUtils.nextInt(DEATH_MESSAGES.length)];
        event.setDeathMessage(UCPUtil.colourise(String.format(format, p.getDisplayName())));
    }


    private static final String[] DEATH_MESSAGES = {
            "%s &fwas too bad at mining",
            "%s &fwas flattened by a cave collapsing"
    };
}
