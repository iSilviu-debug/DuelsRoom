package it.isilviu.duelsroom.commands;

import it.isilviu.duelsroom.utils.config.Messages;
import it.isilviu.duelsroom.utils.config.model.YamlFile;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("duelsroom")
public class DuelsRoomCommand {

    final YamlFile config;

    public DuelsRoomCommand(YamlFile config) {
        this.config = config;
    }

    @Subcommand("info")
    public void onInfo(BukkitCommandActor actor) {
        actor.audience().sendMessage(Messages.getMessage("""
                <gradient:blue:yellow>Plugin <gradient:red:blue>DuelsRoom <gradient:yellow:green>v<gradient:green:blue>1.0
                <gray>Plugin developed by <aqua>silvio.top"""));
    }

    @Subcommand("reload")
    @CommandPermission("duelsroom.admin")
    public void onReload(BukkitCommandActor actor) {
        actor.audience().sendMessage(Messages.getMessage("messages.reload"));
        config.reload();
    }

}
