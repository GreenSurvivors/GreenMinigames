package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.gametypes.MinigameType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record TypeDependentDisplayData(@NotNull MenuItem menuItem,
                                       @NotNull List<@NotNull MinigameType> applicableTypes, int slot) {
}
