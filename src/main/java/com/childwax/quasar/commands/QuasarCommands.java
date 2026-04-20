package com.childwax.quasar.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class QuasarCommands {
    public static void register() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("quasar")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));


        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
            QuasarMannequinCommand.register(root, buildContext);
            QuasarPortalCommand.register(root, buildContext);
            dispatcher.register(root);
        });
    }
}
