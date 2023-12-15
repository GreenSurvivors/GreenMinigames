package au.com.mineauz.minigames.commands;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.minigame.Minigame;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ICommand {
    //    public MinigamePlayerManager playerManager = Minigames.plugin.getPlayerData();
//    public MinigameManager minigameManager = Minigames.plugin.getMinigameData();
    Minigames plugin = Minigames.getPlugin();

    String getName();

    String[] getAliases();

    boolean canBeConsole();

    Component getDescription();

    String[] getParameters();

    Component getUsage();

    String getPermission();

    boolean onCommand(@NotNull CommandSender sender, @Nullable Minigame minigame,
                      @NotNull String label, @NotNull String @Nullable [] args);

    List<String> onTabComplete(CommandSender sender, Minigame minigame, String alias, String[] args);
}
