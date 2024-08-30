package it.isilviu.duelsroom;

import it.isilviu.duelsroom.commands.DuelsRoomCommand;
import it.isilviu.duelsroom.utils.config.model.YamlFile;
import org.bstats.bukkit.Metrics;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitCommandHandler;

public class DuelsRoom extends JavaPlugin {

    static DuelsRoom INSTANCE;
    public static DuelsRoom instance() {
        return INSTANCE;
    }

    private YamlFile config;

    @Override
    public void onEnable() {
        INSTANCE = this;

        // METRICS
        //new Metrics(this, 0);

        // TODO: REMOVE
//        // From Item-NBT-API wiki
//        if (!NBT.preloadApi()) {
//            getLogger().warning("NBT-API wasn't initialized properly, disabling the plugin");
//            getServer().getPluginManager().disablePlugin(this);
//            return;
//        }

        // Configuration
        this.config = new YamlFile(this, "config.yml");



        // Commands
        BukkitCommandHandler handler = BukkitCommandHandler.create(this);
        handler.register(new DuelsRoomCommand(config));
        // (Optional) Register colorful tooltips (Works on 1.13+ only) // From the wiki.
        handler.registerBrigadier();

        getLogger().info("DuelsRoom has been enabled!");
    }

    @Override
    public void onDisable() {

    }

    void registerListeners(Listener... listeners) {
        for (Listener listener : listeners)
            getServer().getPluginManager().registerEvents(listener, this);
    }

    public YamlFile config() {
        return config;
    }
}
