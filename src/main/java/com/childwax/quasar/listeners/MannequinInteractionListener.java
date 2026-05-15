package com.childwax.quasar.listeners;

import com.childwax.quasar.Quasar;
import com.childwax.quasar.registry.MannequinRegistry;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public class MannequinInteractionListener {
    public static void register() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) return null;
            if (hand != InteractionHand.MAIN_HAND) return null;

            if (MannequinRegistry.get(entity.getUUID()) != null) {
                String server = MannequinRegistry.getServerName(entity.getUUID());
                Quasar.sendToServer(serverPlayer, server);
            }
            return null;
        });
    }
}

