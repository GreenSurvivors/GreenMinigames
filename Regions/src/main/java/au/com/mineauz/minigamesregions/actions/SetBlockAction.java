package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.config.BlockDataFlag;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;

public class SetBlockAction extends AAction {
    private final BlockDataFlag blockDataFlag = new BlockDataFlag("type", Material.STONE.createBlockData());
    private final BooleanFlag useBlockData = new BooleanFlag("usedur", false);//todo rename flag

    protected SetBlockAction(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SETBLOCK_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.BLOCK;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        if (useBlockData.getFlag()) {
            return Map.of(RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_BLOCK_NAME),
                    MinigameUtils.limitIgnoreFormat(Component.text(blockDataFlag.getFlag().getAsString()), 16));
        } else {
            return Map.of(RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_BLOCK_NAME),
                    Component.text(blockDataFlag.getFlag().getMaterial().translationKey()));
        }
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
    public void executeRegionAction(@Nullable MinigamePlayer mgPlayer, @NotNull Region region) {
        debug(mgPlayer, region);

        if (region.getWorld() == null) {
            return;
        }

        for (int y = region.getFirstPoint().blockY(); y <= region.getSecondPoint().blockY(); y++) {
            for (int x = region.getFirstPoint().blockX(); x <= region.getSecondPoint().blockX(); x++) {
                for (int z = region.getFirstPoint().blockZ(); z <= region.getSecondPoint().blockZ(); z++) {

                    final @NotNull BlockState bs = region.getWorld().getBlockAt(x, y, z).getState();
                    if (useBlockData.getFlag()) {
                        bs.setBlockData(blockDataFlag.getFlag());
                    } else {
                        bs.setBlockData(blockDataFlag.getFlag().getMaterial().createBlockData());
                    }
                    bs.update(true);
                }
            }
        }
    }

    @Override
    public void executeNodeAction(final @NotNull MinigamePlayer mgPlayer, final @NotNull Node node) {
        debug(mgPlayer, node);
        if (node.getSafeLocation().getBlockAt() == null) {
            return;
        }

        final @NotNull BlockState bs = node.getSafeLocation().getBlockAt().getState(false);
        if (useBlockData.getFlag()) {
            bs.setBlockData(blockDataFlag.getFlag());
        } else {
            bs.setBlockData(blockDataFlag.getFlag().getMaterial().createBlockData());
        }
        bs.update(true);
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        blockDataFlag.saveValue(config);
        useBlockData.saveValue(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        blockDataFlag.loadValue(config);
        useBlockData.loadValue(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu previous) {
        final @NotNull Menu menu = new Menu(3, getDisplayname(), previous.getIntendedViewer());
        menu.setItem(new MenuItemBack(previous), menu.getSize() - 9);
        menu.addItem(blockDataFlag.getMenuItem(RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_BLOCK_NAME)));
        menu.addItem(useBlockData.getMenuItem(ItemType.ENDER_PEARL, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_USEBLOCKDATA_NAME)));
        menu.displayMenu();
        return true;
    }
}
