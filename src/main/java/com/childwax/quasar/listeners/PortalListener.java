package com.childwax.quasar.listeners;

import com.childwax.quasar.Quasar;
import com.childwax.quasar.registry.PortalRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalListener {
    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 3000;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(PortalListener::tick);
    }

    private static void tick(MinecraftServer server) {
        long now = System.currentTimeMillis();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();

            if (cooldowns.containsKey(uuid) && now - cooldowns.get(uuid) < COOLDOWN_MS) continue;

            String portalId = PortalRegistry.findPortalIntersecting(player.getBoundingBox());
            if (portalId == null) continue;

            PortalRegistry.Entry entry = PortalRegistry.get(portalId);
            if (entry == null) continue;

            cooldowns.put(uuid, now);
            Quasar.sendToServer(player, entry.server());
        }
    }
}