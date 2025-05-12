package it.isilviu.duelsroom.hooks.listeners;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.isilviu.duelsroom.api.events.DuelStartEvent;
import it.isilviu.duelsroom.utils.map.DuelsMap;
import it.isilviu.duelsroom.utils.worldguard.module.WorldGuardModule;
import nl.marido.deluxecombat.api.DeluxeCombatAPI;
import nl.marido.deluxecombat.api.DeluxeCombatAPIUtils;
import nl.marido.deluxecombat.events.CombatStateChangeEvent;
import nl.marido.deluxecombat.events.CombatlogEvent;
import nl.marido.deluxecombat.events.EntityCombatlogEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;

public class DeluxeCombatListener implements Listener {

    final DeluxeCombatAPI deluxeCombatAPI = new DeluxeCombatAPI();

    final DuelsMap duelsMap;
    final WorldGuardModule module;

    public DeluxeCombatListener(DuelsMap duelsMap, WorldGuardModule module) {
        this.duelsMap = duelsMap;
        this.module = module;
    }

    /**
     * Force disable combat log when join in the region.
     */
    @EventHandler
    public void onDuelStart(DuelStartEvent event) {
        for (Player player : event.getMembers()) {
            if (deluxeCombatAPI.isInCombat(player))
                deluxeCombatAPI.untag(player);
        }
    }

}
