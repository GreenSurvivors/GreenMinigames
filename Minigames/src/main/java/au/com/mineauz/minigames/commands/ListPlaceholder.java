package au.com.mineauz.minigames.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 3/06/2020.
 */
public class ListPlaceholder extends ACommand { //todo needs pages

    @Override
    public @NotNull String getName() {
        return "placeholders";
    }

    @Override
    public boolean canBeConsole() {
        return true;
    }

    @Override
    public @NotNull Component getDescription() {
        return "List all registered placeholders";
    }

    @Override
    public String[] getUsage() {
        return new String[]{"/minigame placeholders"};
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.placeholders";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull String @NotNull [] args) {
        Set<String> placeholders = PLUGIN.getPlaceHolderManager().getRegisteredPlaceHolders();
        StringBuilder result = new StringBuilder();
        for (String pHolder : placeholders) {
            result.append("%").append(PLUGIN.getName()).append("_").append(pHolder).append("%, ");
        }
        result.delete(result.length() - 1, result.length());
        sender.sendMessage("PlaceHolder List");
        sender.sendMessage(result.toString());
        return true;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return null;
    }
}
