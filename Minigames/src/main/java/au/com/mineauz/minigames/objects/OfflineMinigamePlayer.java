package au.com.mineauz.minigames.objects;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.MinigameSave;
import au.com.mineauz.minigames.objects.safelocation.SafeFullLocation;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

/**
 * player data saved on disk
 */
public class OfflineMinigamePlayer {
    private final @NotNull UUID uuid;
    private final @Nullable ItemStack @NotNull [] storedItems;
    private final @Nullable ItemStack @NotNull [] storedArmour; // todo armour is redundant, as it is already included in inventory
    private final int food;
    private final double health;
    private final float saturation;
    private final float exp;
    private final int level;
    private final @NotNull GameMode lastGM;
    private @Nullable SafeFullLocation loginLocation;

    public OfflineMinigamePlayer(@NotNull UUID uuid, @Nullable ItemStack @NotNull [] items,
                                 @Nullable ItemStack @NotNull [] armour, int food, double health,
                                 float saturation, @NotNull GameMode lastGM, float exp, int level,
                                 final @Nullable SafeFullLocation loginLocation) {
        this.uuid = uuid;
        storedItems = items;
        storedArmour = armour;
        this.food = food;
        this.health = health;
        this.saturation = saturation;
        this.lastGM = lastGM;
        this.exp = exp;
        this.level = level;
        this.loginLocation = loginLocation;
        if (Minigames.getPlugin().getConfig().getBoolean("saveInventory")) {
            try {
                savePlayerData();
            } catch (final @NotNull IOException e) {
                Minigames.getPlugin().getComponentLogger().error("Couldn't save player data for player with uuid " + uuid, e);
            }
        }
    }

    /**
     * loads player data from disk
     *
     * @param uuid the uuid of the user to load
     */
    public OfflineMinigamePlayer(final @NotNull UUID uuid) throws ConfigurateException {
        final @Nullable CommentedConfigurationNode configRoot = MinigameSave.forPlayerData(uuid, Path.of("inventories")).getConfigRoot();
        this.uuid = uuid;
        food = configRoot.node("food").getInt(20);
        health = configRoot.node("health").getDouble(20);
        saturation = configRoot.node("saturation").getInt(15);
        lastGM = configRoot.node("gamemode").get(GameMode.class);
        exp = configRoot.node("exp").getFloat(0);
        level = configRoot.node("level").getInt(0);
        if (configRoot.hasChild("location")) {
            loginLocation = configRoot.node("location").get(SafeFullLocation.class);
            if (loginLocation == null) {
                loginLocation = new SafeFullLocation(Bukkit.getWorlds().getFirst().getSpawnLocation());
            }
        } else {
            loginLocation = new SafeFullLocation(Bukkit.getWorlds().getFirst().getSpawnLocation()); // todo use Bukkits spawn location....
        }

        final @NotNull ItemStack @NotNull [] items = new ItemStack[InventoryType.PLAYER.getDefaultSize()];
        final @NotNull ItemStack[] armour = new ItemStack[4];
        for (int i = 0; i < items.length; i++) {
            final @NotNull ConfigurationNode itemNode = configRoot.node("items", i);
            if (!itemNode.virtual() && !itemNode.isNull()) {
                if (itemNode.isMap()) { // datafixerupper
                    items[i] = ItemStack.deserialize((Map<String, Object>) itemNode.get(TypeFactory.parameterizedClass(Map.class, String.class, Object.class)));
                } else {
                    items[i] = ItemStack.deserializeBytes(itemNode.get(TypeToken.get(byte[].class)));
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            final @NotNull ConfigurationNode armourNode = configRoot.node("armour", i);
            if (!armourNode.virtual() && !armourNode.isNull()) {
                if (armourNode.isMap()) { // datafixerupper
                    armour[i] = ItemStack.deserialize((Map<String, Object>) armourNode.get(TypeFactory.parameterizedClass(Map.class, String.class, Object.class)));
                } else {
                    armour[i] = ItemStack.deserializeBytes(armourNode.get(TypeToken.get(byte[].class)));
                }
            }
        }
        storedItems = items;
        storedArmour = armour;
    }

    public @NotNull UUID getUUID() {
        return uuid;
    }

    public @Nullable ItemStack @NotNull [] getStoredItems() {
        return storedItems;
    }

    public @Nullable ItemStack @NotNull [] getStoredArmour() {
        return storedArmour;
    }

    public int getFood() {
        return food;
    }

    public double getHealth() {
        return health;
    }

    public float getSaturation() {
        return saturation;
    }

    public @NotNull GameMode getLastGamemode() {
        return lastGM;
    }

    public @Nullable SafeFullLocation getLoginLocation() {
        return loginLocation;
    }

    public void setLoginLocation(@Nullable SafeFullLocation loc) {
        loginLocation = loc;
    }

    public float getExp() {
        return exp;
    }

    public int getLevel() {
        return level;
    }

    public void savePlayerData() throws IOException {
        final @NotNull MinigameSave save = MinigameSave.forPlayerData(uuid, Path.of("inventories"));
        final @NotNull ConfigurationNode configRoot = save.getConfigRoot();

        int slot = 0;
        for (ItemStack item : storedItems) {
            if (item != null) {
                configRoot.node("items", slot++).set(item.serializeAsBytes());
            }
        }

        slot = 0;
        for (ItemStack item : storedArmour) {
            if (item != null) {
                configRoot.node("armour", slot++).set(item.serializeAsBytes());
            }
        }

        configRoot.node("food").raw(food);
        configRoot.node("saturation").raw(saturation);
        configRoot.node("health").raw(health);
        configRoot.node("gamemode").set(lastGM);
        configRoot.node("exp").raw(exp);
        configRoot.node("level").raw(level);
        if (loginLocation != null) {
            configRoot.node("location").set(loginLocation);
        }
        save.saveConfig();
    }

    public void deletePlayerData() {
        MinigameSave save = MinigameSave.forPlayerData(uuid, Path.of("inventories"));
        save.deleteFile();
    }
}
