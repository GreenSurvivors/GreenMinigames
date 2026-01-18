package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.BlockDataFlag;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.recorder.RecorderData;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

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
    private final BlockDataFlag toData = new BlockDataFlag("toData", Material.WHITE_WOOL.createBlockData());
    private final IntegerFlag percentageChance = new IntegerFlag("percentagechance", 50);
    private final BooleanFlag replaceAll = new BooleanFlag("replaceAll", true);

    protected RandomFillingAction(final @NotNull NamespacedKey key) {
        super(key);
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
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_TOBLOCK_NAME), Component.text(toData.getFlag().getMaterial().translationKey()),
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
        if (mgPlayer == null || mgPlayer.getMinigame() == null || region.getWorld() == null) {
            return;
        }

        Location temp = region.getFirstPoint().toLocation();
        Random rndGen = ThreadLocalRandom.current();
        RecorderData data = mgPlayer.getMinigame().getRecorderData();

        for (int y = region.getFirstPoint().blockY(); y <= region.getSecondPoint().blockY(); y++) {
            temp.setY(y);
            for (int x = region.getFirstPoint().blockX(); x <= region.getSecondPoint().blockX(); x++) {
                temp.setX(x);
                for (int z = region.getFirstPoint().blockZ(); z <= region.getSecondPoint().blockZ(); z++) {
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
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        toData.saveValue(config);
        percentageChance.saveValue(config);
        replaceAll.saveValue(config);

        // dataFixerUpper
        config.removeChild("totype");
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        percentageChance.loadValue(config);
        replaceAll.loadValue(config);

        //dataFixerUpper
        Material mat = Material.matchMaterial(config.node().getString(""));
        if (mat != null) {
            toData.setFlag(mat.createBlockData());
        } else {
            toData.loadValue(config);
        }
    }

    @Override
    public boolean displayMenu(final @NotNull MinigamePlayer mgPlayer, @NotNull Menu previous) {
        Menu m = new Menu(4, getDisplayname(), mgPlayer);
        m.addItem(new MenuItemBack(previous), m.getSize() - 9);

        //The menu entry for the block that will be placed
        toData.getMenuItem(RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_TOBLOCK_NAME));

        //Percentage of blocks that will get replaced
        m.addItem(new MenuItemNewLine());
        m.addItem(new MenuItemInteger(ItemType.BOOK, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_RANDOMFILLING_PERCENT_NAME),
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
        m.addItem(replaceAll.getMenuItem(ItemType.ENDER_PEARL, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_RANDOMFILLING_MISSES_NAME)));

        m.displayMenu(mgPlayer);

        return false;
    }
}
