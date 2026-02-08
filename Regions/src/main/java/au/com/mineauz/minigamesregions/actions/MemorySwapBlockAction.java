package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.BlockTypeFlag;
import au.com.mineauz.minigames.config.BlockTypeListFlag;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.recorder.RecorderData;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import au.com.mineauz.minigamesregions.util.RegionUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class provides the methods necessary to fill a gameboard with pairs of randomly placed
 * blocks. It's a region action and can as such only run inside a region.
 * <p>
 * The user can define two options in the menu.
 * A) the matchBlock, the block that is the placeholder in the game and which will be replaced by the random blocks.
 * B) the white/blacklist, which restricts or removes blocks from the given blockPool to provide a free choice in gameboard design.
 * Removed blocks will not appear on the game board.
 */
public class MemorySwapBlockAction extends AAction {
    /*
     * Building a blockPool to provide the blocks that could be used in the game.
     */
    private static final @NotNull SequencedSet<@NotNull BlockType> blockPool = new LinkedHashSet<>();

    /*
     * Filling the block pool with blocks than can be pulled and pushed by pistons manually
     */
    static {
        /* TODO Maybe an automatic way of dealing with this. Problem: some curation is necessary
         * to prevent blocks that are to visual similar to appear, for example quartz and white
         * concrete. Letting the user sort this out with the blacklist results in a very long
         * blacklist string, which is annoying for the user.
         */

        //Resource blocks
        blockPool.addAll(Tag.BEACON_BASE_BLOCKS.getValues().stream().map(Material::asBlockType).toList());
        blockPool.add(BlockType.COAL_BLOCK);
        blockPool.add(BlockType.WAXED_CUT_COPPER);
        blockPool.add(BlockType.WAXED_EXPOSED_COPPER);
        blockPool.add(BlockType.WAXED_WEATHERED_CUT_COPPER);
        blockPool.add(BlockType.WAXED_OXIDIZED_COPPER);

        //Concrete
        blockPool.add(BlockType.WHITE_CONCRETE);
        blockPool.add(BlockType.ORANGE_CONCRETE);
        blockPool.add(BlockType.MAGENTA_CONCRETE);
        blockPool.add(BlockType.LIGHT_BLUE_CONCRETE);
        blockPool.add(BlockType.YELLOW_CONCRETE);
        blockPool.add(BlockType.LIME_CONCRETE);
        blockPool.add(BlockType.PINK_CONCRETE);
        blockPool.add(BlockType.GRAY_CONCRETE);
        blockPool.add(BlockType.LIGHT_GRAY_CONCRETE);
        blockPool.add(BlockType.CYAN_CONCRETE);
        blockPool.add(BlockType.PURPLE_CONCRETE);
        blockPool.add(BlockType.BLUE_CONCRETE);
        blockPool.add(BlockType.BROWN_CONCRETE);
        blockPool.add(BlockType.GREEN_CONCRETE);
        blockPool.add(BlockType.RED_CONCRETE);
        blockPool.add(BlockType.BLACK_CONCRETE);

        //Ore blocks
        blockPool.addAll(Tag.DIAMOND_ORES.getValues().stream().map(Material::asBlockType).toList());
        blockPool.addAll(Tag.IRON_ORES.getValues().stream().map(Material::asBlockType).toList());
        blockPool.addAll(Tag.REDSTONE_ORES.getValues().stream().map(Material::asBlockType).toList());
        blockPool.addAll(Tag.EMERALD_ORES.getValues().stream().map(Material::asBlockType).toList());
        blockPool.addAll(Tag.GOLD_ORES.getValues().stream().map(Material::asBlockType).toList());
        blockPool.addAll(Tag.LAPIS_ORES.getValues().stream().map(Material::asBlockType).toList());
        blockPool.add(BlockType.NETHER_QUARTZ_ORE);
        blockPool.add(BlockType.ANCIENT_DEBRIS);

        //Wool blocks
        blockPool.addAll(Tag.WOOL.getValues().stream().map(Material::asBlockType).toList());

        //Logs - we explicitly don't use Tag.LOGS since the "xxx_wood" (all side bark) look a lot like "xxx_log" (annual rings on top/bottom)
        blockPool.add(BlockType.OAK_LOG);
        blockPool.add(BlockType.STRIPPED_OAK_LOG);
        blockPool.add(BlockType.SPRUCE_LOG);
        blockPool.add(BlockType.STRIPPED_SPRUCE_LOG);
        blockPool.add(BlockType.BIRCH_LOG);
        blockPool.add(BlockType.STRIPPED_BIRCH_LOG);
        blockPool.add(BlockType.JUNGLE_LOG);
        blockPool.add(BlockType.STRIPPED_JUNGLE_LOG);
        blockPool.add(BlockType.ACACIA_LOG);
        blockPool.add(BlockType.STRIPPED_ACACIA_LOG);
        blockPool.add(BlockType.MANGROVE_LOG);
        blockPool.add(BlockType.STRIPPED_MANGROVE_LOG);
        blockPool.add(BlockType.DARK_OAK_LOG);
        blockPool.add(BlockType.STRIPPED_DARK_OAK_LOG);
        blockPool.add(BlockType.CRIMSON_STEM);
        blockPool.add(BlockType.STRIPPED_CRIMSON_STEM);
        blockPool.add(BlockType.WARPED_STEM);
        blockPool.add(BlockType.STRIPPED_WARPED_STEM);

        //Planks
        blockPool.addAll(Tag.PLANKS.getValues().stream().map(Material::asBlockType).toList());

        //Stone-alike
        blockPool.add(BlockType.STONE);
        blockPool.add(BlockType.SMOOTH_STONE);
        blockPool.add(BlockType.CHISELED_STONE_BRICKS);
        blockPool.add(BlockType.COBBLESTONE);
        blockPool.add(BlockType.MOSSY_COBBLESTONE);
        blockPool.add(BlockType.STONE_BRICKS);
        blockPool.add(BlockType.BRICKS);
        blockPool.add(BlockType.BASALT);
        blockPool.add(BlockType.CALCITE);
        blockPool.add(BlockType.TUFF);
        blockPool.add(BlockType.DRIPSTONE_BLOCK);
        blockPool.add(BlockType.SMOOTH_BASALT);
        blockPool.add(BlockType.POLISHED_BASALT);
        blockPool.add(BlockType.POLISHED_ANDESITE);
        blockPool.add(BlockType.CHISELED_DEEPSLATE);
        blockPool.add(BlockType.POLISHED_DEEPSLATE);
        blockPool.add(BlockType.DEEPSLATE_BRICKS);
        blockPool.add(BlockType.DEEPSLATE);
        blockPool.add(BlockType.DEEPSLATE_TILES);
        blockPool.add(BlockType.POLISHED_BLACKSTONE);
        blockPool.add(BlockType.GILDED_BLACKSTONE);
        blockPool.add(BlockType.CHISELED_POLISHED_BLACKSTONE);
        blockPool.add(BlockType.NETHERRACK);
        blockPool.add(BlockType.NETHER_BRICKS);
        blockPool.add(BlockType.RED_NETHER_BRICKS);
        blockPool.add(BlockType.SMOOTH_QUARTZ);
        blockPool.add(BlockType.CHISELED_QUARTZ_BLOCK);
        blockPool.add(BlockType.QUARTZ_BRICKS);
        blockPool.add(BlockType.QUARTZ_PILLAR);
        blockPool.add(BlockType.PURPUR_BLOCK);
        blockPool.add(BlockType.PURPUR_PILLAR);
        blockPool.add(BlockType.END_STONE_BRICKS);

        //dirt alike
        blockPool.add(BlockType.DIRT);
        blockPool.add(BlockType.MUD);
        blockPool.add(BlockType.PODZOL);
        blockPool.add(BlockType.CLAY);
        blockPool.add(BlockType.SOUL_SAND);
        blockPool.add(BlockType.SOUL_SOIL);
        blockPool.add(BlockType.PACKED_MUD);
        blockPool.add(BlockType.MUD_BRICKS);
        blockPool.add(BlockType.SANDSTONE);
        blockPool.add(BlockType.RED_SANDSTONE);
        blockPool.add(BlockType.AMETHYST_BLOCK);

        //kinda living
        blockPool.add(BlockType.SCULK);
        blockPool.add(BlockType.BONE_BLOCK);
        blockPool.add(BlockType.NETHER_WART_BLOCK);
        blockPool.add(BlockType.WARPED_WART_BLOCK);
        blockPool.add(BlockType.SHROOMLIGHT);
        blockPool.add(BlockType.DRIED_KELP_BLOCK);
        blockPool.add(BlockType.DEAD_BRAIN_CORAL_BLOCK);
        blockPool.add(BlockType.SPONGE);
        blockPool.add(BlockType.HONEYCOMB_BLOCK);
        blockPool.add(BlockType.OCHRE_FROGLIGHT);
        blockPool.add(BlockType.VERDANT_FROGLIGHT);
        blockPool.add(BlockType.PEARLESCENT_FROGLIGHT);

        //elements
        blockPool.add(BlockType.PACKED_ICE);
        blockPool.add(BlockType.BLUE_ICE);
        blockPool.add(BlockType.SNOW_BLOCK);
        blockPool.add(BlockType.MAGMA_BLOCK);
        blockPool.add(BlockType.PRISMARINE_BRICKS);
        blockPool.add(BlockType.DARK_PRISMARINE);
        blockPool.add(BlockType.SEA_LANTERN);

        //usage blocks
        blockPool.add(BlockType.CRAFTING_TABLE);
        blockPool.add(BlockType.FLETCHING_TABLE);
        blockPool.add(BlockType.SMITHING_TABLE);
        blockPool.add(BlockType.BOOKSHELF);

        //todo config for this, also move this standard list into a ressource file
    }

