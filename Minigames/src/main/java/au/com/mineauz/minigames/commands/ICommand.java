package au.com.mineauz.minigames.commands;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.minigame.Minigame;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ICommand {
    Minigames plugin = Minigames.getPlugin();

    @NotNull String getName();

    @NotNull String @Nullable [] getAliases();

    boolean canBeConsole();

    @NotNull Component getDescription();

    Component getUsage();

    /**
     * If this returns null, everyone should be able to use this command!
     */
    @Nullable String getPermission();

    /**
     * @param sender
     * @param minigame all set commands are guarantied to have not null minigame parameter. Everything else probably is null.
     * @param label
     * @param args
     * @return
     */
    boolean onCommand(@NotNull CommandSender sender, Minigame minigame,
                      @NotNull String label, @NotNull String @Nullable [] args);

    /**
     * @param sender
     * @param minigame
     * @param alias    not null for all set commands, else wise may or may not be null
     * @param args     might be null for all set commands, else wise shouldn't be
     * @return
     */
    @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender, Minigame minigame, String alias, @NotNull String[] args);
}
