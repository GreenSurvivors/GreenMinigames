package au.com.mineauz.minigames.blockRecorder;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.MinigameState;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import com.google.common.collect.Lists;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.papermc.paper.math.Position;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.Hangable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.material.Attachable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class RecorderData implements Listener {
    // list of blocks that need another block to not break
    private static final ArrayList<Material> supportedMats = new ArrayList<>();
    // this plugin
    private static Minigames plugin;

    /*
     * this list of Blocks will be regenerated after all the solid ones.
     * It contains an arrangement of Blocks that need a block under / next or over it to support it.
     * If one block is missing or wrongly added here there shouldn't be big issues anyway.
     * We will test if the Material is affected by gravity or is attachable later.
     * However, just to be sure we will delay regenerating this ones
     */
    static {
        supportedMats.add(Material.WATER);
        supportedMats.add(Material.LAVA);
        supportedMats.addAll(Tag.DOORS.getValues());
        supportedMats.addAll(Tag.RAILS.getValues());
        supportedMats.add(Material.TRIPWIRE);
        supportedMats.addAll(Tag.PRESSURE_PLATES.getValues());
        supportedMats.add(Material.COMPARATOR);
        supportedMats.add(Material.REPEATER);
        supportedMats.add(Material.REDSTONE_WIRE);
        supportedMats.add(Material.SNOW);
        supportedMats.add(Material.NETHER_PORTAL);
        supportedMats.add(Material.PISTON_HEAD);
        supportedMats.add(Material.MOVING_PISTON);
        supportedMats.add(Material.LILY_PAD);
        supportedMats.addAll(Tag.WOOL_CARPETS.getValues());
        supportedMats.add(Material.MOSS_CARPET);
        supportedMats.add(Material.TALL_GRASS);
        supportedMats.add(Material.TALL_SEAGRASS);
        supportedMats.add(Material.DEAD_BUSH);
        supportedMats.add(Material.RED_MUSHROOM);
        supportedMats.add(Material.BROWN_MUSHROOM);
        supportedMats.addAll(Tag.SAPLINGS.getValues());
        supportedMats.addAll(Tag.FLOWERS.getValues());
        supportedMats.addAll(Tag.CORALS.getValues());
        supportedMats.addAll(Tag.CROPS.getValues());
        supportedMats.add(Material.HANGING_ROOTS);
        supportedMats.add(Material.NETHER_WART);
        supportedMats.add(Material.SMALL_DRIPLEAF);
        supportedMats.add(Material.BIG_DRIPLEAF);
        supportedMats.add(Material.KELP_PLANT);
        supportedMats.addAll(Tag.CAVE_VINES.getValues());
        supportedMats.add(Material.VINE);
        supportedMats.add(Material.SCAFFOLDING);
    }

    // the minigame this data belongs to
    private final Minigame minigame;
    // data of entities that will be regenerated
    private final Map<UUID, EntityData> entityData = new HashMap<>();
    // white/blacklisted blocks that can (not) be broken by a player in the minigame
    // and therefore is not required to regenerated
    private final List<Material> wbBlocks = new ArrayList<>();
    // is it a white or a blacklist?
    private boolean whitelistMode = false;
    private boolean hasCreatedRegenBlocks = false;
    //data of blocks that will be regenerated
    private Map<Position, MgBlockData> blockdata = new HashMap<>();

    public RecorderData(Minigame minigame) {
        plugin = Minigames.getPlugin();

        this.minigame = minigame;
    }

    public boolean getWhitelistMode() {
        return whitelistMode;
    }

    public void setWhitelistMode(boolean bool) {
        whitelistMode = bool;
    }

    public Callback<Boolean> getWhitelistModeCallback() {
        return new Callback<>() {

            @Override
            public Boolean getValue() {
                return whitelistMode;
            }

            @Override
            public void setValue(Boolean value) {
                whitelistMode = value;
            }
        };
    }

    public void addWBBlock(Material mat) {
        wbBlocks.add(mat);
    }

    public List<Material> getWBBlocks() {
        return wbBlocks;
    }

    public boolean removeWBBlock(Material mat) {
        return wbBlocks.remove(mat);
    }

    public boolean hasCreatedRegenBlocks() {
        return hasCreatedRegenBlocks;
    }

    public void setCreatedRegenBlocks(boolean bool) {
        hasCreatedRegenBlocks = bool;
    }

    public Minigame getMinigame() {
        return minigame;
    }

    public MgBlockData addBlock(Block block, MinigamePlayer modifier) {
        return addBlock(block.getState(true), modifier);
    }

    public MgBlockData addBlock(BlockState blockstate, MinigamePlayer modifier) {
        MgBlockData bdata = new MgBlockData(blockstate, modifier);
        Position pos = Position.block(blockstate.getLocation());

        if (!blockdata.containsKey(pos)) {
            if (blockstate instanceof InventoryHolder invHolder) {
                if (invHolder instanceof DoubleChest doubleChest) {
                    Location left = doubleChest.getLeftSide().getInventory().getLocation().clone();
                    Location right = doubleChest.getRightSide().getInventory().getLocation().clone();

                    if (bdata.getLocation() == left) {

                        addInventory(bdata, doubleChest.getLeftSide());
                        if (minigame.isRandomizeChests()) {
                            bdata.randomizeContents(minigame.getMinChestRandom(), minigame.getMaxChestRandom());
                        }
                    }

                    MgBlockData secondChest = addBlock(right.getBlock(), modifier);
                    if (secondChest.getInventoryContents() == null) {
                        addInventory(secondChest, doubleChest.getRightSide());
                        if (minigame.isRandomizeChests())
                            secondChest.randomizeContents(minigame.getMinChestRandom(), minigame.getMaxChestRandom());
                    }
                } else if (invHolder instanceof Chest) {
                    addInventory(bdata, invHolder);
                    if (minigame.isRandomizeChests()) {
                        bdata.randomizeContents(minigame.getMinChestRandom(), minigame.getMaxChestRandom());
                    }
                } else {
                    addInventory(bdata, invHolder);
                }
            }

            blockdata.put(pos, bdata);

            return bdata;
        } else { //already known
            //set last modifier of a not random inventory
            if (blockstate.getType() != Material.CHEST || !blockdata.get(pos).hasRandomized()) {
                blockdata.get(pos).setModifier(modifier);
            }

            return blockdata.get(pos);
        }
    }

    public void addInventory(MgBlockData bdata, InventoryHolder ih) {
        ItemStack[] inventory = Arrays.stream(ih.getInventory().getContents()).
                map(itemStack -> itemStack == null ? null : itemStack.clone()).toArray(ItemStack[]::new);

        bdata.setInventory(inventory);
    }

    //add an entity to get reset
    public void addEntity(@NotNull Entity ent, @Nullable MinigamePlayer player, boolean created) {
        EntityData oldData = entityData.get(ent.getUniqueId());
        if (oldData != null) {
            if (oldData.wasCreated() && !created) {
                entityData.remove(ent.getUniqueId());

                return;
            }
        }

        entityData.put(ent.getUniqueId(), new EntityData(ent, player, created));
    }

    public boolean hasBlock(Block block) {
        return blockdata.containsKey(Position.block(block.getLocation()));
    }

    public void restoreAll(MinigamePlayer modifier) {
        if (!blockdata.isEmpty()) {
            restoreBlocks(modifier);
        }

        if (!entityData.isEmpty()) {
            restoreEntities(modifier);
        }
    }

    /**
     * restores all recorded blocks
     */
    public void restoreBlocks() {
        restoreBlocks(null);
        blockdata.clear();
    }

    public void restoreEntities() {
        restoreEntities(null);
        entityData.clear();
    }

    /**
     * @param modifier the player to rollback. If null all blocks get restored
     */
    public void restoreBlocks(final MinigamePlayer modifier) {
        // When rolling back a single player's changes don't change the overall games state
        if (modifier == null) {
            minigame.setState(MinigameState.REGENERATING);
        }
        Iterator<MgBlockData> it = blockdata.values().iterator();

        final List<MgBlockData> baseBlocks = Lists.newArrayList();
        final List<MgBlockData> gravityBlocks = Lists.newArrayList();
        final List<MgBlockData> attachableBlocks = Lists.newArrayList();

        //sort the blocks into the three lists above
        while (it.hasNext()) {
            MgBlockData data = it.next();
            if (modifier == null || modifier.equals(data.getModifier())) {
                it.remove();

                // Clear inventories
                if (data.getLocation().getBlock().getState() instanceof InventoryHolder invHolder) {
                    invHolder.getInventory().clear();
                }

                if (supportedMats.contains(data.getBlockState().getType()) ||
                        data.getBlockState().getBlockData() instanceof Attachable || data.getBlockState() instanceof Hangable) {
                    attachableBlocks.add(data);
                } else if (data.getBukkitBlockData().getMaterial().hasGravity()) {
                    gravityBlocks.add(data);
                } else {
                    baseBlocks.add(data);
                }
            }
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            //first place all base blocks
            //next place the gravity blocks and finally place the attachable blocks
            customBlockComparator(baseBlocks);
            customBlockComparator(attachableBlocks);
            customBlockComparator(gravityBlocks);
            baseBlocks.addAll(gravityBlocks);
            baseBlocks.addAll(attachableBlocks);

            new RollbackScheduler(baseBlocks, minigame, modifier);
        });
    }

    private void customBlockComparator(List<MgBlockData> baseBlocks) {
        baseBlocks.sort(
                Comparator.comparingInt(
                        (MgBlockData o) -> o.getBlockState().getChunk().getX()
                ).thenComparingInt(
                        o -> o.getBlockState().getChunk().getZ()
                ).thenComparingInt(
                        o -> o.getBlockState().getY()
                )
        );
    }

    public void restoreEntities(MinigamePlayer player) {
        Iterator<EntityData> it = entityData.values().iterator();
        while (it.hasNext()) {
            EntityData nextEntityData = it.next();

            if (player == null || player.equals(nextEntityData.getModifier())) {
                if (nextEntityData.wasCreated()) {
                    Entity ent = nextEntityData.getEntity();
                    // Entity needs to be removed
                    if (ent != null && ent.isValid()) {
                        ent.remove();
                    }
                } else {
                    // Entity needs to be spawned
                    //todo restore metadata like armor
                    Location location = nextEntityData.getEntityLocation();
                    location.getWorld().spawnEntity(location, nextEntityData.getEntityType());
                }

                it.remove();
            }
        }
    }

    public void clearRestoreData() {
        entityData.clear();
        blockdata.clear();
    }

    public boolean hasData() {
        return !(blockdata.isEmpty() && entityData.isEmpty());
    }

    public boolean checkBlockSides(Location location) {
        Location temp = location.clone();
        temp.setX(temp.getX() - 1);
        temp.setY(temp.getY() - 1);
        temp.setZ(temp.getZ() - 1);

        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) {
                for (int z = 0; z < 2; z++) {
                    if (hasBlock(temp.getBlock())) {
                        return true;
                    }
                    temp.setZ(temp.getZ() + 1);
                }
                if (hasBlock(temp.getBlock())) {
                    return true;
                }
                temp.setZ(temp.getZ() - 2);
                temp.setX(temp.getX() + 1);
            }
            temp.setX(temp.getX() - 2);
            temp.setY(temp.getY() + 1);
        }
        return false;
    }

    public void saveAllBlockData() { //todo save entity data as well and use hasData() instead of blockdata.isEmpty()
        if (blockdata.isEmpty()) {
            return;
        }

        File file = new File(plugin.getDataFolder() + "/minigames/" + minigame.getName() + "/backup.json");

        // register custom serializer for Position.
        // this is purely for backwards compatibility.
        // If that is not important to you, just use Gson gson = new Gson(); instead of GsonBuilder gsonBuilder = new GsonBuilder();
        GsonBuilder gsonBuilder = new GsonBuilder();
        JsonSerializer<Position> serializer = (src, typeOfSrc, context) -> new JsonPrimitive(src.x() + ":" + src.y() + ":" + src.z());

        gsonBuilder.registerTypeAdapter(Position.class, serializer);
        Gson customGson = gsonBuilder.create();
        Type mapType = new TypeToken<Map<Position, MgBlockData>>() {
        }.getType();

        try (FileWriter writer = new FileWriter(file)) {
            customGson.toJson(blockdata, mapType, writer);
        } catch (FileNotFoundException e) {
            Minigames.getCmpnntLogger().error("File not found!!!", e);
        } catch (IOException e) {
            Minigames.getCmpnntLogger().error("IO Error!", e);
        }
    }

    /**
     * loads block states from backup file
     *
     * @return true if loading the data was successful else false
     */
    public boolean restoreBlockData() { //todo load entity data as well
        if (covertOldFormat()) {
            saveAllBlockData();
            Minigames.getCmpnntLogger().info("Converted backup for: " + minigame.getName());
            return true;
        } else {
            File file = new File(plugin.getDataFolder() + "/minigames/" + minigame.getName() + "/backup.json");

            if (file.exists() && file.isFile() && file.canRead()) {
                // register custom deserializer for Position.
                // this is purely for backwards compatibility.
                // If that is not important to you, just use Gson gson = new Gson(); instead of GsonBuilder gsonBuilder = new GsonBuilder(); and following
                GsonBuilder gsonBuilder = new GsonBuilder();
                JsonDeserializer<Position> deserializer = (json, typeOfT, context) -> {
                    try {
                        String posStr = json.getAsString(); //throws JsonParseException

                        String[] args = posStr.split(":");
                        if (args.length < 3) {
                            throw new JsonParseException("'" + posStr + "' is not a valid position.");
                        }

                        return Position.fine(Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2])); //throws NumberFormatException
                    } catch (JsonParseException | NumberFormatException e) {
                        Minigames.getCmpnntLogger().error("", e);
                        return null;
                    }
                };

                gsonBuilder.registerTypeAdapter(Position.class, deserializer);

                Gson customGson = gsonBuilder.create();
                Type type = new TypeToken<Map<Position, MgBlockData>>() {
                }.getType();
                try (FileReader reader = new FileReader(file)) {
                    blockdata = customGson.fromJson(reader, type);
                    return true;
                } catch (IOException e) {
                    Minigames.getCmpnntLogger().error("", e);
                }
            }
        }
        return false;
    }

    private boolean covertOldFormat() {
        File f = new File(plugin.getDataFolder() + "/minigames/" + minigame.getName() + "/backup.dat");

        if (!f.exists()) {
            return false;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));

            Map<String, String> args = new HashMap<>();
            String line;
            String[] blocks;
            String[] block;
            World w;
            MgBlockData bd;
            BlockState state;
            ItemStack[] inventory;
            String[] sitems;
            ItemStack item;
            Map<String, String> iargs = new HashMap<>();

            while (br.ready()) {
                line = br.readLine();

                blocks = line.split("}\\{");

                for (String bl : blocks) {
                    args.clear();

                    bl = bl.replace("{", "");
                    bl = bl.replace("}", "");

                    block = bl.split(";");
                    for (String b : block) {
                        String[] spl = b.split(":");
                        if (spl.length > 1) {
                            args.put(spl[0], spl[1]);
                        }
                    }

                    w = Bukkit.getWorld(args.get("world"));
                    state = w.getBlockAt(Integer.parseInt(args.get("x")), Integer.parseInt(args.get("y")), Integer.parseInt(args.get("z"))).getState();
                    state.setBlockData(Bukkit.getUnsafe().fromLegacy(Material.getMaterial(args.get("mat")), Byte.parseByte(args.get("data"))));

                    bd = new MgBlockData(state, null);

                    if (args.containsKey("items")) {
                        if (state.getType() == Material.DISPENSER || state.getType() == Material.DROPPER) {
                            inventory = new ItemStack[InventoryType.DISPENSER.getDefaultSize()];
                        } else if (state.getType() == Material.HOPPER) {
                            inventory = new ItemStack[InventoryType.HOPPER.getDefaultSize()];
                        } else if (state.getType() == Material.FURNACE) {
                            inventory = new ItemStack[InventoryType.FURNACE.getDefaultSize()];
                        } else if (state.getType() == Material.BREWING_STAND) {
                            inventory = new ItemStack[InventoryType.BREWING.getDefaultSize()];
                        } else {
                            inventory = new ItemStack[InventoryType.CHEST.getDefaultSize()];
                        }

                        sitems = args.get("items").split("\\)\\(");

                        for (String i : sitems) {
                            i = i.replace("(", "");
                            i = i.replace(")", "");

                            for (String s : i.split("\\|")) {
                                String[] spl = s.split("-");
                                if (spl.length > 1) {
                                    iargs.put(s.split("-")[0], s.split("-")[1]);
                                }
                            }
                            item = new ItemStack(Material.matchMaterial(iargs.get("item")),
                                    Integer.parseInt(iargs.get("c")));
                            if (item.getItemMeta() instanceof Damageable damageable) {
                                damageable.setDamage(Short.parseShort(iargs.get("dur")));
                                item.setItemMeta(damageable);
                            }

                            if (iargs.containsKey("enc")) {
                                for (String s : iargs.get("enc").split("\\]\\[")) {
                                    item.addUnsafeEnchantment(Enchantment.getByName(s.split(",")[0].replace("[", "")),
                                            Integer.parseInt(s.split(",")[1].replace("]", "")));
                                }
                            }

                            inventory[Integer.parseInt(iargs.get("slot"))] = item;
                            iargs.clear();
                        }

                        bd.setInventory(inventory);
                    }

                    blockdata.put(Position.block(bd.getLocation()), bd);
                }
            }

            br.close();
        } catch (FileNotFoundException e) {
            Minigames.getCmpnntLogger().error("File not found!!!", e);
        } catch (IOException e) {
            Minigames.getCmpnntLogger().error("IO Error!", e);
        }

        return true;
    }
}
