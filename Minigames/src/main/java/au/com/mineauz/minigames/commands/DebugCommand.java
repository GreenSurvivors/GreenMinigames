package au.com.mineauz.minigames.commands;

import au.com.mineauz.minigames.Minigames;
import de.interaapps.pastefy.apiclient.PastefyAPI;
import de.interaapps.pastefy.apiclient.exceptions.CreationFailedException;
import de.interaapps.pastefy.apiclient.models.Folder;
import de.interaapps.pastefy.apiclient.models.Paste;
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

            // api
            // unlisted
            // with deletion key
            // text files

            final @Nullable String apiKey = Minigames.getPlugin().getConfig().getString("pasteApiKey", null);

            final PastefyAPI pastefyAPI = new PastefyAPI(apiKey);

            final Folder newFolder = new Folder();
            newFolder.setName("Minigames debug output");

            final Paste mainInfoPaste = new Paste();
            mainInfoPaste.setContent(mainInfo.toString());
            mainInfoPaste.setTitle("mainInfo.txt");

            final Paste minigamesConfigPaste = new Paste();
            minigamesConfigPaste.setContent(getFile(dataPath.resolve("config.yml")));
            minigamesConfigPaste.setTitle("config.yml");

            final Paste spigotConfigPaste = new Paste();
            spigotConfigPaste.setContent(getFile(Paths.get("spigot.yml")));
            spigotConfigPaste.setTitle("spigot.yml");

            final Paste startupLogPaste = new Paste();
            startupLogPaste.setContent(PLUGIN.getStartupLog());
            startupLogPaste.setTitle("startup.log");

            final Paste startupExceptionLogPaste = new Paste();
            startupExceptionLogPaste.setContent(PLUGIN.getStartupExceptionLog());
            startupExceptionLogPaste.setTitle("startupExceptions.log");

            try { // default visibility is unlisted
                pastefyAPI.createFolder(newFolder);

                mainInfoPaste.setFolderId(newFolder.getId());
                pastefyAPI.createPaste(mainInfoPaste);

                minigamesConfigPaste.setFolderId(newFolder.getId());
                pastefyAPI.createPaste(minigamesConfigPaste);

                spigotConfigPaste.setFolderId(newFolder.getId());
                pastefyAPI.createPaste(spigotConfigPaste);

                startupLogPaste.setFolderId(newFolder.getId());
                pastefyAPI.createPaste(startupLogPaste);

                startupExceptionLogPaste.setFolderId(newFolder.getId());
                pastefyAPI.createPaste(startupExceptionLogPaste);
            } catch (CreationFailedException e) {
                sender.sendMessage("Paste Failed with: " + e.getMessage());
                Minigames.getCmpnntLogger().warn("Couldn't create debug paste: ", e);

                return;
            }

            sender.sendMessage("Debug Paste: https://pastefy.app/folder/" + newFolder.getId());
            Minigames.getCmpnntLogger().info("Paste:  https://pastefy.app/folder/" + newFolder.getId());
        });
    }
}
