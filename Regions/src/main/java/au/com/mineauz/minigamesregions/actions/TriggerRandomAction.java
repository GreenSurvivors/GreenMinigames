package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.ActionExecutor;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import au.com.mineauz.minigamesregions.triggers.MgRegTrigger;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TriggerRandomAction extends AAction {
    private final IntegerFlag timesTriggered = new IntegerFlag("timesTriggered", 1);
    private final BooleanFlag allowSameTrigger = new BooleanFlag("randomPerTrigger", false); // todo datafixerupper rename

    protected TriggerRandomAction(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TRIGGERRANDOM_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.REGION_NODE;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TRIGGERRANDOM_TIMES_NAME), Component.text(timesTriggered.getFlag()),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TRIGGERRANDOM_SAME_NAME),
                MinigameMessageManager.getMgMessage(allowSameTrigger.getFlag() ? MgCommandLangKey.COMMAND_STATE_ENABLED : MgCommandLangKey.COMMAND_STATE_DISABLED));
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
        List<ActionExecutor> exs = new ArrayList<>();
        for (ActionExecutor ex : region.getExecutors()) {
            if (ex.getTrigger() == MgRegTrigger.RANDOM) {
                exs.add(ex);
            }
        }
        Collections.shuffle(exs);
        if (timesTriggered.getFlag() == 1) {
            if (region.checkConditions(exs.getFirst(), mgPlayer) && exs.getFirst().canBeTriggered(mgPlayer))
                region.execute(exs.getFirst(), mgPlayer);
        } else {
            for (int i = 0; i < timesTriggered.getFlag(); i++) {
                if (allowSameTrigger.getFlag()) {
                    if (region.checkConditions(exs.getFirst(), mgPlayer) && exs.getFirst().canBeTriggered(mgPlayer)) {
                        region.execute(exs.getFirst(), mgPlayer);
                    }
                    Collections.shuffle(exs);
                } else {
                    if (i == timesTriggered.getFlag()) {
                        break;
                    }
                    if (region.checkConditions(exs.get(i), mgPlayer) && exs.get(i).canBeTriggered(mgPlayer)) {
                        region.execute(exs.get(i), mgPlayer);
                    }
                }
            }
        }
    }

    @Override
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node node) { //todo regions and nodes need another interface, so this can be one methode.
        debug(mgPlayer, node);
        List<ActionExecutor> exs = new ArrayList<>();
        for (ActionExecutor ex : node.getExecutors()) {
            if (ex.getTrigger() == MgRegTrigger.RANDOM) {
                exs.add(ex);
            }
        }
        Collections.shuffle(exs);
        if (timesTriggered.getFlag() == 1) {
            if (node.checkConditions(exs.getFirst(), mgPlayer) && exs.getFirst().canBeTriggered(mgPlayer))
                node.execute(exs.getFirst(), mgPlayer);
        } else {
            for (int i = 0; i < timesTriggered.getFlag(); i++) {
                if (allowSameTrigger.getFlag()) {
                    if (node.checkConditions(exs.getFirst(), mgPlayer) && exs.getFirst().canBeTriggered(mgPlayer)) {
                        node.execute(exs.getFirst(), mgPlayer);
                    }
                    Collections.shuffle(exs);
                } else {
                    if (i == timesTriggered.getFlag()) {
                        break;
                    }
                    if (node.checkConditions(exs.get(i), mgPlayer) && exs.get(i).canBeTriggered(mgPlayer)) {
                        node.execute(exs.get(i), mgPlayer);
                    }
                }
            }
        }
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        timesTriggered.saveValue(config);
        allowSameTrigger.saveValue(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        timesTriggered.loadValue(config);
        allowSameTrigger.loadValue(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu previous) {
        final @NotNull Menu menu = new Menu(3, getDisplayname(), previous.getIntendedViewer());
        menu.addItem(new MenuItemBack(previous), menu.getSize() - 9);
        menu.addItem(timesTriggered.getMenuItem(ItemType.COMMAND_BLOCK, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TRIGGERRANDOM_TIMES_NAME), 1, null));
        menu.addItem(allowSameTrigger.getMenuItem(ItemType.ENDER_PEARL, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TRIGGERRANDOM_SAME_NAME),
                RegionMessageManager.getMessageList(RegionLangKey.MENU_ACTION_TRIGGERRANDOM_SAME_DESCRIPTION)));
        menu.displayMenu();
        return true;
    }
}
