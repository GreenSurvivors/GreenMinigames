package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.config.BlockDataFlag;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.menu.AMenuItem;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.safelocation.SafeFullLocation;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;

public class MatchBlockCondition extends ACondition {
    private final @NotNull BlockDataFlag blockData = new BlockDataFlag("type", Material.STONE.createBlockData()); //todo datafixerupper rename the name
    private final @NotNull BooleanFlag useFullBlockData = new BooleanFlag("usedur", false); //todo datafixerupper rename the name

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
    public boolean checkRegionCondition(final @Nullable MinigamePlayer mgPlayer, final @NotNull Region region) {
        return false;
    }

    @Override
    public boolean checkNodeCondition(final @Nullable MinigamePlayer mgPlayer, final @NotNull Node node) {
        return check(node.getSafeLocation());
    }

    private boolean check(final @NotNull SafeFullLocation location) {
        final @Nullable Block block = location.getBlockAt();
        return block != null && block.getType() == blockData.getFlag().getMaterial() &&
                (!useFullBlockData.getFlag() || block.getBlockData().matches(blockData.getFlag()));
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        blockData.saveValue(config);
        useFullBlockData.saveValue(config);
        saveInvertedStatus(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        blockData.loadValue(config);
        useFullBlockData.loadValue(config);
        loadInvert(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu prev) {
        final @NotNull Menu menu = new Menu(3, getDisplayName(), prev.getIntendedViewer());
        menu.setItem(new MenuItemBack(prev), menu.getSize() - 9);

        final @NotNull AMenuItem menuItemBData = blockData.getMenuItem(RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_BLOCK_NAME));
        menu.addItem(menuItemBData);
        final @NotNull AMenuItem menuItemUseData = useFullBlockData.getMenuItem(ItemType.ENDER_PEARL, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_USEBLOCKDATA_NAME));
        menu.addItem(menuItemUseData);

        addInvertMenuItem(menu);
        menu.displayMenu();
        return true;
    }

    @Override
    public boolean playerNeeded() {
        return false;
    }
}
