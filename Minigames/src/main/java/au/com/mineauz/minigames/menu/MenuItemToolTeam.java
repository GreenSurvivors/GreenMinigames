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

public class MenuItemToolTeam extends MenuItemList<TeamColor> {
    private final @NotNull Callback<TeamColor> value;

    public MenuItemToolTeam(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey, @NotNull Callback<TeamColor> value,
                            @NotNull List<@NotNull TeamColor> options) {
        super(displayType, langKey, value, options);
        this.value = value;
    }

    @Override
    public @NotNull ItemStack onClick() {
        super.onClick();
        MinigamePlayer mgPlayer = getContainer().getViewer();
        if (MinigameTool.hasMinigameTool(mgPlayer)) {
            MinigameTool tool = MinigameTool.getMinigameTool(mgPlayer);
            tool.setTeamColor(value.getValue());
        }
        return getDisplayItem();
    }

    @Override
    public @NotNull ItemStack onRightClick() {
        super.onRightClick();
        MinigamePlayer mgPlayer = getContainer().getViewer();
        if (MinigameTool.hasMinigameTool(mgPlayer)) {
            MinigameTool tool = MinigameTool.getMinigameTool(mgPlayer);
            tool.setTeamColor(value.getValue());
        }
        return getDisplayItem();
    }
}
