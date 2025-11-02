package au.com.mineauz.minigames.signs;

import au.com.mineauz.minigames.MinigameMessageType;
import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TeleportSign implements MinigameSign {

    @Override
    public @NotNull String getName() {
        return "Teleport";
    }

    @Override
    public @Nullable String getCreatePermission() {
        return "minigame.sign.create.teleport";
    }

    @Override
    public @NotNull String getCreatePermissionMessage() {
        return MinigameUtils.getLang("sign.teleport.createPermission");
    }

    @Override
    public @Nullable String getUsePermission() {
        return "minigame.sign.use.teleport";
    }

    @Override
    public @NotNull String getUsePermissionMessage() {
        return MinigameUtils.getLang("sign.teleport.usePermission");
    }

    @Override
    public boolean signCreate(@NotNull SignChangeEvent event) {
        event.setLine(1, ChatColor.GREEN + "Teleport");
        if (event.getLine(2).isEmpty()) {
            return false;
        } else {
            return event.getLine(2).matches("-?[0-9]+,[0-9]+,-?[0-9]+");
        }
    }

    @Override
    public boolean signUse(@NotNull Sign sign, @NotNull MinigamePlayer player) {
        if (!sign.getLine(2).isEmpty() && sign.getLine(2).matches("-?[0-9]+,[0-9]+,-?[0-9]+")) {
            int x;
            int y;
            int z;
            String[] split = sign.getLine(2).split(",");
            x = Integer.parseInt(split[0]);
            y = Integer.parseInt(split[1]);
            z = Integer.parseInt(split[2]);

            if (!sign.getLine(3).isEmpty() && sign.getLine(3).matches("-?[0-9]+,-?[0-9]+")) {
                float yaw;
                float pitch;
                String[] split2 = sign.getLine(3).split(",");
                yaw = Float.parseFloat(split2[0]);
                pitch = Float.parseFloat(split2[1]);
                player.teleport(new Location(player.getPlayer().getWorld(), x + 0.5, y, z + 0.5, yaw, pitch));
                return true;
            }
            player.teleport(new Location(player.getPlayer().getWorld(), x + 0.5, y, z + 0.5));
            return true;
        }
        player.sendMessage(MinigameUtils.getLang("sign.teleport.invalid"), MinigameMessageType.ERROR);
        return false;
    }

    @Override
    public void signBreak(@NotNull Sign sign, @Nullable MinigamePlayer player) {

    }
}
