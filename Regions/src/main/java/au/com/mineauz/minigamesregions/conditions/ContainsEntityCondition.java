package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuItemNewLine;
import au.com.mineauz.minigames.menu.MenuItemString;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import au.com.mineauz.minigamesregions.util.RegionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContainsEntityCondition extends ACondition { // todo same entity settings as SpawnEntity Action also amount and make entityType also optional
    private final @NotNull EnumFlag<@NotNull EntityType> entityType = new EnumFlag<>("entity", EntityType.PLAYER);

    private final BooleanFlag matchName = new BooleanFlag("matchName", false);
    private final StringFlag customName = new StringFlag("name", null);

    protected ContainsEntityCondition(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_CONTAINSENTITY_NAME);
    }

    @Override
    public @NotNull IConditionCategory getCategory() {
        return RegionConditionCategories.WORLD;
    }

    @Override
    public boolean useInRegions() {
        return true;
    }

    @Override
    public boolean useInNodes() {
        return false;
    }

    @Override
    public boolean checkRegionCondition(MinigamePlayer mgPlayer, @NotNull Region region) {
        Collection<Entity> entities = region.getFirstPoint().getWorld().getNearbyEntities(region.getBoundingBox());

        Pattern namePattern = null;
        if (matchName.getFlag()) {
            namePattern = createNamePattern();
        }

        for (Entity entity : entities) {
            if (entity.getType() == entityType.getFlag()) {
                if (matchName.getFlag()) {
                    Matcher matcher = namePattern.matcher((entity.customName() == null) ? "" : entity.getCustomName());
                    if (!matcher.matches()) {
                        continue;
                    }
                }

                return true;
            }
        }

        return false;
    }

    private @NotNull Pattern createNamePattern() {
        String name = customName.getFlag();
        if (name == null) {
            return Pattern.compile(".*");
        }

        StringBuffer buffer = new StringBuffer();

        RegionUtils.createWildcardPattern(name, buffer);

        return Pattern.compile(buffer.toString());
    }

    @Override
    public boolean checkNodeCondition(MinigamePlayer mgPlayer, @NotNull Node node) {
        return false;
    }

    @Override
    public void saveArguments(@NotNull CommentedConfigurationNode config) throws SerializationException {
        entityType.saveValue(config);
        matchName.saveValue(config);
        customName.saveValue(config);
        saveInvertedStatus(config);
    }

    @Override
    public void loadArguments(@NotNull CommentedConfigurationNode config) {
        entityType.loadValue(config);
        matchName.loadValue(config);
        customName.loadValue(config);
        loadInvert(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu prev) {
        final @NotNull Menu menu = new Menu(3, getDisplayName(), prev.getIntendedViewer());

        menu.addItem(entityType.getMenuItem(ItemType.CHICKEN_SPAWN_EGG,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ENTITY_TYPE_NAME)));
        menu.addItem(new MenuItemNewLine());

        menu.addItem(matchName.getMenuItem(ItemType.NAME_TAG,
                RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_CONTAINSENTITY_MATCH_CUSTOMNAME_NAME)));
        final @NotNull MenuItemString menuItem = customName.getMenuItem(ItemType.NAME_TAG,
                RegionMessageManager.getMessage(RegionLangKey.MENU_ENTITY_CUSTOMNAME_NAME),
                RegionMessageManager.getMessageList(RegionLangKey.MENU_CONDITION_CONTAINSENTITY_CUSTOMNAME_DESCRIPTION));
        menuItem.setAllowNull(true);
        menu.addItem(menuItem);

        menu.setItem(new MenuItemBack(prev), menu.getSize() - 9);
        addInvertMenuItem(menu);
        menu.displayMenu();
        return true;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        HashMap<Component, Component> out = new HashMap<>();

        out.put(RegionMessageManager.getMessage(RegionLangKey.MENU_ENTITY_TYPE_NAME),
                Component.translatable(entityType.getFlag().translationKey()));
        if (matchName.getFlag()) {
            out.put(RegionMessageManager.getMessage(RegionLangKey.MENU_ENTITY_CUSTOMNAME_NAME),
                    MiniMessage.miniMessage().deserialize(customName.getFlag()));
        }

        return out;
    }

    @Override
    public boolean playerNeeded() {
        return false;
    }
}
