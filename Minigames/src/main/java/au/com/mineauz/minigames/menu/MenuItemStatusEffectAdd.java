package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.consumer.StringConsumer;
import au.com.mineauz.minigames.minigame.modules.loadout.PlayerLoadout;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class MenuItemStatusEffectAdd extends AMenuItem implements StringConsumer {
    private static final @NotNull Pattern POSITIV_INT_PATTERN = Pattern.compile("[+]?[0-9]+");
    private final @NotNull PlayerLoadout loadout;

    public MenuItemStatusEffectAdd(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                                   final @NotNull PlayerLoadout loadout) {
        super(displayType, langKey);
        this.loadout = loadout;
    }

    public MenuItemStatusEffectAdd(final @Nullable ItemType displayType, final @Nullable Component name,
                                   final @NotNull PlayerLoadout loadout) {
        this(displayType, name, null, loadout);
    }

    public MenuItemStatusEffectAdd(final @Nullable ItemType displayType, final @Nullable Component name,
                                   final @Nullable List<@NotNull Component> description,
                                   final @NotNull PlayerLoadout loadout) {
        super(displayType, name, description);
        this.loadout = loadout;
    }

    @Override
    public @NotNull ItemStack onClick() {
        final @NotNull MinigamePlayer mgPlayer = getMenu().getIntendedViewer();

        // time for a player to write a valid potion into chat
        final @NotNull Duration reopenTime = Duration.ofSeconds(30);
        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_STATUSEFFECTADD_ENTERCHAT,
            Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(reopenTime)));

        getMenu().closeAndWaitForInput(reopenTime, this);
        return ItemStack.empty();
    }

    @Override
    public void acceptString(final @NotNull String entry) {
        final @NotNull String @NotNull [] split = entry.split(", ");
        if (split.length == 3) {
            final @NotNull String effect = split[0].toLowerCase(Locale.ROOT);
            final @Nullable PotionEffectType potionEffectType = Registry.EFFECT.get(NamespacedKey.fromString(effect));
            if (potionEffectType != null) {
                if (POSITIV_INT_PATTERN.matcher(split[1]).matches() && Integer.parseInt(split[1]) != 0) {
                    int level = Integer.parseInt(split[1]) - 1;

                    @Nullable Long dur = split[2].equalsIgnoreCase("inf") ? Long.valueOf(-1L) : MinigameUtils.parsePeriod(split[2]);
                    if (dur != null) {
                        dur = Math.max(-1, Math.min(dur, 100000)); // stay in range
                        dur = TimeUnit.MILLISECONDS.toSeconds(dur) * 20; // millis to ticks

                        final @NotNull List<@NotNull Component> description = new ArrayList<>();
                        description.add(MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_DELETE_SHIFTRIGHTCLICK));

                        final @NotNull PotionEffect potionEffect = new PotionEffect(potionEffectType, dur.intValue(), level);
                        for (final int slot : getMenu().getUsedSlots()) {
                            if (getMenu().getMenuItem(slot) instanceof final @NotNull MenuItemStatusEffect pot) {
                                if (pot.getEffect().getType() == potionEffect.getType()) {
                                    pot.onShiftRightClick();
                                    break;
                                }
                            }
                        }
                        for (int i = 0; i < 36; i++) {
                            if (!getMenu().hasMenuItem(i)) {
                                getMenu().setItem(new MenuItemStatusEffect(ItemType.POTION, Component.translatable(potionEffectType.translationKey()), description, potionEffect, loadout), i);
                                loadout.addPotionEffect(potionEffect);
                                break;
                            }
                        }
                    } else {
                        MinigameMessageManager.sendMgMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTTIME,
                            Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), split[2]));
                    }
                } else {
                    MinigameMessageManager.sendMgMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTNUMBER,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), split[2]));
                }
            } else {
                MinigameMessageManager.sendMgMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTSTATUSEFFECT,
                    Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), split[2]));
            }

            getMenu().cancelWaitForInput();
            getMenu().displayMenu();
        } else {
            getMenu().cancelWaitForInput();
            getMenu().displayMenu();

            MinigameMessageManager.sendMgMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR, MgMenuLangKey.MENU_STATUSEFFECTADD_ERROR_SYNTAX);
        }
    }
}
