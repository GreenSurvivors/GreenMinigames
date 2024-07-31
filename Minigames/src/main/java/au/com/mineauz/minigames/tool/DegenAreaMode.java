package au.com.mineauz.minigames.tool;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.objects.MgRegion;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DegenAreaMode implements ToolMode {

    @Override
    public @NotNull String getName() {
        return "DEGEN_AREA";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_TOOL_DEGENAREA_NAME);
    }

    @Override
    public @NotNull List<@NotNull Component> getDescription() {
        return MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_TOOL_DEGENAREA_DESCRIPTION);
    }

    @Override
    public @NotNull Material getIcon() {
        return Material.LAVA_BUCKET;
    }

    @Override
    public void onLeftClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame,
                            @Nullable Team team, @NotNull PlayerInteractEvent event) {
        if (mgPlayer.hasSelection()) {
            if (minigame.getFloorDegen() != null) {
                minigame.getFloorDegen().setFirstPos(mgPlayer.getSelectionLocations()[0]);
                minigame.getFloorDegen().setSecondPos(mgPlayer.getSelectionLocations()[1]);
            } else {
                //please note: the name is not important
                minigame.setFloorDegen(new MgRegion("degen", mgPlayer.getSelectionLocations()[0], mgPlayer.getSelectionLocations()[1]));
            }
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_SET_DEGENAREA);
        } else {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.TOOL_ERROR_NOREGIONSELECTED);
        }
    }

    @Override
    public void onRightClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame,
                             @Nullable Team team, @NotNull PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            mgPlayer.addSelectionPoint(event.getClickedBlock().getLocation());
            if (mgPlayer.getSelectionLocations()[1] != null) {
                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_SELECTED_REGION);
            }
        }
    }

    @Override
    public void select(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team) {
        if (minigame.getFloorDegen() != null) {
            mgPlayer.setSelection(minigame.getFloorDegen());
            mgPlayer.showSelection(true);
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_SELECTED_REGION);
        } else {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.TOOL_ERROR_NODEGENAREA);
        }
    }

    @Override
    public void deselect(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team) {
        if (minigame.getFloorDegen() != null) {
            mgPlayer.setSelection(minigame.getFloorDegen());
            mgPlayer.showSelection(false);
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_DESELECTED_REGION);
        } else {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.TOOL_ERROR_NODEGENAREA);
        }
    }

    @Override
    public void onSetMode(@NotNull MinigamePlayer player, @NotNull MinigameTool tool) {
    }

    @Override
    public void onUnsetMode(@NotNull MinigamePlayer mgPlayer, @NotNull MinigameTool tool) {
    }
}
