package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;
import java.util.Random;

public class RandomChanceCondition extends ACondition {
    private final @NotNull IntegerFlag chance = new IntegerFlag("chance", 50);

    protected RandomChanceCondition(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return MessageManager.getMessage(RegionLangKey.MENU_CONDITION_RANCOMCHANCE_NAME);
    }

    @Override
    public @NotNull IConditionCategory getCategory() {
        return RegionConditionCategories.MISC;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(MessageManager.getMessage(RegionLangKey.MENU_CONDITION_RANCOMCHANCE_NAME),
                MessageManager.getMessage(RegionLangKey.MENU_PERCENT_FORMAT,
                        Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(chance.getFlag()))));
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
    public boolean checkRegionCondition(final @Nullable MinigamePlayer mgPlayer, final @Nullable Region region) {
        return check();
    }

    @Override
    public boolean checkNodeCondition(final @Nullable MinigamePlayer mgPlayer, final @Nullable Node node) {
        return check();
    }

    private boolean check() {
        double chance = this.chance.getFlag().doubleValue() / 100d;
        Random rand = new Random();
        return rand.nextDouble() <= chance;
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        chance.saveValue(config);
        saveInvertedStatus(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        chance.loadValue(config);
        loadInvert(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu prev) {
        final @NotNull Menu menu = new Menu(3, getDisplayName(), prev.getIntendedViewer());
        menu.setItem(new MenuItemBack(prev), menu.getSize() - 9);
        menu.addItem(chance.getMenuItem(ItemType.ENDER_EYE, MessageManager.getMessage(RegionLangKey.MENU_RNDCHANCE_SETPERCENT_NAME), 1, 99));
        addInvertMenuItem(menu);
        menu.displayMenu();
        return true;
    }

    @Override
    public boolean playerNeeded() {
        return false;
    }
}
