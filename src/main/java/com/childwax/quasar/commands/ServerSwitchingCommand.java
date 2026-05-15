package com.childwax.quasar.commands;

import com.childwax.quasar.Quasar;
import com.childwax.quasar.config.Config;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public class ServerSwitchingCommand {

    public static void register() {
        Config config = Config.load();

        for (Map.Entry<String, String> command : config.commands.entrySet()) {
            registerCommand(command.getKey(), command.getValue());
        }

    }

    private static void registerCommand(String commandName, String serverName) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    Commands.literal(commandName)
                            .executes(ctx -> {
                                ServerPlayer player = ctx.getSource().getPlayer();
                                Quasar.sendToServer(player, serverName);
                                return 1;
                            })
            );
        });
    }
}