package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.config.BlockDataFlag;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SetBlockAction extends AAction {
    private final BlockDataFlag blockDataFlag = new BlockDataFlag(Material.STONE.createBlockData(), "type");//todo rename flag
    private final BooleanFlag useBlockData = new BooleanFlag(false, "usedur");//todo rename flag

    protected SetBlockAction(@NotNull String name) {
        super(name);
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
        Location temp = region.getFirstPoint();
        for (int y = region.getFirstPoint().getBlockY(); y <= region.getSecondPoint().getBlockY(); y++) {
            temp.setY(y);
            for (int x = region.getFirstPoint().getBlockX(); x <= region.getSecondPoint().getBlockX(); x++) {
                temp.setX(x);
                for (int z = region.getFirstPoint().getBlockZ(); z <= region.getSecondPoint().getBlockZ(); z++) {
                    temp.setZ(z);

                    BlockState bs = temp.getBlock().getState();
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
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer,
                                  @NotNull Node node) {
        debug(mgPlayer, node);
        BlockState bs = node.getLocation().getBlock().getState();
        if (useBlockData.getFlag()) {
            bs.setBlockData(blockDataFlag.getFlag());
        } else {
            bs.setBlockData(blockDataFlag.getFlag().getMaterial().createBlockData());
        }
        bs.update(true);
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        blockDataFlag.saveValue(config, path);
        useBlockData.saveValue(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        blockDataFlag.loadValue(config, path);
        useBlockData.loadValue(config, path);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, Menu previous) {
        Menu menu = new Menu(3, getDisplayname(), mgPlayer);
        menu.addItem(new MenuItemBack(previous), menu.getSize() - 9);
        menu.addItem(blockDataFlag.getMenuItem(RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_BLOCK_NAME)));
        menu.addItem(useBlockData.getMenuItem(Material.ENDER_PEARL, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_USEBLOCKDATA_NAME)));
        menu.displayMenu(mgPlayer);
        return true;
    }
}
