package me.kmathers.createtorquerails;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.kmathers.createtorquerails.TorqueRailsCommon;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

public final class TorqueRailsCommands {

    private static final int OP_LEVEL = 2;

    public static final PermissionNode<Boolean> TOGGLE = new PermissionNode<>(CreateTorqueRails.MODID, "toggle", PermissionTypes.BOOLEAN, (player, uuid, context) -> player != null && player.hasPermissions(OP_LEVEL));

    private TorqueRailsCommands() {
    }

    @SubscribeEvent
    public static void registerPermissions(PermissionGatherEvent.Nodes event) {
        event.addNodes(TOGGLE);
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralCommandNode<CommandSourceStack> root = dispatcher.register(Commands.literal(CreateTorqueRails.MODID)
                .requires(source -> hasPermission(source, TOGGLE))
                .then(Commands.literal("toggle")
                        .requires(source -> hasPermission(source, TOGGLE))
                        .executes(TorqueRailsCommands::toggle)));
        dispatcher.register(Commands.literal("ctr").requires(root.getRequirement()).redirect(root));
    }

    private static int toggle(CommandContext<CommandSourceStack> context) {
        TorqueRailsCommon.toggleBypassEngine();
        if (!TorqueRailsCommon.getBypassEngine()) {
            context.getSource().sendSuccess(() -> Component.translatable("createtorquerails.command.toggle.on"), true);
        } else {
            context.getSource().sendSuccess(() -> Component.translatable("createtorquerails.command.toggle.off"), true);
        }
        return 1;
    }

    private static boolean hasPermission(CommandSourceStack source, PermissionNode<Boolean> node) {
        ServerPlayer player = source.getPlayer();
        return player != null ? PermissionAPI.getPermission(player, node) : source.hasPermission(OP_LEVEL);
    }
}
