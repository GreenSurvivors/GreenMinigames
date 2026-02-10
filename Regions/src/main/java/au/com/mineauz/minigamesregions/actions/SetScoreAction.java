package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;

public class SetScoreAction extends AScoreAction {
    private final @NotNull IntegerFlag amount = new IntegerFlag("amount", 1);

    protected SetScoreAction(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SETSCORE_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.MINIGAME;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(MinigameMessageManager.getMgMessage(MgMiscLangKey.STATISTIC_SCORE_NAME), Component.text(amount.getFlag()));
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
    public void executeNodeAction(final @NotNull MinigamePlayer mgPlayer, final @NotNull Node node) {
        if (!mgPlayer.isInMinigame()) return;
        mgPlayer.setScore(amount.getFlag());
        mgPlayer.getMinigame().setScore(mgPlayer, mgPlayer.getScore());
        checkScore(mgPlayer);
    }

    @Override
    public void executeRegionAction(final @Nullable MinigamePlayer mgPlayer, final @NotNull Region region) {
        if (mgPlayer == null || !mgPlayer.isInMinigame()) return;
        mgPlayer.setScore(amount.getFlag());
        mgPlayer.getMinigame().setScore(mgPlayer, mgPlayer.getScore());
        checkScore(mgPlayer);
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
        menu.addItem(amount.getMenuItem(ItemType.ENDER_PEARL,
                MinigameMessageManager.getMgMessage(MgMiscLangKey.STATISTIC_SCORE_NAME), null, null));
        menu.setItem(new MenuItemBack(previous), menu.getSize() - 9);
        menu.displayMenu();
        return true;
    }
}
