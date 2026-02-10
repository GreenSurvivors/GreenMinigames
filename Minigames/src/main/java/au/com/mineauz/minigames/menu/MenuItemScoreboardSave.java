package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.minigame.scoreboard.ScoreboardDisplay;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MenuItemScoreboardSave extends AMenuItem {
    private final @NotNull ScoreboardDisplay display;

    public MenuItemScoreboardSave(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                                  final @NotNull ScoreboardDisplay display) {
        super(displayType, langKey);
        this.display = display;
    }

    public MenuItemScoreboardSave(final @Nullable ItemType displayType, final @Nullable Component name,
                                  final @NotNull ScoreboardDisplay display) {
        this(displayType, name, null, display);
    }

    public MenuItemScoreboardSave(final @Nullable ItemType displayType, final @Nullable Component name,
                                  final @Nullable List<@NotNull Component> description,
                                  final @NotNull ScoreboardDisplay display) {
        super(displayType, name, description);
        this.display = display;
    }

    @Override
    public @NotNull ItemStack onClick() {
        display.placeRootSign();
        display.reload();

        getMenu().getIntendedViewer().getPlayer().closeInventory();
        return ItemStack.empty();
    }
}
