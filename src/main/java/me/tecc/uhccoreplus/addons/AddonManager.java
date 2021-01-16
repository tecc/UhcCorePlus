package me.tecc.uhccoreplus.addons;

import com.gmail.val59000mc.UhcCore;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class AddonManager {
    private static final Logger log = UhcCore.getPlugin().getLogger();
    private static AddonManager instance;

    private final Map<Class<? extends Addon>, Addon> addons;
    private final Map<Class<? extends Addon>, AddonDescription> addonConfigurations;
    private final Map<File, ClassLoader> classLoaders;


    public AddonManager() {
        instance = this;
        addonConfigurations = new HashMap<>();
        addons = new HashMap<>();
        classLoaders = new HashMap<>();
    }

    public static AddonManager getAddonManager() {
        return instance;
    }

    public List<Addon> getAddons() {
        return new ArrayList<>(this.addons.values());
    }

    public void loadAddons() {
        log.info("Loading addons...");
        List<File> files;
        try {
            files = getAddonFiles();
        } catch (IOException e) {
            log.severe("Couldn't get addon files.");
            e.printStackTrace();
            return;
        }
        if (files.size() == 0) {
            log.info("No addons to load.");
            return;
        }
        int successfulLoads = 0;
        for (File file : files) {
            String baseName = FilenameUtils.getBaseName(file.getAbsolutePath());
            log.info("Attempting to load addon " + baseName + ".");
            try {
                Addon addon = loadAddon(file);
                if (addon == null) {
                    log.severe("Couldn't load addon " + baseName);
                    continue;
                }
                log.info("Loaded addon " + addon.id);
                successfulLoads++;
            } catch (Exception e) {
                log.severe("Couldn't load addon " + baseName);
                e.printStackTrace();
            }
        }
        log.info("Loaded " + successfulLoads + " addons out of " + files.size() + " available.");
    }

    public void enableAddons() {
        addons.forEach((clz, addon) -> addon.enable());
    }

    public void disableAddons() {
        addons.forEach((clz, addon) -> addon.disable());
    }

    public void unloadAddon(Class<? extends Addon> addon, boolean clearCachedClassLoader) {
        disableAddon(addon);

        if (clearCachedClassLoader)
            classLoaders.remove(getAddonConfig(addon).getFile());
        addons.remove(addon);
        addonConfigurations.remove(addon);
    }

    public Addon loadAddon(File file, ClassLoader classLoader) {
        String filename = FilenameUtils.getName(file.getAbsolutePath());

        AddonDescription config = new AddonDescription(file);
        try {
            InputStream stream = classLoader.getResourceAsStream("addon.yml");
            if (stream == null) return null;
            config.load(new InputStreamReader(stream));
        } catch (IOException e) {
            log.warning("File " + filename + " is a valid JAR, but is not an addon. Skipping.");
            return null;
        } catch (InvalidConfigurationException e) {
            log.warning("File " + filename + " has an addon configuration, but the configuration is invalid. Skipping.");
        }

        String id = config.getString("id");
        if (id == null) {
            log.severe("Addon " + filename + " does not have an ID, and is therefore not a valid addon. Skipping.");
            return null;
        }
        if (this.addonExists(id)) {
            log.severe("Addon " + filename + " has a duplicate ID (" + id + "). Skipping.");
            return null;
        }
        Class<?> clz;
        try {
            clz = Class.forName(config.getString("mainClass"), true, classLoader);
        } catch (ClassNotFoundException e) {
            log.severe("Addon " + id + "has a valid configuration, but the main class was not found. Skipping.");
            return null;
        }
        if (this.addonExists(clz)) {
            log.severe("Addon " + id + " has duplicate main class (" + clz.getCanonicalName() + "). Skipping");
            return null;
        }
        if (!Addon.class.isAssignableFrom(clz)) return null;
        Class<? extends Addon> addonClass = clz.asSubclass(Addon.class);
        this.addonConfigurations.put(addonClass, config);

        Addon addon;
        try {
            addon = addonClass.newInstance();
        } catch (IllegalAccessException e) {
            log.severe("Addon " + id + " main class cannot be instantiated due to insufficient access. Skipping.");
            this.addonConfigurations.remove(addonClass);
            return null;
        } catch (InstantiationException e) {
            log.severe("There was an exception whilst trying to instantiate addon " + id + " main class. Skipping.");
            this.addonConfigurations.remove(addonClass);
            return null;
        }
        this.addons.put(addonClass, addon);
        return addon;
    }

    /**
     * Loads an addon from a file.
     *
     * @param file The addon file to load.
     * @return The addon loaded from the file. If any error occurred it will return null.
     * @implNote If the file has previously been loaded, the class loader for that file will not be instantiated again.
     * This is due to the addon manager not deleting class loaders that aren't in use.
     */
    public Addon loadAddon(File file) {
        String filename = FilenameUtils.getName(file.getAbsolutePath());
        ClassLoader classLoader;

        if (classLoaders.containsKey(file))
            classLoader = classLoaders.get(file);
        else {
            try {
                classLoader = new URLClassLoader(
                        new URL[]{file.toURI().toURL()},
                        this.getClass().getClassLoader()
                );
            } catch (MalformedURLException e) {
                log.severe("Class loader for file " + filename + " got a malformed URL. Skipping.");
                return null;
            }
            classLoaders.put(file, classLoader);
        }

        return loadAddon(file, classLoader);
    }

    @Contract("null -> false")
    public boolean addonExists(String id) {
        if (id == null) return false;
        return addons.values().stream().anyMatch(a -> id.equalsIgnoreCase(a.id));
    }

    @Contract("null -> false")
    public boolean addonExists(Class<?> clz) {
        if (clz == null) return false;
        return addons.keySet().stream().anyMatch(clz2 -> clz2.equals(clz));
    }

    public List<File> getAddonFiles() throws IOException {
        UhcCore core = UhcCore.getPlugin();

        // makes sure the directory is proper
        File addonsDirectory = new File(core.getDataFolder(), "addons");
        if (!addonsDirectory.isDirectory()) {
            if (!addonsDirectory.delete())
                throw new IOException("Couldn't delete invalid addons directory.");
        }
        if (!addonsDirectory.exists()) {
            if (!addonsDirectory.mkdir())
                throw new IOException("Couldn't create new addons directory");
            else return Collections.emptyList(); // because there isn't any files in the directory
        }

        File[] filteredFiles = addonsDirectory.listFiles((file) -> {
            if (!file.isFile())
                return false;
            try {
                new JarFile(file);
                return true;
            } catch (IOException e) {
                return false;
            }
        });

        return Arrays.asList(filteredFiles == null ? new File[]{} : filteredFiles);
    }

    @Nullable
    public Addon getAddon(Class<? extends Addon> clz) {
        return this.addons.get(clz);
    }

    @Nullable
    public Addon getAddon(String id) {
        for (Addon addon : this.getAddons()) {
            if (addon.id.equalsIgnoreCase(id))
                return addon;
        }
        return null;
    }

    @NotNull
    public AddonDescription getAddonConfig(Class<? extends Addon> clz) {
        return this.addonConfigurations.get(clz);
    }

    @SuppressWarnings("ConstantConditions")
    public void enableAddon(String id) {
        if (!addonExists(id))
            return;
        getAddon(id).enable();
    }

    @SuppressWarnings("ConstantConditions")
    public void enableAddon(Class<? extends Addon> clz) {
        if (!addonExists(clz))
            return;
        getAddon(clz).enable();
    }

    @SuppressWarnings("ConstantConditions")
    public void disableAddon(String id) {
        if (!addonExists(id))
            return;
        getAddon(id).disable();
    }

    @SuppressWarnings("ConstantConditions")
    public void disableAddon(Class<? extends Addon> clz) {
        if (!addonExists(clz))
            return;
        getAddon(clz).disable();
    }

    public boolean toggleAddon(@NotNull Addon addon) {
        // using the functions in AddonManager in case i ever modify them to do something more
        if (addon.isEnabled()) {
            disableAddon(addon.getClass());
            return false;
        } else {
            enableAddon(addon.getClass());
            return true;
        }
    }

}
