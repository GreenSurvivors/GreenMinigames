package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.minigame.reward.Rewards;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MenuItemDisplayRewards extends MenuItem {
    private final @NotNull Rewards rewards;

    public MenuItemDisplayRewards(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey, @NotNull Rewards rewards) {
        super(displayType, langKey);
        this.rewards = rewards;
    }

    public MenuItemDisplayRewards(@Nullable ItemType displayType, @Nullable Component name, @NotNull Rewards rewards) {
        super(displayType, name);
        this.rewards = rewards;
    }

    public MenuItemDisplayRewards(@Nullable ItemType displayType, @Nullable Component name,
                                  @Nullable List<@NotNull Component> description, @NotNull Rewards rewards) {
        super(displayType, name, description);
        this.rewards = rewards;
    }

    @Override
    public @NotNull ItemStack onClick() {
        Menu rewardMenu = rewards.createMenu(getName(), getContainer().getViewer(), getContainer());

        rewardMenu.displayMenu(getContainer().getViewer());
        return ItemStack.empty();
    }
}
