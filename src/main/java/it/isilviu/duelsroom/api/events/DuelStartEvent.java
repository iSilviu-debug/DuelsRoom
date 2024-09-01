package it.isilviu.duelsroom.api.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DuelStartEvent extends Event {
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

    final List<Player> members;

    /**
     * DuelStartEvent
     * When a duel starts, this event is called.
     * @param members List of Players in the duel (region)
     */
    public DuelStartEvent(List<UUID> members) {
        this.members = members.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
    }

    @NotNull
    public List<Player> getMembers() {
        return members;
    }
}
