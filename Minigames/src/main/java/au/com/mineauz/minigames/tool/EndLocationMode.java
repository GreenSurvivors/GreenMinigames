package au.com.mineauz.minigames.tool;

import au.com.mineauz.minigames.managers.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EndLocationMode implements ToolMode {

    @Override
    public String getName() {
        return "END";
    }

    @Override
    public Component getDisplayName() {
        return "End Position";
    }

    @Override
    public List<Component> getDescription() { //todo translation String
        return List.of(
                "Sets the end",
                "location");
    }

    @Override
    public Material getIcon() {
        return Material.GOLD_BLOCK;
    }

    @Override
    public void onLeftClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame,
                            @Nullable Team team, @NotNull PlayerInteractEvent event) {

    }

    @Override
    public void onRightClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame,
                             @Nullable Team team, @NotNull PlayerInteractEvent event) {
        minigame.setEndPosition(mgPlayer.getLocation());
        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MinigameLangKey.TOOL_SET_ENDLOCATION);
    }

    @Override
    public void onEntityLeftClick(MinigamePlayer player, Minigame minigame, Team team, EntityDamageByEntityEvent event) {

    }

    @Override
    public void onEntityRightClick(MinigamePlayer player, Minigame minigame, Team team, PlayerInteractEntityEvent event) {

    }

    @Override
    public void select(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team) {
        if (minigame.getEndPosition() != null) {
            mgPlayer.getPlayer().sendBlockChange(minigame.getEndPosition(),
                    Material.SKELETON_SKULL.createBlockData());
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MinigameLangKey.TOOL_SELECTED_ENDLOCATION);
        } else {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MinigameLangKey.TOOL_ERROR_NOENDLOCATION);
        }
    }

    @Override
    public void deselect(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team) {
        if (minigame.getEndPosition() != null) {
            mgPlayer.getPlayer().sendBlockChange(minigame.getEndPosition(),
                    minigame.getEndPosition().getBlock().getBlockData());
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MinigameLangKey.TOOL_DESELECTED_ENDLOCATION);
        } else {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MinigameLangKey.TOOL_ERROR_NOENDLOCATION);
        }
    }

    @Override
    public void onSetMode(MinigamePlayer player, MinigameTool tool) {
    }

    @Override
    public void onUnsetMode(@NotNull MinigamePlayer mgPlayer, MinigameTool tool) {
    }
}
