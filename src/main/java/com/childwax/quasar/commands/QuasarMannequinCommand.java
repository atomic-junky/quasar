package com.childwax.quasar.commands;

import com.childwax.quasar.Quasar;
import com.childwax.quasar.registry.MannequinRegistry;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;

public class QuasarMannequinCommand {
    private static final SuggestionProvider<CommandSourceStack> MANNEQUIN_SUGGESTIONS = (ctx, builder) -> {
        MannequinRegistry.getAll().forEach((uuidStr, entry) -> {
            Entity entity = ctx.getSource().getLevel().getEntity(UUID.fromString(uuidStr));
            String label = entity != null
                    ? uuidStr + " (" + entry.server() + " @ " + (int)entity.getX() + " " + (int)entity.getY() + " " + (int)entity.getZ() + ")"
                    : uuidStr + " (unloaded)";
            builder.suggest(uuidStr, Component.literal(label));
        });
        return builder.buildFuture();
    };

    public static void register(LiteralArgumentBuilder<CommandSourceStack> parent, CommandBuildContext buildContext) {
        MannequinRegistry.load();

        parent.then(Commands.literal("mannequin")

                .then(Commands.literal("summon")
                        .then(Commands.argument("name", ComponentArgument.textComponent(buildContext))
                                .then(Commands.argument("server", StringArgumentType.word())
                                        .executes(ctx -> summon(
                                                ctx.getSource(),
                                                ComponentArgument.getRawComponent(ctx, "name"),
                                                StringArgumentType.getString(ctx, "server"),
                                                ctx.getSource().getPosition(),
                                                new CompoundTag()
                                        ))
                                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                                .executes(ctx -> summon(
                                                        ctx.getSource(),
                                                        ComponentArgument.getRawComponent(ctx, "name"),
                                                        StringArgumentType.getString(ctx, "server"),
                                                        Vec3Argument.getVec3(ctx, "pos"),
                                                        new CompoundTag()
                                                ))
                                                .then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
                                                        .executes(ctx -> summon(
                                                                ctx.getSource(),
                                                                ComponentArgument.getRawComponent(ctx, "name"),
                                                                StringArgumentType.getString(ctx, "server"),
                                                                Vec3Argument.getVec3(ctx, "pos"),
                                                                CompoundTagArgument.getCompoundTag(ctx, "nbt")
                                                        ))
                                                )
                                        )
                                )
                        )
                )

                .then(Commands.literal("list")
                        .executes(ctx -> list(ctx.getSource()))
                )

                .then(Commands.literal("kill")
                        .then(Commands.argument("target", StringArgumentType.string())
                                .suggests(MANNEQUIN_SUGGESTIONS)
                                .executes(ctx -> kill(
                                        ctx.getSource(),
                                        resolveMannequin(ctx.getSource(), StringArgumentType.getString(ctx, "target"))
                                ))
                        )
                )

                .then(Commands.literal("info")
                        .then(Commands.argument("target", StringArgumentType.string())
                                .suggests(MANNEQUIN_SUGGESTIONS)
                                .executes(ctx -> info(
                                        ctx.getSource(),
                                        resolveMannequin(ctx.getSource(), StringArgumentType.getString(ctx, "target"))
                                ))
                        )
                )

