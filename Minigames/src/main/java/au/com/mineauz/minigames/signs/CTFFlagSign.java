package au.com.mineauz.minigames.signs;

import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgSignLangKey;
import au.com.mineauz.minigames.mechanics.CTFMechanic;
import au.com.mineauz.minigames.minigame.modules.team.TeamColor;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.text.WordUtils;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CTFFlagSign extends AMinigameSign {

    @Override
    public @NotNull Component getName() {
        return MessageManager.getMessage(MgSignLangKey.TYPE_CTFFLAG);
    }

    @Override
    public @Nullable String getCreatePermission() {
        return "minigame.sign.create.flag";
    }

    @Override
    public @Nullable String getUsePermission() {
        return null;
    }

    public static boolean isCapture(@NotNull Component signLine) {
        return PlainTextComponentSerializer.plainText().serialize(signLine).equalsIgnoreCase(MessageManager.getStrippedMessage(MgSignLangKey.SUBTYPE_CAPTURE));
    }

    @Override
    public boolean signCreate(@NotNull SignChangeEvent event) {
        PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

        event.line(1, getName());
        TeamColor col = TeamColor.matchColor(plainSerializer.serialize(event.line(2)));

        if (col != null) {
            event.line(2, Component.text(WordUtils.capitalizeFully(col.toString()), col.getColor()));
        } else if (isNeutral(event.line(2))) {
            event.line(2, MessageManager.getMessage(MgSignLangKey.TEAM_NEUTRAL));
        } else if (isCapture(event.line(2)) && !plainSerializer.serialize(event.line(3)).isEmpty()) {
            event.line(2, MessageManager.getMessage(MgSignLangKey.SUBTYPE_CAPTURE));

            col = TeamColor.matchColor(plainSerializer.serialize(event.line(3)));
            if (col != null) {
                event.line(3, Component.text(WordUtils.capitalizeFully(col.toString()), col.getColor()));
            } else if (isNeutral(event.line(3))) {
                event.line(3, MessageManager.getMessage(MgSignLangKey.TEAM_NEUTRAL));
            } else {
                event.getBlock().breakNaturally();
                MessageManager.sendMessage(event.getPlayer(), MinigameMessageType.ERROR, MgMiscLangKey.SIGN_ERROR_TEAM_INVALIDFORMAT,
                    Placeholder.component(MinigamePlaceHolderKey.TEXT.getKey(), MessageManager.getMessage(MgSignLangKey.TEAM_NEUTRAL)));
                return false;
            }
        }

        return true;
    }

    // the error in this Java doc is known. We don't need access, we need documentation.
    /**
     * actual handling of taking the flag is in {@link CTFMechanic#takeFlag(PlayerInteractEvent)}
     */
    @Override
    public boolean signUse(@NotNull Sign sign, @NotNull MinigamePlayer mgPlayer) {
        if (mgPlayer.isInMinigame()) {
            if (mgPlayer.getPlayer().getInventory().getItemInMainHand().isEmpty()) {
                if (mgPlayer.getMinigame().isSpectator(mgPlayer)) {
                    return false;
                }

                // actual handling is in CTFMechanic!
            } else {
                MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.SIGN_ERROR_EMPTYHAND);
            }
        }
        return false;
    }

    @Override
    public void signBreak(@NotNull Sign sign, @NotNull MinigamePlayer mgPlayer) {
    }
}
