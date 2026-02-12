package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuItemInteger;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.script.ScriptObject;
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

public class AddScoreAction extends AScoreAction {
    private final IntegerFlag amount = new IntegerFlag("amount", 1);

    protected AddScoreAction(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return MessageManager.getMessage(RegionLangKey.MENU_ACTION_SCOREADD_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.MINIGAME;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(MessageManager.getMessage(MgMiscLangKey.STATISTIC_SCORE_NAME), Component.text(amount.getFlag()));
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
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node base) {
        executeAction(mgPlayer, base);
    }

    @Override
    public void executeRegionAction(@Nullable MinigamePlayer mgPlayer, @NotNull Region base) {
        executeAction(mgPlayer, base);
    }

    private void executeAction(@Nullable MinigamePlayer player, @NotNull ScriptObject base) {
        if (player == null || !player.isInMinigame()) return;
        debug(player, base);
        player.addScore(amount.getFlag());
        player.getMinigame().setScore(player, player.getScore());
        checkScore(player);
    }


    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        amount.saveValue(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        amount.loadValue(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu previous) {
        final @NotNull Menu menu = new Menu(3, getDisplayname(), previous.getIntendedViewer());
        menu.addItem(new MenuItemInteger(ItemType.ENDER_PEARL,
                MessageManager.getMessage(MgMiscLangKey.STATISTIC_SCORE_NAME), new Callback<>() {

            @Override
            public Integer getValue() {
                return amount.getFlag();
            }

            @Override
            public void setValue(Integer value) {
                amount.setFlag(value);
            }
        }, null, null));
        menu.setItem(new MenuItemBack(previous), menu.getSize() - 9);
        menu.displayMenu();
        return true;
    }
}
