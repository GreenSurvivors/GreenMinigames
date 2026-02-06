package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.util.Map;

public class FallingBlockAction extends AAction {

    protected FallingBlockAction(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_FALLINGBLOCK_NAME);
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
        return true;
    }

    @Override
    public void executeRegionAction(@Nullable MinigamePlayer mgPlayer,
                                    @NotNull Region region) {
        debug(mgPlayer, region);

        if (region.getWorld() == null) {
            return;
        }

        final Location temp = region.getFirstPoint().toLocation();
        for (int y = NumberConversions.floor(region.getMinY()); y <= region.getMaxY(); y++) {
            temp.setY(y);
            for (int x = NumberConversions.floor(NumberConversions.floor(region.getMinX())); x <= region.getMaxX(); x++) {
                temp.setX(x);
                for (int z = NumberConversions.floor(region.getMinZ()); z <= region.getMaxZ(); z++) {
                    temp.setZ(z);
                    if (temp.getBlock().getType().isAir()) {
                        temp.getWorld().spawn(temp, FallingBlock.class,
                                fallingBlock -> fallingBlock.setBlockData(temp.getBlock().getBlockData()));
                        temp.getBlock().setBlockData(BlockType.AIR.createBlockData());
                    }
                }
            }
        }
    }

    @Override
    public void executeNodeAction(final @NotNull MinigamePlayer mgPlayer, final @NotNull Node node) {
        debug(mgPlayer, node);

        if (node.getSafeLocation().getWorld() == null) {
            return;
        }

        final Block block = node.getSafeLocation().getBlockAt();
        if (block.getType().isAir()) {
            node.getSafeLocation().getWorld().spawn(node.getSafeLocation().toLocation(), FallingBlock.class, fallingBlock ->
                    fallingBlock.setBlockData(block.getBlockData()));
            block.setBlockData(BlockType.AIR.createBlockData());
        }
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) {
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, Menu previous) {
        return false;
    }
}
