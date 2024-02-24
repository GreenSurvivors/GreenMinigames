package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.config.ItemFlag;
import au.com.mineauz.minigames.managers.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuItemComponent;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GiveItemAction extends AAction {
    private final ItemFlag item = new ItemFlag(new ItemStack(Material.STONE), "item");
    private final IntegerFlag count = new IntegerFlag(1, "count");

    protected GiveItemAction(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_GIVEITEM_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.PLAYER;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        Map<Component, Component> out = new HashMap<>();
        ItemMeta meta = item.getFlag().getItemMeta();

        out.put(RegionMessageManager.getMessage(RegionLangKey.MENU_ITEM_NAME),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_GIVEITEM_ITEM,
                        Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(count.getFlag())),
                        Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), Component.translatable(item.getFlag().translationKey()))));
        if (meta.displayName() != null) {
            out.put(RegionMessageManager.getMessage(RegionLangKey.MENU_ITEM_DISPLAYNAME_NAME), meta.displayName());
        }
        if (meta.lore() != null) {
            out.put(RegionMessageManager.getMessage(RegionLangKey.MENU_ITEM_LORE_NAME),
                    Component.join(JoinConfiguration.separator(Component.text(";")), meta.lore()));
        }

        return out;
    }

    @Override
    public boolean useInRegions() {
        return true;
    }

    @Override
    public boolean useInNodes() {
        return true;
    }

    @Override
    public void executeRegionAction(@Nullable MinigamePlayer mgPlayer, @NotNull Region region) {
        debug(mgPlayer, region);
        if (mgPlayer != null) {
            execute(mgPlayer);
        }
    }

    @Override
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node node) {
        debug(mgPlayer, node);
        execute(mgPlayer);
    }

    private void execute(@NotNull MinigamePlayer player) {
        Map<Integer, ItemStack> unadded = player.getPlayer().getInventory().addItem(item.getFlag());

        if (!unadded.isEmpty()) {
            for (ItemStack i : unadded.values()) {
                player.getLocation().getWorld().dropItem(player.getLocation(), i);
            }
        }
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config, @NotNull String path) {
        item.saveValue(config, path);
        count.saveValue(config, path);

        //dataFixerUpper
        config.set(path + ".type", null);
        config.set(path + ".name", null);
        config.set(path + ".lore", null);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config, @NotNull String path) {
        item.loadValue(config, path);
        count.loadValue(config, path);

        ItemStack tempItem = item.getFlag();
        tempItem.setAmount(count.getFlag());

        //dataFixerUpper
        if (config.contains(path + ".type")) {
            Material mat = Material.matchMaterial(config.getString(path + ".type", ""));

            if (mat != null) {
                tempItem.setType(mat);
            }
        }
        ItemMeta meta = tempItem.getItemMeta();
        if (config.contains(path + ".name")) {
            Material mat = Material.matchMaterial(config.getString(path + ".type", ""));

            if (mat != null) {
                tempItem.setType(mat);
            }
        }

        if (config.contains(path + ".name")) {
            meta.displayName(MiniMessage.miniMessage().deserialize(config.getString(path + ".name", "")));
        }
        if (config.contains(path + ".lore")) {
            List<Component> newLore = Arrays.stream(config.getString(path + ".lore", "").split(";")).
                    map(MiniMessage.miniMessage()::deserialize).toList(); //as the description states semicolons will be used for new lines
            meta.lore(newLore);
        }
        tempItem.setItemMeta(meta);

        item.setFlag(tempItem);
    }

    @Override
    public boolean displayMenu(@NotNull final MinigamePlayer mgPlayer, Menu previous) {
        Menu menu = new Menu(3, getDisplayname(), mgPlayer);

        menu.addItem(new MenuItemBack(previous), menu.getSize() - 9);
        menu.addItem(item.getMenuItem(RegionMessageManager.getMessage(RegionLangKey.MENU_ITEM_NAME)));

        menu.addItem(count.getMenuItem(Material.STONE_SLAB,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ITEM_AMOUNT_NAME), 1, 64));

        MenuItemComponent menuItemLore = new MenuItemComponent(Material.WRITTEN_BOOK,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ITEM_LORE_NAME),
                RegionMessageManager.getMessageList(RegionLangKey.MENU_ACTION_GIVEITEM_LORE_DESCRIPTION), new Callback<>() {
            @Override
            public Component getValue() {
                ItemMeta meta = item.getFlag().getItemMeta();

                if (meta.hasLore()) {
                    return Component.join(JoinConfiguration.separator(Component.text(";")), meta.lore());
                } else {
                    return MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_ELEMENTNOTSET);
                }
            }

            @Override
            public void setValue(@Nullable Component value) {
                ItemStack itemStack = item.getFlag();
                ItemMeta meta = itemStack.getItemMeta();

                if (value == null) {
                    meta.lore(null);
                } else {
                    MiniMessage miniMessage = MiniMessage.miniMessage();

                    String valueStr = miniMessage.serialize(value);
                    List<Component> newLore = Arrays.stream(valueStr.split(";")).map(miniMessage::deserialize).toList();

                    meta.lore(newLore);
                }

                itemStack.setItemMeta(meta);
                item.setFlag(itemStack);
            }
        });
        menuItemLore.setAllowNull(true);
        menu.addItem(menuItemLore);
        menu.displayMenu(mgPlayer);
        return true;
    }
}
