package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.config.ItemFlag;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuItemComponent;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GiveItemAction extends AAction {
    private final @NotNull ItemFlag item = new ItemFlag("item", ItemType.STONE.createItemStack());
    private final @NotNull IntegerFlag count = new IntegerFlag("count", 1);

    protected GiveItemAction(final @NotNull Key key) {
        super(key);
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
        mgPlayer.getPlayer().give(item.getFlag());
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        item.saveValue(config);
        count.saveValue(config);

        //dataFixerUpper
        config.removeChild("type");
        config.removeChild("name");
        config.removeChild("lore");
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        item.loadValue(config);
        count.loadValue(config);

        ItemStack tempItem = item.getFlag();
        tempItem.setAmount(count.getFlag());

        //dataFixerUpper
        if (config.hasChild("type")) {
            Material mat = Material.matchMaterial(config.node("type").getString(""));

            if (mat != null) {
                tempItem = tempItem.withType(mat);
            }
        }
        ItemMeta meta = tempItem.getItemMeta();

        if (config.hasChild("name")) {
            meta.displayName(MiniMessage.miniMessage().deserialize(config.node("name").getString("")));
        }
        if (config.hasChild("lore")) {
            List<Component> newLore = Arrays.stream(config.node("lore").getString("").split(";")).
                    map(MiniMessage.miniMessage()::deserialize).toList(); //as the description states semicolons will be used for new lines
            meta.lore(newLore);
        }
        tempItem.setItemMeta(meta);
        // dataFixerUpper end

        item.setFlag(tempItem);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu previous) {
        final @NotNull Menu menu = new Menu(3, getDisplayname(), previous.getIntendedViewer());

        menu.setItem(new MenuItemBack(previous), menu.getSize() - 9);
        menu.addItem(item.getMenuItem(RegionMessageManager.getMessage(RegionLangKey.MENU_ITEM_NAME)));

        menu.addItem(count.getMenuItem(ItemType.STONE_SLAB,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ITEM_AMOUNT_NAME), 1, 64));

        MenuItemComponent menuItemLore = new MenuItemComponent(ItemType.WRITTEN_BOOK,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ITEM_LORE_NAME),
                RegionMessageManager.getMessageList(RegionLangKey.MENU_ACTION_GIVEITEM_LORE_DESCRIPTION), new Callback<>() {
            @Override
            public @NotNull Component getValue() {
                ItemMeta meta = item.getFlag().getItemMeta();

                if (meta.hasLore()) {
                    return Component.join(JoinConfiguration.separator(Component.text(";")), meta.lore());
                } else {
                    return MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_ELEMENTNOTSET);
                }
            }

            @Override
            public void setValue(final @Nullable Component value) {
                final @NotNull ItemStack itemStack = item.getFlag();
                final @NotNull ItemMeta meta = itemStack.getItemMeta();

                if (value == null) {
                    meta.lore(null);
                } else {
                    final @NotNull MiniMessage miniMessage = MiniMessage.miniMessage();

                    final @NotNull String valueStr = miniMessage.serialize(value);
                    final @NotNull List<@NotNull Component> newLore = Arrays.stream(valueStr.split(";")).map(miniMessage::deserialize).toList();

                    meta.lore(newLore);
                }

                itemStack.setItemMeta(meta);
                item.setFlag(itemStack);
            }
        });
        menuItemLore.setAllowNull(true);
        menu.addItem(menuItemLore);
        menu.displayMenu();
        return true;
    }
}
