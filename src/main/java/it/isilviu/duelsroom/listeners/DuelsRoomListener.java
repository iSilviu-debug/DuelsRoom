package it.isilviu.duelsroom.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.flags.EnumFlag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.isilviu.duelsroom.DuelsRoom;
import it.isilviu.duelsroom.api.events.DuelDoorEvent;
import it.isilviu.duelsroom.api.events.DuelStartEvent;
import it.isilviu.duelsroom.api.events.DuelStopEvent;
import it.isilviu.duelsroom.utils.WorldEditUtils;
import it.isilviu.duelsroom.utils.config.Messages;
import it.isilviu.duelsroom.utils.config.model.YamlFile;
import it.isilviu.duelsroom.utils.map.DuelsMap;
import it.isilviu.duelsroom.utils.worldguard.flags.CustomFlag;
import it.isilviu.duelsroom.utils.worldguard.flags.enums.Flag;
import it.isilviu.duelsroom.utils.worldguard.module.WorldGuardModule;
import it.isilviu.duelsroom.utils.worldguard.module.events.RegionEnteredEvent;
import it.isilviu.duelsroom.utils.worldguard.module.events.RegionLeftEvent;
import it.isilviu.duelsroom.utils.worldguard.module.model.Entry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class DuelsRoomListener implements Listener {

    final HashMap<String, List<BlockVector3>> inFight;
    final YamlFile config;
    final CustomFlag customFlag;

    final WorldGuardModule module;

    public DuelsRoomListener(YamlFile config, CustomFlag customFlag, WorldGuardModule module, DuelsMap map) {
        this.config = config;
        this.customFlag = customFlag;
        this.module = module;
        this.inFight = map;

        Bukkit.getScheduler().runTaskTimer(DuelsRoom.instance(), () -> {
            Maps.newHashMap(queueBreak).forEach((region, task) -> {
                if (task.isCancelled())
                    return;

                ProtectedRegion protectedRegion = regions.get(region);
                if (protectedRegion == null)
                    return;

                AtomicInteger count = queueBreakCount.get(region);
                if (count == null || count.get() <= 0)
                    return;

                List<UUID> members = Entry.factory.regionPlayers.get(region);
                if (members == null)
                    return;

                for (UUID uuid : members) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null)
                        continue;

                    Component actionbar = Messages.getMessage("messages.doors.actionbar.open_soon", "time", String.valueOf(count.decrementAndGet()));
                    if (TextComponent.IS_NOT_EMPTY.test(actionbar))
                        player.sendActionBar(actionbar);

                    Component chat = Messages.getMessage("messages.doors.chat.open_soon", "time", String.valueOf(count.get()));
                    if (TextComponent.IS_NOT_EMPTY.test(chat))
                        player.sendMessage(chat);
                }
            });
        }, 20L, 20L);
    }

    final HashMap<String, BukkitTask> queuePlace = new HashMap<>();
    final HashMap<String, BukkitTask> queueBreak = new HashMap<>();

    final HashMap<String, ProtectedRegion> regions = new HashMap<>(); // This works with queueBreak, so I can get it from the map instead of calculate it.
    final HashMap<String, AtomicInteger> queueBreakCount = new HashMap<>();

    @EventHandler
    public void onJoin(RegionEnteredEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (player == null) return;

        // Check if is duel-room
        StateFlag flag = customFlag.getFlag(Flag.DUEL_ROOM);
        IntegerFlag sizeFlag = customFlag.getFlag(Flag.DUEL_ROOM_SIZE);
        IntegerFlag thicknessFlag = customFlag.getFlag(Flag.DUEL_ROOM_THICKNESS);
        EnumFlag<Material> enumFlag = customFlag.getFlag(Flag.DUEL_ROOM_BLOCK);

        ProtectedRegion region = event.getRegion();
        if (region.getFlag(flag) != StateFlag.State.ALLOW) return;

        Integer integerFlag = region.getFlag(sizeFlag);
        if (integerFlag == null) integerFlag = 2; // Default value.
        int membersSize = integerFlag;

        Integer borderFlag = region.getFlag(thicknessFlag);
        if (borderFlag == null) borderFlag = 1; // Default value.
        int borderSize = borderFlag;

        Material material = region.getFlag(enumFlag);
        if (material == null) material = Material.GLASS; // Default value.

        List<Material> materialList = config.getMaterialList("door.replace");
        if (materialList.isEmpty())
            materialList = List.of(Material.AIR, Material.LIGHT);

        // Check members.
        List<UUID> members = event.getMembers();
        if (members.size() + 1 != membersSize) { // Remove if: a player join after (integerFlag + 1), or players are < integerFlag, or if he is alone (0).
            List<BlockVector3> fights = inFight.remove(region.getId());
            if (fights != null) {
                try {
                    // Remove blocks.
                    EditSession editSession = WorldEditUtils.placeGlassBlocks(event.getPlayer().getWorld(), Material.AIR, List.of(material), fights);
                    editSession.commit();
                } catch (MaxChangedBlocksException e) {
                    throw new RuntimeException(e);
                }
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
            regions.remove(region.getId()); // Remove it from the map if present.
            queueBreakCount.remove(region.getId()); // Remove it from the map if present.
            return; // This is already placed, so we don't need to break it because we need it.
        }

        // Do the barrier zone.
        final BlockVector3 min = region.getMinimumPoint();
        final BlockVector3 max = region.getMaximumPoint();
        final List<BlockVector2> points = region.getPoints(); // Referred even with the maxPoint and minPoint. (If Cuboid)

        List<BlockVector3> glassBlocks = WorldEditUtils.generateGlassPerimeter(points, min.getY(), max.getY(), borderSize);

        Material finalMaterial = material; // Bleh, final variable.
        List<Material> finalMaterialList = materialList; // Bleh, final variable.
        BukkitTask task = Bukkit.getScheduler().runTaskLater(DuelsRoom.instance(), () -> {
            this.queuePlace.remove(region.getId()); // Remove it from the queue.
            if (members.size() != membersSize) return; // Double-Check. // The members.size will automatically +1.

            // Call the event. // TODO: Change material from the event.
            Bukkit.getPluginManager().callEvent(new DuelDoorEvent(region, members, DuelDoorEvent.Type.CLOSE));

            try {
                EditSession editSession = WorldEditUtils.placeGlassBlocks(event.getLocation().getWorld(),
                        finalMaterial,
                        finalMaterialList,
                        glassBlocks);
                editSession.close();
            } catch (MaxChangedBlocksException e) {
                throw new RuntimeException(e);
            }

            // Put the region (and not the players) in the fight.
            this.inFight.put(region.getId(), glassBlocks);

            // Call the event.
            Bukkit.getPluginManager().callEvent(new DuelStartEvent(members));
        }, (long) (20L * config.getDouble("door.place", 0.5)));
        this.queuePlace.put(region.getId(), task);
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

            UUID random = members.get(members.size() - 1); // Get a random player.
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
            regions.remove(region.getId()); // Remove it from the map.
            queueBreakCount.remove(region.getId()); // Remove it from the map.

            List<BlockVector3> fights = inFight.remove(region.getId());
            if (fights == null) return;

            try {
                EditSession editSession = WorldEditUtils.placeGlassBlocks(event.getLocation().getWorld(), Material.AIR, List.of(finalMaterial), fights);
                editSession.commit();
            } catch (MaxChangedBlocksException e) {
                throw new RuntimeException(e);
            }

            // Call the event.
            Bukkit.getPluginManager().callEvent(new DuelDoorEvent(region, event.getMembers(), DuelDoorEvent.Type.OPEN));
        }, (long) (20L * config.getDouble("door.break", 0)));
        this.queueBreak.put(region.getId(), task);
        this.regions.put(region.getId(), region); // Save the region to get it from the map.
        this.queueBreakCount.put(region.getId(), new AtomicInteger((int) config.getDouble("door.break", 0))); // Save the region to get it from the map.

        // Call the event.
        Bukkit.getPluginManager().callEvent(new DuelStopEvent(event.getMembers(), player, event.getCause() == RegionLeftEvent.Cause.DEATH
                ? DuelStopEvent.Type.END_DIED : DuelStopEvent.Type.END_LEFT));

        // This mf, I don't know why EditSession from the map, can't undo.
//        try (EditSession editSession = inFight.get(region.getId())) {
//            editSession.undo(editSession);
//        }

    }

    // Bypass combat for some plugin.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCombat(EntityDamageByEntityEvent event) {
        if (!event.isCancelled()) return;

        boolean enable = config.getBoolean("combat.bypass_pvp", false);
        if (!enable) return;

        Player victim = event.getEntity() instanceof Player ? (Player) event.getEntity() : null;
        if (victim == null) return;

        Player damager = event.getDamager() instanceof Player ? (Player) event.getDamager()
                : event.getDamager() instanceof Projectile projectile ? (Player) projectile.getShooter() : null;
        if (damager == null) return;

        // Check if is duel-room
        Set<ProtectedRegion> region = module.getRegions(damager.getLocation());
        Set<ProtectedRegion> region2 = module.getRegions(victim.getLocation());
        if (region.isEmpty() || region2.isEmpty()) return;

        // Check if the region is in the fight.
        for (ProtectedRegion protectedRegion : region) {
            if (inFight.containsKey(protectedRegion.getId()) && region2.contains(protectedRegion)) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
