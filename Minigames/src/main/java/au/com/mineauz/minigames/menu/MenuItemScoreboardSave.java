package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.minigame.ScoreboardDisplay;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MenuItemScoreboardSave extends MenuItem {
    private final @NotNull ScoreboardDisplay disp;

    public MenuItemScoreboardSave(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey, @NotNull ScoreboardDisplay disp) {
        super(displayType, langKey);
        this.disp = disp;
    }

    public MenuItemScoreboardSave(@Nullable ItemType displayType, @Nullable Component name, @NotNull ScoreboardDisplay disp) {
        super(displayType, name);
        this.disp = disp;
    }

    public MenuItemScoreboardSave(@Nullable ItemType displayType, @Nullable Component name,
                                  @Nullable List<@NotNull Component> description, @NotNull ScoreboardDisplay disp) {
        super(displayType, name, description);
        this.disp = disp;
    }

    @Override
    public @NotNull ItemStack onClick() {
        disp.placeRootSign();
        disp.reload();

        getContainer().getViewer().getPlayer().closeInventory();
        return ItemStack.empty();
    }
}
