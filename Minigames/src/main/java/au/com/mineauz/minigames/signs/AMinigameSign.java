package au.com.mineauz.minigames.signs;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgSignLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AMinigameSign {
    private static final NamespacedKey MINIGAME_NAME_KEY = new NamespacedKey(Minigames.getPlugin(), "minigame_name");

    private static final NamespacedKey BET_MONEY_AMOUNT_KEY = new NamespacedKey(Minigames.getPlugin(), "money_amount");
    private static final DecimalFormat FALLBACK_BET_MONEY_FORMAT = new DecimalFormat("$#0.00");
    private static final Pattern FALLBACK_BET_MONEY_PATTERN = Pattern.compile("\\$\\s*?(?<amount>[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)");

    public abstract @NotNull Component getName();

    public boolean isType(@NotNull Component signLine) { // this would be the correct way for a single sign
        PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

        return plainSerializer.serialize(getName()).equalsIgnoreCase(plainSerializer.serialize(signLine));
    }

    public boolean isType(@NotNull String signLine) { // but this is the best way for multiple since the line needs to get serialized only once
        return PlainTextComponentSerializer.plainText().serialize(getName()).equalsIgnoreCase(signLine);
    }

    public static boolean isNeutral(@NotNull Component signLine) {
        return PlainTextComponentSerializer.plainText().serialize(signLine).equalsIgnoreCase(MinigameMessageManager.getStrippedMgMessage(MgSignLangKey.TEAM_NEUTRAL));
    }

    public abstract @Nullable String getCreatePermission();

    /**
     * if the return value is null, there is no permission and everybody should be allowed to use it
     */
    public abstract @Nullable String getUsePermission();

    /**
     * if false the sign is invalid and the event will be canceled.
     */
    public abstract boolean signCreate(@NotNull SignChangeEvent event);

    public abstract boolean signUse(@NotNull Sign sign, @NotNull MinigamePlayer mgPlayer);

    public abstract void signBreak(@NotNull Sign sign, @NotNull MinigamePlayer mgPlayer);

    public static @Nullable Minigame getMinigame(@NotNull Sign sign) {
        Minigame result = Minigames.getPlugin().getMinigameManager().getMinigame(
                PlainTextComponentSerializer.plainText().serialize(sign.getSide(Side.FRONT).line(2)));

        if (result == null) {
            String name = sign.getPersistentDataContainer().get(MINIGAME_NAME_KEY, PersistentDataType.STRING);
            if (name != null) {
                result = Minigames.getPlugin().getMinigameManager().getMinigame(name);
            }
        }

        return result;
    }

    public static @Nullable Minigame getMinigame(@NotNull Sign sign, @Nullable Component changedSecondLine) {
        Minigame result = null;
        if (changedSecondLine != null) {
            result = Minigames.getPlugin().getMinigameManager().getMinigame(
                    PlainTextComponentSerializer.plainText().serialize(changedSecondLine));
        }

        if (result == null) {
            String name = sign.getPersistentDataContainer().get(MINIGAME_NAME_KEY, PersistentDataType.STRING);
            if (name != null) {
                result = Minigames.getPlugin().getMinigameManager().getMinigame(name);
            }
        }

        return result;
    }

    protected static void setPersistentMinigame(@NotNull Sign sign, @NotNull Minigame minigame) {
        sign.getPersistentDataContainer().set(MINIGAME_NAME_KEY, PersistentDataType.STRING, minigame.getName());
    }

    protected static @Nullable Double getMoneyBetFromLine(final @Nullable Component line, final boolean acceptPlainNumber) {
        if (line == null) {
            return null;
        }

        final @NotNull String line3 = PlainTextComponentSerializer.plainText().serialize(line);
        final double amount;

        final Matcher fallbackMatcher = FALLBACK_BET_MONEY_PATTERN.matcher(line3);
        if (fallbackMatcher.matches()) {
            amount = Double.parseDouble(fallbackMatcher.group("amount"));
        } else if (acceptPlainNumber && NumberUtils.isParsable(line3)){
            amount = Double.parseDouble(line3);
        } else {
            return null;
        }

        return amount;
    }

    protected static @Nullable Double getMoneyBet(final @NotNull Sign sign) {
        @Nullable Double moneyBetAmount = sign.getPersistentDataContainer().get(BET_MONEY_AMOUNT_KEY, PersistentDataType.DOUBLE);
        if (moneyBetAmount == null) {
            return getMoneyBetFromLine(sign.getSide(Side.FRONT).line(3), false);
        }
        return moneyBetAmount;
    }

    protected static void setMoneyBet(final @NotNull SignChangeEvent event, final double amount) {
        ((Sign)event.getBlock().getState()).getPersistentDataContainer().set(BET_MONEY_AMOUNT_KEY, PersistentDataType.DOUBLE, amount);

        if (Minigames.getPlugin().hasEconomy()) {
            event.line(3, Component.text(Minigames.getPlugin().getEconomy().format(amount)));
        } else {
            event.line(3, Component.text(FALLBACK_BET_MONEY_FORMAT.format(amount)));
        }
    }
}
