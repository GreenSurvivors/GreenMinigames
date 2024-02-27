package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.config.ItemFlag;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TakeItemAction extends AAction { // todo make material match optional
    private final ItemFlag itemToSearchFor = new ItemFlag(new ItemStack(Material.STONE), "item");
    private final IntegerFlag count = new IntegerFlag(1, "amount");

    private final BooleanFlag matchName = new BooleanFlag(false, "matchName");
    private final BooleanFlag matchLore = new BooleanFlag(false, "matchLore");
    private final BooleanFlag matchEnchantments = new BooleanFlag(false, "matchEnchantments");
    private final BooleanFlag matchExact = new BooleanFlag(false, "matchExact");

    protected TakeItemAction(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TAKEITEM_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.PLAYER;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(
                RegionMessageManager.getMessage(RegionLangKey.MENU_ITEM_NAME),
                Component.translatable(itemToSearchFor.getFlag().getType().translationKey()),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ITEM_AMOUNT_NAME),
                Component.text(count.getFlag()));
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

    private void execute(@NotNull MinigamePlayer mgPlayer) {
        ItemStack match = itemToSearchFor.getFlag().clone();
        int stillToRemove = count.getFlag();

        @Nullable ItemStack @NotNull [] contents = mgPlayer.getPlayer().getInventory().getContents();
        ItemLoop:
        for (int i = 0; i < contents.length; i++) {
            ItemStack itemToTest = contents[i];
            if (itemToTest != null && itemToTest.getType() == match.getType()) {
                if (matchExact.getFlag()) {
                    if (match.hasItemMeta() != itemToTest.hasItemMeta() || (
                            match.hasItemMeta() && !match.getItemMeta().equals(itemToTest.getItemMeta()))) {
                        continue;
                    }
                } else {
                    if (matchName.getFlag() && itemToTest.displayName() != match.displayName()) {
                        continue;
                    }

                    if (matchLore.getFlag()) {
                        if (match.lore() == null && itemToTest.lore() != null) {
                            continue;
                        }

                        if (match.lore() != null) {
                            if (itemToTest.lore() == null) {
                                continue;
                            }

                            for (Component lore : match.lore()) {
                                for (Component loreToCheck : itemToTest.lore()) {
                                    if (lore != loreToCheck) {
                                        continue ItemLoop;
                                    }
                                }
                            }
                        }
                    }

                    if (matchEnchantments.getFlag()) {
                        Map<Enchantment, Integer> enchantmentsToSearchFor = new HashMap<>(match.getEnchantments());
                        Map<Enchantment, Integer> enchantmentsToCheck = new HashMap<>(itemToTest.getEnchantments());

                        for (Map.Entry<Enchantment, Integer> enchantmentToSearchFor : enchantmentsToSearchFor.entrySet()) {
                            if (Objects.equals(enchantmentsToCheck.get(enchantmentToSearchFor.getKey()), enchantmentToSearchFor.getValue())) {
                                enchantmentsToCheck.remove(enchantmentToSearchFor.getKey());
                            } else {
                                continue ItemLoop;
                            }
                        }

                        if (!enchantmentsToCheck.isEmpty()) {
                            continue;
                        }
                    }
                }


                if (stillToRemove >= itemToTest.getAmount()) {
                    stillToRemove -= itemToTest.getAmount();
                    contents[i] = null; // remove item
                } else {
                    itemToTest.setAmount(itemToTest.getAmount() - stillToRemove);
                    contents[i] = itemToTest;

                    break;
                }
            }
        }

        mgPlayer.getPlayer().getInventory().setContents(contents);
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config, @NotNull String path) {
        //datafixerupper
        if (config.contains(path + ".type")) {
            config.set(path + ".type", null);
        }
        itemToSearchFor.saveValue(config, path);
        count.saveValue(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config, @NotNull String path) {
        // datafixerupper
        if (config.contains(path + ".type")) {
            Material legacy = Material.matchMaterial(config.getString(path + ".type", ""));
            if (legacy != null) {
                itemToSearchFor.setFlag(new ItemStack(legacy));
            } else {
                itemToSearchFor.loadValue(config, path);
            }
        } else {
            itemToSearchFor.loadValue(config, path);
        }
        count.loadValue(config, path);
    }

    @Override
    public boolean displayMenu(final @NotNull MinigamePlayer mgPlayer, Menu previous) { // todo hide turned of matches
        final Menu menu = new Menu(3, getDisplayname(), mgPlayer);
        menu.addItem(new MenuItemBack(previous), menu.getSize() - 9);

        // we need a reference for two object we will create soon down the line
        final CompletableFuture<MenuItemString> futureNameItem = new CompletableFuture<>();
        final CompletableFuture<MenuItemString> futureLoreItem = new CompletableFuture<>();

        final MenuItemItemNbt itemMenuItem = new MenuItemItemNbt(itemToSearchFor.getFlagOrDefault(),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TAKEITEM_ITEM_NAME), new Callback<>() {
            @Override
            public ItemStack getValue() {
                return itemToSearchFor.getFlagOrDefault();
            }

            @Override
            public void setValue(ItemStack value) {
                itemToSearchFor.setFlag(value);

                ItemMeta meta = value.getItemMeta();
                // sync with other menu Items
                try { // try - catch just to shut the IDE / compiler up. Everything gets already checked beforehand.
                    if (futureNameItem.isDone() && !futureNameItem.isCompletedExceptionally() && meta.displayName() != null) {
                        futureNameItem.get().checkValidEntry(value.getItemMeta().getDisplayName()); //todo component
                    }

                    if (futureLoreItem.isDone() && !futureLoreItem.isCompletedExceptionally() && meta.lore() != null) {
                        futureLoreItem.get().checkValidEntry(String.join(";", meta.getLore())); // todo component
                    }
                } catch (Throwable ignored) {
                }
            }
        });

        menu.addItem(itemMenuItem);
        menu.addItem(count.getMenuItem(Material.STONE_SLAB, RegionMessageManager.getMessage(RegionLangKey.MENU_ITEM_AMOUNT_NAME), 1, 999));

        menu.addItem(new MenuItemNewLine());

        menu.addItem(matchName.getMenuItem(Material.NAME_TAG, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TAKEITEM_MATCH_NAME_NAME)));
        final MenuItemString nameMenuItem = new MenuItemString(Material.NAME_TAG,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ITEM_DISPLAYNAME_NAME),
                RegionMessageManager.getMessageList(RegionLangKey.MENU_ACTION_TAKEITEM_NAME_DESCRIPTION), new Callback<>() {
            private String localCache = itemToSearchFor.getFlag().getItemMeta().getDisplayName();

            @Override
            public String getValue() {
                return localCache;
            }

            @Override
            public void setValue(String value) {
                localCache = value;
                itemMenuItem.processNewName(MiniMessage.miniMessage().deserialize(value));
            }
        });

        nameMenuItem.setAllowNull(true);
        futureNameItem.complete(nameMenuItem);
        menu.addItem(nameMenuItem);

        menu.addItem(matchLore.getMenuItem(Material.BOOK, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TAKEITEM_MATCH_LORE_NAME)));
        final MenuItemString loreMenuItem = new MenuItemString(Material.BOOK,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ITEM_LORE_NAME),
                RegionMessageManager.getMessageList(RegionLangKey.MENU_ACTION_TAKEITEM_LORE_DESCRIPTION), new Callback<>() {
            private String localCache = itemToSearchFor.getFlag().getLore() == null ? null : String.join(";", itemToSearchFor.getFlag().getLore());

            @Override
            public String getValue() {
                return localCache;
            }

            @Override
            public void setValue(String value) {
                MiniMessage miniMessage = MiniMessage.miniMessage();

                String[] loreArray = value.split(";");
                List<Component> newLore = new ArrayList<>(loreArray.length);
                for (String line : loreArray) {
                    newLore.add(miniMessage.deserialize(line));
                }
                itemMenuItem.processNewLore(newLore);

                localCache = value;
            }
        });
        loreMenuItem.setAllowNull(true);
        futureLoreItem.complete(loreMenuItem);
        menu.addItem(loreMenuItem);

        menu.addItem(matchEnchantments.getMenuItem(Material.ENCHANTED_BOOK,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TAKEITEM_MATCH_ENCHANTMENTS_NAME)));
        menu.addItem(matchExact.getMenuItem(Material.BOOKSHELF,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TAKEITEM_MATCH_EXACT_NAME))); //todo with callback to turn the others on/off

        menu.displayMenu(mgPlayer);
        return true;
    }
}
