package it.isilviu.duelsroom.listeners;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.flags.EnumFlag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.isilviu.duelsroom.DuelsRoom;
import it.isilviu.duelsroom.api.events.DuelStartEvent;
import it.isilviu.duelsroom.api.events.DuelStopEvent;
import it.isilviu.duelsroom.utils.WorldEditUtils;
import it.isilviu.duelsroom.utils.config.model.YamlFile;
import it.isilviu.duelsroom.utils.worldguard.flags.CustomFlag;
import it.isilviu.duelsroom.utils.worldguard.flags.enums.Flag;
import it.isilviu.duelsroom.utils.worldguard.module.events.RegionEnteredEvent;
import it.isilviu.duelsroom.utils.worldguard.module.events.RegionLeftEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DuelsRoomListener implements Listener {

    final YamlFile config;
    final CustomFlag customFlag;

    public DuelsRoomListener(YamlFile config, CustomFlag customFlag) {
        this.config = config;
        this.customFlag = customFlag;
    }

    final public HashMap<String, List<BlockVector3>> inFight = new HashMap<>();
    final HashMap<String, BukkitTask> queuePlace = new HashMap<>();
    final HashMap<String, BukkitTask> queueBreak = new HashMap<>();

    @EventHandler
    public void onJoin(RegionEnteredEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (player == null) return;

        // Check if is duel-room
        StateFlag flag = customFlag.getFlag(Flag.DUEL_ROOM);
        IntegerFlag sizeFlag = customFlag.getFlag(Flag.DUEL_ROOM_SIZE);
        EnumFlag<Material> enumFlag = customFlag.getFlag(Flag.DUEL_ROOM_BLOCK);

        ProtectedRegion region = event.getRegion();
        if (region.getFlag(flag) != StateFlag.State.ALLOW) return;

        Integer integerFlag = region.getFlag(sizeFlag);
        if (integerFlag == null) integerFlag = 2; // Default value.
        int membersSize = integerFlag;

        Material material = region.getFlag(enumFlag);
        if (material == null) material = Material.GLASS; // Default value.

        // Check members.
        List<UUID> members = event.getMembers();
        if (members.size() + 1 != membersSize) { // Remove if: a player join after (integerFlag + 1), or players are < integerFlag, or if he is alone (0).
            List<BlockVector3> fights = inFight.remove(region.getId());
            if (fights != null) {
                // Remove blocks.
                EditSession editSession = WorldEditUtils.placeGlassBlocks(event.getPlayer().getWorld(), Material.AIR, material, fights);
                editSession.flushQueue();
            }

            // Check Queue Place
            if (queuePlace.containsKey(region.getId())) { // Can cause if 3 players join in the same time. So we don't need to place the glass blocks.
                BukkitTask task = queuePlace.remove(region.getId());
                task.cancel();
            }

            // Call the event.
            if (fights != null) // Call only if the fight has begun.
                Bukkit.getPluginManager().callEvent(new DuelStopEvent(members, player, DuelStopEvent.Type.INTRUSION));
            return;
        }

        // Check Queue Place
        if (queuePlace.containsKey(region.getId())) {
            return; // We are going to place it already, so we don't need to do it again.
        }

        // Remove Break Queue
        if (queueBreak.containsKey(region.getId())) {
            BukkitTask task = queueBreak.remove(region.getId());
            task.cancel();
            return; // This is already placed, so we don't need to break it because we need it.
        }

        // Do the barrier zone.
        final BlockVector3 min = region.getMinimumPoint();
        final BlockVector3 max = region.getMaximumPoint();
        final List<BlockVector2> points = region.getPoints(); // Referred even with the maxPoint and minPoint. (If Cuboid)

        List<BlockVector3> glassBlocks = WorldEditUtils.generateGlassPerimeter(points, min.getY(), max.getY());

        try {
            Material finalMaterial = material; // Bleh, final variable.
            BukkitTask task = Bukkit.getScheduler().runTaskLater(DuelsRoom.instance(), () -> {
                this.queuePlace.remove(region.getId()); // Remove it from the queue.
                if (members.size() != membersSize) return; // Double-Check. // The members.size will automatically +1.

                EditSession editSession = WorldEditUtils.placeGlassBlocks(event.getLocation().getWorld(), finalMaterial, Material.AIR, glassBlocks);
                editSession.close();

                // Put the region (and not the players) in the fight.
                this.inFight.put(region.getId(), glassBlocks);

                // Call the event.
                Bukkit.getPluginManager().callEvent(new DuelStartEvent(members));
            }, (long) (20L * config.getDouble("door.place", 0.5)));
            this.queuePlace.put(region.getId(), task);
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void onLeave(RegionLeftEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (player == null) return;

        // Check if is duel-room
        StateFlag flag = customFlag.getFlag(Flag.DUEL_ROOM);
        EnumFlag<Material> enumFlag = customFlag.getFlag(Flag.DUEL_ROOM_BLOCK);

        ProtectedRegion region = event.getRegion();
        if (region.getFlag(flag) != StateFlag.State.ALLOW) return;

        // Check if the region is in the fight.
        if (!inFight.containsKey(region.getId())) {

            // Recalculate
            IntegerFlag sizeFlag = customFlag.getFlag(Flag.DUEL_ROOM_SIZE);
            Integer integerFlag = region.getFlag(sizeFlag);
            if (integerFlag == null) integerFlag = 2; // Default value.
            int membersSize = integerFlag;

            List<UUID> members = event.getMembers();
            members.remove(player.getUniqueId()); // Exclude the player. (In Death event is already excluded)
            if (members.size() != membersSize) return;

            UUID random = members.getLast(); // Get a random player.
            assert random != null; // IMPOSSIBLE CASE.
            members.remove(random);

            // Call the event.
            Bukkit.getPluginManager().callEvent(new RegionEnteredEvent(random, region, members, player.getLocation()));
            members.add(random);
            return;
        }

        Material material = region.getFlag(enumFlag);
        if (material == null) material = Material.GLASS; // Default value.

        // Remove blocks.
        Material finalMaterial = material;
        BukkitTask task = Bukkit.getScheduler().runTaskLater(DuelsRoom.instance(), () -> {
            queueBreak.remove(region.getId()); // Remove it from the queue.

            List<BlockVector3> fights = inFight.remove(region.getId());
            if (fights == null) return;

            EditSession editSession = WorldEditUtils.placeGlassBlocks(event.getLocation().getWorld(), Material.AIR, finalMaterial, fights);
            editSession.flushQueue();
        }, (long) (20L * config.getDouble("door.break", 0)));
        this.queueBreak.put(region.getId(), task);

        // Call the event.
        Bukkit.getPluginManager().callEvent(new DuelStopEvent(event.getMembers(), player, DuelStopEvent.Type.END));

        // This mf, I don't know why EditSession from the map, can't undo.
//        try (EditSession editSession = inFight.get(region.getId())) {
//            editSession.undo(editSession);
//        }

    }
}
