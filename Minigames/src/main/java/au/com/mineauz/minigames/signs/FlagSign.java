package au.com.mineauz.minigames.signs;

import au.com.mineauz.minigames.events.TakeFlagEvent;
import au.com.mineauz.minigames.managers.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameLangKey;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.TeamColor;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.apache.commons.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;

public class FlagSign implements MinigameSign {

    @Override
    public @NotNull String getName() {
        return "Flag";
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
        event.setLine(1, ChatColor.GREEN + "Flag");
        if (TeamColor.matchColor(event.getLine(2)) != null) {
            TeamColor col = TeamColor.matchColor(event.getLine(2));
            event.setLine(2, col.getColor() + WordUtils.capitalize(col.toString()));
        } else if (event.getLine(2).equalsIgnoreCase("neutral")) {
            event.setLine(2, ChatColor.GRAY + "Neutral");
        } else if (event.getLine(2).equalsIgnoreCase("capture") && !event.getLine(3).isEmpty()) {
            event.setLine(2, ChatColor.GREEN + "Capture");
            if (TeamColor.matchColor(event.getLine(3)) != null) {
                TeamColor col = TeamColor.matchColor(event.getLine(3));
                event.setLine(3, col.getColor() + WordUtils.capitalize(col.toString()));
            } else if (event.getLine(3).equalsIgnoreCase("neutral")) {
                event.setLine(3, ChatColor.GRAY + "Neutral");
            } else {
                event.getBlock().breakNaturally();
                MinigameMessageManager.sendMgMessage(event.getPlayer(), MinigameMessageType.ERROR, MinigameLangKey.SIGN_ERROR_TEAM_INVALIDFORMAT);
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean signUse(@NotNull Sign sign, @NotNull MinigamePlayer mgPlayer) {
        if (mgPlayer.isInMinigame()) {
            if (mgPlayer.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
                Minigame mgm = mgPlayer.getMinigame();

                if (mgm.isSpectator(mgPlayer)) {
                    return false;
                }
                if (!sign.getLine(2).isEmpty() && mgPlayer.getPlayer().isOnGround() &&
                        mgm.getMechanicName().equals("ctf") &&
                        !mgPlayer.hasFlag(ChatColor.stripColor(sign.getLine(2))) &&
                        !mgPlayer.getTeam().getDisplayName().equals(ChatColor.stripColor(sign.getLine(2) + " Team"))) {
                    TakeFlagEvent ev = new TakeFlagEvent(mgm, mgPlayer, ChatColor.stripColor(sign.getLine(2)));
                    Bukkit.getPluginManager().callEvent(ev);
                    return true;
                }
            } else {
                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MinigameLangKey.SIGN_ERROR_EMPTYHAND);
            }
        }
        return false;
    }

    @Override
    public void signBreak(@NotNull Sign sign, MinigamePlayer mgPlayer) {

    }

}
