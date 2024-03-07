package au.com.mineauz.minigames;

import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.minigame.TeamColor;
import au.com.mineauz.minigames.minigame.modules.ALoadoutAddon;
import au.com.mineauz.minigames.minigame.modules.LoadoutAddonFactory;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

public class PlayerLoadout {
    private final static Pattern NUMBER = Pattern.compile("[+-]?[0-9]+");

    private final Map<Integer, ItemStack> itemSlots = new HashMap<>();
    private final List<PotionEffect> effects = new ArrayList<>();
    private final @NotNull Map<@NotNull String, @NotNull ALoadoutAddon> addons = new HashMap<>();
    private final @NotNull String loadoutName;
    private boolean usePermission = false;
    private boolean fallDamage = true;
    private boolean hunger = false;
    private int level = -1;
    private boolean deletable = true;
    private @Nullable Component displayname = null;
    private boolean lockInventory = false;
    private boolean lockArmour = false;
    private boolean allowOffHand = true;
    private @Nullable TeamColor team;
    private boolean displayInMenu = true;

    public PlayerLoadout(@NotNull String name) {
        loadoutName = name;
        team = TeamColor.matchColor(name);
    }

    public Callback<Component> getDisplayNameCallback() {
        return new Callback<>() {

            @Override
            public Component getValue() {
                return displayname;
            }

            @Override
            public void setValue(Component value) {
                displayname = value;
            }
        };
    }

    public @NotNull Component getDisplayName() {
        return Objects.requireNonNullElseGet(displayname, () -> Component.text(loadoutName));
    }

    public void setDisplayName(@NotNull Component name) {
        displayname = name;
    }

    public boolean getUsePermissions() {
        return usePermission;
    }

    public void setUsePermissions(boolean bool) {
        usePermission = bool;
    }

    public Callback<Boolean> getUsePermissionsCallback() {
        return new Callback<>() {

            @Override
            public Boolean getValue() {
                return usePermission;
            }

            @Override
            public void setValue(Boolean value) {
                usePermission = value;
            }
        };
    }

    public String getName() {
        return loadoutName;
    }

    public void addItem(ItemStack item, int slot) {
        itemSlots.put(slot, item);
    }

    public void addPotionEffect(PotionEffect effect) {
        for (PotionEffect pot : effects) {
            if (effect.getType().getKey().equals(pot.getType().getKey())) {
                effects.remove(pot);
                break;
            }
        }
        effects.add(effect);
    }

    public void removePotionEffect(PotionEffect effect) {
        if (effects.contains(effect)) {
            effects.remove(effect);
        } else {
            for (PotionEffect pot : effects) {
                if (pot.getType().getKey().equals(effect.getType().getKey())) {
                    effects.remove(pot);
                    break;
                }
            }
        }
    }

    public List<PotionEffect> getAllPotionEffects() {
        return effects;
    }

    public void equipLoadout(@NotNull MinigamePlayer mgPlayer) {
        mgPlayer.getPlayer().getInventory().clear();
        mgPlayer.getPlayer().getInventory().setHelmet(null);
        mgPlayer.getPlayer().getInventory().setChestplate(null);
        mgPlayer.getPlayer().getInventory().setLeggings(null);
        mgPlayer.getPlayer().getInventory().setBoots(null);
        for (PotionEffect potion : mgPlayer.getPlayer().getActivePotionEffects()) {
            mgPlayer.getPlayer().removePotionEffect(potion.getType());
        }
        if (!itemSlots.isEmpty()) {
            Player player = mgPlayer.getPlayer();

            for (Map.Entry<Integer, ItemStack> slotItem : itemSlots.entrySet()) {
                if (slotItem.getKey() >= 0 && slotItem.getKey() < 100) {
                    player.getInventory().setItem(slotItem.getKey(), slotItem.getValue());
                } else {
                    switch (slotItem.getKey()) {
                        case 100 -> player.getInventory().setBoots(slotItem.getValue());
                        case 101 -> player.getInventory().setLeggings(slotItem.getValue());
                        case 102 -> player.getInventory().setChestplate(slotItem.getValue());
                        case 103 -> player.getInventory().setHelmet(slotItem.getValue());
                        case -106 -> player.getInventory().setItemInOffHand(slotItem.getValue());
                    }
                }
            }
            mgPlayer.updateInventory();
        }

        final MinigamePlayer fplayer = mgPlayer;
        Bukkit.getScheduler().runTask(Minigames.getPlugin(), () -> fplayer.getPlayer().addPotionEffects(effects));

        for (ALoadoutAddon addon : addons.values()) {
            addon.applyLoadout(mgPlayer);
        }

        if (level != -1) {
            mgPlayer.getPlayer().setLevel(level);
        }
    }