                .then(Commands.literal("edit")
                        .then(Commands.argument("target", StringArgumentType.string())
                                .suggests(MANNEQUIN_SUGGESTIONS)

                                .then(Commands.literal("server")
                                        .then(Commands.argument("server", StringArgumentType.word())
                                                .executes(ctx -> editServer(
                                                        ctx.getSource(),
                                                        resolveMannequin(ctx.getSource(), StringArgumentType.getString(ctx, "target")),
                                                        StringArgumentType.getString(ctx, "server")
                                                ))
                                        )
                                )
                                .then(Commands.literal("name")
                                        .then(Commands.argument("name", ComponentArgument.textComponent(buildContext))
                                                .executes(ctx -> editName(
                                                        ctx.getSource(),
                                                        resolveMannequin(ctx.getSource(), StringArgumentType.getString(ctx, "target")),
                                                        ComponentArgument.getRawComponent(ctx, "name")
                                                ))
                                        )
                                )
                                .then(Commands.literal("pos")
                                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                                .executes(ctx -> editPos(
                                                        ctx.getSource(),
                                                        resolveMannequin(ctx.getSource(), StringArgumentType.getString(ctx, "target")),
                                                        Vec3Argument.getVec3(ctx, "pos")
                                                ))
                                        )
                                )
                                .then(Commands.literal("yaw")
                                        .then(Commands.argument("degrees", FloatArgumentType.floatArg(-180f, 180f))
                                                .executes(ctx -> editYaw(
                                                        ctx.getSource(),
                                                        resolveMannequin(ctx.getSource(), StringArgumentType.getString(ctx, "target")),
                                                        FloatArgumentType.getFloat(ctx, "degrees")
                                                ))
                                        )
                                )
                                .then(Commands.literal("pose")
                                        .then(Commands.literal(""))
                                )
                        )
                )
            );

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (level.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

            String serverName = MannequinRegistry.get(entity.getUUID());
            if (serverName != null) {
                Quasar.sendToServer((ServerPlayer) player, serverName);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
    }

    private static int summon(CommandSourceStack source, Component name, String server, Vec3 pos, CompoundTag nbt) throws CommandSyntaxException {
        try {
            nbt.putBoolean("Invulnerable", true);
            nbt.putBoolean("immovable", true);

            Holder.Reference<EntityType<?>> holder = EntityType.MANNEQUIN.builtInRegistryHolder();
            Entity entity = SummonCommand.createEntity(source, holder, pos, nbt, true);

            if (entity == null) {
                source.sendFailure(Component.literal("Failed to spawn mannequin"));
                return 0;
            }

            Display.TextDisplay textDisplay = new Display.TextDisplay(EntityType.TEXT_DISPLAY, source.getLevel());
            textDisplay.setPos(pos.x, pos.y + 2.2, pos.z);
            textDisplay.setText(name);
            textDisplay.setBillboardConstraints(Display.BillboardConstraints.VERTICAL);
            source.getLevel().addFreshEntity(textDisplay);

            MannequinRegistry.register(entity.getUUID(), server, textDisplay.getUUID());
            sendInfo(source, entity.getUUID());
            return 1;

        } catch (Exception e) {
            source.sendFailure(Component.literal("Error: " + e.getClass().getSimpleName() + " - " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int kill(CommandSourceStack source, Entity entity) {
        if (MannequinRegistry.get(entity.getUUID()) == null) {
            source.sendFailure(notAMannequin());
            return 0;
        }

        ServerLevel level = source.getLevel();
        UUID tdUUID = MannequinRegistry.getTextDisplay(entity.getUUID());
        if (tdUUID != null) {
            Entity td = level.getEntity(tdUUID);
            if (td != null) td.discard();
        }

        MannequinRegistry.remove(entity.getUUID());
        entity.discard();
        source.sendSuccess(() -> Component.literal("ProxyMannequin removed"), true);
        return 1;
    }

    private static int list(CommandSourceStack source) {
        Map<String, MannequinRegistry.Entry> all = MannequinRegistry.getAll();

        if (all.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No ProxyMannequins registered"), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§6=== ProxyMannequins (" + all.size() + ") ==="), false);

        all.forEach((uuidStr, entry) -> {
            UUID uuid = UUID.fromString(uuidStr);
            Entity entity = source.getLevel().getEntity(uuid);

            String pos = entity != null
                    ? String.format("%.1f / %.1f / %.1f", entity.getX(), entity.getY(), entity.getZ())
                    : "unloaded";

            MutableComponent line = Component.literal("§7• §6" + entry.server() + " §8[" + uuidStr.substring(0, 8) + "…] §7" + pos + " ")
                    .append(Component.literal("§a[info]")
                            .withStyle(s -> s
                                    .withClickEvent(new ClickEvent.RunCommand("/proxymannequin info " + uuidStr))
                                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("View details")))
                            )
                    )
                    .append(Component.literal(" §c[kill]")
                            .withStyle(s -> s
                                    .withClickEvent(new ClickEvent.SuggestCommand("/proxymannequin kill " + uuidStr))
                                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("Remove this mannequin")))
                            )
                    );

            source.sendSuccess(() -> line, false);
        });

        return all.size();
    }

    private static int info(CommandSourceStack source, Entity entity) {
        if (MannequinRegistry.getEntry(entity.getUUID()) == null) {
            source.sendFailure(notAMannequin());
            return 0;
        }
        sendInfo(source, entity.getUUID());
        return 1;
    }

    private static void sendInfo(CommandSourceStack source, UUID uuid) {
        MannequinRegistry.Entry entry = MannequinRegistry.getEntry(uuid);
        Entity entity = source.getLevel().getEntity(uuid);
        if (entry == null || entity == null) return;

        String uuidStr = uuid.toString();
        String pos = String.format("%.2f %.2f %.2f", entity.getX(), entity.getY(), entity.getZ());
        String yaw = String.format("%.1f", entity.getYRot());

        source.sendSuccess(() -> Component.literal("§8┌── §6ProxyMannequin §8[" + uuidStr.substring(0, 8) + "…]"), false);
        source.sendSuccess(() -> buildEditableLine("Server", entry.server(), "/proxymannequin edit " + uuidStr + " server "), false);
        source.sendSuccess(() -> buildEditableLine("Position", pos, "/proxymannequin edit " + uuidStr + " pos " + pos), false);
        source.sendSuccess(() -> buildEditableLine("Yaw", yaw + "°", "/proxymannequin edit " + uuidStr + " yaw "), false);
        source.sendSuccess(() -> buildEditableLine("NBT", "{...}", "/proxymannequin edit " + uuidStr + " nbt "), false);
        source.sendSuccess(() -> Component.literal("§8└──────────────"), false);
    }

    private static Entity resolveMannequin(CommandSourceStack source, String uuidStr) throws CommandSyntaxException {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            throw EntityArgument.NO_ENTITIES_FOUND.create();
        }

        if (MannequinRegistry.getEntry(uuid) == null) {
            throw EntityArgument.NO_ENTITIES_FOUND.create();
        }

        Entity entity = source.getLevel().getEntity(uuid);
        if (entity == null) {
            throw EntityArgument.NO_ENTITIES_FOUND.create();
        }

        return entity;
    }

    private static MutableComponent buildEditableLine(String label, String value, String command) {
        return Component.literal("§8│ §7" + label + ": §f" + value + " ")
                .append(Component.literal("§e[✎]")
                        .withStyle(s -> s
                                .withClickEvent(new ClickEvent.SuggestCommand(command))
                                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Edit " + label.toLowerCase())))
                        )
                );
    }

    private static int editServer(CommandSourceStack source, Entity entity, String newServer) {
        MannequinRegistry.Entry entry = MannequinRegistry.getEntry(entity.getUUID());
        if (entry == null) { source.sendFailure(notAMannequin()); return 0; }

        MannequinRegistry.register(entity.getUUID(), newServer, MannequinRegistry.getTextDisplay(entity.getUUID()));
        source.sendSuccess(() -> Component.literal("Server updated → " + newServer), true);
        return 1;
    }

    private static int editName(CommandSourceStack source, Entity entity, Component newName) {
        UUID tdUUID = MannequinRegistry.getTextDisplay(entity.getUUID());
        if (tdUUID == null) { source.sendFailure(notAMannequin()); return 0; }

        Entity td = source.getLevel().getEntity(tdUUID);
        if (td instanceof Display.TextDisplay textDisplay) {
            textDisplay.setText(newName);
            source.sendSuccess(() -> Component.literal("Name updated"), true);
            return 1;
        }

        source.sendFailure(Component.literal("Text display not found"));
        return 0;
    }

    private static int editPos(CommandSourceStack source, Entity entity, Vec3 pos) {
        if (MannequinRegistry.get(entity.getUUID()) == null) { source.sendFailure(notAMannequin()); return 0; }

        entity.teleportTo(pos.x, pos.y, pos.z);

        UUID tdUUID = MannequinRegistry.getTextDisplay(entity.getUUID());
        if (tdUUID != null) {
            Entity td = source.getLevel().getEntity(tdUUID);
            if (td != null) td.setPos(pos.x, pos.y + 2.2, pos.z);
        }

        source.sendSuccess(() -> Component.literal(String.format("Position updated → %.2f / %.2f / %.2f", pos.x, pos.y, pos.z)), true);
        return 1;
    }

    private static int editYaw(CommandSourceStack source, Entity entity, float yaw) {
        if (MannequinRegistry.get(entity.getUUID()) == null) { source.sendFailure(notAMannequin()); return 0; }

        entity.setYRot(yaw);
        entity.setYHeadRot(yaw);
        entity.setYBodyRot(yaw + 45f);

        source.sendSuccess(() -> Component.literal("Yaw updated → " + yaw + "°"), true);
        return 1;
    }

    private static Component notAMannequin() {
        return Component.literal("This entity is not a ProxyMannequin");
    }
}