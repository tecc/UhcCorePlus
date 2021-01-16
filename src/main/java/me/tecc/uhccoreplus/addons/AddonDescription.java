package me.tecc.uhccoreplus.addons;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class AddonDescription extends YamlConfiguration {
    File file;

    AddonDescription(@NotNull File addonFile) {
        file = addonFile;
    }

    @NotNull
    public String getId() {
        String id = this.getString("id");
        if (id == null)
            throw new InvalidAddonDescriptionException("No ID was found!");
        return id;
    }

    @NotNull
    public File getFile() {
        return file;
    }
}
