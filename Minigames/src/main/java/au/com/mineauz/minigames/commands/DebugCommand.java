package au.com.mineauz.minigames.commands;

import au.com.mineauz.minigames.Minigames;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kitteh.pastegg.*;
import org.kitteh.pastegg.client.FormatCodec;
import org.kitteh.pastegg.reply.IReply;
import org.kitteh.pastegg.reply.ReplyStatus;
import org.kitteh.pastegg.reply.SuccessReply;
import org.kitteh.pastegg.reply.content.PasteResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/*
 * This Command remains untranslated,
 * since I'm lazy and just used if something goes wrong.
 * Whoever reads this please finish my work here!
 */
public class DebugCommand extends ACommand {

    @Override
    public @NotNull String getName() {
        return "debug";
    }

    @Override
    public boolean canBeConsole() {
        return true;
    }

    @Override
    public @NotNull Component getDescription() {
        return Component.text("Debugs stuff.");
    }

    @Override
    public @NotNull Component getUsage() {
        return Component.text("/minigame debug");
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.debug";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull String @NotNull [] args) {
        if (args.length > 0) {
            switch (args[0].toUpperCase()) { //todo
                case "ON" -> {
                    if (Minigames.getPlugin().isDebugging()) {
                        sender.sendMessage(ChatColor.GRAY + "Debug mode already active.");
                    } else {
                        Minigames.getPlugin().toggleDebug();
                        sender.sendMessage(ChatColor.GRAY + "Debug mode active.");
                    }
                }
                case "OFF" -> {
                    if (!Minigames.getPlugin().isDebugging()) {
                        sender.sendMessage(ChatColor.GRAY + "Debug mode already inactive.");
                    } else {
                        Minigames.getPlugin().toggleDebug();
                        sender.sendMessage(ChatColor.GRAY + "Debug mode inactive.");
                    }
                }
                case "PASTE" -> {
                    sender.sendMessage(ChatColor.GRAY + "Generating a paste.....");
                    generatePaste(sender);
                }
                default -> {
                    return false;
                }
            }
        } else {
            Minigames.getPlugin().toggleDebug();

            if (Minigames.getPlugin().isDebugging())
                sender.sendMessage(ChatColor.GRAY + "Debug mode active.");
            else
                sender.sendMessage(ChatColor.GRAY + "Deactivated debug mode.");
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender,
                                                         @NotNull String @NotNull [] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 0) {
            out.add("NO");
            out.add("YES");
            out.add("PASTE");
        }
        return out;
    }

    private @NotNull String getFile(@NotNull Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return ExceptionUtils.getStackTrace(e);
        }
    }

    private void generatePaste(@NotNull CommandSender sender) {
        StringBuilder mainInfo = new StringBuilder();
        mainInfo.append(Bukkit.getName()).append(" version: ").append(Bukkit.getServer().getVersion()).append('\n');
        mainInfo.append("Plugin version: ").append(Minigames.getPlugin().getDescription().getVersion()).append('\n');
        mainInfo.append("Java version: ").append(System.getProperty("java.version")).append('\n');
        mainInfo.append('\n');
        mainInfo.append("Plugins:\n");
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            mainInfo.append(' ').append(plugin.getName()).append(" - ").append(plugin.getDescription().getVersion()).append('\n');
            mainInfo.append("  ").append(plugin.getDescription().getAuthors()).append('\n');
        }

        Bukkit.getScheduler().runTaskAsynchronously(Minigames.getPlugin(), () -> {
            Path dataPath = Minigames.getPlugin().getDataFolder().toPath();

            String apiKey = Minigames.getPlugin().getConfig().getString("pasteApiKey", null);

            PasteFile mainInfoFile = new PasteFile("mainInfo.txt", FormatCodec.TEXT_TO_TEXT.encode(mainInfo.toString()));

            PasteFile config = new PasteFile("config.yml",
                    FormatCodec.TEXT_TO_TEXT.encode(getFile(dataPath.resolve("config.yml"))),
                    HighlightLanguage.Yaml);
            PasteFile spigot = new PasteFile("spigot.yml",
                    FormatCodec.TEXT_TO_TEXT.encode(getFile(Paths.get("spigot.yml"))),
                    HighlightLanguage.Yaml);
            PasteFile startupLog = new PasteFile("startup.log",
                    FormatCodec.TEXT_TO_TEXT.encode(PLUGIN.getStartupLog()));
            PasteFile startupExceptionsLog = new PasteFile("startupExceptions.log",
                    FormatCodec.TEXT_TO_TEXT.encode(PLUGIN.getStartupExceptionLog()));

            PasteBuilder builder = new PasteBuilder();
            builder.addFile(startupLog);
            builder.addFile(startupExceptionsLog);

            try {
                IReply reply = builder
                        .setApiKey(apiKey)
                        .name("Minigames Debug Outpout")
                        .visibility(Visibility.UNLISTED)
                        .addFile(mainInfoFile)
                        .addFile(spigot)
                        .addFile(config)
                        .build();

                if (reply.status() == ReplyStatus.SUCCESS && reply instanceof SuccessReply successReply) {
                    PasteResult result = successReply.result();

                    sender.sendMessage("Debug Paste: https://paste.gg/" + result.id());
                    sender.sendMessage("Deletion Key: " + result.deletionKey());
                    Minigames.getCmpnntLogger().info("Paste:  https://paste.gg/" + result.id());
                    Minigames.getCmpnntLogger().info("Paste:  Deletion Key: " + result.deletionKey());
                } else {
                    sender.sendMessage("Paste Failed.");
                }
            } catch (InvalidPasteException | IOException e) {
                sender.sendMessage("Paste Failed" + e.getMessage());
                Minigames.getCmpnntLogger().warn("", e);
            }
        });
    }
}
