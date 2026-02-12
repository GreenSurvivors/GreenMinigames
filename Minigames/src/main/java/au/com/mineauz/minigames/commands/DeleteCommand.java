package au.com.mineauz.minigames.commands;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DeleteCommand extends ACommand {

    @Override
    public @NotNull String getName() {
        return "delete";
    }

    @Override
    public boolean canBeConsole() {
        return true;
    }

    @Override
    public @NotNull Component getDescription() {
        return MessageManager.getMessage(MgCommandLangKey.COMMAND_DELETE_DESCRIPTION);
    }

    @Override
    public @NotNull Component getUsage() {
        return MessageManager.getMessage(MgCommandLangKey.COMMAND_DELETE_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.delete";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull String @NotNull [] args) {
        if (args.length > 0) {
            Minigame mgm = PLUGIN.getMinigameManager().getMinigame(args[0]);

            if (mgm != null) {
                final @NotNull Path save = PLUGIN.getDataPath().resolve("minigames").resolve(mgm.getName());
                if (Files.isDirectory(save)) {
                    try {
                        Files.delete(save);
                    } catch (IOException e) {
                        PLUGIN.getComponentLogger().warn("couldn't delete files for minigame " + save + ". Still going to try to delete from config.");
                    }

                    List<String> ls = PLUGIN.getConfig().getStringList("minigames");
                    ls.remove(mgm.getName());
                    PLUGIN.getConfig().set("minigames", ls);
                    PLUGIN.getMinigameManager().removeMinigame(mgm.getName());
                    PLUGIN.saveConfig();
                    MessageManager.sendMessage(sender, MinigameMessageType.SUCCESS, MgCommandLangKey.COMMAND_DELETE_SUCCESS,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), mgm.getName()));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender,
                                                         @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            List<String> mgs = new ArrayList<>(Minigames.getPlugin().getMinigameManager().getAllMinigames().keySet());
            return CommandDispatcher.tabCompleteMatch(mgs, args[0]);
        }
        return null;
    }
}
