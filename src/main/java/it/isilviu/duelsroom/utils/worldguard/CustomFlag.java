package it.isilviu.duelsroom.utils.worldguard;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import it.isilviu.beans.flags.Flag;

import java.util.HashMap;

public class CustomFlag {

    private final FlagRegistry registry;
    private final HashMap<Flag, com.sk89q.worldguard.protection.flags.Flag<?>> flags = new HashMap<>();

    public CustomFlag(/*Configuration<Config> configuration*/) { // Neglecting the fact that we need WorldGuard.
        this.registry = WorldGuard.getInstance().getFlagRegistry();

        // Create flags
        flags.put(Flag.EXPLOSION_REGEN, new StateFlag(Flag.EXPLOSION_REGEN.getName(), false));
        flags.put(Flag.BLOCK_IN_HAND, new StringFlag(Flag.BLOCK_IN_HAND.getName(), ""));
        flags.put(Flag.BLOCK_REFILL, new StringFlag(Flag.BLOCK_REFILL.getName(), ""));
        flags.put(Flag.BLOCK_REFILL_WIN, new StringFlag(Flag.BLOCK_REFILL_WIN.getName(), ""));
        flags.put(Flag.ADMIN_BLOCK_PLACE, new StateFlag(Flag.ADMIN_BLOCK_PLACE.getName(), false));
        flags.put(Flag.ADMIN_BLOCK_BREAK, new StateFlag(Flag.ADMIN_BLOCK_BREAK.getName(), false));

        // Load flags and create them
        //configuration.find(Config.CONFIG)

        // Register flags
        flags.forEach((flag, stateFlag) -> registry.register(stateFlag));
    }

    /**
     * Gets flags.
     * @param flag the Flag
     * @return the StateFlag associated with the Flag
     */
    public <T extends com.sk89q.worldguard.protection.flags.Flag> T getFlag(Flag flag) {
        return (T) flags.get(flag);
    }
}
