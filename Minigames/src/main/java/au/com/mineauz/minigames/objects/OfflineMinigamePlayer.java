package au.com.mineauz.minigames.objects;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.MinigameSave;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.UUID;

public class OfflineMinigamePlayer {
    private final @NotNull UUID uuid;
    private final ItemStack @NotNull [] storedItems;
    private final ItemStack @NotNull [] storedArmour;
    private final int food;
    private final double health;
    private final float saturation;
    private float exp = -1; //TODO: Set to default value after 1.7
    private int level = -1; //Set To default value after 1.7
    private final @NotNull GameMode lastGM;
    private @Nullable Location loginLocation;

    public OfflineMinigamePlayer(@NotNull UUID uuid, ItemStack @NotNull [] items,
                                 ItemStack @NotNull [] armour, int food, double health,
                                 float saturation, @NotNull GameMode lastGM, float exp, int level,
                                 @Nullable Location loginLocation) {
        this.uuid = uuid;
        storedItems = items;
        storedArmour = armour;
        this.food = food;
        this.health = health;
        this.saturation = saturation;
        this.lastGM = lastGM;
        this.exp = exp;
        this.level = level;
        if (loginLocation != null && loginLocation.getWorld() == null) {
            loginLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
        }
        this.loginLocation = loginLocation;
        if (Minigames.getPlugin().getConfig().getBoolean("saveInventory")) {
            savePlayerData();
        }
    }

    public OfflineMinigamePlayer(@NotNull UUID uuid) {
        MinigameSave save = new MinigameSave("playerdata" + File.separator + "inventories" + File.separator + uuid);
        FileConfiguration config = save.getConfig();
        char configSeparator = config.options().pathSeparator();
        this.uuid = uuid;
        food = config.getInt("food");
        health = config.getDouble("health");
        saturation = config.getInt("saturation");
        lastGM = GameMode.valueOf(config.getString("gamemode"));
        if (config.contains("exp")) {
            exp = ((Double) config.getDouble("exp")).floatValue();
        }
        if (config.contains("level")) {
            level = config.getInt("level");
        }
        if (config.contains("location")) {
            loginLocation = new Location(Minigames.getPlugin().getServer().getWorld(config.getString("location.world", "")),
                    config.getDouble("location" + configSeparator + "x"),
                    config.getDouble("location" + configSeparator + "y"),
                    config.getDouble("location" + configSeparator + "z"),
                    (float) config.getDouble("location" + configSeparator + "yaw"),
                    (float) config.getDouble("location" + configSeparator + "pitch"));
            if (loginLocation.getWorld() == null) {
                loginLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
            }
        } else
            loginLocation = Bukkit.getWorlds().get(0).getSpawnLocation();

        ItemStack[] items = Minigames.getPlugin().getServer().createInventory(null, InventoryType.PLAYER).getContents();
        ItemStack[] armour = new ItemStack[4];
        for (int i = 0; i < items.length; i++) {
            if (config.contains("items" + configSeparator + i)) {
                items[i] = config.getItemStack("items" + configSeparator + i);
            }
        }
        for (int i = 0; i < 4; i++) {
            armour[i] = config.getItemStack("armour" + configSeparator + i);
        }
        storedItems = items;
        storedArmour = armour;
    }

    public @NotNull UUID getUUID() {
        return uuid;
    }

    public ItemStack @NotNull [] getStoredItems() {
        return storedItems;
    }

    public ItemStack @NotNull [] getStoredArmour() {
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

    public @Nullable Location getLoginLocation() {
        return loginLocation;
    }

    public void setLoginLocation(@NotNull Location loc) {
        loginLocation = loc;
    }

    public float getExp() {
        return exp;
    }

    public int getLevel() {
        return level;
    }

    public void savePlayerData() {
        MinigameSave save = new MinigameSave("playerdata" + File.separator + "inventories" + File.separator + uuid);
        FileConfiguration config = save.getConfig();
        char configSeparator = config.options().pathSeparator();

        int num = 0;
        for (ItemStack item : storedItems) {
            if (item != null) {
                config.set("items" + configSeparator + num, item);
            }
            num++;
        }

        num = 0;
        for (ItemStack item : storedArmour) {
            if (item != null) {
                config.set("armour" + configSeparator + num, item);
            }
            num++;
        }


        config.set("food", food);
        config.set("saturation", saturation);
        config.set("health", health);
        config.set("gamemode", lastGM.toString());
        config.set("exp", exp);
        config.set("level", level);
        if (loginLocation != null) {
            config.set("location" + configSeparator + "x", loginLocation.getBlockX());
            config.set("location" + configSeparator + "y", loginLocation.getBlockY());
            config.set("location" + configSeparator + "z", loginLocation.getBlockZ());
            config.set("location" + configSeparator + "yaw", loginLocation.getYaw());
            config.set("location" + configSeparator + "pitch", loginLocation.getPitch());
            config.set("location" + configSeparator + "world", loginLocation.getWorld().getName());
        }
        save.saveConfig();
    }

    public void deletePlayerData() {
        MinigameSave save = new MinigameSave("playerdata" + File.separator + "inventories" + File.separator + uuid);
        save.deleteFile();
    }
}
