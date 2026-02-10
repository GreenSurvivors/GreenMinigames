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
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TakeItemAction extends AAction { // todo make ItemType match optional
    private final @NotNull ItemFlag itemToSearchFor = new ItemFlag("item", new ItemStack(Material.STONE));
    private final @NotNull IntegerFlag count = new IntegerFlag("amount", 1);

    private final @NotNull BooleanFlag matchName = new BooleanFlag("matchName", false);
    private final @NotNull BooleanFlag matchLore = new BooleanFlag("matchLore", false);
    private final @NotNull BooleanFlag matchEnchantments = new BooleanFlag("matchEnchantments", false);
    private final @NotNull BooleanFlag matchExact = new BooleanFlag("matchExact", false);

    protected TakeItemAction(final @NotNull Key key) {
        super(key);
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
    public void executeRegionAction(final @Nullable MinigamePlayer mgPlayer, final @NotNull Region region) {
        debug(mgPlayer, region);

        if (mgPlayer != null) {
            execute(mgPlayer);
        }
    }

    @Override
    public void executeNodeAction(final @NotNull MinigamePlayer mgPlayer, final @NotNull Node node) {
        debug(mgPlayer, node);
        execute(mgPlayer);
    }

    private void execute(final @NotNull MinigamePlayer mgPlayer) {
        final ItemStack match = itemToSearchFor.getFlag().clone();
        int stillToRemove = count.getFlag();

        final @Nullable ItemStack @NotNull [] contents = mgPlayer.getPlayer().getInventory().getContents();
        ItemLoop:
        for (int i = 0; i < contents.length; i++) {
            final @Nullable ItemStack itemToTest = contents[i];
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
                        final @NotNull Map<@NotNull Enchantment, @NotNull Integer> enchantmentsToSearchFor = new HashMap<>(match.getEnchantments());
                        final @NotNull Map<@NotNull Enchantment, @NotNull Integer> enchantmentsToCheck = new HashMap<>(itemToTest.getEnchantments());

                        for (final @NotNull Map.Entry<@NotNull Enchantment, @NotNull Integer> enchantmentToSearchFor : enchantmentsToSearchFor.entrySet()) {
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
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        //datafixerupper
        config.removeChild("type");

        itemToSearchFor.saveValue(config);
        count.saveValue(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        // datafixerupper
        if (config.hasChild("type")) {
            @Nullable ItemType legacy = null;
            final @Nullable Key itemKey = NamespacedKey.fromString(config.node("type").getString("").toLowerCase(Locale.ROOT));

            if (itemKey != null) {
                legacy = Registry.ITEM.get(itemKey);
            }
            if (legacy != null) {
                itemToSearchFor.setFlag(legacy.createItemStack());
            } else {
                itemToSearchFor.loadValue(config);
            }
        } else {
            itemToSearchFor.loadValue(config);
        }
        count.loadValue(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu previous) { // todo hide turned of matches
        final @NotNull Menu menu = new Menu(3, getDisplayname(), previous.getIntendedViewer());
        menu.setItem(new MenuItemBack(previous), menu.getSize() - 9);

        // we need a reference for two object we will create soon down the line
        final @NotNull CompletableFuture<@NotNull MenuItemString> futureNameItem = new CompletableFuture<>();
        final @NotNull CompletableFuture<@NotNull MenuItemString> futureLoreItem = new CompletableFuture<>();

        final @NotNull MenuItemItemNbt itemMenuItem = new MenuItemItemNbt(itemToSearchFor.getFlagOrDefault(),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TAKEITEM_ITEM_NAME), new Callback<>() {
            @Override
            public ItemStack getValue() {
                return itemToSearchFor.getFlagOrDefault();
            }

            @Override
            public void setValue(final @NotNull ItemStack value) {
                itemToSearchFor.setFlag(value);

                final @NotNull ItemMeta meta = value.getItemMeta();
                // sync with other menu Items
                try { // try - catch just to shut the IDE / compiler up. Everything gets already checked beforehand.
                    if (futureNameItem.isDone() && !futureNameItem.isCompletedExceptionally() && meta.displayName() != null) {
                        futureNameItem.get().acceptString(value.getItemMeta().getDisplayName()); //todo component
                    }

                    if (futureLoreItem.isDone() && !futureLoreItem.isCompletedExceptionally() && meta.lore() != null) {
                        futureLoreItem.get().acceptString(String.join(";", meta.getLore())); // todo component
                    }
                } catch (Throwable ignored) {
                }
            }
        });

        menu.addItem(itemMenuItem);
        menu.addItem(count.getMenuItem(ItemType.STONE_SLAB, RegionMessageManager.getMessage(RegionLangKey.MENU_ITEM_AMOUNT_NAME), 1, 999));

        menu.addItem(new MenuItemNewLine());

        menu.addItem(matchName.getMenuItem(MenuDisplayTypes.nameType(), RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TAKEITEM_MATCH_NAME_NAME)));
        final @NotNull MenuItemString nameMenuItem = new MenuItemString(MenuDisplayTypes.nameType(),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ITEM_DISPLAYNAME_NAME),
                RegionMessageManager.getMessageList(RegionLangKey.MENU_ACTION_TAKEITEM_NAME_DESCRIPTION), new Callback<>() {
            private String localCache = itemToSearchFor.getFlag().getItemMeta().getDisplayName();

            @Override
            public String getValue() {
                return localCache;
            }

            @Override
            public void setValue(final @NotNull String value) {
                localCache = value;
                itemMenuItem.processNewName(MiniMessage.miniMessage().deserialize(value));
            }
        });

        nameMenuItem.setAllowNull(true);
        futureNameItem.complete(nameMenuItem);
        menu.addItem(nameMenuItem);

        menu.addItem(matchLore.getMenuItem(ItemType.BOOK, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TAKEITEM_MATCH_LORE_NAME)));
        final MenuItemString loreMenuItem = new MenuItemString(ItemType.BOOK,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ITEM_LORE_NAME),
                RegionMessageManager.getMessageList(RegionLangKey.MENU_ACTION_TAKEITEM_LORE_DESCRIPTION), new Callback<>() {
            private @Nullable String localCache = itemToSearchFor.getFlag().getLore() == null ? null : String.join(";", itemToSearchFor.getFlag().getLore());

            @Override
            public @Nullable String getValue() {
                return localCache;
            }

            @Override
            public void setValue(final @NotNull String value) {
                MiniMessage miniMessage = MiniMessage.miniMessage();

                final @NotNull String @NotNull[] loreArray = value.split(";");
                final @NotNull List<@NotNull Component> newLore = new ArrayList<>(loreArray.length);
                for (final @NotNull String line : loreArray) {
                    newLore.add(miniMessage.deserialize(line));
                }
                itemMenuItem.processNewLore(newLore);

                localCache = value;
            }
        });
        loreMenuItem.setAllowNull(true);
        futureLoreItem.complete(loreMenuItem);
        menu.addItem(loreMenuItem);

        menu.addItem(matchEnchantments.getMenuItem(ItemType.ENCHANTED_BOOK,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TAKEITEM_MATCH_ENCHANTMENTS_NAME)));
        menu.addItem(matchExact.getMenuItem(ItemType.BOOKSHELF,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TAKEITEM_MATCH_EXACT_NAME))); //todo with callback to turn the others on/off

        menu.displayMenu();
        return true;
    }
}
