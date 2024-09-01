package it.isilviu.duelsroom;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.EnumFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.isilviu.duelsroom.commands.DuelsRoomCommand;
import it.isilviu.duelsroom.listeners.CombatListener;
import it.isilviu.duelsroom.listeners.DuelsRoomListener;
import it.isilviu.duelsroom.listeners.PluginListener;
import it.isilviu.duelsroom.utils.WorldEditUtils;
import it.isilviu.duelsroom.utils.config.model.YamlFile;
import it.isilviu.duelsroom.utils.worldguard.flags.CustomFlag;
import it.isilviu.duelsroom.utils.worldguard.flags.enums.Flag;
import it.isilviu.duelsroom.utils.worldguard.module.WorldGuardModule;
import org.bstats.MetricsBase;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.util.List;

public class DuelsRoom extends JavaPlugin {

    static DuelsRoom INSTANCE;
    public static DuelsRoom instance() {
        return INSTANCE;
    }

    private DuelsRoomListener duelRoomsListener; // Really? DuelRoomsListener?
    private YamlFile config;
    private CustomFlag customFlags;

    @Override
    public void onLoad() {

        // Flags - onLoad, from the WIKI.
        this.customFlags = new CustomFlag();
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        // Configuration
        this.config = new YamlFile(this, "config.yml");

        // METRICS
        Metrics metrics = new Metrics(this, 23249);
        if (config.getBoolean("metrics", true)) { // Please, don't disable it. (every night I see the stars)
            MetricsBase metricsBase = metrics.getMetricsBase();
            if (!metricsBase.isEnabled()) metricsBase.startSubmitting();
        }

        // Modules
        new WorldGuardModule(this);

        // Listeners
        this.duelRoomsListener = new DuelsRoomListener(config, customFlags);
        registerListeners(duelRoomsListener, new CombatListener(config), new PluginListener());

        // Commands
        BukkitCommandHandler handler = BukkitCommandHandler.create(this);
        handler.register(new DuelsRoomCommand(config));
        // (Optional) Register colorful tooltips (Works on 1.13+ only) // From the wiki.
        handler.registerBrigadier();

        getLogger().info("DuelsRoom has been enabled!");
    }

    @Override
    public void onDisable() { // TODO: Optimize... Recode...
        // Lazy to create a map/system, so.
        for (World world : Bukkit.getWorlds()) {
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
            if (regionManager == null) continue;

            for (ProtectedRegion region : regionManager.getRegions().values()) {
                List<BlockVector3> blockVector3s = duelRoomsListener.inFight.get(region.getId());
                if (blockVector3s == null) continue;

                EnumFlag<Material> enumFlag = customFlags.getFlag(Flag.DUEL_ROOM_BLOCK);

                Material material = region.getFlag(enumFlag);
                if (material == null) material = Material.GLASS; // Default value.

                EditSession editSession = WorldEditUtils.placeGlassBlocks(world, Material.AIR, material, blockVector3s);
                editSession.flushQueue();
            }
        }
    }

    void registerListeners(Listener... listeners) {
        for (Listener listener : listeners)
            getServer().getPluginManager().registerEvents(listener, this);
    }

    public YamlFile config() {
        return config;
    }
}
