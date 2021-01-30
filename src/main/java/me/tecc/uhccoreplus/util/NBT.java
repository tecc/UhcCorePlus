package me.tecc.uhccoreplus.util;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class NBT {
    public static final String ID_VALUE = "UCP_id";

    public static UUID itemGetUuid(ItemStack item, String key) {
        return new NBTItem(item).getUUID(key);
    }
}
