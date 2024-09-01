package it.isilviu.duelsroom.utils.worldguard.module.listeners;

import com.google.common.collect.Lists;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.isilviu.duelsroom.utils.worldguard.module.WorldGuardModule;
import it.isilviu.duelsroom.utils.worldguard.module.events.RegionEnteredEvent;
import it.isilviu.duelsroom.utils.worldguard.module.events.RegionLeftEvent;
import it.isilviu.duelsroom.utils.worldguard.module.events.RegionsEnteredEvent;
import it.isilviu.duelsroom.utils.worldguard.module.events.RegionsLeftEvent;
import it.isilviu.duelsroom.utils.worldguard.module.model.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class WorldguardPlayerListener implements Listener {

    final PluginManager pm = Bukkit.getPluginManager();
    final WorldGuardModule worldGuardModule;

    public WorldguardPlayerListener(WorldGuardModule worldGuardModule) {
        this.worldGuardModule = worldGuardModule;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();

        Set<ProtectedRegion> protectedRegions = worldGuardModule.getRegions(location);
        if (protectedRegions.isEmpty()) return;

        RegionsEnteredEvent regionsEnteredEvent = new RegionsEnteredEvent(player.getUniqueId(), protectedRegions);
        pm.callEvent(regionsEnteredEvent);

        for (ProtectedRegion protectedRegion : protectedRegions) {
            List<UUID> members = Entry.factory.regionPlayers.computeIfAbsent(protectedRegion.getId(), k -> Lists.newArrayList());

            RegionEnteredEvent regionEnteredEvent = new RegionEnteredEvent(player.getUniqueId(), protectedRegion, members, location);
            pm.callEvent(regionEnteredEvent);

            members.add(player.getUniqueId());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();

        Set<ProtectedRegion> protectedRegions = worldGuardModule.getRegions(location);
        if (protectedRegions.isEmpty()) return;

        RegionsLeftEvent regionsLeftEvent = new RegionsLeftEvent(player.getUniqueId(), protectedRegions);
        pm.callEvent(regionsLeftEvent);

        for (ProtectedRegion protectedRegion : protectedRegions) {
            List<UUID> members = Entry.factory.regionPlayers.computeIfAbsent(protectedRegion.getId(), k -> Lists.newArrayList());

            RegionLeftEvent regionLeftEvent = new RegionLeftEvent(player.getUniqueId(), protectedRegion, members, player.getLocation());
            pm.callEvent(regionLeftEvent);

            members.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location location = player.getLocation();

        Set<ProtectedRegion> protectedRegions = worldGuardModule.getRegions(location);
        if (protectedRegions.isEmpty()) return;

        RegionsLeftEvent regionsLeftEvent = new RegionsLeftEvent(player.getUniqueId(), protectedRegions);
        pm.callEvent(regionsLeftEvent);

        for (ProtectedRegion protectedRegion : protectedRegions) {
            List<UUID> members = Entry.factory.regionPlayers.computeIfAbsent(protectedRegion.getId(), k -> Lists.newArrayList());

            RegionLeftEvent regionLeftEvent = new RegionLeftEvent(player.getUniqueId(), protectedRegion, members, player.getLocation());
            pm.callEvent(regionLeftEvent);

            members.remove(player.getUniqueId());
        }
    }
}
