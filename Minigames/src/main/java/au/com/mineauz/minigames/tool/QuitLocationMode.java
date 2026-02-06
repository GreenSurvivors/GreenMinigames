package au.com.mineauz.minigames.tool;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.safelocation.SafeFullLocation;
import net.kyori.adventure.text.Component;
import org.bukkit.block.BlockType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class QuitLocationMode implements ToolMode {

    @Override
    public @NotNull String getName() {
        return "QUIT";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_TOOL_LOCATION_QUIT_NAME);
    }

    @Override
    public @NotNull List<@NotNull Component> getDescription() {
        return MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_TOOL_LOCATION_QUIT_DESCRIPTION);
    }

    @Override
    public @NotNull ItemType getIcon() {
        return ItemType.OAK_DOOR;
    }

    @Override
    public void onLeftClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame,
                            @Nullable Team team, @NotNull PlayerInteractEvent event) {

    }

    @Override
    public void onRightClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame,
                             @Nullable Team team, @NotNull PlayerInteractEvent event) {
        minigame.setQuitLocation(new SafeFullLocation(mgPlayer.getLocation()));
        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_SET_QUITLOCATION);
    }

    @Override
    public void select(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team) {
        if (minigame.getQuitLocation() != null) {
            if (mgPlayer.getLocation().getWorld().equals(minigame.getQuitLocation().getWorld())) {
                mgPlayer.getPlayer().sendBlockChange(minigame.getQuitLocation().toLocation(), BlockType.SKELETON_SKULL.createBlockData());
            }
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_SELECTED_QUITLOCATION);
        } else {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.TOOL_ERROR_NOQUITLOCATION);
        }
    }

    @Override
    public void deselect(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team) {
        if (minigame.getQuitLocation() != null) {
            if (mgPlayer.getLocation().getWorld().equals(minigame.getQuitLocation().getWorld())) {
                mgPlayer.getPlayer().sendBlockChange(minigame.getQuitLocation().toLocation(),
                    minigame.getQuitLocation().getBlockAt().getBlockData());
            }
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_DESELECTED_QUITLOCATION);
        } else {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.TOOL_ERROR_NOQUITLOCATION);
        }
    }

    @Override
    public void onSetMode(@NotNull MinigamePlayer player, @NotNull MinigameTool tool) {
    }

    @Override
    public void onUnsetMode(@NotNull MinigamePlayer mgPlayer, @NotNull MinigameTool tool) {
    }
}
