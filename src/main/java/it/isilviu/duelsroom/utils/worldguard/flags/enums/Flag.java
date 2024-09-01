package it.isilviu.duelsroom.utils.worldguard.flags.enums;

public enum Flag {
    DUEL_ROOM("duel-room"),
    DUEL_ROOM_SIZE("duel-room-size"),
    DUEL_ROOM_BLOCK("duel-room-block")
    ;

    final String name;

    Flag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
