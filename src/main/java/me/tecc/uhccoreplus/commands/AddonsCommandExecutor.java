package me.tecc.uhccoreplus.commands;

import com.gmail.val59000mc.commands.UhcCommandExecutor;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.scenarios.Scenario;
import me.tecc.uhccoreplus.addons.Addon;
import me.tecc.uhccoreplus.addons.AddonManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AddonsCommandExecutor implements CommandExecutor {
    private AddonManager addonManager = AddonManager.getAddonManager();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage("This command requires an argument.");
            return true;
        }
        String sub = args[0];

        if (!sender.hasPermission("uhc.addons")) {
            sender.sendMessage("You don't have the necessary permissions for this command (uhc.addons)");
            return true;
        }
        switch (sub) {
            case "help":
                List<String> help = Arrays.asList(
                        label + " help - displays help",
                        label + " list - lists all addons",
                        label + " enable <addon> - enables an addon",
                        label + " disable <addon> - disables an addon"
                );
                sender.sendMessage("");

            case "list":
                sender.sendMessage(StringUtils.join(getAddonNameList(), "\n- "));
                return true;

            case "enable":
                if (args.length < 2) {
                    sender.sendMessage("This command requires 2 arguments.");
                    return true;
                }
                String addonId = args[1];
                if (!addonManager.addonExists(addonId)) {
                    sender.sendMessage("That is not a valid addon. Do /" + label + " list for a list of addons.");
                    return true;
                }
                addonManager.enableAddon(addonId);
                return true;
            case "disable":
                if (args.length < 2) {
                    sender.sendMessage("This command requires 2 arguments.");
                    return true;
                }
                addonId = args[1];
                if (!addonManager.addonExists(addonId)) {
                    sender.sendMessage("That is not a valid addon. Do /" + label + " list for a list of addons.");
                    return true;
                }
                addonManager.disableAddon(addonId);
                sender.sendMessage("Disabled addon " + addonId + ".");
                return true;
            default:
                sender.sendMessage("Invalid subcommand. Do /" + label + " help for help.");
                return true;
        }
    }

    private List<String> getAddonNameList() {
        return addonManager.getAddons().stream().map(addon -> addon.id).collect(Collectors.toList());
    }
}
