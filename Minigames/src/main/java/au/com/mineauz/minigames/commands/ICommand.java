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

    /**
     * if this returns null, no aliases exists. Only {@link #getName()} is always valid
     *
     * @return
     */
    default @NotNull String @Nullable [] getAliases() {
        return null;
    }

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
     * @param args
     * @return
     */
    boolean onCommand(@NotNull CommandSender sender, Minigame minigame, @NotNull String @Nullable [] args);

    /**
     * @param sender
     * @param minigame
     * @param args     might be null for all set commands, else wise shouldn't be
     * @return
     */
    @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender, Minigame minigame, @NotNull String[] args);
}
