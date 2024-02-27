package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuItemNewLine;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.recorder.RecorderData;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.RegionModule;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionPlaceHolderKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;

/**
 * This class provides the ability to swap the blocks in two regions that have the same size. If
 * swap region is set to true, it will switch these two regions blocks, if it is set to false, it
 * will replace the TO regions blocks with the FROM regions block.
 * <p>
 * It allows to have template regions that can be copied into game or two switch two regions.
 */
public class RegionSwapAction extends AAction {
    private final StringFlag fromRegion = new StringFlag("", "fromRegion");
    private final StringFlag toRegion = new StringFlag("", "toRegion");
    private final BooleanFlag swapRegion = new BooleanFlag(true, "swapRegion");

    protected RegionSwapAction(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_REGIONSWAP_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.BLOCK;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_REGIONSWAP_FROM_NAME), Component.text(fromRegion.getFlag()),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_REGIONSWAP_TO_NAME), Component.text(toRegion.getFlag()),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_REGIONSWAP_SWAP_NAME),
                MinigameMessageManager.getMgMessage(swapRegion.getFlag() ? MgCommandLangKey.COMMAND_STATE_ENABLED : MgCommandLangKey.COMMAND_STATE_DISABLED));
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
    public void executeRegionAction(@Nullable MinigamePlayer mgPlayer, @NotNull Region region) {
        debug(mgPlayer, region);

    }

    /**
     * This method loops through all blocks in either region and saves their BlockState. If both
     * regions are the same size, it will switch the regions blocks or replace the To (target) regions
     * block with the From (start) regions block.
     */
    @Override
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node node) {
        debug(mgPlayer, node);

        Region startRegion = null;
        Region targetRegion = null;
        ArrayList<BlockState> startRegionBlocks = new ArrayList<>();
        ArrayList<BlockState> targetRegionBlocks = new ArrayList<>();

        if (!mgPlayer.isInMinigame()) {
            return;
        }
        Minigame mgm = mgPlayer.getMinigame();

        if (mgm != null) {
            RegionModule rmod = RegionModule.getMinigameModule(mgm);

            if (rmod != null) {
                if (rmod.hasRegion(fromRegion.getFlag())) {
                    startRegion = rmod.getRegion(fromRegion.getFlag());
                } else {
                    MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, RegionMessageManager.getBundleKey(),
                            RegionLangKey.ACTION_ERROR_NOREGION,
                            Placeholder.unparsed(RegionPlaceHolderKey.REGION.getKey(), fromRegion.getFlag()));
                }

                if (rmod.hasRegion(toRegion.getFlag())) {
                    targetRegion = rmod.getRegion(toRegion.getFlag());
                } else {
                    MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, RegionMessageManager.getBundleKey(),
                            RegionLangKey.ACTION_ERROR_NOREGION,
                            Placeholder.unparsed(RegionPlaceHolderKey.REGION.getKey(), toRegion.getFlag()));
                }
            } else {
                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTGAMEMECHANIC,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), RegionModule.getFactory().getName()),
                        Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), mgm.getDisplayName()));
            }
        }

        if (startRegion != null && targetRegion != null) {

            fillRegionBlockList(startRegion, startRegionBlocks);

            fillRegionBlockList(targetRegion, targetRegionBlocks);

            if (startRegionBlocks.size() == targetRegionBlocks.size() && swapRegion.getFlag()) {
                for (int i = 0; i < targetRegionBlocks.size(); i++) {
                    RecorderData data = mgPlayer.getMinigame().getRecorderData();
                    data.addBlock(targetRegionBlocks.get(i).getBlock(), null);
                    data.addBlock(startRegionBlocks.get(i).getBlock(), null);

                    Material tempType = targetRegionBlocks.get(i).getType();
                    BlockData tempData = targetRegionBlocks.get(i).getBlockData();

                    targetRegionBlocks.get(i).setType(startRegionBlocks.get(i).getType());
                    targetRegionBlocks.get(i).setBlockData(startRegionBlocks.get(i).getBlockData());
                    targetRegionBlocks.get(i).update(true, false);

                    startRegionBlocks.get(i).setType(tempType);
                    startRegionBlocks.get(i).setBlockData(tempData);
                    startRegionBlocks.get(i).update(true, false);
                }

            } else if (startRegionBlocks.size() == targetRegionBlocks.size()) {
                for (int i = 0; i < targetRegionBlocks.size(); i++) {
                    RecorderData data = mgPlayer.getMinigame().getRecorderData();
                    data.addBlock(targetRegionBlocks.get(i).getBlock(), null);
                    targetRegionBlocks.get(i).setType(startRegionBlocks.get(i).getType());
                    targetRegionBlocks.get(i).setBlockData(startRegionBlocks.get(i).getBlockData());
                    targetRegionBlocks.get(i).update(true, false);
                }
            } else {
                MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, RegionMessageManager.getBundleKey(),
                        RegionLangKey.ACTION_REGIONSWAP_ERROR_SIZE);
            }
        }

    }

    private void fillRegionBlockList(Region targetRegion, ArrayList<BlockState> targetRegionBlocks) {
        for (int y = targetRegion.getFirstPoint().getBlockY(); y <= targetRegion.getSecondPoint().getBlockY(); y++) {
            for (int x = targetRegion.getFirstPoint().getBlockX(); x <= targetRegion.getSecondPoint().getBlockX(); x++) {
                for (int z = targetRegion.getFirstPoint().getBlockZ(); z <= targetRegion.getSecondPoint().getBlockZ(); z++) {
                    targetRegionBlocks.add(targetRegion.getFirstPoint().getWorld().getBlockAt(x, y, z).getState());
                }
            }
        }
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config, @NotNull String path) {
        fromRegion.saveValue(config, path);
        toRegion.saveValue(config, path);
        swapRegion.saveValue(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config, @NotNull String path) {
        fromRegion.loadValue(config, path);
        toRegion.loadValue(config, path);
        swapRegion.loadValue(config, path);

    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, Menu previous) {
        Menu m = new Menu(3, getDisplayname(), mgPlayer);
        m.addItem(new MenuItemBack(previous), m.getSize() - 9);
        m.addItem(fromRegion.getMenuItem(Material.ENDER_EYE, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_REGIONSWAP_FROM_NAME)));
        m.addItem(swapRegion.getMenuItem(Material.ENDER_PEARL, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_REGIONSWAP_SWAP_NAME)));

        m.addItem(new MenuItemNewLine());
        m.addItem(toRegion.getMenuItem(Material.ENDER_EYE, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_REGIONSWAP_TO_NAME)));

        m.displayMenu(mgPlayer);
        return true;
    }

}
