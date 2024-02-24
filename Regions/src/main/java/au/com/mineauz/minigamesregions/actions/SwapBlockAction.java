package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.config.BlockDataFlag;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.managers.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.recorder.RecorderData;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.material.Directional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SwapBlockAction extends AAction { // todo once paper no longer relocates Craftbukkit, merge Blockdata via nms state, so everything that can remain of old data will, if the keep setting is on
    private final BlockDataFlag matchType = new BlockDataFlag(Material.STONE.createBlockData(), "matchtype");
    private final BlockDataFlag toData = new BlockDataFlag(Material.COBBLESTONE.createBlockData(), "totype");
    private final BooleanFlag keepAttachment = new BooleanFlag(false, "keepattachment");

    protected SwapBlockAction(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SWAPBLOCK_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.BLOCK;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_FROMBLOCK_NAME),
                MinigameUtils.limitIgnoreFormat(Component.text(matchType.getFlag().getAsString()), 16),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_TOBLOCK_NAME),
                MinigameUtils.limitIgnoreFormat(Component.text(toData.getFlag().getAsString()), 16),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SWAPBLOCK_KEEP_NAME),
                MinigameMessageManager.getMgMessage(keepAttachment.getFlag() ? MgCommandLangKey.COMMAND_STATE_ENABLED : MgCommandLangKey.COMMAND_STATE_DISABLED));
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
    public void executeRegionAction(@Nullable MinigamePlayer mgPlayer, @NotNull Region region) {
        debug(mgPlayer, region);
        for (int y = region.getFirstPoint().getBlockY(); y <= region.getSecondPoint().getBlockY(); y++) {
            for (int x = region.getFirstPoint().getBlockX(); x <= region.getSecondPoint().getBlockX(); x++) {
                for (int z = region.getFirstPoint().getBlockZ(); z <= region.getSecondPoint().getBlockZ(); z++) {
                    Block block = region.getFirstPoint().getWorld().getBlockAt(x, y, z);

                    if (block.getBlockData().getMaterial() == matchType.getFlag().getMaterial()) {

                        // Block matches, now replace it
                        BlockData newBlockData = toData.getFlag().clone();
                        BlockFace facing = null;

                        if (keepAttachment.getFlag()) {
                            // Keep attachments if possible
                            BlockData data = block.getBlockData();
                            if (data instanceof Directional) {
                                facing = ((Directional) data).getFacing();
                            }
                        }
                        if (newBlockData instanceof Directional && facing != null) {
                            ((Directional) newBlockData).setFacingDirection(facing);
                        }

                        RecorderData data = region.getMinigame().getRecorderData();
                        data.addBlock(block, null);

                        // Update block type
                        block.setBlockData(newBlockData);
                    }
                }
            }
        }
    }

    @Override
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer,
                                  @NotNull Node node) {
        debug(mgPlayer, node);
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        matchType.saveValue(config, path);
        toData.saveValue(config, path);
        keepAttachment.saveValue(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config,
                              @NotNull String path) {
        matchType.loadValue(config, path);
        toData.loadValue(config, path);
        keepAttachment.loadValue(config, path);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, Menu previous) {
        Menu m = new Menu(3, getDisplayname(), mgPlayer);
        m.addItem(new MenuItemBack(previous), m.getSize() - 9);
        m.addItem(new MenuItemBlockData(matchType.getFlag().getMaterial(), RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_FROMBLOCK_NAME), new Callback<>() {

            @Override
            public BlockData getValue() {
                return matchType.getFlag();
            }

            @Override
            public void setValue(BlockData value) {
                matchType.setFlag(value);
            }


        }));
        m.addItem(new MenuItemNewLine());
        m.addItem(new MenuItemBlockData(toData.getFlag().getMaterial(), RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_TOBLOCK_NAME), new Callback<>() {

            @Override
            public BlockData getValue() {
                return toData.getFlag();
            }

            @Override
            public void setValue(BlockData value) {
                toData.setFlag(value);
            }
        }));

        m.addItem(keepAttachment.getMenuItem(Material.PISTON, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SWAPBLOCK_KEEP_NAME),
                RegionMessageManager.getMessageList(RegionLangKey.MENU_ACTION_SWAPBLOCK_KEEP_DESCRIPTION)));
        m.displayMenu(mgPlayer);
        return true;
    }
}
