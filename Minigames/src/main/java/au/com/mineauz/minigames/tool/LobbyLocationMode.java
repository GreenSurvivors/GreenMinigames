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

public class LobbyLocationMode implements ToolMode {

    @Override
    public @NotNull String getName() {
        return "LOBBY";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return MessageManager.getMessage(MgMenuLangKey.MENU_TOOL_LOCATION_LOBBY_NAME);
    }

    @Override
    public @NotNull List<@NotNull Component> getDescription() {
        return MessageManager.getMessageList(MgMenuLangKey.MENU_TOOL_LOCATION_LOBBY_DESCRIPTION);
    }

    @Override
    public @NotNull ItemType getIcon() {
        return ItemType.OAK_TRAPDOOR;
    }

    @Override
    public void onLeftClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame,
                            @Nullable Team team, @NotNull PlayerInteractEvent event) {

    }

    @Override
    public void onRightClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame,
                             @Nullable Team team, @NotNull PlayerInteractEvent event) {
        minigame.setLobbyLocation(new SafeFullLocation(mgPlayer.getLocation()));
        MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_SET_LOBBYLOCATION);
    }

    @Override
    public void select(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team) {
        if (minigame.getLobbyLocation() != null) {
            final @Nullable Player player = mgPlayer.getPlayer();
            if (player != null && player.getWorld().equals(minigame.getLobbyLocation().getWorld())) {
                player.sendBlockChange(minigame.getLobbyLocation().toLocation(), BlockType.SKELETON_SKULL.createBlockData());
            }
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_SELECTED_LOBBYLOCATION);
        } else {
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.TOOL_ERROR_NOLOBBYLOCATION);
        }
    }

    @Override
    public void deselect(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team) {
        if (minigame.getLobbyLocation() != null) {
            final @Nullable Player player = mgPlayer.getPlayer();
            if (player != null && player.getWorld().equals(minigame.getLobbyLocation().getWorld())) {
                player.sendBlockChange(minigame.getLobbyLocation().toLocation(),
                    minigame.getLobbyLocation().getBlockAt().getBlockData());
            }
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_DESELECTED_LOBBYLOCATION);
        } else {
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.TOOL_ERROR_NOLOBBYLOCATION);
        }
    }

    @Override
    public void onSetMode(@NotNull MinigamePlayer player, @NotNull MinigameTool tool) {
    }

    @Override
    public void onUnsetMode(@NotNull MinigamePlayer mgPlayer, @NotNull MinigameTool tool) {
    }
}
