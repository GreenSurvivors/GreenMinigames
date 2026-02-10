package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.ComponentFlag;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.ResourcePack;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class ResourcePackModule extends AMinigameModule { //todo rework to work with multiple ressource packs
    private final @NotNull BooleanFlag enabled = new BooleanFlag("resourcePackEnabled", false);
    private final @NotNull ComponentFlag resourcePackDisplayName = new ComponentFlag("resourcePackName", Component.empty());
    private final @NotNull BooleanFlag forced = new BooleanFlag("forceResourcePack", false);
    private @NotNull String resourcePackName = PlainTextComponentSerializer.plainText().serialize(resourcePackDisplayName.getFlag());

    public ResourcePackModule(final @NotNull Minigame mgm, final @NotNull Key key) {
        super(mgm, key);
    }

    public static @Nullable ResourcePackModule getMinigameModule(final @NotNull Minigame mgm) {
        return ((ResourcePackModule) mgm.getModule(MgDefaultModules.RESOURCEPACK.getKey()));
    }

    public boolean isEnabled() {
        return enabled.getFlag();
    }

    public void setEnabled(Boolean bool) {
        enabled.setFlag(bool);
    }

    public boolean isForced() {
        return forced.getFlag();
    }

    public void setResourcePackname(final  @NotNull Component name) {
        resourcePackDisplayName.setFlag(name);
    }

    public @NotNull Component getResourcePackDisplayName() {
        return resourcePackDisplayName.getFlag();
    }

    public @NotNull String getResourcePackName() {
        return resourcePackName;
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public void save(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        enabled.saveValue(config);
        resourcePackDisplayName.saveValue(config);
        forced.saveValue(config);
    }

    @Override
    public void load(final @NotNull CommentedConfigurationNode config) {
        enabled.loadValue(config);
        resourcePackDisplayName.loadValue(config);
        forced.loadValue(config);
    }

    @Override
    public void addEditMenuOptions(final @NotNull Menu previousMenu) {
        final @NotNull Menu menu = new Menu(3, MgMenuLangKey.MENU_RESOURCEPACK_OPTIONS_NAME, previousMenu.getIntendedViewer());
        menu.setPreviousPage(previousMenu);
        menu.addItem(enabled.getMenuItem(ItemType.MAP, MgMenuLangKey.MENU_RESOURCEPACK_OPTIONS_ENABLE_NAME));
        MenuItemComponent item = new MenuItemComponent(ItemType.PAPER, MgMenuLangKey.MENU_RESOURCEPACK_OPTIONS_DISPLAYNAME_NAME,
            new Callback<>() {
                @Override
                public @NotNull Component getValue() {
                    return resourcePackDisplayName.getFlag();
                }

                @Override
                public void setValue(final @NotNull Component value) {
                    resourcePackDisplayName.setFlag(value);
                    resourcePackName = PlainTextComponentSerializer.plainText().serialize(value);
                }
            }) {
            @Override
            public void acceptString(final @NotNull String string) {
                if (string.isEmpty()) {
                    super.acceptString(string);
                    return;
                }
                final @Nullable ResourcePack pack = Minigames.getPlugin().getResourcePackManager().getResourcePack(string);
                if (pack == null) {
                    getMenu().cancelWaitForInput();
                    getMenu().displayMenu();
                    MinigameMessageManager.sendMgMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR,
                        MgMiscLangKey.MINIGAME_RESSOURCEPACK_NORESSOURCEPACK,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), string));
                } else {
                    super.acceptString(string);
                }
            }
        };
        menu.addItem(item);
        menu.addItem(forced.getMenuItem(ItemType.SKELETON_SKULL, MgMenuLangKey.MENU_RESOURCEPACK_OPTIONS_FORCE_NAME));
        MenuItemPage previousMenuItem = new MenuItemPage(ItemType.MAP, MgMenuLangKey.MENU_RESOURCEPACK_OPTIONS_NAME, menu);
        menu.setItem(new MenuItemBack(previousMenu), menu.getSize() - 9);
        previousMenu.addItem(previousMenuItem);
    }
}
