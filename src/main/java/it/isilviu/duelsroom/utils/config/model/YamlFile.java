package it.isilviu.duelsroom.utils.config.model;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class YamlFile extends YamlConfiguration {

    private final File file;

    public YamlFile(Plugin plugin, String name) {
        this(plugin, new File(plugin.getDataFolder(), name));
    }

    public YamlFile(Plugin plugin, File file) {
        this.file = file;

        if (!file.exists())
            plugin.saveResource(file.getPath().replace(file.getParent(), "").substring(1), false);

        this.reload();
    }

    public void save() {
        try {
            this.save(file);
        } catch (IOException ignored) {
        }
    }

    public void reload() {
        try {
            this.load(file);
        } catch (FileNotFoundException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot find " + file, ex);
        } catch (InvalidConfigurationException | IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
        }
    }

    /* STUFF */
    @Nullable
    public SoundModel getSound(String path) {
        ConfigurationSection sound = this.getConfigurationSection(path);
        if (sound == null) return null;

        try {
            return new SoundModel(Sound.valueOf(sound.getString("sound")),
                    (float) sound.getDouble("volume", 1),
                    (float) sound.getDouble("pitch", 1));
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().log(Level.WARNING, "Invalid sound: " + sound.getString("sound") + " in path: " + path);
            return null;
        }
    }

    public List<Material> getMaterialList(String path) {
        List<String> materials = this.getStringList(path);
        if (materials.isEmpty()) return List.of();

        return materials.stream()
                .map(Material::matchMaterial)
                .filter(Objects::nonNull)
                .toList();
    }
}
