package au.com.mineauz.minigamesregions;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.display.IDisplayObject;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RegionDisplayManager {
    private final @NotNull Map<@NotNull Player, @NotNull Map<@NotNull Region, @NotNull IDisplayObject>> regionDisplays;
    private final @NotNull Map<@NotNull Player, @NotNull Map<@NotNull Node, @NotNull IDisplayObject>> nodeDisplays;

    private final @NotNull SetMultimap<@NotNull Object, @NotNull MinigamePlayer> activeWatchers;
    private final @NotNull Map<@NotNull Object, @NotNull ArmorStand> nameDisplay;

    public RegionDisplayManager() {
        regionDisplays = new HashMap<>();
        nodeDisplays = new HashMap<>();

        activeWatchers = HashMultimap.create();
        nameDisplay = new IdentityHashMap<>();
    }

    private void showInfo(@NotNull Region region, @NotNull MinigamePlayer player) {
        activeWatchers.put(region, player);

        ArmorStand stand = nameDisplay.get(region);
        if (stand == null) {
            Location midPoint = region.getFirstPoint().clone().add(region.getSecondPoint()).add(1, 1, 1).multiply(0.5).subtract(0, 1.4, 0);
            stand = region.getFirstPoint().getWorld().spawn(midPoint, ArmorStand.class);
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

        ArmorStand stand = nameDisplay.get(node);
        if (stand == null) {
            stand = node.getLocation().getWorld().spawn(node.getLocation().clone().subtract(0, 0.75, 0), ArmorStand.class);
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

    public void show(@NotNull Region region, @NotNull MinigamePlayer player) {
        Map<Region, IDisplayObject> regions = regionDisplays.computeIfAbsent(player.getPlayer(), k -> new IdentityHashMap<>());

        IDisplayObject display = Minigames.getPlugin().display.displayCuboid(player.getPlayer(), region.getFirstPoint(), region.getSecondPoint().clone().add(1, 1, 1));
        display.show();
        regions.put(region, display);

        showInfo(region, player);
    }

    public void show(@NotNull Node node, @NotNull MinigamePlayer player) {
        Map<Node, IDisplayObject> nodes = nodeDisplays.computeIfAbsent(player.getPlayer(), k -> new IdentityHashMap<>());

        IDisplayObject display = Minigames.getPlugin().display.displayPoint(player.getPlayer(), node.getLocation(), true);
        display.show();
        nodes.put(node, display);

        showInfo(node, player);
    }

    public void hide(@NotNull Region region, @NotNull MinigamePlayer player) {
        Map<Region, IDisplayObject> regions = regionDisplays.get(player.getPlayer());
        if (regions == null) {
            return;
        }

        IDisplayObject display = regions.remove(region);
        if (display != null) {
            display.remove();
        }

        hideInfo(region, player);
    }

    public void hide(@NotNull Node node, @NotNull MinigamePlayer player) {
        Map<Node, IDisplayObject> nodes = nodeDisplays.get(player.getPlayer());
        if (nodes == null) {
            return;
        }

        IDisplayObject display = nodes.remove(node);
        if (display != null) {
            display.remove();
        }

        hideInfo(node, player);
    }

    public void showAll(@NotNull Minigame minigame, @NotNull MinigamePlayer player) {
        RegionModule module = RegionModule.getMinigameModule(minigame);
        for (Region region : module.getRegions()) {
            show(region, player);
        }

        for (Node node : module.getNodes()) {
            show(node, player);
        }
    }

    public void hideAll(@NotNull Minigame minigame, @NotNull MinigamePlayer player) {
        RegionModule module = RegionModule.getMinigameModule(minigame);
        for (Region region : module.getRegions()) {
            hide(region, player);
        }

        for (Node node : module.getNodes()) {
            hide(node, player);
        }
    }

    public void hideAll(@NotNull Player player) {
        MinigamePlayer mplayer = Minigames.getPlugin().getPlayerManager().getMinigamePlayer(player);
        Map<Region, IDisplayObject> regions = regionDisplays.remove(player);
        if (regions != null) {
            for (IDisplayObject display : regions.values()) {
                display.remove();
            }

            for (Region region : regions.keySet()) {
                hideInfo(region, mplayer);
            }
        }

        Map<Node, IDisplayObject> nodes = nodeDisplays.remove(player);
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
        for (ArmorStand stand : nameDisplay.values()) {
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
