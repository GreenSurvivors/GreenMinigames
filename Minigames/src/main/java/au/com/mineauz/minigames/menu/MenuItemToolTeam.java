package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.minigame.TeamColor;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.tool.MinigameTool;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MenuItemToolTeam extends MenuItemList<TeamColor> {
    private final Callback<TeamColor> value;

    public MenuItemToolTeam(@Nullable Material displayMat, @NotNull MinigameLangKey langKey, @NotNull Callback<TeamColor> value,
                            @NotNull List<@NotNull TeamColor> options) {
        super(displayMat, langKey, value, options);
        this.value = value;
    }

    @Override
    public ItemStack onClick() {
        super.onClick();
        MinigamePlayer mgPlayer = getContainer().getViewer();
        if (MinigameTool.hasMinigameTool(mgPlayer)) {
            MinigameTool tool = MinigameTool.getMinigameTool(mgPlayer);
            tool.setTeamColor(value.getValue());
        }
        return getDisplayItem();
    }

    @Override
    public ItemStack onRightClick() {
        super.onRightClick();
        MinigamePlayer mgPlayer = getContainer().getViewer();
        if (MinigameTool.hasMinigameTool(mgPlayer)) {
            MinigameTool tool = MinigameTool.getMinigameTool(mgPlayer);
            tool.setTeamColor(value.getValue());
        }
        return getDisplayItem();
    }
}
