package com.childwax.quasar.listeners;

import com.childwax.quasar.config.Config;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;


public class ServerConnectionListener {
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Config config = Config.load();
            ServerPlayer player = handler.getPlayer();

            switch (config.joinBehaviour) {
                case WORLD_SPAWN -> {
                    teleportToWorldSpawn(player);
                }
                case PLAYER_SPAWN -> {
                    assert player.getRespawnConfig() != null;
                    BlockPos spawn = player.getRespawnConfig().respawnData().pos();
                    ServerLevel spawnLevel = server.getLevel(player.getRespawnConfig().respawnData().dimension());
                    if (spawnLevel != null) {
                        player.teleportTo(
                                spawnLevel,
                                spawn.getX() + 0.5,
                                spawn.getY(),
                                spawn.getZ() + 0.5,
                                Set.of(),
                                player.getYRot(),
                                player.getXRot(),
                                false
                        );
                    } else {
                        teleportToWorldSpawn(player);
                    }
                }
                case NONE -> {}
            }
        });
    }

    private static void teleportToWorldSpawn(ServerPlayer player) {
        BlockPos spawn = player.level().getRespawnData().pos();
        float yaw = player.level().getRespawnData().yaw();
        player.teleportTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
    }
}