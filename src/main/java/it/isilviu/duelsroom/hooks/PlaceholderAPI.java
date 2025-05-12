package it.isilviu.duelsroom.hooks;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.isilviu.duelsroom.utils.config.model.YamlFile;
import it.isilviu.duelsroom.utils.java.FormatTimeStatic;
import it.isilviu.duelsroom.utils.map.DuelsMap;
import it.isilviu.duelsroom.utils.worldguard.module.WorldGuardModule;
import it.isilviu.duelsroom.utils.worldguard.module.model.Entry;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PlaceholderAPI extends PlaceholderExpansion {

    final YamlFile config;
    final DuelsMap duelsMap;

    final WorldGuardModule module;

    public PlaceholderAPI(YamlFile config, WorldGuardModule module, DuelsMap duelsMap) {
        this.config = config;
        this.module = module;
        this.duelsMap = duelsMap;
        register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "duelsroom";
    }

    @Override
    public @NotNull String getAuthor() {
        return "iSilviu";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.2";
    }

    /**
     * Your identifier is: duelsroom
     * We use the format: %duelsroom_<placeholder>%
     * For name a duel room we use %duelsroom_<state>:<name>%
     */
    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        String[] split = params.split(":");
        if (split.length < 2) return "Incorrect placeholder format. Use: %duelsroom_<state>:<name>%";

        String state = split[0];
        String name = split[1];

        switch (state.toLowerCase()) {
            case "status" -> { // true: in fight, false: not in fight, or not found
                return String.valueOf(duelsMap.containsKey(name));
            }
            case "members" -> {
                ProtectedRegion region = module.getRegion(player.getWorld(), name);
                if (region == null || !Entry.factory.regionPlayers.containsKey(region.getId()))
                    return config.getString("placeholders.not_found", config.getString("placeholders.not_found_members", null));

                List<UUID> members = Entry.factory.regionPlayers.get(region.getId());

                return members.stream()
                        .map(Bukkit::getPlayer).filter(Objects::nonNull)
                        .map(Player::getName)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse(config.getString("placeholders.members_empty", null));
            }
            case "members_size" -> {
                ProtectedRegion region = module.getRegion(player.getWorld(), name);
                if (region == null || !Entry.factory.regionPlayers.containsKey(region.getId()))
                    return config.getString("placeholders.not_found", config.getString("placeholders.not_found_members_size", null));

                List<UUID> members = Entry.factory.regionPlayers.get(region.getId());
                return String.valueOf(members.size());
            }
            case "time" -> {
                if (!duelsMap.containsKey(name))
                    return config.getString("placeholders.not_found", config.getString("placeholders.not_found_time", null));

                return FormatTimeStatic.formatTime(config.getString("placeholder.time_format"), System.currentTimeMillis() - duelsMap.getDuelTime(name));
            }

            default -> {
                return "Invalid placeholder format. Missing a valid state: STATUS, MEMBERS, MEMBERS_SIZE";
            }
        }
    }
}
