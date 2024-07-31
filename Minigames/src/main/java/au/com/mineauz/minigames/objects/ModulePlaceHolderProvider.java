package au.com.mineauz.minigames.objects;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ModulePlaceHolderProvider {

    boolean hasPlaceHolder(@NotNull String placeHolder);

    default @Nullable String onPlaceHolderRequest(Player player, String game, String placeHolder) {
        return null;
    }

    @NotNull List<@NotNull String> getIdentifiers();
}
