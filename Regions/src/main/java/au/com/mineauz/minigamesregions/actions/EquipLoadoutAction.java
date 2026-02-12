package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.modules.loadout.LoadoutModule;
import au.com.mineauz.minigames.minigame.modules.loadout.PlayerLoadout;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;

public class EquipLoadoutAction extends AAction {
    private final StringFlag loadout = new StringFlag("loadout", "default");
    private final BooleanFlag equipOnTrigger = new BooleanFlag("equipOnTrigger", false);

    protected EquipLoadoutAction(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return MessageManager.getMessage(RegionLangKey.MENU_ACTION_EQUIPLOADOUT_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.MINIGAME;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(MessageManager.getMessage(RegionLangKey.MENU_ACTION_LOADOUT_NAME), Component.text(loadout.getFlag()));
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
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node node) {
        debug(mgPlayer, node);
        if (!mgPlayer.isInMinigame()) return;
        LoadoutModule lmod = LoadoutModule.getMinigameModule(mgPlayer.getMinigame());
        if (lmod != null && lmod.hasLoadout(loadout.getFlag())) {
            PlayerLoadout pLoadOut = lmod.getLoadout(loadout.getFlag());
            mgPlayer.setLoadout(pLoadOut);
            if (equipOnTrigger.getFlag()) {
                pLoadOut.equipLoadout(mgPlayer);
            }
        }
    }

    @Override
    public void executeRegionAction(@Nullable MinigamePlayer mgPlayer, @NotNull Region region) {
        debug(mgPlayer, region);
        if (mgPlayer == null || !mgPlayer.isInMinigame()) return;
        LoadoutModule lmod = LoadoutModule.getMinigameModule(mgPlayer.getMinigame());
        if (lmod != null && lmod.hasLoadout(loadout.getFlag())) {
            PlayerLoadout pLoadOut = lmod.getLoadout(loadout.getFlag());
            mgPlayer.setLoadout(pLoadOut);
            if (equipOnTrigger.getFlag()) {
                pLoadOut.equipLoadout(mgPlayer);
            }
        }
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        loadout.saveValue(config);
        equipOnTrigger.saveValue(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        loadout.loadValue(config);
        equipOnTrigger.loadValue(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu previous) {
        final @NotNull Menu menu = new Menu(3, getDisplayname(), previous.getIntendedViewer());
        menu.setItem(new MenuItemBack(previous), menu.getSize() - 9);
        menu.addItem(new MenuItemString(ItemType.DIAMOND_SWORD, MessageManager.getMessage(RegionLangKey.MENU_ACTION_LOADOUT_NAME), new Callback<>() {

            @Override
            public String getValue() {
                return loadout.getFlag();
            }

            @Override
            public void setValue(String value) {
                loadout.setFlag(value);
            }
        }));

        menu.addItem(new MenuItemBoolean(ItemType.PAPER, MessageManager.getMessage(RegionLangKey.MENU_ACTION_LOADOUT_ONTRIGGER_NAME),
                MessageManager.getMessageList(RegionLangKey.MENU_ACTION_LOADOUT_ONTRIGGER_DESCRIPTION), new Callback<>() {
            @Override
            public Boolean getValue() {
                return equipOnTrigger.getFlag();
            }

            @Override
            public void setValue(Boolean value) {
                equipOnTrigger.setFlag(value);
            }
        }));
        menu.displayMenu();
        return true;
    }
}
