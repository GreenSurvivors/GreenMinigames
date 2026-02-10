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
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class provides the ability to swap the blocks in two regions that have the same size. If
 * swap region is set to true, it will switch these two regions blocks, if it is set to false, it
 * will replace the TO regions blocks with the FROM regions block.
 * <p>
 * It allows to have template regions that can be copied into game or two switch two regions.
 */
public class RegionSwapAction extends AAction {
    private final @NotNull StringFlag fromRegion = new StringFlag("fromRegion", "");
    private final @NotNull StringFlag toRegion = new StringFlag("toRegion", "");
    private final @NotNull BooleanFlag swapRegion = new BooleanFlag("swapRegion", true);

    protected RegionSwapAction(final @NotNull Key key) {
        super(key);
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
    public void executeRegionAction(final @Nullable MinigamePlayer mgPlayer, final @NotNull Region region) {
        debug(mgPlayer, region);

    }

    /**
     * This method loops through all blocks in either region and saves their BlockState. If both
     * regions are the same size, it will switch the regions blocks or replace the To (target) regions
     * block with the From (start) regions block.
     */
    @Override
    public void executeNodeAction(final @NotNull MinigamePlayer mgPlayer, final @NotNull Node node) {
        debug(mgPlayer, node);

        if (!mgPlayer.isInMinigame()) {
            return;
        }
        final Minigame mgm = mgPlayer.getMinigame();
        final RegionModule rmod = RegionModule.getMinigameModule(mgm);

        if (rmod != null) {
            final @Nullable Region startRegion;
            final @Nullable Region targetRegion;

            if (rmod.hasRegion(fromRegion.getFlag())) {
                startRegion = rmod.getRegion(fromRegion.getFlag());
            } else {
                MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, RegionMessageManager.getBundleKey(),
                        RegionLangKey.ACTION_ERROR_NOREGION,
                        Placeholder.unparsed(RegionPlaceHolderKey.REGION.getKey(), fromRegion.getFlag()));
                return;
            }

            if (rmod.hasRegion(toRegion.getFlag())) {
                targetRegion = rmod.getRegion(toRegion.getFlag());
            } else {
                MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, RegionMessageManager.getBundleKey(),
                        RegionLangKey.ACTION_ERROR_NOREGION,
                        Placeholder.unparsed(RegionPlaceHolderKey.REGION.getKey(), toRegion.getFlag()));
                return;
            }

            if (startRegion != null && targetRegion != null) {
                final @NotNull List<@NotNull BlockState> startRegionBlocks = fillRegionBlockList(startRegion);
                final @NotNull List<@NotNull BlockState> targetRegionBlocks = fillRegionBlockList(targetRegion);

                final @NotNull RecorderData data = mgPlayer.getMinigame().getRecorderData();

                if (startRegionBlocks.size() == targetRegionBlocks.size()) {
                    if (swapRegion.getFlag()) {
                        for (int i = 0; i < targetRegionBlocks.size(); i++) {
                            final @NotNull BlockState startBlockState = startRegionBlocks.get(i);
                            final @NotNull BlockState targetBlockState = targetRegionBlocks.get(i);

                            data.addBlock(targetBlockState, null);
                            data.addBlock(startBlockState, null);

                            Material tempType = targetBlockState.getType();
                            BlockData tempData = targetBlockState.getBlockData();

                            targetBlockState.setType(startBlockState.getType());
                            targetBlockState.setBlockData(startBlockState.getBlockData());
                            targetBlockState.update(true, false);

                            startBlockState.setType(tempType);
                            startBlockState.setBlockData(tempData);
                            startBlockState.update(true, false);
                        }
                    } else {
                        for (int i = 0; i < targetRegionBlocks.size(); i++) {
                            final @NotNull BlockState targetBlockState = targetRegionBlocks.get(i);
                            final @NotNull BlockState startBlockState = startRegionBlocks.get(i);

                            data.addBlock(targetBlockState, null);
                            targetBlockState.setType(startBlockState.getType());
                            targetBlockState.setBlockData(startBlockState.getBlockData());
                            targetBlockState.update(true, false);
                        }
                    }
                } else {
                    MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, RegionMessageManager.getBundleKey(),
                        RegionLangKey.ACTION_REGIONSWAP_ERROR_SIZE);
                }
            }
        } else {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTGAMEMECHANIC,
                Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), RegionModule.getFactory().getKey().value()),
                Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), mgm.getDisplayName()));
        }
    }

    private @NotNull List<@NotNull BlockState> fillRegionBlockList(final @NotNull Region targetRegion) {
        final @NotNull List<@NotNull BlockState> result = new ArrayList<>();

        for (int y = targetRegion.getFirstPoint().blockY(); y <= targetRegion.getSecondPoint().blockY(); y++) {
            for (int x = targetRegion.getFirstPoint().blockX(); x <= targetRegion.getSecondPoint().blockX(); x++) {
                for (int z = targetRegion.getFirstPoint().blockZ(); z <= targetRegion.getSecondPoint().blockZ(); z++) {
                    result.add(targetRegion.getFirstPoint().getWorld().getBlockAt(x, y, z).getState(false));
                }
            }
        }

        return result;
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        fromRegion.saveValue(config);
        toRegion.saveValue(config);
        swapRegion.saveValue(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        fromRegion.loadValue(config);
        toRegion.loadValue(config);
        swapRegion.loadValue(config);

    }

    @Override
    public boolean displayMenu(final @NotNull Menu previous) {
        final @NotNull Menu menu = new Menu(3, getDisplayname(), previous.getIntendedViewer());
        menu.setItem(new MenuItemBack(previous), menu.getSize() - 9);
        menu.addItem(fromRegion.getMenuItem(ItemType.ENDER_EYE, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_REGIONSWAP_FROM_NAME)));
        menu.addItem(swapRegion.getMenuItem(ItemType.ENDER_PEARL, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_REGIONSWAP_SWAP_NAME)));

        menu.addItem(new MenuItemNewLine());
        menu.addItem(toRegion.getMenuItem(ItemType.ENDER_EYE, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_REGIONSWAP_TO_NAME)));

        menu.displayMenu();
        return true;
    }
}
