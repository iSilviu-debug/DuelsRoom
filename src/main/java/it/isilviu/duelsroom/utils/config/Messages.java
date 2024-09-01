package it.isilviu.duelsroom.utils.config;

import it.isilviu.duelsroom.DuelsRoom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Messages {

    public static Component getMessage(String path, String... strings) {
        String message = DuelsRoom.instance().config().getString(path, path);

        if (strings.length % 2 == 1)
            throw new RuntimeException("Variables parameters length invalid.");

        for (int n = 0; n < strings.length; n += 2)
            message = message.replaceAll(strings[n].contains("<") && strings[n].contains(">")
                            ? strings[n]
                            : "<" + strings[n] + ">",
                    strings[n + 1]);

        return MiniMessage.miniMessage().deserialize(message);
    }
}
