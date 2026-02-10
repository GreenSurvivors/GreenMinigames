package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.minigame.reward.Rewards;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MenuItemDisplayRewards extends AMenuItem {
    private final @NotNull Rewards rewards;

    public MenuItemDisplayRewards(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                                  final @NotNull Rewards rewards) {
        super(displayType, langKey);
        this.rewards = rewards;
    }

    public MenuItemDisplayRewards(final @Nullable ItemType displayType, final @Nullable Component name,
                                  final @NotNull Rewards rewards) {
        this(displayType, name, null, rewards);
    }

    public MenuItemDisplayRewards(final @Nullable ItemType displayType, final @Nullable Component name,
                                  final @Nullable List<@NotNull Component> description,
                                  final @NotNull Rewards rewards) {
        super(displayType, name, description);
        this.rewards = rewards;
    }

    @Override
    public @NotNull ItemStack onClick() {
        final @NotNull Menu rewardMenu = rewards.createMenu(getName(), getMenu());

        rewardMenu.displayMenu();
        return ItemStack.empty();
    }
}
