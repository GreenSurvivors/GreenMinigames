package au.com.mineauz.minigames.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ListCommand extends ACommand {

    @Override
    public @NotNull String getName() {
        return "list";
    }

    @Override
    public boolean canBeConsole() {
        return true;
    }

    @Override
    public @NotNull Component getDescription() {
        return "Lists all the Minigames.";
    }

    @Override
    public String[] getUsage() {
        return new String[]{"/minigame list"};
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.list";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull String @NotNull [] args) {
        List<String> mglist = PLUGIN.getConfig().getStringList("minigames");
        StringBuilder minigames = new StringBuilder();

        for (int i = 0; i < mglist.size(); i++) {
            minigames.append(mglist.get(i));
            if (i != mglist.size() - 1) {
                minigames.append(", ");
            }
        }

        sender.sendMessage(ChatColor.GRAY + minigames.toString());
        return true;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender,
                                                         @NotNull String @NotNull [] args) {
        return null;
    }

}
