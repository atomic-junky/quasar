package com.childwax.quasar;

import com.childwax.quasar.commands.QuasarCommands;
import com.childwax.quasar.commands.ServerSwitchingCommand;
import com.childwax.quasar.listeners.MannequinInteractionListener;
import com.childwax.quasar.listeners.PortalListener;
import com.childwax.quasar.listeners.ServerConnectionListener;
import com.childwax.quasar.listeners.ServerStatusListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Quasar implements ModInitializer {
    public static final String MOD_ID = "quasar";
    private static MinecraftServer server;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        PortalListener.register();
        ServerConnectionListener.register();
        ServerStatusListener.register();
        MannequinInteractionListener.register();

        QuasarCommands.register();
        ServerSwitchingCommand.register();

        PayloadTypeRegistry.clientboundPlay().register(QuasarPacket.PACKET_ID, QuasarPacket.codec);
    }

    public static void sendToServer(ServerPlayer player, String serverName) {
        ServerPlayNetworking.send(player, new QuasarPacket("Connect", serverName));
    }

    public static void setServer(MinecraftServer minecraftServer) { server = minecraftServer; }
    public static MinecraftServer getServer() { return server; }
    public static void removeServer() { server = null; }
}
