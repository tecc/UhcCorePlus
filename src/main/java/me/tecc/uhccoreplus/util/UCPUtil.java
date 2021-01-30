package me.tecc.uhccoreplus.util;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Pattern;

public class UCPUtil {
    public static String colourise(String str, char altCode) {
        return ChatColor.translateAlternateColorCodes(altCode, str);
    }
    public static String colourise(String str) {
        return colourise(str, '&');
    }

    public static String getNbtFromItem(ItemStack item, String key) {
        NBTItem nbt = new NBTItem(item);
        return nbt.getString(key);
    }

    public static String UUID_PATTERN = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    public static boolean isUUID(String s) {
        return s.matches(UUID_PATTERN);
    }
}
