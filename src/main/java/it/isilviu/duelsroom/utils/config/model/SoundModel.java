package it.isilviu.duelsroom.utils.config.model;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public record SoundModel(Sound sound, float volume, float pitch) {

    public SoundModel(Sound sound) {
        this(sound, 1, 1);
    }

    public SoundModel(String sound) {
        this(Sound.valueOf(sound), 1, 1);
    }

    public SoundModel(String sound, float volume, float pitch) {
        this(Sound.valueOf(sound), volume, pitch);
    }

    public void play(Player player) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
