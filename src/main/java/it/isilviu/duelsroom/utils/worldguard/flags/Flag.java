package it.isilviu.duelsroom.utils.worldguard.flags;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Flag {
    EXPLOSION_REGEN("tiktok-explosion-regen"),
    BLOCK_IN_HAND("block-in-hand"),
    BLOCK_REFILL("block-refill"),
    BLOCK_REFILL_WIN("block-refill-win"),

    ADMIN_BLOCK_BREAK("admin-block-break"),
    ADMIN_BLOCK_PLACE("admin-block-place"),
    ;

    final String name;
}
