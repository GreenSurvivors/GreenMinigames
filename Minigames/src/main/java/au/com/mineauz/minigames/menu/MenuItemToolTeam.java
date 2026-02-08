package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.minigame.modules.team.TeamColor;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.tool.MinigameTool;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// list chosen instead of enum, because not every color is applicable
public class MenuItemToolTeam extends MenuItemList<TeamColor> {

    public MenuItemToolTeam(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                            final @NotNull Callback<TeamColor> callback, final @NotNull List<@NotNull TeamColor> options) {
        super(displayType, langKey, callback, options);
    }

    @Override
    public @NotNull ItemStack onClick() {
        final @NotNull ItemStack result = super.onClick();
        final @NotNull MinigamePlayer mgPlayer = getMenu().getIntendedViewer();
        if (MinigameTool.hasMinigameTool(mgPlayer)) {
            MinigameTool tool = MinigameTool.getMinigameTool(mgPlayer);
            tool.setTeamColor(callback.getValue());
        }
        return result;
    }

    @Override
    public @NotNull ItemStack onRightClick() {
        final @NotNull ItemStack result = super.onRightClick();
        final @NotNull MinigamePlayer mgPlayer = getMenu().getIntendedViewer();
        if (MinigameTool.hasMinigameTool(mgPlayer)) {
            MinigameTool tool = MinigameTool.getMinigameTool(mgPlayer);
            tool.setTeamColor(callback.getValue());
        }
        return result;
    }
}
