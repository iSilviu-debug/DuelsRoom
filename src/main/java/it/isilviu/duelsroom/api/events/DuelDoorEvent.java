package it.isilviu.duelsroom.api.events;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DuelDoorEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Contract(pure = true)
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    /* HANDLERS */

    final ProtectedRegion region;
    final Type type;

    final List<UUID> members;

    /**
     * Called when a door is opened or closed
     * @param region WorldGuard's ProtectedRegion region
     * @param type Type of the event (OPEN or CLOSE)
     */
    public DuelDoorEvent(ProtectedRegion region, List<UUID> members, Type type) {
        this.region = region;
        this.members = members;
        this.type = type;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        OPEN,
        CLOSE
    }
}

