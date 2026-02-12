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

public class EndLocationMode implements ToolMode {

    @Override
    public @NotNull String getName() {
        return "END";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return MessageManager.getMessage(MgMenuLangKey.MENU_TOOL_LOCATION_END_NAME);
    }

    @Override
    public @NotNull List<@NotNull Component> getDescription() {
        return MessageManager.getMessageList(MgMenuLangKey.MENU_TOOL_LOCATION_END_DESCRIPTION);
    }

    @Override
    public @NotNull ItemType getIcon() {
        return ItemType.GOLD_BLOCK;
    }

    @Override
    public void onLeftClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame,
                            @Nullable Team team, @NotNull PlayerInteractEvent event) {

    }

    @Override
    public void onRightClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame,
                             @Nullable Team team, @NotNull PlayerInteractEvent event) {
        minigame.setEndLocation(new SafeFullLocation(mgPlayer.getLocation()));
        MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_SET_ENDLOCATION);
    }

    @Override
    public void select(final @NotNull MinigamePlayer mgPlayer, final @NotNull Minigame minigame, final @Nullable Team team) {
        if (minigame.getEndLocation() != null) {
            final @Nullable Player player = mgPlayer.getPlayer();
            if (player != null && player.getWorld().equals(minigame.getEndLocation().getWorld())) {
                player.sendBlockChange(minigame.getEndLocation().toLocation(),
                    BlockType.SKELETON_SKULL.createBlockData());
            }
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_SELECTED_ENDLOCATION);
        } else {
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.TOOL_ERROR_NOENDLOCATION);
        }
    }

    @Override
    public void deselect(final @NotNull MinigamePlayer mgPlayer, final @NotNull Minigame minigame, final @Nullable Team team) {
        if (minigame.getEndLocation() != null) {
            final @Nullable Player player = mgPlayer.getPlayer();
            if (player != null && player.getWorld().equals(minigame.getEndLocation().getWorld())) {
                player.sendBlockChange(minigame.getEndLocation().toLocation(),
                    minigame.getEndLocation().getBlockAt().getBlockData());
            }
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_DESELECTED_ENDLOCATION);
        } else {
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.TOOL_ERROR_NOENDLOCATION);
        }
    }

    @Override
    public void onSetMode(@NotNull MinigamePlayer player, @NotNull MinigameTool tool) {
    }

    @Override
    public void onUnsetMode(@NotNull MinigamePlayer mgPlayer, @NotNull MinigameTool tool) {
    }
}
