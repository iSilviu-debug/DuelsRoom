package it.isilviu.duelsroom.listeners;

import it.isilviu.duelsroom.api.events.DuelStartEvent;
import it.isilviu.duelsroom.api.events.DuelStopEvent;
import it.isilviu.duelsroom.utils.config.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class PluginListener implements Listener {

    // Messages part

    @EventHandler
    public void onDuelStart(DuelStartEvent event) {
        List<Player> players = event.getMembers();

        Component component = Component.empty();
        for (Player player : players) {
            Component playerComponent = Messages.getMessage("messages.format_players", "player", player.getName());
            if (!TextComponent.IS_NOT_EMPTY.test(component)) component = playerComponent;
            else component = component.append(Component.text(", ")).append(playerComponent);
        }

        Component message = Messages.getMessage("messages.duel.start", "players", MiniMessage.miniMessage().serialize(component));
        if (!TextComponent.IS_NOT_EMPTY.test(message)) return;

        for (Player player : players) {
            player.sendMessage(message);
        }
    }

    @EventHandler
    public void onDuelStop(DuelStopEvent event) {
        List<Player> players = event.getMembers();

        switch (event.getType()) {
            case INTRUSION:
                for (Player player : players) {
                    player.sendMessage(Messages.getMessage("messages.duel.intrusion", "player", event.getLoser().getName()));
                }
                break;
            case END:
                event.getLoser().sendMessage(Messages.getMessage("messages.status.lose"));

                for (Player player : players) {
                    if (player == event.getLoser()) continue;
                    player.sendMessage(Messages.getMessage("messages.status.win", "loser", event.getLoser().getName()));
                }
                break;
        }
    }
}
