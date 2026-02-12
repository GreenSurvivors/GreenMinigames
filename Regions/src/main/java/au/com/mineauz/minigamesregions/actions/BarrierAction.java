package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import io.papermc.paper.math.Position;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.util.Map;

/**
 * Everytime this action is executed, the player triggering it will be teleported 0.5 blocks
 * towards the nearest face of the box a region spans.
 * The action may overshoot and teleport on the other side of the border.
 * The action does not care if it's safe to teleport to the given location, you may end up inside a block if you are unlucky.
 * The action, even if run every tick, will not prevent entering / leaving the region, just "gently" push you towards a border.
 * (however gently you would call ramming someone forcefully to suffocate inside the floor).
 * If a player effectively moves faster than 0.5 blocks/execution they may stay on the other side of the barrier as long as they wish to be.
 * To not effectively "glue" every player to the border, this action needs to get used alongside an appropriate trigger and/or condition,
 * to only move the players inside or outside. (Where shoving the player inside the box seams way harder than moving them outside.)
 */
 /*
 * Note: Do to its limitations, this actions appears rather useless to me.
 * However, at this point I don't know what the best path moving forward would be without breaking all past expectations.
 * A hard teleport? A config to only allow passing from one side but not the other (effectively either allowing player inside / outside)?
 * Creating a general player moved trigger to appropriately make the boundaries of the region a hard border, but allow existing inside and outside?
 * Is the slow shove a desired effect or just bad programming?
 */
public class BarrierAction extends AAction {

    protected BarrierAction(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return MessageManager.getMessage(RegionLangKey.MENU_ACTION_BARRIER_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.WORLD;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of();
    }

    @Override
    public boolean useInRegions() {
        return true;
    }

    @Override
    public boolean useInNodes() {
        return false;
    }

    @Override
    public void executeNodeAction(final @NotNull MinigamePlayer mgPlayer, final @NotNull Node node) {
        debug(mgPlayer, node);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void executeRegionAction(final @Nullable MinigamePlayer mgPlayer, final @NotNull Region region) {
        debug(mgPlayer, region);
        if (mgPlayer == null || !mgPlayer.isInMinigame()) return;

        final @NotNull Location playerLoc = mgPlayer.getLocation();
        final @NotNull Position rgMinPos = region.getMin();
        final @NotNull Position rgMaxPos = region.getMax();

        final boolean isInside = region.getPlayers().contains(mgPlayer);

        // distance to min face and to (max + 1) face (block-based upper bound)
        final double xDistanceMin = Math.abs(playerLoc.getX() - rgMinPos.x());
        final double yDistanceMin = Math.abs(playerLoc.getY() - rgMinPos.y());
        final double zDistanceMin = Math.abs(playerLoc.getZ() - rgMinPos.z());

        final double xDistanceMax = Math.abs(playerLoc.getX() - (rgMaxPos.x() + 1));
        final double yDistanceMax = Math.abs(playerLoc.getY() - (rgMaxPos.y() + 1));
        final double zDistanceMax = Math.abs(playerLoc.getZ() - (rgMaxPos.z() + 1));

        // get the nearest boundary of every axis
        final boolean isMinXNearest = xDistanceMin < xDistanceMax;
        final boolean isMinYNearest = yDistanceMin < yDistanceMax;
        final boolean isMinZNearest = zDistanceMin < zDistanceMax;

        // shortest distance to the region boundary in each axis
        final double xDistance = isMinXNearest ? xDistanceMin : xDistanceMax;
        final double yDistance = isMinYNearest ? yDistanceMin : yDistanceMax;
        final double zDistance = isMinZNearest ? zDistanceMin : zDistanceMax;

        // move 0.5 only to the closest boundary
        // rare xor operator (^) incoming. (a ^ b) <==> (a != b) for primitiv booleans
        if (xDistance < yDistance && xDistance < zDistance) {
            playerLoc.add(isInside ^ isMinXNearest ? 0.5 : -0.5, 0, 0);
        } else if (yDistance < xDistance && yDistance < zDistance) {
            playerLoc.add(0, isInside ^ isMinXNearest ? 0.5 : -0.5, 0);
        } else if (zDistance < xDistance && zDistance < yDistance) {
            playerLoc.add(0, 0, isInside ^ isMinXNearest ? 0.5 : -0.5);
        }

        mgPlayer.teleport(playerLoc);
        if (isInside) {
            region.removePlayer(mgPlayer);
        } else {
            region.addPlayer(mgPlayer);
        }
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) {
        // None
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        //None
    }

    @Override
    public boolean displayMenu(final @NotNull Menu previous) {
        return false;
    }
}
