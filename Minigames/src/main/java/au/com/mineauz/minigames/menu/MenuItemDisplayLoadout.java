package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.consumer.StringConsumer;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.loadout.LoadoutModule;
import au.com.mineauz.minigames.minigame.modules.loadout.PlayerLoadout;
import au.com.mineauz.minigames.minigame.modules.team.TeamColor;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MenuItemDisplayLoadout extends AMenuItem implements StringConsumer {
    private final @NotNull PlayerLoadout loadout;
    private @Nullable Minigame minigame;
    private boolean allowDelete = true;

    ///  without a minigame the given loadout will be interpreted as a global one
    public MenuItemDisplayLoadout(final @Nullable ItemType displayType, final @Nullable Component name,
                                  final @NotNull PlayerLoadout loadout) {
        this(displayType, name, loadout, null);
    }

    /// given a valid Minigame, the loadout will be treated as if part of the given minigames {@link LoadoutModule}
    public MenuItemDisplayLoadout(final @Nullable ItemType displayType, @Nullable Component name,
                                  final @NotNull PlayerLoadout loadout,
                                  final @Nullable Minigame minigame) {
        this(displayType, name, null, loadout, minigame);
    }

    ///  without a minigame the given loadout will be interpreted as a global one
    public MenuItemDisplayLoadout(final @Nullable ItemType displayType, final @Nullable Component name,
                                  final @Nullable List<@NotNull Component> description,
                                  final @NotNull PlayerLoadout loadout) {
        this(displayType, name, description, loadout, null);
    }

    public MenuItemDisplayLoadout(final @Nullable ItemType displayType, final @Nullable Component name,
                                  final @Nullable List<@NotNull Component> description,
                                  final @NotNull PlayerLoadout loadout,
                                  final @Nullable Minigame minigame) {
        super(displayType, name, description);
        this.loadout = loadout;
        this.minigame = minigame;
        if (!loadout.isDeletable()) {
            allowDelete = false;
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull ItemStack onClick() {
        final @NotNull Menu loadoutMenu = new Menu(5, loadout.getDisplayName(), getMenu().getIntendedViewer());
        final @NotNull Menu loadoutSettingsMenu = new Menu(6, loadout.getDisplayName(), getMenu().getIntendedViewer());
        loadoutSettingsMenu.setPreviousPage(loadoutMenu);

        final @NotNull List<@NotNull AMenuItem> menuItems = new ArrayList<>();
        if (!loadout.getName().equals("default")) {
            menuItems.add(new MenuItemBoolean(ItemType.GOLD_INGOT,
                MgMenuLangKey.MENU_DISPLAYLOADOUT_USEPERMISSIONS_NAME,
                MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_DISPLAYLOADOUT_USEPERMISSIONS_DESCRIPTION,
                    Placeholder.unparsed(MinigamePlaceHolderKey.LOADOUT.getKey(), loadout.getName().toLowerCase())),
                loadout.getUsePermissionsCallback()));
        }
        final @NotNull MenuItemComponent disName = new MenuItemComponent(ItemType.PAPER, MgMenuLangKey.MENU_DISPLAYNAME_NAME, loadout.getDisplayNameCallback());
        disName.setAllowNull(true);
        menuItems.add(disName);
        menuItems.add(new MenuItemBoolean(ItemType.LEATHER_BOOTS,
            MgMenuLangKey.MENU_DISPLAYLOADOUT_ALLOWFALLDAMAGE_NAME, loadout.getFallDamageCallback()));
        menuItems.add(new MenuItemBoolean(ItemType.APPLE,
            MgMenuLangKey.MENU_DISPLAYLOADOUT_ALLOWHUNGER_NAME, loadout.getHungerCallback()));
        menuItems.add(new MenuItemInteger(ItemType.EXPERIENCE_BOTTLE,
            MgMenuLangKey.MENU_DISPLAYLOADOUT_XPLEVEL_NAME,
            MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_DISPLAYLOADOUT_XPLEVEL_DESCRIPTION),
            loadout.getLevelCallback(), -1, null));
        menuItems.add(new MenuItemBoolean(ItemType.DIAMOND_SWORD,
            MgMenuLangKey.MENU_DISPLAYLOADOUT_LOCKINVENTORY_NAME,
            loadout.getInventoryLockedCallback()));
        menuItems.add(new MenuItemBoolean(ItemType.DIAMOND_CHESTPLATE,
            MgMenuLangKey.MENU_DISPLAYLOADOUT_LOCKARMOR_NAME,
            loadout.getArmourLockedCallback()));
        menuItems.add(new MenuItemBoolean(ItemType.SHIELD,
            MgMenuLangKey.MENU_DISPLAYLOADOUT_ALLOWOFFHAND_NAME,
            loadout.getAllowOffHandCallback()));
        menuItems.add(new MenuItemBoolean(ItemType.WHITE_STAINED_GLASS_PANE,
            MgMenuLangKey.MENU_DISPLAYLOADOUT_DISPLAYINMENU_NAME,
            loadout.getDisplayInMenuCallback()));
        menuItems.add(new MenuItemList<>(ItemType.LEATHER_CHESTPLATE,
            MgMenuLangKey.MENU_DISPLAYLOADOUT_LOCKTOTEAM_NAME,
            loadout.getTeamColorCallback(), List.of(TeamColor.values())));
        loadoutSettingsMenu.addItems(menuItems);
        MenuItemBack menuItemBack = new MenuItemBack(loadoutMenu);
        loadoutSettingsMenu.setItem(menuItemBack, getMenu().getSize() - 9);

        loadout.addAddonMenuItems(loadoutSettingsMenu);

        Menu potionMenu = new Menu(5, getMenu().getTitle(), getMenu().getIntendedViewer());

        potionMenu.setPreviousPage(loadoutMenu);
        potionMenu.setItem(new MenuItemStatusEffectAdd(MenuDisplayTypes.createType(), MgMenuLangKey.MENU_STATUSEFFECTADD_NAME, loadout), potionMenu.getSize() - 1);
        potionMenu.setItem(menuItemBack, potionMenu.getSize() - 2);

        final @NotNull List<@NotNull Component> description = MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_DELETE_SHIFTRIGHTCLICK);
        final @NotNull List<@NotNull AMenuItem> potionMenuItems = new ArrayList<>();

        for (PotionEffect eff : loadout.getAllPotionEffects()) {
            potionMenuItems.add(new MenuItemStatusEffect(ItemType.POTION, Component.translatable(eff.getType().translationKey()), description, eff, loadout));
        }
        potionMenu.addItems(potionMenuItems);

        loadoutMenu.setAllowModify(true);
        loadoutMenu.setPreviousPage(getMenu());

        loadoutMenu.setItem(new MenuItemSaveLoadoutPage(ItemType.CHEST, MgMenuLangKey.MENU_DISPLAYLOADOUT_SETTINGS_NAME, loadout, loadoutSettingsMenu), 42);
        loadoutMenu.setItem(new MenuItemSaveLoadoutPage(ItemType.POTION, MgMenuLangKey.MENU_DISPLAYLOADOUT_EFFECTS_NAME, loadout, potionMenu), 43);
        loadoutMenu.setItem(new MenuItemSaveLoadoutPage(MenuDisplayTypes.saveType(), MgMenuLangKey.MENU_DISPLAYLOADOUT_SAVE_NAME, loadout, getMenu()), 44);
        final int numOfSlots = loadout.allowOffHand() ? 41 : 40; // todo don't hardcode
        for (int i = numOfSlots; i < 42; i++) {
            loadoutMenu.setItem(new MenuItemSlotFiller(), i);
        }
        loadoutMenu.displayMenu();

        for (Integer slot : loadout.getItemSlots()) {
            if (slot >= 0 && slot < 100) {
                loadoutMenu.addItemStack(loadout.getItem(slot), slot);
            } else {
                switch (slot) {
                    case 100 -> loadoutMenu.addItemStack(loadout.getItem(slot), 39);
                    case 101 -> loadoutMenu.addItemStack(loadout.getItem(slot), 38);
                    case 102 -> loadoutMenu.addItemStack(loadout.getItem(slot), 37);
                    case 103 -> loadoutMenu.addItemStack(loadout.getItem(slot), 36);
                    case -106 -> {
                        if (loadout.allowOffHand()) {
                            loadoutMenu.addItemStack(loadout.getItem(slot), 40);
                        }
                    }
                }
            }
        }

        return ItemStack.empty();
    }

    @Override
    public @NotNull ItemStack onShiftRightClick() {
        if (allowDelete) {
            MinigamePlayer mgPlayer = getMenu().getIntendedViewer();
            final @NotNull Duration reopenTime = Duration.ofSeconds(10);
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_DISPLAYLOADOUT_ENTERCHAT,
                Placeholder.component(MinigamePlaceHolderKey.TEXT.getKey(), getName()),
                Placeholder.unparsed(MinigamePlaceHolderKey.LOADOUT.getKey(), loadout.getName()),
                Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(reopenTime)));
            getMenu().closeAndWaitForInput(reopenTime, this);
            return ItemStack.empty();
        }

        return getDisplayItem();
    }

    @Override
    public void acceptString(final @NotNull String string) {
        String loadoutName = loadout.getName();

        if (string.equalsIgnoreCase("yes")) {
            if (minigame != null) {
                LoadoutModule.getMinigameModule(minigame).deleteLoadout(loadoutName);
            } else {
                LoadoutModule.deleteGlobalLoadout(loadoutName);
            }
            getMenu().removeItem(getSlot());
            getMenu().cancelWaitForInput();
            getMenu().displayMenu();
            MinigameMessageManager.sendMgMessage(getMenu().getIntendedViewer(), MinigameMessageType.SUCCESS, MgMenuLangKey.MENU_DISPLAYLOADOUT_DELETE,
                Placeholder.unparsed(MinigamePlaceHolderKey.LOADOUT.getKey(), loadoutName));
        } else {
            MinigameMessageManager.sendMgMessage(getMenu().getIntendedViewer(), MinigameMessageType.WARNING, MgMenuLangKey.MENU_DISPLAYLOADOUT_NOTDELETE,
                Placeholder.unparsed(MinigamePlaceHolderKey.LOADOUT.getKey(), loadoutName));
            getMenu().cancelWaitForInput();
            getMenu().displayMenu();
        }
    }

    public void setAllowDelete(final boolean bool) {
        allowDelete = bool;
    }
}
