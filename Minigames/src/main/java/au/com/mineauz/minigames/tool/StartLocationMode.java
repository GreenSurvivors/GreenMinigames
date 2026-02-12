package au.com.mineauz.minigames.tool;

import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.safelocation.SafeFullLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StartLocationMode implements ToolMode { //todo waring if other world

    @Override
    public @NotNull String getName() {
        return "START";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return MessageManager.getMessage(MgMenuLangKey.MENU_TOOL_LOCATION_START_NAME);
    }

    @Override
    public @NotNull List<@NotNull Component> getDescription() {
        return MessageManager.getMessageList(MgMenuLangKey.MENU_TOOL_LOCATION_START_DESCRIPTION);
    }

    @Override
    public @NotNull ItemType getIcon() {
        return ItemType.SKELETON_SKULL;
    }

    @Override
    public void onLeftClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team, @NotNull PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
            int x = event.getClickedBlock().getLocation().getBlockX();
            int y = event.getClickedBlock().getLocation().getBlockY();
            int z = event.getClickedBlock().getLocation().getBlockZ();
            String world = event.getClickedBlock().getLocation().getWorld().getName();

            int nx;
            int ny;
            int nz;
            String nworld;
            @Nullable SafeFullLocation delLoc = null;
            if (team != null) {
                if (team.hasStartLocations()) {
                    for (SafeFullLocation loc : team.getStartLocations()) {
                        nx = loc.blockX();
                        ny = loc.blockY();
                        nz = loc.blockZ();
                        nworld = loc.getWorld().getName();

                        if (x == nx && y == ny && z == nz && world.equals(nworld)) {
                            delLoc = loc;
                            break;
                        }
                    }
                }
                if (delLoc != null) {
                    team.getStartLocations().remove(delLoc);

                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_REMOVE_STARTLOCTION,
                            Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(team.getDisplayName() + " ", team.getTextColor())));
                } else {
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.TOOL_ERROR_NOSTARTLOCATION,
                            Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(team.getDisplayName() + " ", team.getTextColor())));
                }
            } else {
                for (SafeFullLocation loc : minigame.getStartLocations()) {
                    nx = loc.blockX();
                    ny = loc.blockY();
                    nz = loc.blockZ();
                    nworld = loc.getWorld().getName();

                    if (x == nx && y == ny && z == nz && world.equals(nworld)) {
                        delLoc = loc;
                        break;
                    }
                }
                if (delLoc != null) {
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_REMOVE_STARTLOCTION,
                            Placeholder.unparsed(MinigamePlaceHolderKey.TEAM.getKey(), ""));
                } else {
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.TOOL_ERROR_NOSTARTLOCATION,
                            Placeholder.unparsed(MinigamePlaceHolderKey.TEAM.getKey(), ""));
                }
            }
        }
    }

    @Override
    public void onRightClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team, @NotNull PlayerInteractEvent event) {
        if (team == null) {
            minigame.addStartLocation(new SafeFullLocation(mgPlayer.getLocation()));

            MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_ADDED_STARTLOCATION,
                    Placeholder.unparsed(MinigamePlaceHolderKey.TEAM.getKey(), ""),
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
        } else {
            team.addStartLocation(new SafeFullLocation(mgPlayer.getLocation()));

            MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_ADDED_STARTLOCATION,
                    Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(team.getDisplayName() + " ", team.getTextColor())),
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
        }
    }

    @Override
    public void select(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team) {
        final @Nullable Player player = mgPlayer.getPlayer();
        if (player == null) {
            return;
        }

        if (team != null) {
            for (SafeFullLocation loc : team.getStartLocations()) {
                if (player.getWorld().equals(loc.getWorld())) {
                    player.sendBlockChange(loc.toLocation(), BlockType.SKELETON_SKULL.createBlockData());
                }
            }

            MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_SELECTED_STARTLOCATION,
                    Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(team.getDisplayName() + " ", team.getTextColor())),
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
        } else {
            for (SafeFullLocation loc : minigame.getStartLocations()) {
                if (player.getWorld().equals(loc.getWorld())) {
                    player.sendBlockChange(loc.toLocation(), BlockType.SKELETON_SKULL.createBlockData());
                }
            }
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_SELECTED_STARTLOCATION,
                    Placeholder.unparsed(MinigamePlaceHolderKey.TEAM.getKey(), ""),
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
        }
    }

    @Override
    public void deselect(final @NotNull MinigamePlayer mgPlayer, final @NotNull Minigame minigame, final @Nullable Team team) {
        final @Nullable Player player = mgPlayer.getPlayer();
        if (player == null) {
            return;
        }

        if (team != null) {
            for (final @NotNull SafeFullLocation loc : team.getStartLocations()) {
                if (player.getLocation().getWorld().equals(loc.getWorld())) {
                    player.sendBlockChange(loc.toLocation(), loc.getBlockAt().getBlockData());
                }
            }

            MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_DESELECTED_STARTLOCATION,
                    Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(team.getDisplayName() + " ", team.getTextColor())),
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
        } else {
            for (SafeFullLocation loc : minigame.getStartLocations()) {
                if (player.getWorld().equals(loc.getWorld())) {
                    player.sendBlockChange(loc.toLocation(), loc.getBlockAt().getBlockData());
                }
            }

            MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_DESELECTED_STARTLOCATION,
                    Placeholder.unparsed(MinigamePlaceHolderKey.TEAM.getKey(), ""),
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
        }
    }

    @Override
    public void onSetMode(@NotNull MinigamePlayer player, @NotNull MinigameTool tool) {
    }

    @Override
    public void onUnsetMode(@NotNull MinigamePlayer mgPlayer, @NotNull MinigameTool tool) {
    }
}
