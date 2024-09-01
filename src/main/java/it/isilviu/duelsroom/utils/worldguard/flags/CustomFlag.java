package it.isilviu.duelsroom.utils.worldguard.flags;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.EnumFlag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.flags.registry.SimpleFlagRegistry;
import it.isilviu.duelsroom.utils.worldguard.flags.enums.Flag;
import org.bukkit.Material;

import java.util.HashMap;

public class CustomFlag {

    private final FlagRegistry registry;
    private final HashMap<Flag, com.sk89q.worldguard.protection.flags.Flag<?>> flags = new HashMap<>();

    public CustomFlag(/*Configuration<Config> configuration*/) { // Neglecting the fact that we need WorldGuard.
        this.registry = WorldGuard.getInstance().getFlagRegistry();

        // Create enums
        flags.put(Flag.DUEL_ROOM, new StateFlag(Flag.DUEL_ROOM.getName(), false));
        flags.put(Flag.DUEL_ROOM_SIZE, new IntegerFlag(Flag.DUEL_ROOM_SIZE.getName()));
        flags.put(Flag.DUEL_ROOM_BLOCK, new EnumFlag<>(Flag.DUEL_ROOM_BLOCK.getName(), Material.class));

        // Load enums and create them
        //configuration.find(Config.CONFIG)

        // Register enums
        flags.forEach((flag, stateFlag) -> {
            try {
                registry.register(stateFlag);
            } catch (FlagConflictException e) {
                com.sk89q.worldguard.protection.flags.Flag<?> existing = registry.get(flag.getName());
                flags.put(flag, existing);
            }
        });
    }

    /**
     * Gets enums.
     * @param flag the Flag
     * @return the StateFlag associated with the Flag
     */
    public <T extends com.sk89q.worldguard.protection.flags.Flag> T getFlag(Flag flag) {
        return (T) flags.get(flag);
    }
}
