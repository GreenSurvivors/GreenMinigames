package au.com.mineauz.minigames.signs;

import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgSignLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.loadout.LoadoutModule;
import au.com.mineauz.minigames.minigame.modules.loadout.PlayerLoadout;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LoadoutSign extends AMinigameSign {

    @Override
    public @NotNull Component getName() {
        return MessageManager.getMessage(MgSignLangKey.TYPE_LOADOUT);
    }

    @Override
    public @Nullable String getCreatePermission() {
        return "minigame.sign.create.loadout";
    }

    @Override
    public @Nullable String getUsePermission() {
        return "minigame.sign.use.loadout";
    }

    @Override
    public boolean signCreate(@NotNull SignChangeEvent event) {
        event.line(1, getName());
        if (event.getLine(2).equalsIgnoreCase("menu"))
            event.setLine(2, ChatColor.GREEN + "Menu");
        return true;
    }

    @Override
    public boolean signUse(final @NotNull Sign sign, final @NotNull MinigamePlayer mgPlayer) {
        final @Nullable Player player = mgPlayer.getPlayer();
        if (player != null && player.getInventory().getItemInMainHand().isEmpty() && mgPlayer.isInMinigame()) {
            Minigame mgm = mgPlayer.getMinigame();

            if (mgm == null || mgm.isSpectator(mgPlayer)) {
                return false;
            }

            LoadoutModule loadoutModule = LoadoutModule.getMinigameModule(mgm);

            if (sign.getSide(Side.FRONT).getLine(2).equals(ChatColor.GREEN + "Menu")) {
                boolean nores = !sign.getSide(Side.FRONT).getLine(3).equalsIgnoreCase("respawn");
                LoadoutModule.getMinigameModule(mgm).displaySelectionMenu(mgPlayer, nores);
            } else {
                String loadOutName = sign.getSide(Side.FRONT).getLine(2);
                PlayerLoadout loadout = loadoutModule.getLoadout(loadOutName);

                if (loadout == null) {
                    //loadout module of Minigame failed. try to get global loadout
                    loadout = LoadoutModule.getGlobalLoadout(loadOutName);
                }

                if (loadout != null) {
                    if (!loadout.usesPermissions() || player.hasPermission("minigame.loadout." + sign.getSide(Side.FRONT).getLine(2).toLowerCase())) {
                        if (mgPlayer.setLoadout(loadout)) {
                            MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_LOADOUT_EQUIPPED,
                                Placeholder.component(MinigamePlaceHolderKey.LOADOUT.getKey(), sign.getSide(Side.FRONT).line(2)));

                            if (mgm.getType() == MinigameType.SINGLEPLAYER ||
                                mgm.hasStarted()) {
                                if (sign.getSide(Side.FRONT).getLine(3).equalsIgnoreCase("respawn")) {
                                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_LOADOUT_NEXTRESPAWN,
                                        Placeholder.component(MinigamePlaceHolderKey.LOADOUT.getKey(), loadout.getDisplayName()));
                                } else if (sign.getSide(Side.FRONT).getLine(3).equalsIgnoreCase("temporary")) {
                                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_LOADOUT_TEMPORARILY);
                                    loadout.equipLoadout(mgPlayer);
                                    mgPlayer.setLoadout(mgPlayer.getDefaultLoadout());
                                } else {
                                    loadout.equipLoadout(mgPlayer);
                                }
                            }
                        }
                        return true;
                    } else {
                        MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOPERMISSION);
                    }
                } else {
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.PLAYER_LOADOUT_ERROR_NOLOADOUT);
                }
            }
        } else if (player != null && !player.getInventory().getItemInMainHand().isEmpty()) {
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.SIGN_ERROR_EMPTYHAND);
        }
        return false;
    }

    @Override
    public void signBreak(@NotNull Sign sign, @NotNull MinigamePlayer mgPlayer) {
    }
}
