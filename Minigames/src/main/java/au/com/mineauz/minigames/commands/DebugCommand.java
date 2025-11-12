package au.com.mineauz.minigames.commands;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import de.greensurvivors.Paste;
import de.greensurvivors.PasteContent;
import de.greensurvivors.Session;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

            if (Minigames.getPlugin().isDebugging()) {
                sender.sendMessage(ChatColor.GRAY + "Debug mode active.");
            } else {
                sender.sendMessage(ChatColor.GRAY + "Deactivated debug mode.");
            }
        }
        return true;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender,
                                                         @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return MinigameUtils.tabCompleteMatch(List.of(
                "ON",
                "OFF",
                "PASTE"
            ), args[0]);
        }

        return null;
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
        mainInfo.append("Plugin version: ").append(Minigames.getPlugin().getPluginMeta().getVersion()).append('\n');
        mainInfo.append("Java version: ").append(System.getProperty("java.version")).append('\n');
        mainInfo.append('\n');
        mainInfo.append("Plugins:\n");
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            mainInfo.append(' ').append(plugin.getName()).append(" - ").append(plugin.getPluginMeta().getVersion()).append('\n');
            mainInfo.append("  ").append(plugin.getPluginMeta().getAuthors()).append('\n');
        }

        Bukkit.getScheduler().runTaskAsynchronously(Minigames.getPlugin(), () -> {
            Path dataPath = Minigames.getPlugin().getDataFolder().toPath();

            final @Nullable String apiKey = Minigames.getPlugin().getConfig().getString("pasteApiKey", null);

            try (Session pastefySession = Session.newSession(apiKey)) {// default visibility is unlisted
                PasteContent.BundledContent bundledContent = PasteContent.newBundledContent();
                bundledContent.addContent("mainInfo.txt", PasteContent.fromString(mainInfo.toString()));
                bundledContent.addContent("config.yml", PasteContent.fromString(getFile(dataPath.resolve("config.yml"))));
                bundledContent.addContent("spigot.yml", PasteContent.fromString(getFile(Paths.get("spigot.yml"))));
                bundledContent.addContent("startup.log", PasteContent.fromString(PLUGIN.getStartupLog()));
                bundledContent.addContent("startupExceptions.log", PasteContent.fromString(PLUGIN.getStartupExceptionLog()));

                pastefySession.createPaste(Paste.newBuilder(bundledContent).setTitle("Minigames debug output")).thenAccept(pasteReplay -> {
                    sender.sendMessage("Debug Paste: https://pastefy.app/" + pasteReplay.getId());
                    Minigames.getCmpnntLogger().info("Paste:  https://pastefy.app/" + pasteReplay.getId());
                }).exceptionally(throwable -> {
                    sender.sendMessage("Paste Failed with: " + throwable.getMessage());
                    Minigames.getCmpnntLogger().warn("Couldn't create debug paste: ", throwable);

                    return null;
                });
            } catch (Exception e) {
                sender.sendMessage("Paste Failed with: " + e.getMessage());
                Minigames.getCmpnntLogger().warn("Couldn't create debug paste: ", e);
            }
        });
    }
}
