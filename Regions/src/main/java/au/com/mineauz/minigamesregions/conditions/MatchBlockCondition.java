package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.config.BlockDataFlag;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class MatchBlockCondition extends ACondition {
    private final BlockDataFlag blockData = new BlockDataFlag(Material.STONE.createBlockData(), "type");
    private final BooleanFlag useFullBlockData = new BooleanFlag(false, "usedur"); //todo rename the name

    protected MatchBlockCondition(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_MATCHBLOCK_NAME);
    }

    @Override
    public @NotNull IConditionCategory getCategory() {
        return RegionConditionCategories.WORLD;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        if (useFullBlockData.getFlag()) {
            return Map.of(RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_BLOCK_NAME),
                    MinigameUtils.limitIgnoreFormat(Component.text(blockData.getFlag().getAsString()), 16));
        } else {
            return Map.of(RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_BLOCK_NAME),
                    Component.text(blockData.getFlag().getMaterial().translationKey()));
        }
    }

    @Override
    public boolean useInRegions() {
        return false;
    }

    @Override
    public boolean useInNodes() {
        return true;
    }

    @Override
    public boolean checkRegionCondition(MinigamePlayer player, @NotNull Region region) {
        return false;
    }

    @Override
    public boolean checkNodeCondition(MinigamePlayer player, @NotNull Node node) {
        return check(node.getLocation());
    }

    private boolean check(Location location) {
        Block block = location.getBlock();
        return block.getType() == blockData.getFlag().getMaterial() &&
                (!useFullBlockData.getFlag() || block.getBlockData().matches(blockData.getFlag()));
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config, @NotNull String path) {
        blockData.saveValue(config, path);
        useFullBlockData.saveValue(config, path);
        saveInvert(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config, @NotNull String path) {
        blockData.loadValue(config, path);
        useFullBlockData.loadValue(config, path);
        loadInvert(config, path);
    }

    @Override
    public boolean displayMenu(MinigamePlayer player, Menu prev) {
        Menu menu = new Menu(3, getDisplayName(), player);
        menu.addItem(new MenuItemBack(prev), menu.getSize() - 9);

        final MenuItem menuItemBData = blockData.getMenuItem(RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_BLOCK_NAME));
        menu.addItem(menuItemBData);
        final MenuItem menuItemUseData = useFullBlockData.getMenuItem(Material.ENDER_PEARL, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_USEBLOCKDATA_NAME));
        menu.addItem(menuItemUseData);

        addInvertMenuItem(menu);
        menu.displayMenu(player);
        return true;
    }

    @Override
    public boolean playerNeeded() {
        return false;
    }
}
