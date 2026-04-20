package com.childwax.quasar.commands;

import com.childwax.quasar.registry.PortalRegistry;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class QuasarPortalCommand {

    private static final SuggestionProvider<CommandSourceStack> PORTAL_SUGGESTIONS = (ctx, builder) -> {
        PortalRegistry.getAll().forEach((id, entry) -> {
            builder.suggest(id, Component.literal(id + " → " + entry.server()));
        });
        return builder.buildFuture();
    };

    public static void register(LiteralArgumentBuilder<CommandSourceStack> parent, CommandBuildContext buildContext) {
        PortalRegistry.load();

        parent.then(Commands.literal("portal")

                .then(Commands.literal("create")
                        .then(Commands.argument("server", StringArgumentType.word())
                                .then(Commands.argument("pos1", Vec3Argument.vec3())
                                        .then(Commands.argument("pos2", Vec3Argument.vec3())
                                                .executes(ctx -> create(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "server"),
                                                        Vec3Argument.getVec3(ctx, "pos1"),
                                                        Vec3Argument.getVec3(ctx, "pos2")
                                                ))
                                        )
                                )
                                .executes(ctx -> createAtPlayer(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "server")
                                ))
                        )
                )

                .then(Commands.literal("remove")
                        .then(Commands.argument("id", StringArgumentType.string())
                                .suggests(PORTAL_SUGGESTIONS)
                                .executes(ctx -> remove(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "id")
                                ))
                        )
                )

                .then(Commands.literal("list")
                        .executes(ctx -> list(ctx.getSource()))
                )

                .then(Commands.literal("info")
                        .then(Commands.argument("id", StringArgumentType.string())
                                .suggests(PORTAL_SUGGESTIONS)
                                .executes(ctx -> info(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "id")
                                ))
                        )
                )

                .then(Commands.literal("edit")
                        .then(Commands.argument("id", StringArgumentType.string())
                                .suggests(PORTAL_SUGGESTIONS)

                                .then(Commands.literal("server")
                                        .then(Commands.argument("server", StringArgumentType.word())
                                                .executes(ctx -> editServer(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "id"),
                                                        StringArgumentType.getString(ctx, "server")
                                                ))
                                        )
                                )

                                .then(Commands.literal("pos1")
                                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                                .executes(ctx -> editPos1(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "id"),
                                                        Vec3Argument.getVec3(ctx, "pos")
                                                ))
                                        )
                                )

                                .then(Commands.literal("pos2")
                                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                                .executes(ctx -> editPos2(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "id"),
                                                        Vec3Argument.getVec3(ctx, "pos")
                                                ))
                                        )
                                )
                        )
                )
        );
    }

    private static int create(CommandSourceStack source, String server, Vec3 pos1, Vec3 pos2) {
        String id = PortalRegistry.register(server, pos1, pos2);
        sendInfo(source, id);
        return 1;
    }

    private static int createAtPlayer(CommandSourceStack source, String server) {
        Vec3 pos = source.getPosition();
        Vec3 pos1 = pos.subtract(1, 0, 1);
        Vec3 pos2 = pos.add(1, 3, 1);
        String id = PortalRegistry.register(server, pos1, pos2);
        sendInfo(source, id);
        return 1;
    }

    private static int remove(CommandSourceStack source, String id) {
        if (PortalRegistry.get(id) == null) {
            source.sendFailure(Component.literal("Portal not found: " + id));
            return 0;
        }
        PortalRegistry.remove(id);
        source.sendSuccess(() -> Component.literal("Portal removed: " + id), true);
        return 1;
    }

    private static int list(CommandSourceStack source) {
        Map<String, PortalRegistry.Entry> all = PortalRegistry.getAll();

        if (all.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No portals registered"), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§6=== Portals (" + all.size() + ") ==="), false);

        all.forEach((id, entry) -> {
            String pos1 = formatVec(entry.pos1().toVec3());
            String pos2 = formatVec(entry.pos2().toVec3());

            MutableComponent line = Component.literal("§7• §6" + entry.server() + " §8[" + id + "] §7" + pos1 + " → " + pos2 + " ")
                    .append(Component.literal("§a[info]")
                            .withStyle(s -> s
                                    .withClickEvent(new ClickEvent.RunCommand("/proxyportal info " + id))
                                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("View details")))
                            )
                    )
                    .append(Component.literal(" §c[kill]")
                            .withStyle(s -> s
                                    .withClickEvent(new ClickEvent.SuggestCommand("/proxyportal kill " + id))
                                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("Remove this portal")))
                            )
                    );

            source.sendSuccess(() -> line, false);
        });

        return all.size();
    }

    private static int info(CommandSourceStack source, String id) {
        if (PortalRegistry.get(id) == null) {
            source.sendFailure(Component.literal("Portal not found: " + id));
            return 0;
        }
        sendInfo(source, id);
        return 1;
    }

    private static void sendInfo(CommandSourceStack source, String id) {
        PortalRegistry.Entry entry = PortalRegistry.get(id);
        if (entry == null) return;

        String pos1 = formatVec(entry.pos1().toVec3());
        String pos2 = formatVec(entry.pos2().toVec3());

        source.sendSuccess(() -> Component.literal("§8┌── §6Portal §8[" + id + "]"), false);
        source.sendSuccess(() -> buildEditableLine("Server", entry.server(), "/proxyportal edit " + id + " server "), false);
        source.sendSuccess(() -> buildEditableLine("Pos1",   pos1,          "/proxyportal edit " + id + " pos1 " + pos1), false);
        source.sendSuccess(() -> buildEditableLine("Pos2",   pos2,          "/proxyportal edit " + id + " pos2 " + pos2), false);
        source.sendSuccess(() -> Component.literal("§8└──────────────"), false);
    }

    private static int editServer(CommandSourceStack source, String id, String server) {
        PortalRegistry.Entry entry = PortalRegistry.get(id);
        if (entry == null) { source.sendFailure(Component.literal("Portal not found: " + id)); return 0; }

        PortalRegistry.update(id, new PortalRegistry.Entry(server, entry.pos1(), entry.pos2()));
        source.sendSuccess(() -> Component.literal("Server updated → " + server), true);
        return 1;
    }

    private static int editPos1(CommandSourceStack source, String id, Vec3 pos) {
        PortalRegistry.Entry entry = PortalRegistry.get(id);
        if (entry == null) { source.sendFailure(Component.literal("Portal not found: " + id)); return 0; }

        PortalRegistry.update(id, new PortalRegistry.Entry(entry.server(), PortalRegistry.Vec3Data.of(pos), entry.pos2()));
        source.sendSuccess(() -> Component.literal("Pos1 updated → " + formatVec(pos)), true);
        return 1;
    }

    private static int editPos2(CommandSourceStack source, String id, Vec3 pos) {
        PortalRegistry.Entry entry = PortalRegistry.get(id);
        if (entry == null) { source.sendFailure(Component.literal("Portal not found: " + id)); return 0; }

        PortalRegistry.update(id, new PortalRegistry.Entry(entry.server(), entry.pos1(), PortalRegistry.Vec3Data.of(pos)));
        source.sendSuccess(() -> Component.literal("Pos2 updated → " + formatVec(pos)), true);
        return 1;
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

    private static String formatVec(Vec3 v) {
        return String.format("%.1f %.1f %.1f", v.x, v.y, v.z);
    }
}