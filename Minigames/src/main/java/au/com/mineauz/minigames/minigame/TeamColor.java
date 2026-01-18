package au.com.mineauz.minigames.minigame;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.text.WordUtils;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage") // shutup Itemtype
public enum TeamColor {
    BLACK(NamedTextColor.BLACK, ItemType.BLACK_WOOL),
    BLUE(NamedTextColor.BLUE, ItemType.BLUE_WOOL),
    CYAN(NamedTextColor.DARK_AQUA, ItemType.CYAN_WOOL),
    DARK_BLUE(NamedTextColor.DARK_BLUE, ItemType.BLUE_CONCRETE),
    DARK_GRAY(NamedTextColor.DARK_GRAY, ItemType.GRAY_CONCRETE),
    DARK_GREEN(NamedTextColor.DARK_GREEN, ItemType.GREEN_CONCRETE),
    DARK_PURPLE(NamedTextColor.DARK_PURPLE, ItemType.PURPLE_CONCRETE),
    DARK_RED(NamedTextColor.DARK_RED, ItemType.RED_CONCRETE),
    GRAY(NamedTextColor.GRAY, ItemType.GRAY_WOOL),
    GREEN(NamedTextColor.GREEN, ItemType.GREEN_WOOL),
    LIGHT_BLUE(NamedTextColor.AQUA, ItemType.LIGHT_BLUE_WOOL),
    ORANGE(NamedTextColor.GOLD, ItemType.ORANGE_WOOL),
    PURPLE(NamedTextColor.LIGHT_PURPLE, ItemType.PURPLE_WOOL),
    RED(NamedTextColor.RED, ItemType.RED_WOOL),
    WHITE(NamedTextColor.WHITE, ItemType.WHITE_WOOL),
    YELLOW(NamedTextColor.YELLOW, ItemType.YELLOW_WOOL),

    NONE(NamedTextColor.DARK_RED, ItemType.BARRIER);

    private final @NotNull NamedTextColor color;
    private final @NotNull ItemType displayType;

    TeamColor(@NotNull NamedTextColor color, @NotNull ItemType displayType) {
        this.color = color;
        this.displayType = displayType;
    }

    public static @Nullable TeamColor matchColor(@NotNull String colorName) {
        for (TeamColor col : values()) {
            if (colorName.equalsIgnoreCase(col.name()) || colorName.equalsIgnoreCase(col.getUserFriendlyName())) {
                return col;
            }
        }
        return null;
    }

    public static @NotNull Set<@NotNull TeamColor> validColors() {
        return Arrays.stream(TeamColor.values()).filter(tc -> tc != NONE).collect(Collectors.toSet());
    }

    public static @NotNull Component inputColorNamesComp(final @NotNull Collection<@NotNull TeamColor> colors) {
        return Component.join(JoinConfiguration.separator(MiniMessage.miniMessage().deserialize("<gray>, </gray>")),
            colors.stream().map( color ->
                    Component.text(color.getUserFriendlyName().replaceAll(" ", "_"), color.getColor())
                ).collect(Collectors.toSet()));
    }

    public @NotNull Component getCompName() {
        return Component.text(this.getUserFriendlyName(), this.getColor());
    }

    /**
     * Not to confuse with {@link TeamColor#name()}, this returns a user-friendly representation,
     * potentially containing spaces instead of underlines
     */
    public @NotNull String getUserFriendlyName() {
        return WordUtils.capitalizeFully(super.toString().replaceAll("_", " "));
    }

    public @NotNull NamedTextColor getColor() {
        return color;
    }

    public @NotNull ItemType getDisplayType() {
        return displayType;
    }
}
