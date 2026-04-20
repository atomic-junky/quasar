package com.childwax.quasar;

import com.childwax.quasar.commands.QuasarCommands;
import com.childwax.quasar.commands.ServerSwitchingCommand;
import com.childwax.quasar.listeners.PortalListener;
import com.childwax.quasar.listeners.ServerConnectionListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class Quasar implements ModInitializer {
    public static final String MOD_ID = "quasar";

    @Override
    public void onInitialize() {
        ServerSwitchingCommand.register();
        QuasarCommands.register();
        PortalListener.register();
        ServerConnectionListener.register();
        PayloadTypeRegistry.clientboundPlay().register(QuasarPacket.PACKET_ID, QuasarPacket.codec);
    }

    public static void sendToServer(ServerPlayer player, String serverName) {
        ServerPlayNetworking.send(player, new QuasarPacket("Connect", serverName));
    }
}