    public void removeLoadout(@NotNull MinigamePlayer player) {
        for (ALoadoutAddon addon : addons.values()) {
            addon.clearLoadout(player);
        }
    }

    public Set<Integer> getItemSlots() {
        return itemSlots.keySet();
    }

    public ItemStack getItem(int slot) {
        return itemSlots.get(slot);
    }

    public void clearLoadout() {
        itemSlots.clear();
    }

    public boolean hasFallDamage() {
        return fallDamage;
    }

    public void setHasFallDamage(boolean bool) {
        fallDamage = bool;
    }

    public Callback<Boolean> getFallDamageCallback() {
        return new Callback<>() {

            @Override
            public Boolean getValue() {
                return fallDamage;
            }

            @Override
            public void setValue(Boolean value) {
                fallDamage = value;
            }
        };
    }

    public boolean hasHunger() {
        return hunger;
    }

    public void setHasHunger(boolean bool) {
        hunger = bool;
    }

    public Callback<Boolean> getHungerCallback() {
        return new Callback<>() {

            @Override
            public Boolean getValue() {
                return hunger;
            }

            @Override
            public void setValue(Boolean value) {
                hunger = value;
            }
        };
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Callback<Integer> getLevelCallback() {
        return new Callback<>() {

            @Override
            public Integer getValue() {
                return level;
            }

            @Override
            public void setValue(Integer value) {
                if (level >= -1)
                    level = value;
            }
        };
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean value) {
        deletable = value;
    }

    public boolean isInventoryLocked() {
        return lockInventory;
    }

    public void setInventoryLocked(boolean locked) {
        lockInventory = locked;
    }

    public Callback<Boolean> getInventoryLockedCallback() {
        return new Callback<>() {

            @Override
            public Boolean getValue() {
                return isInventoryLocked();
            }

            @Override
            public void setValue(Boolean value) {
                setInventoryLocked(value);
            }
        };
    }

    public boolean isArmourLocked() {
        return lockArmour;
    }

    public void setArmourLocked(boolean locked) {
        lockArmour = locked;
    }

    public Callback<Boolean> getArmourLockedCallback() {
        return new Callback<>() {

            @Override
            public Boolean getValue() {
                return isArmourLocked();
            }

            @Override
            public void setValue(Boolean value) {
                setArmourLocked(value);
            }
        };
    }

    public boolean allowOffHand() {
        return allowOffHand;
    }

    public Callback<Boolean> getAllowOffHandCallback() {
        return new Callback<>() {
            @Override
            public Boolean getValue() {
                return allowOffHand;
            }

            @Override
            public void setValue(Boolean value) {
                allowOffHand = value;
            }
        };
    }

    public void setAllowOffHand(boolean allow) {
        allowOffHand = allow;
    }

    public TeamColor getTeamColor() {
        return team;
    }

    public void setTeamColor(TeamColor color) {
        team = color;
    }

    public Callback<TeamColor> getTeamColorCallback() {
        return new Callback<>() {

            @Override
            public TeamColor getValue() {
                if (getTeamColor() == null) {
                    return TeamColor.NONE;
                }
                return getTeamColor();
            }

            @Override
            public void setValue(TeamColor value) {
                setTeamColor(value);
            }
        };
    }

    public boolean isDisplayedInMenu() {
        return displayInMenu;
    }

    public Callback<Boolean> getDisplayInMenuCallback() {
        return new Callback<>() {

            @Override
            public Boolean getValue() {
                return isDisplayedInMenu();
            }

            @Override
            public void setValue(Boolean value) {
                setDisplayInMenu(value);
            }
        };
    }

    public void setDisplayInMenu(boolean bool) {
        displayInMenu = bool;
    }

    /**
     * registers an addon in this loadout
     * @param addonFactory The addonFactory
     */
    public void registerAddon(@NotNull LoadoutAddonFactory addonFactory) {
        addons.put(addonFactory.getAddonName(), addonFactory.makeNewLoadoutAddon());
    }

    public void addAddonMenuItems(@NotNull Menu menu) {
        for (ALoadoutAddon addon : addons.values()) {
            addon.addMenuOptions(menu);
        }
    }

    /**
     * unregisters an addon in this loadout
     * @param name The addons name
     */
    public void unregisterAddon(@NotNull String name) {
        addons.remove(name);
    }

    public void save(@NotNull FileConfiguration config, @NotNull String path) {
        for (Integer slot : getItemSlots()) {
            config.set(path + ".items." + slot, getItem(slot));
        }

        for (PotionEffect eff : getAllPotionEffects()) {
            config.set(path + ".potions." + eff.getType().getKey() + ".amp", eff.getAmplifier());
            config.set(path + ".potions." + eff.getType().getKey() + ".dur", eff.getDuration());
        }

        if (getUsePermissions()) {
            config.set(path + ".usepermissions", true);
        }

        if (!hasFallDamage()) {
            config.set(path + ".falldamage", hasFallDamage());
        }

        if (hasHunger()) {
            config.set(path + ".hunger", hasHunger());
        }

        config.set(path + ".displayName", MiniMessage.miniMessage().serialize(getDisplayName()));

        if (isArmourLocked()) {
            config.set(path + ".armourLocked", isArmourLocked());
        }

        if (isInventoryLocked()) {
            config.set(path + ".inventoryLocked", isInventoryLocked());
        }

        if (getTeamColor() != null) {
            config.set(path + ".team", getTeamColor().toString());
        }

        if (!isDisplayedInMenu()) {
            config.set(path + ".displayInMenu", isDisplayedInMenu());
        }

        if (!allowOffHand()) {
            config.set(path + ".allowOffhand", allowOffHand());
        }

        for (ALoadoutAddon addon : addons.values()) {
            String subPath = path + ".addons." + addon.getName().replace('.', '-');
            addon.save(config, subPath);
        }
    }

    public void load(@NotNull FileConfiguration config, @NotNull String path) {
        ConfigurationSection itemSection = config.getConfigurationSection(path + ".items");
        if (itemSection != null) {
            for (String key : itemSection.getKeys(false)) {
                if (NUMBER.matcher(key).matches()) {
                    addItem(itemSection.getItemStack(key), Integer.parseInt(key));
                }
            }
        }

        ConfigurationSection potionSection = config.getConfigurationSection(path + ".potions");
        if (potionSection != null) {
            for (String effectName : potionSection.getKeys(false)) {
                PotionEffectType effectType = Registry.EFFECT.get(NamespacedKey.fromString(effectName.toLowerCase(java.util.Locale.ENGLISH)));

                if (effectType != null) {
                    PotionEffect effect = new PotionEffect(effectType,
                            potionSection.getInt(effectName + ".dur"),
                            potionSection.getInt(effectName + ".amp")
                    );

                    addPotionEffect(effect);
                }
            }
        }

        if (config.contains(path + ".usepermissions")) {
            setUsePermissions(config.getBoolean(path + ".usepermissions"));
        }

        if (config.contains(path + ".falldamage")) {
            setHasFallDamage(config.getBoolean(path + ".falldamage"));
        }

        if (config.contains(path + ".hunger")) {
            setHasHunger(config.getBoolean(path + ".hunger"));
        }

        String rawDisplayName = config.getString(path + ".displayName");
        if (rawDisplayName != null) {
            setDisplayName(MiniMessage.miniMessage().deserialize(rawDisplayName));
        }

        if (config.contains(path + ".inventoryLocked")) {
            setInventoryLocked(config.getBoolean(path + ".inventoryLocked"));
        }

        if (config.contains(path + ".armourLocked")) {
            setArmourLocked(config.getBoolean(path + ".armourLocked"));
        }

        String rawTeamColor = config.getString(path + ".team");
        if (rawTeamColor != null) {
            setTeamColor(TeamColor.matchColor(rawTeamColor));
        }

        if (config.contains(path + ".displayInMenu")) {
            setDisplayInMenu(config.getBoolean(path + ".displayInMenu"));
        }

        if (config.contains(path + ".allowOffhand")) {
            setAllowOffHand(config.getBoolean(path + ".allowOffhand"));
        }

        ConfigurationSection addonSection = config.getConfigurationSection(path + ".addons");
        if (addonSection != null) {
            for (ALoadoutAddon addon : addons.values()) {
                addon.load(config, path + ".addons");
            }
        }
    }
}
