package com.childwax.quasar.listeners;

import com.childwax.quasar.Quasar;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class ServerStatusListener {
    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(Quasar::setServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(_server -> {
            Quasar.removeServer();
        });
    }
}
