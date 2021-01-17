package me.tecc.uhccoreplus.scenarios;

import com.gmail.val59000mc.scenarios.Option;
import com.gmail.val59000mc.scenarios.ScenarioListener;
import me.tecc.uhccoreplus.util.UCPUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class DeadweightListener extends ScenarioListener {
    /**
     * Chooses how many items need to be in the inventory before the scenario takes effect.
     *
     * Default: 27 * 64
     */
    @Option
    private long minimumEffectiveItemCount = 27 * 64;

    /**
     * How much of a difference there needs to be before it sends a message.
     *
     * Default: 0.01f
     */
    @Option
    private float messageThreshold = 0.01f;

    private Map<UUID, Float> playerSpeedMessages;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        long count = countInventoryItems(p.getInventory());
        float newSpeed = .2f / count;
        if (newSpeed == p.getWalkSpeed()) return;
        p.setWalkSpeed(newSpeed);

        float lastMessage = playerSpeedMessages.getOrDefault(p.getUniqueId(), 0.2f);
        if (Math.abs(newSpeed - lastMessage) < messageThreshold) return;
        if (Math.signum(lastMessage) == -1.0f && newSpeed > Math.abs(lastMessage)) {
            p.sendMessage(
                    UCPUtil.colourise("&aYou're getting faster as you have less items in your inventory!")
            );
            playerSpeedMessages.put(p.getUniqueId(), newSpeed);
        }
        else if (Math.signum(lastMessage) == 1.0f && newSpeed < Math.abs(lastMessage)) {
            p.sendMessage(
                    UCPUtil.colourise("&cYou're getting slower as you have too many items in your inventory!")
            );
            playerSpeedMessages.put(p.getUniqueId(), -newSpeed);
        }
    }

    // counts each item stack and scales them appropriately
    private long countInventoryItems(Inventory inv) {
        AtomicLong count = new AtomicLong();
        inv.forEach(item -> {
            long relativityToStack = 64 / item.getMaxStackSize();
            count.addAndGet(item.getAmount() * relativityToStack);
        });

        return Math.max(count.get() - minimumEffectiveItemCount, 1);
    }
}
