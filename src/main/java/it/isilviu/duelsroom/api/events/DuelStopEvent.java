package it.isilviu.duelsroom.api.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DuelStopEvent extends Event {
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
    final Player loser;
    final Type type;

    /**
     * DuelStopEvent
     * When a duel ends, this event is called.
     * The duel can end for two reasons:
     * - END: The duel ends normally (died, teleport, etc.)
     * - INTRUSION: Another Player has entered the duel
     * @param members List of Players in the duel (region)
     * @param loser Player who lost the duel
     * @param type Type of the end
     */
    public DuelStopEvent(List<UUID> members, Player loser, Type type) {
        this.members = members.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
        this.loser = loser;
        this.type = type;
    }

    @NotNull
    public List<Player> getMembers() {
        return members;
    }

    public Player getLoser() {
        return loser;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        END,
        INTRUSION
    }
}
