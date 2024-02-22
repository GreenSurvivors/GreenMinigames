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
import au.com.mineauz.minigamesregions.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContainsEntityCondition extends ACondition {
    private final EnumFlag<EntityType> entityType = new EnumFlag<>(EntityType.PLAYER, "entity");

    private final BooleanFlag matchName = new BooleanFlag(false, "matchName");
    private final StringFlag customName = new StringFlag(null, "name");

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
    public boolean checkRegionCondition(MinigamePlayer player, @NotNull Region region) {
        Collection<Entity> entities = region.getFirstPoint().getWorld().getNearbyEntities(region.getBoundingBox());

        Pattern namePattern = null;
        if (matchName.getFlag()) {
            namePattern = createNamePattern();
        }

        for (Entity entity : entities) {
            if (entity.getType() == entityType.getFlag()) {
                if (matchName.getFlag()) {
                    Matcher m = namePattern.matcher((entity.getCustomName() == null) ? "" : entity.getCustomName());
                    if (!m.matches()) {
                        continue;
                    }
                }

                return true;
            }
        }

        return false;
    }

    private Pattern createNamePattern() {
        String name = customName.getFlag();
        if (name == null) {
            return Pattern.compile(".*");
        }

        StringBuffer buffer = new StringBuffer();

        PlayerHasItemCondition.createPattern(name, buffer); // todo don't do that. PlayerHasItem should provide this methode here!

        return Pattern.compile(buffer.toString());
    }

    @Override
    public boolean checkNodeCondition(MinigamePlayer player, @NotNull Node node) {
        return false;
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config, @NotNull String path) {
        entityType.saveValue(config, path);
        matchName.saveValue(config, path);
        customName.saveValue(config, path);
        saveInvert(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config, @NotNull String path) {
        entityType.loadValue(config, path);
        matchName.loadValue(config, path);
        customName.loadValue(config, path);
        loadInvert(config, path);
    }

    @Override
    public boolean displayMenu(MinigamePlayer player, Menu prev) {
        Menu menu = new Menu(3, getDisplayName(), player);

        menu.addItem(entityType.getMenuItem("Entity Type", Material.CHICKEN_SPAWN_EGG));
        menu.addItem(new MenuItemNewLine());

        menu.addItem(matchName.getMenuItem("Match Display Name", Material.NAME_TAG));
        MenuItemString menuItem = (MenuItemString) customName.getMenuItem(Material.NAME_TAG, "Display Name", List.of("The name to match.", "Use % to do a wildcard match"));
        menuItem.setAllowNull(true);
        menu.addItem(menuItem);

        menu.addItem(new MenuItemBack(prev), menu.getSize() - 9);
        addInvertMenuItem(menu);
        menu.displayMenu(player);
        return true;
    }

    @Override
    public void describe(@NotNull Map<String, Object> out) {
        out.put("Type", entityType.getFlag());
        if (matchName.getFlag()) {
            out.put("Match Name", customName.getFlag());
        }
    }

    @Override
    public boolean PlayerNeeded() {
        return false;
    }
}
