package tech.dttp.serversimplified.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import tech.dttp.serversimplified.Permissions;
import tech.dttp.serversimplified.ServerSimplified;
import net.minecraft.server.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.text.TranslatableText;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

import static tech.dttp.serversimplified.Util.isHuman;

public class StaffChatCommand {

    private static Set<String> staffChat = new HashSet<>();
    static boolean consoleInSChat = false;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager
                .literal("staffchat")
                .requires(
                        (commandSource) ->
                                ServerSimplified.getConfiguration().getPermissions().checkPermissions(commandSource, "staffchat")
                                        || commandSource.hasPermissionLevel(2))
                .then(CommandManager.argument("message", MessageArgumentType.message())
                        .executes(context -> {
                            sendToStaffChat(generateStaffChatMessage(isHuman(context.getSource()) ? context.getSource().getEntity().getDisplayName().toString() : "Console", MessageArgumentType.getMessage(context, "message").toString()), context.getSource().getMinecraftServer());
                            return 1;
                        }))
                .executes(context -> {
                    boolean added = false;
                    if (isHuman(context.getSource())) {
                        String uuid = context.getSource().getPlayer().getUuidAsString();
                        if (staffChat.contains(uuid)) {
                            staffChat.remove(uuid);
                        } else {
                            staffChat.add(uuid);
                            added = true;
                        }
                    } else {
                        consoleInSChat = !consoleInSChat;
                        added = consoleInSChat;
                    }

                    context.getSource().sendFeedback(new LiteralText(added ? "Moved into staff chat." : "Moved into global chat."), false);
                    return 1;
                });

        dispatcher.register(builder);
    }

    public static Text generateStaffChatMessage(String name, String message) {
        message = StringUtils.normalizeSpace(message);
        Text originalMessage = new TranslatableText("chat.type.text", new Object[]{name, message});
        return new LiteralText("[SC] ").append(originalMessage);
    }

    public static boolean isInStaffChat(String uuid) {
        return staffChat.contains(uuid);
    }

    public static void sendToStaffChat(Text message, MinecraftServer server) {
        server.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> {
            if (ServerSimplified.getConfiguration().getPermissions().hasPermission(serverPlayerEntity.getUuidAsString(), "staffchat")
                    || ServerSimplified.getConfiguration().getPermissions().hasPermission(serverPlayerEntity.getUuidAsString(), "staffchat.view")
                    || serverPlayerEntity.hasPermissionLevel(2)) {
                serverPlayerEntity.sendMessage(message, true);
            }
        });
    }

}