    private final BlockTypeFlag matchType = new BlockTypeFlag("matchtype", BlockType.COBBLESTONE);
    private final BlockTypeListFlag wbList = new BlockTypeListFlag("config.blacklist", new ArrayList<>());
    // is it a white or a blacklist?
    private final BooleanFlag whitelistMode = new BooleanFlag("whitelistmode", false);

    protected MemorySwapBlockAction(final @NotNull Key key) {
        super(key);
    }

    /**
     * Returns an array of BlockType that will consist of all blocks of the block pool minus the
     * ones on the blacklist. The blacklist string format is block1,block2,block4.
     *
     * @return ArrayList<PhantomBlock>
     */
    private @NotNull SequencedSet<@NotNull BlockType> cleanBlockPool() {
        if (wbList.getFlag().isEmpty()) {
            return blockPool;
        }

        SequencedSet<BlockType> output;
        if (whitelistMode.getFlag()) {
            output = blockPool.stream().filter(m -> wbList.getFlag().contains(m)).collect(Collectors.toCollection(LinkedHashSet::new));
        } else {
            output = new LinkedHashSet<>(blockPool);
            wbList.getFlag().forEach(output::remove);
        }

        return output;
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_MEMORYSWAPBLOCK_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.BLOCK;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_FROMBLOCK_NAME), Component.translatable(matchType.getFlag().translationKey()),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_MEMORYSWAPBLOCK_POOLSIZE), Component.text(blockPool.size()),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_MEMORYSWAPBLOCK_WHITELIST_MODE_NAME),
                MinigameMessageManager.getMgMessage(whitelistMode.getFlag() ? MgMiscLangKey.BOOL_TRUE : MgMiscLangKey.BOOL_FALSE),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_MEMORYSWAPBLOCK_WHITELIST_SIZE), Component.text(wbList.getFlag().size())
        );
    }

    @Override
    public boolean useInRegions() {
        return true;
    }

    @Override
    public boolean useInNodes() {
        return false;
    }

    /**
     * This will search for a certain type of block (user definable via the menu) and replaced with a
     * random block from the block pool minus the blacklisted blocks (also user definable via the
     * menu).
     * <p>
     * The block will always have a pair unless there is an odd number of blocks to replace. If there
     * is an odd numbered amount of blocks there will be one unmatched block and player will be
     * warned. If there are more blocks to replace than it is possible, the surplus blocks will be
     * skipped and the player will be warned.
     */
    @Override
    public void executeRegionAction(@Nullable MinigamePlayer mgPlayer, @NotNull Region region) {
        debug(mgPlayer, region);
        final SequencedSet<@NotNull BlockType> localBockTypePool = cleanBlockPool();
        final List<@NotNull Block> blocksToSwap = new ArrayList<>();

        //Collects all blocks to be swapped
        for (int y = (int) region.getMinY(); y <= region.getMaxY(); y++) {
            for (int x = (int) region.getMinX(); x <= region.getMaxX(); x++) {
                for (int z = (int) region.getMinZ(); z <= region.getMaxZ(); z++) {
                    Block block = region.getFirstPoint().getWorld().getBlockAt(x, y, z);

                    if (block.getType().asBlockType() == matchType.getFlag()) {
                        blocksToSwap.add(block);
                    }
                }
            }
        }

        //Sanity checks that can be handled without throwing an exception but need a warning to player
        if (blocksToSwap.size() % 2 != 0) {
            if (mgPlayer != null) {
                MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, RegionMessageManager.getBundleKey(),
                        RegionLangKey.ACTION_MEMORYSWAPBLOCK_ERROR_ODD);
            } else {
                RegionMessageManager.debugMessage("This game board of \"" + region.getName() + "\" has an odd amount of playing fields, there will be unmatched blocks!");
            }
        }
        if (blocksToSwap.size() > 2 * localBockTypePool.size()) {
            if (mgPlayer != null) {
                MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, RegionMessageManager.getBundleKey(),
                        RegionLangKey.ACTION_MEMORYSWAPBLOCK_ERROR_TOOBIG,
                        Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(localBockTypePool.size())));
            }
        }

        // make a random collection of used materials
        if ((2 * localBockTypePool.size()) > blocksToSwap.size()) {
            RegionUtils.shuffle(localBockTypePool, new Random());
        }

        //shuffle blocks to swap, to make picking 2 random ones easy.
        Collections.shuffle(blocksToSwap);

        //iterator to iterate through without an extra loop
        final @NotNull Iterator<BlockType> matIt = localBockTypePool.iterator();

        // to stop in case of uneven size
        final int max = blocksToSwap.size() - 1;

        // for every 2 blocks of the list, set a random material
        for (int i = 0; i < max; i += 2) {
            if (matIt.hasNext()) {
                //save block data in recorder
                RecorderData data = mgPlayer.getMinigame().getRecorderData();
                data.addBlock(blocksToSwap.get(i), null);
                data.addBlock(blocksToSwap.get(i + 1), null);

                BlockType newMat = matIt.next();
                blocksToSwap.get(i).setType(newMat.asMaterial());
                blocksToSwap.get(i + 1).setType(newMat.asMaterial());
            } else {
                break;
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
        matchType.saveValue(config);
        wbList.saveValue(config);
        whitelistMode.saveValue(config);

    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) throws ConfigurateException {
        matchType.loadValue(config);
        wbList.loadValue(config);
        whitelistMode.loadValue(config);

    }

    @Override
    public boolean displayMenu(final @NotNull Menu previous) {
        final @NotNull Menu menu = new Menu(3, getDisplayname(), previous.getIntendedViewer());
        menu.addItem(new MenuItemBack(previous), menu.getSize() - 9);

        //The menu entry for the from-block, aka the block that will be replaced
        menu.addItem(matchType.getMenuItem(RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_MATCHBLOCK_NAME)));

        //Menu entry for the white/blacklist entry, aka the blocks that will be only accounted for / removed from the block pool
        menu.addItem(new MenuItemNewLine());
        menu.addItem(new MenuItemDisplayWhitelist(ItemType.BOOK, MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_MINIGAME_WHITELIST_BLOCK_NAME),
                RegionMessageManager.getMessageList(RegionLangKey.MENU_ACTION_MEMORYSWAPBLOCK_WHITELIST_DESCRIPTION),
                wbList.getFlag(), new Callback<>() {

            @Override
            public Boolean getValue() {
                return whitelistMode.getFlag();
            }

            @Override
            public void setValue(Boolean value) {
                whitelistMode.setFlag(value);
            }
        }, RegionMessageManager.getMessageList(RegionLangKey.MENU_ACTION_MEMORYSWAPBLOCK_WHITELIST_MODE_DESCRIPTION)));

        menu.displayMenu();
        return false;
    }
}
