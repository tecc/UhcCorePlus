package me.tecc.uhccoreplus.util;

import org.bukkit.ChatColor;

public class UCPUtil {
    public static String colourise(String str, char altCode) {
        return ChatColor.translateAlternateColorCodes(altCode, str);
    }
    public static String colourise(String str) {
        return colourise(str, '&');
    }
}
