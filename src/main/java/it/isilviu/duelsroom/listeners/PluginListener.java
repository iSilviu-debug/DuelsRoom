package it.isilviu.duelsroom.listeners;

import it.isilviu.duelsroom.api.events.DuelDoorEvent;
import it.isilviu.duelsroom.api.events.DuelStartEvent;
import it.isilviu.duelsroom.api.events.DuelStopEvent;
import it.isilviu.duelsroom.utils.config.Messages;
import it.isilviu.duelsroom.utils.config.model.SoundModel;
import it.isilviu.duelsroom.utils.config.model.YamlFile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.UUID;

public class PluginListener implements Listener {

    final YamlFile config;

    public PluginListener(YamlFile config) {
        this.config = config;
    }

    // More Stuff
    @EventHandler
    public void onDuelStartStuff(DuelStartEvent event) {
        SoundModel sound = config.getSound("sounds.start");
        if (sound != null) {
            for (Player player : event.getMembers())
                sound.play(player);
        }

        List<String> commands = config.getStringList("commands.start");
        if (commands.isEmpty()) return;

        for (Player player : event.getMembers()) {
            for (String command : commands) {
                String cmd = command.replace("<player>", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }
    }

    @EventHandler
    public void onDuelStopStuff(DuelStopEvent event) {
        if (event.getType() == DuelStopEvent.Type.INTRUSION) {
            List<String> commands = config.getStringList("commands.intrusion");
            if (!commands.isEmpty()) for (Player player : event.getMembers()) {
                for (String command : commands) {
                    String cmd = command.replace("<player>", player.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            }

            SoundModel sound = config.getSound("sounds.intrusion");
            if (sound != null) {
                for (Player player : event.getMembers())
                    sound.play(player);
            }
            return;
        }

        List<String> winnersCommands = config.getStringList(event.getType() == DuelStopEvent.Type.END_DIED ? "commands.end.winner.killed" : "commands.end.winner.escaped");
        if (!winnersCommands.isEmpty()) for (String command : winnersCommands) {
            for (Player player : event.getMembers()) {
                if (player.equals(event.getLoser())) continue;

                String cmd = command.replace("<player>", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }

        List<String> losersCommands = config.getStringList("commands.end.loser");
        if (!losersCommands.isEmpty()) for (String command : losersCommands) {
            String cmd = command.replace("<player>", event.getLoser().getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        List<String> allCommands = config.getStringList("commands.end.all");
        if (!allCommands.isEmpty()) for (String command : allCommands) {
            String playersName = event.getMembers().stream()
                    .filter(p -> !p.equals(event.getLoser()))
                    .map(Player::getName)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

            String cmd = command.replace("<loser>", event.getLoser().getName()).replace("<winner>", playersName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        SoundModel sound = config.getSound("sounds.end");
        if (sound != null) {
            for (Player player : event.getMembers())
                sound.play(player);
        }
    }

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
                    Component message =Messages.getMessage("messages.duel.intrusion", "player", event.getLoser().getName());
                    if (!TextComponent.IS_NOT_EMPTY.test(message)) break;

                    player.sendMessage(message);
                }
                break;
            case END_DIED, END_LEFT:
                Component messageLose = Messages.getMessage("messages.status.lose");
                if (TextComponent.IS_NOT_EMPTY.test(messageLose)) {
                    event.getLoser().sendMessage(messageLose);
                }

                for (Player player : players) {
                    if (player == event.getLoser()) continue;

                    Component message = Messages.getMessage("messages.status.win", "loser", event.getLoser().getName());
                    if (!TextComponent.IS_NOT_EMPTY.test(message)) break;

                    player.sendMessage(message);
                }
                break;
        }
    }

    @EventHandler
    public void onDuelDoor(DuelDoorEvent event) {
        if (event.getType() != DuelDoorEvent.Type.OPEN) return;

        List<UUID> uuids = event.getMembers();

        for (UUID uuid : uuids) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            Component actionbar = Messages.getMessage("messages.doors.actionbar.open");
            if (TextComponent.IS_NOT_EMPTY.test(actionbar))
                player.sendActionBar(actionbar);

            Component message = Messages.getMessage("messages.doors.chat.open");
            if (TextComponent.IS_NOT_EMPTY.test(message))
                player.sendMessage(message);
        }
    }
}
