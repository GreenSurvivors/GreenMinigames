package au.com.mineauz.minigamesregions;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.display.IDisplayObject;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import io.papermc.paper.math.FinePosition;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RegionDisplayManager {
    private final @NotNull Map<@NotNull UUID, @NotNull Map<@NotNull Region, @NotNull IDisplayObject>> regionDisplays = new HashMap<>();
    private final @NotNull Map<@NotNull UUID, @NotNull Map<@NotNull Node, @NotNull IDisplayObject>> nodeDisplays = new HashMap<>();

    private final @NotNull SetMultimap<@NotNull Object, @NotNull MinigamePlayer> activeWatchers = HashMultimap.create();
    private final @NotNull Map<@NotNull Object, @NotNull ArmorStand> nameDisplay = new IdentityHashMap<>();
    
    private final @NotNull Minigames minigamesPlugin;

    public RegionDisplayManager(@NotNull Minigames minigamesPlugin) {
        this.minigamesPlugin = minigamesPlugin;
    }

    private void showInfo(final @NotNull Region region, final @NotNull MinigamePlayer player) {
        if (region.getWorld() == null) {
            return;
        }

        activeWatchers.put(region, player);

        ArmorStand stand = nameDisplay.get(region);
        if (stand == null) {
            final @NotNull FinePosition max = region.getMax();
            final @NotNull FinePosition min = region.getMin();

            // the +1 is offset of block coordinates. About the 1.4 Idk. magic value.
            final double x = (min.x() + max.x() + 1) * 0.5;
            final double y = (min.y() + max.y() + 1) * 0.5 + 1.4;
            final double z = (min.z() + max.z() + 1) * 0.5;

            stand = region.getWorld().spawn(new Location(region.getWorld(), x, y, z), ArmorStand.class);
            stand.setGravity(false);
            stand.setSmall(true);
            stand.setVisible(false);
            stand.setCustomNameVisible(true);

            nameDisplay.put(region, stand);
        }

        String info = ChatColor.BLUE +
                "Region: " +
                ChatColor.WHITE +
                region.getName() +
                " Enabled: " + region.getEnabled();
        stand.setCustomName(info); //todo
    }

    private void showInfo(@NotNull Node node, @NotNull MinigamePlayer player) {
        activeWatchers.put(node, player);

        if (node.getSafeLocation().getWorld() == null) {
            return;
        }

        ArmorStand stand = nameDisplay.get(node);
        if (stand == null) {
            stand = node.getSafeLocation().getWorld().spawn(node.getSafeLocation().offset(0, -0.75, 0).toLocation(), ArmorStand.class);
            stand.setGravity(false);
            stand.setSmall(true);
            stand.setVisible(false);
            stand.setCustomNameVisible(true);

            nameDisplay.put(node, stand);
        }

        String info = ChatColor.RED + "Node: " +
                ChatColor.WHITE + node.getName();

        // TODO: Add more info
        stand.setCustomName(info);
    }

    private void hideInfo(@NotNull Object object, @NotNull MinigamePlayer player) {
        activeWatchers.remove(object, player);
        if (activeWatchers.get(object).isEmpty()) {
            ArmorStand stand = nameDisplay.remove(object);
            if (stand != null)
                stand.remove();
        }
    }

    public void show(final @NotNull Region region, final @NotNull MinigamePlayer player) {
        final @NotNull Map<@NotNull Region, @NotNull IDisplayObject> regions = regionDisplays.computeIfAbsent(player.getUUID(), k -> new IdentityHashMap<>());

        final @NotNull IDisplayObject display = minigamesPlugin.getDisplayManager().displayCuboid(player.getPlayer(), region);
        display.show();
        regions.put(region, display);

        showInfo(region, player);
    }

    public void show(final @NotNull Node node, final @NotNull MinigamePlayer mgPlayer) {
        final @NotNull Map<@NotNull Node, @NotNull IDisplayObject> nodes = nodeDisplays.computeIfAbsent(mgPlayer.getUUID(), k -> new IdentityHashMap<>());

        final @NotNull IDisplayObject display = minigamesPlugin.getDisplayManager().displayPoint(mgPlayer.getPlayer(), node.getSafeLocation(), true);
        display.show();
        nodes.put(node, display);

        showInfo(node, mgPlayer);
    }

    public void hide(final @NotNull Region region, final @NotNull MinigamePlayer player) {
        final @Nullable Map<@NotNull Region, @NotNull IDisplayObject> regions = regionDisplays.get(player.getUUID());
        if (regions == null) {
            return;
        }

        final @Nullable IDisplayObject display = regions.remove(region);
        if (display != null) {
            display.remove();
        }

        hideInfo(region, player);
    }

    public void hide(final @NotNull Node node, final @NotNull MinigamePlayer player) {
        final @Nullable Map<@NotNull Node, @NotNull IDisplayObject> nodes = nodeDisplays.get(player.getUUID());
        if (nodes == null) {
            return;
        }

        final @Nullable IDisplayObject display = nodes.remove(node);
        if (display != null) {
            display.remove();
        }

        hideInfo(node, player);
    }

    public void showAll(final @NotNull Minigame minigame, final @NotNull MinigamePlayer player) {
        final RegionModule module = RegionModule.getMinigameModule(minigame);
        for (Region region : module.getRegions()) {
            show(region, player);
        }

        for (Node node : module.getNodes()) {
            show(node, player);
        }
    }

    public void hideAll(final @NotNull Minigame minigame, final @NotNull MinigamePlayer mgPlayer) {
        final RegionModule module = RegionModule.getMinigameModule(minigame);
        for (Region region : module.getRegions()) {
            hide(region, mgPlayer);
        }

        for (Node node : module.getNodes()) {
            hide(node, mgPlayer);
        }
    }

    public void hideAll(final @NotNull Player player) {
        MinigamePlayer mplayer = minigamesPlugin.getPlayerManager().getMinigamePlayer(player);
        final @Nullable Map<@NotNull Region, @NotNull IDisplayObject> regions = regionDisplays.remove(player.getUniqueId());
        if (regions != null) {
            for (IDisplayObject display : regions.values()) {
                display.remove();
            }

            for (Region region : regions.keySet()) {
                hideInfo(region, mplayer);
            }
        }

        final @Nullable Map<@NotNull Node, @NotNull IDisplayObject> nodes = nodeDisplays.remove(player.getUniqueId());
        if (nodes != null) {
            for (IDisplayObject display : nodes.values()) {
                display.remove();
            }

            for (Node node : nodes.keySet()) {
                hideInfo(node, mplayer);
            }
        }
    }

    public void shutdown() {
        for (final @NotNull ArmorStand stand : nameDisplay.values()) {
            stand.remove();
        }
    }

    public void update(@NotNull Node node) {
        Set<MinigamePlayer> watchers = new HashSet<>(activeWatchers.get(node));

        ArmorStand stand = nameDisplay.remove(node);
        if (stand != null)
            stand.remove();

        for (MinigamePlayer player : watchers) {
            hide(node, player);
            show(node, player);
        }
    }

    public void update(@NotNull Region region) {
        Set<MinigamePlayer> watchers = new HashSet<>(activeWatchers.get(region));

        ArmorStand stand = nameDisplay.remove(region);
        if (stand != null)
            stand.remove();

        for (MinigamePlayer player : watchers) {
            hide(region, player);
            show(region, player);
        }
    }
}
