package au.com.mineauz.minigames.commands;

import au.com.mineauz.minigames.minigame.Minigame;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// this did not get the lang rework since the whole plugin needs checking before this command could get re-enabled maybe it will get removed all together
public class ReloadCommand extends ACommand {

    @Override
    public @NotNull String getName() {
        return "reload";
    }

    @Override
    public boolean canBeConsole() {
        return true;
    }

    @Override
    public @NotNull Component getDescription() {
        return Component.text("Reloads the Minigames config files.");
    }

    @Override
    public @NotNull Component getUsage() {
        return Component.text("/minigame reload");
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.reload";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull String @NotNull [] args) { // todo just abstract the process of enabling the plugin
        for (Player p : PLUGIN.getServer().getOnlinePlayers()) {
            if (PLUGIN.getPlayerManager().getMinigamePlayer(p).isInMinigame()) {
                PLUGIN.getPlayerManager().quitMinigame(PLUGIN.getPlayerManager().getMinigamePlayer(p), true);
            }
        }

        PLUGIN.getMinigameManager().getAllMinigames().clear();

        PLUGIN.saveDefaultConfig();

        List<String> mgs = new ArrayList<>();
        if (PLUGIN.getConfig().contains("minigames")) {
            mgs = PLUGIN.getConfig().getStringList("minigames");
        }
        final List<String> allMGS = new ArrayList<>(mgs);

        if (!mgs.isEmpty()) {
            for (String mgm : allMGS) {
                Minigame game = new Minigame(mgm);
                game.loadMinigame();
                PLUGIN.getMinigameManager().addMinigame(game);
            }
        }

        sender.sendMessage(ChatColor.GREEN + "Reloaded Minigame configs");
        return true;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender,
                                                         @NotNull String @NotNull [] args) {
        return null;
    }
}
