package au.com.mineauz.minigames.minigame;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.stats.*;
import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.sign.Side;
import org.bukkit.configuration.Configuration;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ScoreboardDisplay {
    public static final int defaultWidth = 3;
    public static final int defaultHeight = 3;
    private final @NotNull Location rootBlock;
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

    public ScoreboardDisplay(@NotNull Minigame minigame, int width, int height, @NotNull Location rootBlock, @NotNull BlockFace facing) {
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

    public static @Nullable ScoreboardDisplay load(@NotNull Minigame minigame, @NotNull Configuration config, @NotNull String path) {
        char configSeparator = config.options().pathSeparator();

        int width = config.getInt(path + configSeparator + "width");
        int height = config.getInt(path + configSeparator + "height");
        Location location = MinigameUtils.loadShortLocation(config.getConfigurationSection(path + configSeparator + "location"));
        BlockFace facing = BlockFace.valueOf(config.getString(path + configSeparator + "dir"));

        // from invalid world
        if (location == null) {
            return null;
        }

        ScoreboardDisplay display = new ScoreboardDisplay(minigame, width, height, location, facing);
        display.setOrder(ScoreboardOrder.valueOf(config.getString(path + configSeparator + "order")));
        MinigameStat stat = MinigameStatistics.getStat(config.getString(path + configSeparator + "stat", "wins"));
        StatisticValueField field = StatisticValueField.valueOf(config.getString(path + configSeparator + "field", "Total"));
        display.setStat(stat, field);
        Block block = location.getBlock();
        block.setMetadata("MGScoreboardSign", new FixedMetadataValue(Minigames.getPlugin(), true));
        block.setMetadata("Minigame", new FixedMetadataValue(Minigames.getPlugin(), minigame));

        return display;
    }

    public @NotNull Location getRoot() {
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

        List<Block> blocks = new ArrayList<>(width * height);

        // Find the corner that is the top left part of the scoreboard
        Location min = rootBlock.clone();
        min.add(-horizontal.getModX() * ((double) width / 2.0D), -1, -horizontal.getModZ() * ((double) width / 2.0D));

        // Grab each sign of the scoreboards in order
        Block block = min.getBlock();

        for (int y = 0; y < height; ++y) {
            Block start = block;
            for (int x = 0; x < width; ++x) {
                // Only add signs
                if (Tag.WALL_SIGNS.isTagged(block.getType()) || (!onlySigns && block.getType() == Material.AIR)) {
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
        final MenuItemCustom statisticChoice = new MenuItemCustom(Material.WRITABLE_BOOK, MgMenuLangKey.MENU_SCOREBOARD_STATISTIC_NAME,
                List.of(settings.getDisplayName().color(NamedTextColor.GREEN)));

        final MenuItemCustom fieldChoice = new MenuItemCustom(Material.PAPER, MgMenuLangKey.MENU_SCOREBOARD_STATISTIC_FIELD_NAME,
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
            return null;
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
            return null;
        });

        setupMenu.addItem(statisticChoice);
        setupMenu.addItem(fieldChoice);

        setupMenu.addItem(new MenuItemEnum<>(Material.ENDER_PEARL, MgMenuLangKey.MENU_SCOREBOARD_ORDER_NAME, new Callback<>() {

            @Override
            public @NotNull ScoreboardOrder getValue() {
                return order;
            }

            @Override
            public void setValue(@NotNull ScoreboardOrder value) {
                order = value;
            }
        }, ScoreboardOrder.class));

        setupMenu.addItem(new MenuItemScoreboardSave(MenuUtility.getCreateMaterial(), MgMenuLangKey.MENU_SCOREBOARD_CREATE_NAME, this),
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
            block.setType(Material.AIR);
        }
    }

    public void placeSigns(@NotNull Material material) throws IllegalArgumentException{
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

    public void save(@NotNull Configuration config, @NotNull String path) {
        char configSeparator = config.options().pathSeparator();

        config.set(path + configSeparator + "height", height);
        config.set(path + configSeparator + "width", width);
        config.set(path + configSeparator + "dir", facing.name());
        config.set(path + configSeparator + "stat", stat.getName());
        config.set(path + configSeparator + "field", field.name());
        config.set(path + configSeparator + "order", order.name());
        MinigameUtils.saveShortLocation(config, path + configSeparator + "location", rootBlock);
    }

    public void placeRootSign() {
        // For external calls
        if (settings == null) {
            settings = minigame.getSettings(stat);
        }

        Block root = rootBlock.getBlock();
        if (Tag.ALL_SIGNS.isTagged(root.getType())) {
            BlockState state = root.getState();
            if (state instanceof Sign sign) {
                sign.getSide(Side.FRONT).line(0, minigame.getDisplayName().color(NamedTextColor.BLUE));
                sign.getSide(Side.FRONT).line(1, settings.getDisplayName().color(NamedTextColor.GREEN));
                sign.getSide(Side.FRONT).line(2, field.getTitle().color(NamedTextColor.GREEN));
                sign.getSide(Side.FRONT).line(3, Component.text("(" + WordUtils.capitalizeFully(order.toString()) + ")"));
                sign.update();

                sign.setMetadata("MGScoreboardSign", new FixedMetadataValue(Minigames.getPlugin(), true));
                sign.setMetadata("Minigame", new FixedMetadataValue(Minigames.getPlugin(), minigame));
            } else {
                Minigames.getCmpnntLogger().warn("No Root Sign Block at: " + root.getLocation());
            }
        } else {
            Minigames.getCmpnntLogger().warn("No Root Sign Block at: " + root.getLocation());
        }
    }

    public void reload() {
        needsLoad = false;
        CompletableFuture<List<StoredStat>> future = Minigames.getPlugin().getBackend().loadStats(minigame, stat, field, order, 0, width * height * 2);

        // The update callback to be provided to the future. MUST be executed on the bukkit server thread
        future.handle((result, exp) -> Bukkit.getScheduler().runTask(Minigames.getPlugin(), () -> {
            if (exp == null) {
                stats = result;
                needsLoad = false;
                updateSigns();
            } else {
                Minigames.getCmpnntLogger().error("Error when loading scoreboard " + stat.getDisplayName() + " for minigame " + minigame.getName(), exp);
                stats = List.of();
                needsLoad = true;
            }
        }));
    }
}
