package it.isilviu.duelsroom.utils.worldguard.module.model;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import it.isilviu.duelsroom.utils.worldguard.module.events.*;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.*;


/**
 * @author Weby &amp; Anrza (info@raidstone.net)
 * @version 1.0.0
 * @since 3/3/19
 */
public class Entry extends Handler implements Listener {

    public final PluginManager pm = Bukkit.getPluginManager();
    public static final Factory factory = new Factory();

    public static class Factory extends Handler.Factory<Entry> {

        final public HashMap<String, List<UUID>> regionPlayers = new HashMap<>();

        @Override
        public Entry create(Session session) {
            return new Entry(session);
        }
    }

    public Entry(Session session) {
        super(session);
    }

    @Override
    public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet unused, Set<ProtectedRegion> entered, Set<ProtectedRegion> left, MoveType unused2)
    {
        final HashMap<String, List<UUID>> regionPlayers = factory.regionPlayers;

        RegionsChangedEvent rce = new RegionsChangedEvent(player.getUniqueId(), left, entered);
        pm.callEvent(rce);
        if(rce.isCancelled()) return false;

        RegionsEnteredEvent ree = new RegionsEnteredEvent(player.getUniqueId(), entered);
        pm.callEvent(ree);
        if(ree.isCancelled()) return false;

        RegionsLeftEvent rle = new RegionsLeftEvent(player.getUniqueId(), left);
        pm.callEvent(rle);
        if(rle.isCancelled()) return false;

        for(ProtectedRegion r : entered) {
            List<UUID> members = regionPlayers.computeIfAbsent(r.getId(), k -> Lists.newArrayList());

            RegionEnteredEvent regentered = new RegionEnteredEvent(player.getUniqueId(), r, members, BukkitAdapter.adapt(player.getLocation()));
            pm.callEvent(regentered);
            if(regentered.isCancelled()) return false;

            regionPlayers.compute(r.getId(), (protectedRegion, uuids) -> {
                if(uuids == null) {
                    uuids = new java.util.ArrayList<>();
                }
                uuids.add(player.getUniqueId());
                return uuids;
            });
        }

        for(ProtectedRegion r : left) {
            List<UUID> members = regionPlayers.computeIfAbsent(r.getId(), k -> Lists.newArrayList());

            RegionLeftEvent regleft = new RegionLeftEvent(player.getUniqueId(), r, members, BukkitAdapter.adapt(player.getLocation()));
            pm.callEvent(regleft);
            if(regleft.isCancelled()) return false;

            regionPlayers.compute(r.getId(), (protectedRegion, uuids) -> {
                if(uuids == null) { // Can null when reload something's.
                    uuids = new java.util.ArrayList<>();
                }
                uuids.remove(player.getUniqueId());
                return uuids;
            });
        }
        return true;
    }





}
