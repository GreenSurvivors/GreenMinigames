package au.com.mineauz.minigames.signs;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuitSign implements MinigameSign {

    private static final Minigames plugin = Minigames.getPlugin();

    @Override
    public @NotNull String getName() {
        return "Quit";
    }

    @Override
    public @Nullable String getCreatePermission() {
        return "minigame.sign.create.quit";
    }

    @Override
    public @NotNull String getCreatePermissionMessage() {
        return MinigameUtils.getLang("sign.quit.createPermission");
    }

    @Override
    public @Nullable String getUsePermission() {
        return null;
    }

    @Override
    public @Nullable String getUsePermissionMessage() {
        return null;
    }

    @Override
    public boolean signCreate(@NotNull SignChangeEvent event) {
        event.setLine(1, ChatColor.GREEN + "Quit");
        return true;
    }

    @Override
    public boolean signUse(@NotNull Sign sign, @NotNull MinigamePlayer player) {
        if (player.isInMinigame() && player.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
            plugin.getPlayerManager().quitMinigame(player, false);
            return true;
        } else if (player.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR)
            player.sendInfoMessage(MinigameUtils.getLang("sign.emptyHand"));
        return false;
    }

    @Override
    public void signBreak(@NotNull Sign sign, @Nullable MinigamePlayer player) {

    }
}
