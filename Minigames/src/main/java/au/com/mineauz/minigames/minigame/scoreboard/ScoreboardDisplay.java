package au.com.mineauz.minigames.minigame.scoreboard;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.safelocation.SafeBlockLocation;
import au.com.mineauz.minigames.stats.*;
import com.google.common.base.Preconditions;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.block.sign.Side;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.NumberConversions;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ScoreboardDisplay {
    public static final int DEFAULT_WIDTH = 3;
    public static final int DEFAULT_HEIGHT = 3;
    protected static final @NotNull NamespacedKey SCOREBOARD_MINIGAME_KEY = new NamespacedKey(Minigames.getPlugin(), "scoreboard_minigame");
    private final @NotNull SafeBlockLocation rootBlock;
    private final @MonotonicNonNull Minigames plugin = Minigames.getPlugin();
    private final @NotNull Minigame minigame;
    private final int width;
    private final int height;
    private final @NotNull BlockFace facing;
    private @NotNull MinigameStat stat;
    private @NotNull StatisticValueField field;
    private @NotNull ScoreboardOrder order;
    private StatSettings settings;

    private @NotNull List<@NotNull StoredStat> stats;

    private boolean needsLoad;

    public ScoreboardDisplay(final @NotNull Minigame minigame, final int width, final int height, final @NotNull SafeBlockLocation rootBlock, final @NotNull BlockFace facing) {
        this.minigame = minigame;
        this.width = width;
        this.height = height;
        this.rootBlock = rootBlock;
        this.facing = facing;

        // Default values
        stat = MinigameStatistics.Wins;
        field = StatisticValueField.Total;
        order = ScoreboardOrder.DESCENDING;

        stats = new ArrayList<>(width * height * 2);
        needsLoad = true;
    }

    public static @Nullable ScoreboardDisplay load(final @NotNull Minigame minigame, final @NotNull CommentedConfigurationNode node) throws SerializationException {
        final int width = node.node("width").getInt();
        final int height = node.node("height").getInt();
        final @Nullable SafeBlockLocation location = node.node("location").get(TypeToken.get(SafeBlockLocation.class));
        final @Nullable BlockFace facing = BlockFace.valueOf(node.node("dir").getString());

        // from invalid world
        if (location == null || location.getWorld() == null) {
            return null;
        }

        ScoreboardDisplay display = new ScoreboardDisplay(minigame, width, height, location, facing);
        display.setOrder(node.node("order").get(TypeToken.get(ScoreboardOrder.class)));
        MinigameStat stat = MinigameStatistics.getStat(node.node("stat").getString("wins"));
        StatisticValueField field = node.node("field").get(TypeToken.get(StatisticValueField.class), StatisticValueField.Total);
        display.setStat(stat, field);

        final @Nullable Block block = location.getBlockAt(); // should never be null, since we are checking the existence of the world right above

        // this is a datafixerupper. In the future this just will get set whenever the sign gets placed.
        if (block != null && block.getState(false) instanceof PersistentDataHolder persistentDataHolder) {
            persistentDataHolder.getPersistentDataContainer().set(SCOREBOARD_MINIGAME_KEY, PersistentDataType.STRING, minigame.getName());
        }

        return display;
    }

    public static @Nullable String getMinigameOfScoreboardString(final @NotNull Sign sign) {
        return sign.getPersistentDataContainer().get(SCOREBOARD_MINIGAME_KEY, PersistentDataType.STRING);
    }

    public @NotNull SafeBlockLocation getRoot() {
        return rootBlock;
    }

    public @NotNull MinigameStat getStat() {
        return stat;
    }

    public @NotNull StatisticValueField getField() {
        return field;
    }

    public void setStat(@NotNull MinigameStat stat, @NotNull StatisticValueField field) {
        this.stat = stat;
        this.field = field;
    }

    public @NotNull ScoreboardOrder getOrder() {
        return order;
    }

    public void setOrder(@NotNull ScoreboardOrder order) {
        this.order = order;
        stats.clear();
        needsLoad = true;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public @NotNull Minigame getMinigame() {
        return minigame;
    }

    public @NotNull BlockFace getFacing() {
        return facing;
    }

    public boolean needsLoad() {
        return needsLoad;
    }

    private @NotNull List<@NotNull Block> getSignBlocks(boolean onlySigns) {
        // Find the horizontal direction (going across the signs, left to right)
        BlockFace horizontal = switch (facing) {
            case NORTH -> BlockFace.WEST;
            case SOUTH -> BlockFace.EAST;
            case WEST -> BlockFace.SOUTH;
            case EAST -> BlockFace.NORTH;
            default -> throw new AssertionError("Invalid facing " + facing);
        };

        final @NotNull List<@NotNull Block> blocks = new ArrayList<>(width * height);

        // Find the corner that is the top left part of the scoreboard
        SafeBlockLocation min = rootBlock.offset(NumberConversions.floor(-horizontal.getModX() * ((double) width / 2.0D)), -1, NumberConversions.floor(-horizontal.getModZ() * ((double) width / 2.0D)));

        // Grab each sign of the scoreboards in order
        Block block = min.getBlockAt();

        for (int y = 0; y < height; ++y) {
            Block start = block;
            for (int x = 0; x < width; ++x) {
                // Only add signs
                if (Tag.WALL_SIGNS.isTagged(block.getType()) || !onlySigns && block.getType().isAir()) {
                    blocks.add(block);
                }

                block = block.getRelative(horizontal);
            }
            block = start.getRelative(BlockFace.DOWN);
        }

        return blocks;
    }

    /**
     * Updates all signs with the current values of the stats
     */
    public void updateSigns() {
        settings = minigame.getSettings(stat);

        placeRootSign();

        List<Block> signs = getSignBlocks(true);

        int nextIndex = 0;
        for (Block sign : signs) {
            if (nextIndex <= stats.size() - 2) {
                updateSign(sign, nextIndex + 1, stats.get(nextIndex++), stats.get(nextIndex++));
            } else if (nextIndex <= stats.size() - 1) {
                updateSign(sign, nextIndex + 1, stats.get(nextIndex++));
            } else {
                clearSign(sign);
            }
        }
    }

    private void updateSign(@NotNull Block block, int place, @NotNull StoredStat @NotNull ... stats) {
        Preconditions.checkArgument(stats.length >= 1 && stats.length <= 2);

        Sign sign = (Sign) block.getState();
        sign.getSide(Side.FRONT).line(0, MinigameUtils.limitIgnoreFormat(Component.text(place + ". ").color(NamedTextColor.GREEN).append(stats[0].getPlayerDisplayName().color(NamedTextColor.BLACK)), 15));
        sign.getSide(Side.FRONT).line(1, MinigameUtils.limitIgnoreFormat(stat.displayValueSign(stats[0].getValue(), settings).color(NamedTextColor.BLUE), 15));

        if (stats.length == 2) {
            ++place;
            sign.getSide(Side.FRONT).line(2, MinigameUtils.limitIgnoreFormat(Component.text(place + ". ").color(NamedTextColor.GREEN).append(stats[1].getPlayerDisplayName().color(NamedTextColor.BLACK)), 15));
            sign.getSide(Side.FRONT).line(3, MinigameUtils.limitIgnoreFormat(stat.displayValueSign(stats[1].getValue(), settings).color(NamedTextColor.BLUE), 15));
        } else {
            sign.getSide(Side.FRONT).line(2, Component.empty());
            sign.getSide(Side.FRONT).line(3, Component.empty());
        }

        sign.update();
    }

    public void displayMenu(@NotNull MinigamePlayer player) {
        final Menu setupMenu = new Menu(3, MgMenuLangKey.MENU_SCOREBOARD_SETUP_NAME, player);

        StatSettings settings = minigame.getSettings(stat);
        final MenuItemCustom statisticChoice = new MenuItemCustom(ItemType.WRITABLE_BOOK, MgMenuLangKey.MENU_SCOREBOARD_STATISTIC_NAME,
            List.of(settings.getDisplayName().color(NamedTextColor.GREEN)));

        final MenuItemCustom fieldChoice = new MenuItemCustom(ItemType.PAPER, MgMenuLangKey.MENU_SCOREBOARD_STATISTIC_FIELD_NAME,
            List.of(field.getTitle().color(NamedTextColor.GREEN)));

        statisticChoice.setClick(() -> {
            Menu childMenu = MinigameStatistics.createStatSelectMenu(setupMenu, new Callback<>() {
                @Override
                public MinigameStat getValue() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setValue(@NotNull MinigameStat value) {
                    stat = value;
                    StatSettings settings12 = minigame.getSettings(stat);
                    statisticChoice.setBaseDescriptionPart(List.of(settings12.getDisplayName().color(NamedTextColor.GREEN)));

                    // Check that the field is valid
                    StatisticValueField first = null;
                    boolean valid = false;
                    for (StatisticValueField sfield : settings12.getFormat().getFields()) {
                        if (first == null) {
                            first = sfield;
                        }

                        if (sfield == field) {
                            valid = true;
                            break;
                        }
                    }

                    // Update the field
                    if (!valid) {
                        field = first;
                        fieldChoice.setBaseDescriptionPart(List.of(value.getDisplayName().color(NamedTextColor.GREEN)));
                    }
                }
            });

            childMenu.displayMenu(setupMenu.getViewer());
            return ItemStack.empty();
        });

        fieldChoice.setClick(() -> {
            StatSettings settings1 = minigame.getSettings(stat);
            Menu childMenu = MinigameStatistics.createStatFieldSelectMenu(setupMenu, settings1.getFormat(), new Callback<>() {
                @Override
                public StatisticValueField getValue() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setValue(@NotNull StatisticValueField value) {
                    field = value;
                    fieldChoice.setBaseDescriptionPart(List.of(value.getTitle().color(NamedTextColor.GREEN)));
                }
            });

            childMenu.displayMenu(setupMenu.getViewer());
            return ItemStack.empty();
        });

        setupMenu.addItem(statisticChoice);
        setupMenu.addItem(fieldChoice);

        setupMenu.addItem(new MenuItemEnum<>(ItemType.ENDER_PEARL, MgMenuLangKey.MENU_SCOREBOARD_ORDER_NAME, new Callback<>() {

            @Override
            public @NotNull ScoreboardOrder getValue() {
                return order;
            }

            @Override
            public void setValue(@NotNull ScoreboardOrder value) {
                order = value;
            }
        }, ScoreboardOrder.class));

        setupMenu.addItem(new MenuItemScoreboardSave(MenuUtility.createType(), MgMenuLangKey.MENU_SCOREBOARD_CREATE_NAME, this),
            setupMenu.getSize() - 1);
        setupMenu.displayMenu(player);
    }

    private void clearSign(@NotNull Block block) {
        Sign sign = (Sign) block.getState();
        sign.getSide(Side.FRONT).line(0, Component.empty());
        sign.getSide(Side.FRONT).line(1, Component.empty());
        sign.getSide(Side.FRONT).line(2, Component.empty());
        sign.getSide(Side.FRONT).line(3, Component.empty());
        sign.update();
    }

    public void deleteSigns() {
        List<Block> blocks = getSignBlocks(true);

        for (Block block : blocks) {
            block.setBlockData(BlockType.AIR.createBlockData());
        }
    }

    public void placeSigns(@NotNull Material material) throws IllegalArgumentException {
        if (!Tag.WALL_SIGNS.isTagged(material)) {
            throw new IllegalArgumentException("Wrong material for ScoreboardDisplay! (expected some kind of (wall) sign, got: " + material);
        }

        List<Block> blocks = getSignBlocks(false);

        for (Block block : blocks) {
            block.setType(material);
            Directional directional = (Directional) block.getBlockData();
            directional.setFacing(facing);
            block.setBlockData(directional);
        }
    }

    public void save(final @NotNull ConfigurationNode config) throws SerializationException {
        config.node("height").set(height);
        config.node("width").set(width);
        config.node("dir").set(facing.name());
        config.node("stat").set(stat.getName());
        config.node("field").set(field.name());
        config.node("order").set(order.name());
        config.node("location").set(rootBlock);
    }

    public void placeRootSign() {
        // For external calls
        if (settings == null) {
            settings = minigame.getSettings(stat);
        }

        Block root = rootBlock.getBlockAt();
        if (root != null) {
            if (Tag.ALL_SIGNS.isTagged(root.getType())) {
                BlockState state = root.getState(false);
                if (state instanceof Sign sign) {
                    sign.getSide(Side.FRONT).line(0, minigame.getDisplayName().color(NamedTextColor.BLUE));
                    sign.getSide(Side.FRONT).line(1, settings.getDisplayName().color(NamedTextColor.GREEN));
                    sign.getSide(Side.FRONT).line(2, field.getTitle().color(NamedTextColor.GREEN));
                    sign.getSide(Side.FRONT).line(3, Component.text("(" + WordUtils.capitalizeFully(order.toString()) + ")"));
                    sign.getPersistentDataContainer().set(SCOREBOARD_MINIGAME_KEY, PersistentDataType.STRING, minigame.getName());
                    sign.update();

                } else {
                    plugin.getComponentLogger().warn("No Root Sign Block at: " + root.getLocation());
                }
            } else {
                plugin.getComponentLogger().warn("No Root Sign Block at: " + root.getLocation());
            }
        } else {
            plugin.getComponentLogger().warn("World " + rootBlock.getWorldName() + " for ScoreboardDisplay of Minigame " + minigame.getName() + " wasn't loaded and I couldn't place the root sign!");
        }
    }

    public void reload() {
        needsLoad = false;
        final @NotNull CompletableFuture<List<StoredStat>> future = plugin.getBackend().loadStats(minigame, stat, field, order, 0, width * height * 2);

        // The update callback to be provided to the future. MUST be executed on the bukkit server thread
        future.handle((result, exp) -> Bukkit.getScheduler().runTask(plugin, () -> {
            if (exp == null) {
                stats = result;
                needsLoad = false;
                updateSigns();
            } else {
                plugin.getComponentLogger().error("Error when loading scoreboard " + stat.getDisplayName() + " for minigame " + minigame.getName(), exp);
                stats = List.of();
                needsLoad = true;
            }
        }));
    }
}
