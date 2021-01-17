package me.tecc.uhccoreplus.scenarios;

import com.gmail.val59000mc.scenarios.ScenarioListener;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class DogLoverListener extends ScenarioListener {
    @EventHandler
    public void onTryTameEvent(PlayerInteractEntityEvent event) {
        PlayerInventory inv = event.getPlayer().getInventory();
        Entity target = event.getRightClicked();

        ItemStack item = inv.getItem(inv.getHeldItemSlot());
        if (item == null) return;
        if (item.getType() != Material.BONE)
            return;
        if (target.getType() != EntityType.WOLF)
            return;

        Wolf wolf = (Wolf) target;

        item.setAmount(item.getAmount() - 1);
        wolf.setOwner(event.getPlayer());
    }
}
