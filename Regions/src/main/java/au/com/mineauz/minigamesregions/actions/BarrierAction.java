package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import io.papermc.paper.math.Position;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.util.Map;

public class BarrierAction extends AAction {

    protected BarrierAction(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_BARRIER_NAME);
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
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer,
                                  @NotNull Node node) {
        debug(mgPlayer, node);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void executeRegionAction(@Nullable MinigamePlayer mgPlayer, @NotNull Region region) {
        debug(mgPlayer, region);
        if (mgPlayer == null || !mgPlayer.isInMinigame()) return;
        Location locationPlayerNow = mgPlayer.getLocation();
        Position[] selection = {region.getFirstPoint(), region.getSecondPoint()};
        double xdis1 = Math.abs(locationPlayerNow.getX() - selection[0].x());
        double ydis1 = Math.abs(locationPlayerNow.getY() - selection[0].y());
        double zdis1 = Math.abs(locationPlayerNow.getZ() - selection[0].z());
        double xdis2 = Math.abs(locationPlayerNow.getX() - (selection[1].x() + 1));
        double ydis2 = Math.abs(locationPlayerNow.getY() - (selection[1].y() + 1));
        double zdis2 = Math.abs(locationPlayerNow.getZ() - (selection[1].z() + 1));
        boolean isMinX = false;
        boolean isMinY = false;
        boolean isMinZ = false;
        double xval;
        double yval;
        double zval;
        if (xdis1 < xdis2) {
            isMinX = true;
            xval = xdis1;
        } else {
            xval = xdis2;
        }
        if (!(ydis1 < ydis2)) {
            yval = ydis2;
        } else {
            isMinY = true;
            yval = ydis1;
        }
        if (zdis1 < zdis2) {
            isMinZ = true;
            zval = zdis1;
        } else {
            zval = zdis2;
        }
        if (xval < yval && xval < zval) {
            if (region.getPlayers().contains(mgPlayer)) {
                if (isMinX) {
                    locationPlayerNow.setX(locationPlayerNow.getX() - 0.5);
                } else {
                    locationPlayerNow.setX(locationPlayerNow.getX() + 0.5);
                }
            } else {
                if (isMinX) {
                    locationPlayerNow.setX(locationPlayerNow.getX() + 0.5);
                } else {
                    locationPlayerNow.setX(locationPlayerNow.getX() - 0.5);
                }
            }
        } else if (yval < xval && yval < zval) {
            if (region.getPlayers().contains(mgPlayer)) {
                if (isMinY) {
                    locationPlayerNow.setY(locationPlayerNow.getY() - 0.5);
                } else {
                    locationPlayerNow.setY(locationPlayerNow.getY() + 0.5);
                }
            } else {
                if (isMinY) {
                    locationPlayerNow.setY(locationPlayerNow.getY() + 0.5);
                } else {
                    locationPlayerNow.setY(locationPlayerNow.getY() - 0.5);
                }
            }
        } else if (zval < xval && zval < yval) {
            if (region.getPlayers().contains(mgPlayer)) {
                if (isMinZ) {
                    locationPlayerNow.setZ(locationPlayerNow.getZ() - 0.5);
                } else {
                    locationPlayerNow.setZ(locationPlayerNow.getZ() + 0.5);
                }
            } else {
                if (isMinZ) {
                    locationPlayerNow.setZ(locationPlayerNow.getZ() + 0.5);
                } else {
                    locationPlayerNow.setZ(locationPlayerNow.getZ() - 0.5);
                }
            }
        }
        mgPlayer.teleport(locationPlayerNow);
        if (region.getPlayers().contains(mgPlayer)) {
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
    public boolean displayMenu(@NotNull Menu previous) {
        return false;
    }
}
