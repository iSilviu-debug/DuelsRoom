package it.isilviu.duelsroom.listeners;

import com.google.common.collect.Sets;
import it.isilviu.duelsroom.DuelsRoom;
import it.isilviu.duelsroom.api.events.DuelStartEvent;
import it.isilviu.duelsroom.api.events.DuelStopEvent;
import it.isilviu.duelsroom.utils.config.Messages;
import it.isilviu.duelsroom.utils.config.model.YamlFile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.DecimalFormat;
import java.util.*;

public class CombatListener implements Listener {

    final Set<UUID> inCombat = new HashSet<>() {
        @Override
        public boolean add(UUID uuid) { // Prevent Duplicate UUID
            if (inCombat.contains(uuid)) return true;
            return super.add(uuid);
        }
    };
    final HashMap<UUID, Long> inTimeCombat = new HashMap<>();
    final YamlFile config;

    public CombatListener(YamlFile config) {
        this.config = config;
        Bukkit.getScheduler().runTaskTimer(DuelsRoom.instance(), () -> {
            Sets.newHashSet(inCombat).forEach((uuid) -> {
            // Prevent ConcurrentHashSet
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) {
                    inTimeCombat.remove(uuid);
                    inCombat.remove(uuid); // IMPOSSIBLE CHECK!!!
                    return;
                }

                if (!isInCombat(player)) {
                    this.remove(player);

                    Component component = Messages.getMessage("messages.combat.actionbar.idle", "time", this.formatTime(player));
                    if (!TextComponent.IS_NOT_EMPTY.test(component)) return;

                    player.sendActionBar(component);
                    return;
                }

                Component component = Messages.getMessage("messages.combat.actionbar.combat", "time", this.formatTime(player));
                if (!TextComponent.IS_NOT_EMPTY.test(component)) return;

                player.sendActionBar(component);
            });
        }, 1L, 1L);
    }

    @EventHandler
    public void onDuelStart(DuelStartEvent event) {
        boolean onCombat = config.getBoolean("combat.enabled", true);
        if (!onCombat) return;

        List<Player> members = event.getMembers();
        for (Player member : members) {
            inCombat.add(member.getUniqueId());
        }

        boolean onDuelCombat = config.getBoolean("combat.instant", false);
        if (!onDuelCombat) return;

        for (Player member : members) {
            this.addOrUpdate(member);
            member.sendMessage(Messages.getMessage("messages.combat.start"));
        }
    }

    @EventHandler
    public void onDuelStop(DuelStopEvent event) {
        boolean onCombat = config.getBoolean("combat.enabled", true);
        if (!onCombat) return;

        List<Player> members = event.getMembers();
        for (Player member : members) {
            inCombat.remove(member.getUniqueId());
        }

        for (Player member : members) {
            if (!this.isInCombat(member)) {
                this.remove(member); // Auto Remove. (Without message)
                continue;
            }
            this.remove(member); // Remove. (With message)
            member.sendMessage(Messages.getMessage("messages.combat.end"));
        }
    }

    @EventHandler
    public void onEntityDamgeByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!(event.getEntity() instanceof Player entity)) return;

        if (damager.equals(entity)) return;

        if (!inCombat.contains(damager.getUniqueId()) || !inCombat.contains(entity.getUniqueId())) return;

        if (!isInCombat(damager)) damager.sendMessage(Messages.getMessage("messages.combat.start"));
        this.addOrUpdate(damager);

        if (!isInCombat(entity)) entity.sendMessage(Messages.getMessage("messages.combat.start"));
        this.addOrUpdate(entity);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!inCombat.contains(player.getUniqueId())) return;
        if (!isInCombat(player)) return;

        inCombat.remove(player.getUniqueId()); // Double Check. #onDuelStop()
        inTimeCombat.remove(player.getUniqueId()); // Double Check. #onDuelStop()

        player.setHealth(0.0); // Kill Player.
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("duelsroom.command.bypass")) return;
        if (!inCombat.contains(player.getUniqueId())) return;

        String command = event.getMessage().split(" ")[0].replace("/", "");
        if (config.getStringList("combat.commands").contains(command)) return;

        event.setCancelled(true);
        player.sendMessage(Messages.getMessage("messages.combat.command"));
    }

    void addOrUpdate(Player player) {
        inTimeCombat.put(player.getUniqueId(), System.currentTimeMillis());
    }

    void remove(Player player) {
        inTimeCombat.remove(player.getUniqueId());
    }

    boolean isInCombat(Player player) {
        return inTimeCombat.get(player.getUniqueId()) instanceof Long time && System.currentTimeMillis() - time <= (config.getLong("combat.duration", 10) * 1000);
    }

    final DecimalFormat format = new DecimalFormat("0.0");

    String formatTime(Player player) {
        Long time = inTimeCombat.get(player.getUniqueId());
        if (time == null) return "0.0";

        return format.format((((time + config.getLong("combat.duration", 10) * 1000) - System.currentTimeMillis()) / 1000.0));
    }
}
