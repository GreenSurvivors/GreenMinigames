package au.com.mineauz.minigames.tool;

import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.safelocation.SafeFullLocation;
import net.kyori.adventure.text.Component;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpectatorLocationMode implements ToolMode { //todo waring if other world

    @Override
    public @NotNull String getName() {
        return "SPECTATOR_START";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return MessageManager.getMessage(MgMenuLangKey.MENU_TOOL_LOCATION_SPECTATORSTART_NAME);
    }

    @Override
    public @NotNull List<@NotNull Component> getDescription() {
        return MessageManager.getMessageList(MgMenuLangKey.MENU_TOOL_LOCATION_SPECTATORSTART_DESCRIPTION);
    }

    @Override
    public @NotNull ItemType getIcon() {
        return ItemType.SOUL_SAND;
    }

    @Override
    public void onLeftClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame,
                            @Nullable Team team, @NotNull PlayerInteractEvent event) {
    }

    @Override
    public void onRightClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame,
                             @Nullable Team team, @NotNull PlayerInteractEvent event) {
        minigame.setSpectatorLocation(new SafeFullLocation(mgPlayer.getLocation()));
        MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_SET_SPECTATORLOCATION);
    }

    @Override
    public void select(final @NotNull MinigamePlayer mgPlayer, final @NotNull Minigame minigame, final @Nullable Team team) {
        if (minigame.getSpectatorLocation() != null) {
            final @Nullable Player player = mgPlayer.getPlayer();
            if (player != null && player.getWorld().equals(minigame.getSpectatorLocation().getWorld())) {
                player.sendBlockChange(minigame.getSpectatorLocation().toLocation(), BlockType.SKELETON_SKULL.createBlockData());
            }
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_SELECTED_SPECTATORLOCATION);
        } else {
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.TOOL_ERROR_NOSPECTATORLOCATION);
        }
    }

    @Override
    public void deselect(final @NotNull MinigamePlayer mgPlayer, final @NotNull Minigame minigame, final @Nullable Team team) {
        if (minigame.getSpectatorLocation() != null) {
            final @Nullable Player player = mgPlayer.getPlayer();
            if (player != null && player.getWorld().equals(minigame.getSpectatorLocation().getWorld())) {
                player.sendBlockChange(minigame.getSpectatorLocation().toLocation(),
                    minigame.getSpectatorLocation().getBlockAt().getBlockData());
            }
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_DESELECTED_SPECTATORLOCATION);
        } else {
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.TOOL_ERROR_NOSPECTATORLOCATION);
        }
    }

    @Override
    public void onSetMode(@NotNull MinigamePlayer player, @NotNull MinigameTool tool) {
    }

    @Override
    public void onUnsetMode(@NotNull MinigamePlayer mgPlayer, @NotNull MinigameTool tool) {
    }
}
