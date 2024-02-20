package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.BlockDataFlag;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.IntegerFlag;
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
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/***
 * This action fills a region randomly with a new block. There are two modes. Either "replace all",
 * where every block in that region is either replaced by air or the chosen block, 
 * or "replace selective" where blocks in the region are only replaced by the chosen block. 
 *
 */
public class RandomFillingAction extends AAction {
    private final BlockDataFlag toData = new BlockDataFlag(Material.WHITE_WOOL.createBlockData(), "toData");
    private final IntegerFlag percentageChance = new IntegerFlag(50, "percentagechance");
    private final BooleanFlag replaceAll = new BooleanFlag(true, "replaceAll");

    protected RandomFillingAction(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_RANDOMFILLING_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.BLOCK;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable ComponentLike> describe() {
        return Map.of(
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_RANDOMFILLING_TOBLOCK_NAME), Component.text(toData.getFlag().getMaterial().translationKey()),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_RANDOMFILLING_PERCENT_NAME), Component.text(percentageChance.getFlag()),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_RANDOMFILLING_MISSES_NAME),
                MinigameMessageManager.getMgMessage(replaceAll.getFlag() ? MgCommandLangKey.COMMAND_STATE_ENABLED : MgCommandLangKey.COMMAND_STATE_DISABLED));
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
        if (mgPlayer == null || mgPlayer.getMinigame() == null) {
            return;
        }

        Location temp = region.getFirstPoint();
        Random rndGen = ThreadLocalRandom.current();
        RecorderData data = mgPlayer.getMinigame().getRecorderData();

        for (int y = region.getFirstPoint().getBlockY(); y <= region.getSecondPoint().getBlockY(); y++) {
            temp.setY(y);
            for (int x = region.getFirstPoint().getBlockX(); x <= region.getSecondPoint().getBlockX(); x++) {
                temp.setX(x);
                for (int z = region.getFirstPoint().getBlockZ(); z <= region.getSecondPoint().getBlockZ(); z++) {
                    temp.setZ(z);
                    int randomDraw = rndGen.nextInt(100);  //Generating a number between [0-99]
                    randomDraw++;                //Adding one to handle edge cases (0 %, 100 %) correctly.

                    data.addBlock(temp.getBlock(), null);

                    if (randomDraw <= percentageChance.getFlag()) {
                        temp.getBlock().setBlockData(toData.getFlag(), false);
                    } else if (replaceAll.getFlag()) {
                        temp.getBlock().setType(Material.AIR);
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
    public void saveArguments(@NotNull FileConfiguration config, @NotNull String path) {
        toData.saveValue(config, path);
        percentageChance.saveValue(config, path);
        replaceAll.saveValue(config, path);

        // dataFixerUpper
        config.set(path + "totype", null);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config, @NotNull String path) {
        percentageChance.loadValue(config, path);
        replaceAll.loadValue(config, path);

        //dataFixerUpper
        Material mat = Material.matchMaterial(config.getString(path + "totype", ""));
        if (mat != null) {
            toData.setFlag(mat.createBlockData());
        } else {
            toData.loadValue(config, path);
        }
    }

    @Override
    public boolean displayMenu(final @NotNull MinigamePlayer mgPlayer, Menu previous) {

        Menu m = new Menu(4, getDisplayname(), mgPlayer);
        m.addItem(new MenuItemBack(previous), m.getSize() - 9);

        //The menu entry for the block that will be placed
        toData.getMenuItem(RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_RANDOMFILLING_TOBLOCK_NAME));

        //Percentage of blocks that will get replaced
        m.addItem(new MenuItemNewLine());
        m.addItem(new MenuItemInteger(Material.BOOK, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_RANDOMFILLING_PERCENT_NAME),
                new Callback<>() {

                    @Override
                    public Integer getValue() {
                        return percentageChance.getFlag();
                    }

                    @Override
                    public void setValue(Integer value) {
                        percentageChance.setFlag(value);
                    }

                }, 0, 100));

        //Replace all or replace selectively
        m.addItem(new MenuItemNewLine());
        m.addItem(replaceAll.getMenuItem(Material.ENDER_PEARL, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_RANDOMFILLING_MISSES_NAME)));

        m.displayMenu(mgPlayer);

        return false;
    }
}
