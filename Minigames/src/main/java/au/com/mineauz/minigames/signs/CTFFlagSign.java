package au.com.mineauz.minigames.signs;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgSignLangKey;
import au.com.mineauz.minigames.mechanics.CTFMechanic;
import au.com.mineauz.minigames.minigame.TeamColor;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.text.WordUtils;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class CTFFlagSign extends AMinigameSign {

    @Override
    public @NotNull Component getName() {
        return MinigameMessageManager.getMgMessage(MgSignLangKey.TYPE_CTFFLAG);
    }

    @Override
    public String getCreatePermission() {
        return "minigame.sign.create.flag";
    }

    @Override
    public String getUsePermission() {
        return null;
    }

    @Override
    public boolean signCreate(@NotNull SignChangeEvent event) {
        PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

        event.line(1, getName());
        TeamColor col = TeamColor.matchColor(plainSerializer.serialize(event.line(2)));

        if (col != null) {
            event.line(2, Component.text(WordUtils.capitalizeFully(col.toString()), col.getColor()));
        } else if (plainSerializer.serialize(event.line(2)).equalsIgnoreCase("neutral")) {
            event.line(2, Component.text("Neutral", NamedTextColor.GRAY));
        } else if (plainSerializer.serialize(event.line(2)).equalsIgnoreCase("capture") &&
                !plainSerializer.serialize(event.line(3)).isEmpty()) {
            event.line(2, Component.text("Capture", NamedTextColor.GREEN));

            col = TeamColor.matchColor(plainSerializer.serialize(event.line(3)));
            if (col != null) {
                event.line(3, Component.text(WordUtils.capitalizeFully(col.toString()), col.getColor()));
            } else if (plainSerializer.serialize(event.line(3)).equalsIgnoreCase("neutral")) {
                event.line(3, Component.text("Neutral", NamedTextColor.GRAY));
            } else {
                event.getBlock().breakNaturally();
                MinigameMessageManager.sendMgMessage(event.getPlayer(), MinigameMessageType.ERROR, MgMiscLangKey.SIGN_ERROR_TEAM_INVALIDFORMAT);
                return false;
            }
        }

        return true;
    }

    // the error in this Java doc is known. We don't need access, we need dokumention.

    /**
     * actual handling of taking the flag is in {@link CTFMechanic#takeFlag(PlayerInteractEvent)}
     */
    @Override
    public boolean signUse(@NotNull Sign sign, @NotNull MinigamePlayer mgPlayer) {
        if (mgPlayer.isInMinigame()) {
            if (mgPlayer.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
                if (mgPlayer.getMinigame().isSpectator(mgPlayer)) {
                    return false;
                }

                // actual handling is in CTFMechanic!
            } else {
                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.SIGN_ERROR_EMPTYHAND);
            }
        }
        return false;
    }

    @Override
    public void signBreak(@NotNull Sign sign, @NotNull MinigamePlayer mgPlayer) {
    }
}
