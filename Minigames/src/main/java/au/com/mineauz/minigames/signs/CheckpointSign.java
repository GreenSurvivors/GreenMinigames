package au.com.mineauz.minigames.signs;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgSignLangKey;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;

public class CheckpointSign extends AMinigameSign {

    @Override
    public @NotNull Component getName() {
        return MinigameMessageManager.getMgMessage(MgSignLangKey.TYPE_CHECKPOINT);
    }

    @Override
    public String getCreatePermission() {
        return "minigame.sign.create.checkpoint";
    }

    @Override
    public String getUsePermission() {
        return "minigame.sign.use.checkpoint";
    }

    @Override
    public boolean signCreate(@NotNull SignChangeEvent event) { // todo get from lang
        event.line(1, getName());
        if (event.getLine(2).equalsIgnoreCase("global")) {
            event.setLine(2, ChatColor.BLUE + "Global");
        }
        return true;
    }

    @Override
    public boolean signUse(@NotNull Sign sign, @NotNull MinigamePlayer mgPlayer) {
        if ((mgPlayer.isInMinigame() || (!mgPlayer.isInMinigame() && sign.getLine(2).equals(ChatColor.BLUE + "Global")))
                && mgPlayer.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
            if (mgPlayer.isInMinigame() && mgPlayer.getMinigame().isSpectator(mgPlayer)) {
                return false;
            }
            if (mgPlayer.getPlayer().isOnGround()) { // todo why? if really necessary use something working
                Location newloc = mgPlayer.getPlayer().getLocation();
                if (!sign.getLine(2).equals(ChatColor.BLUE + "Global")) {
                    mgPlayer.setCheckpoint(newloc);
                } else {
                    mgPlayer.getStoredPlayerCheckpoints().setGlobalCheckpoint(newloc);
                }

                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.SIGN_CHECKPOINT_SET);
                return true;
            } else {
                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.SIGN_CHECKPOINT_FAIL);
            }
        } else {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.SIGN_ERROR_EMPTYHAND);
        }
        return false;
    }

    @Override
    public void signBreak(@NotNull Sign sign, @NotNull MinigamePlayer mgPlayer) {

    }

}
