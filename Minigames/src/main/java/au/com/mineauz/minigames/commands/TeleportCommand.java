package au.com.mineauz.minigames.commands;

import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.minigame.modules.team.TeamColor;
import au.com.mineauz.minigames.minigame.modules.team.TeamsModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TeleportCommand extends ACommand {
    private static final Pattern COORD_PATTERN = Pattern.compile("~?(?:-?[0-9]+(?:.[0-9]+)?)|~");

    @Override
    public @NotNull String getName() {
        return "teleport";
    }

    @Override
    public @NotNull String @Nullable [] getAliases() {
        return new String[]{"tp"};
    }

    @Override
    public boolean canBeConsole() {
        return true;
    }

    @Override
    public @NotNull Component getDescription() {
        return MessageManager.getMessage(MgCommandLangKey.COMMAND_TELEPORT_DESCRIPTION);
    }

    @Override
    public @NotNull Component getUsage() {
        return MessageManager.getMessage(MgCommandLangKey.COMMAND_TELEPORT_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.teleport";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull String @NotNull [] args) {
        if (args.length > 0) {
            @NotNull List<@NotNull Player> matchedPlayer = PLUGIN.getServer().matchPlayer(args[0]);
            MinigamePlayer mgPlayer;
            if (!matchedPlayer.isEmpty()) {
                mgPlayer = PLUGIN.getPlayerManager().getMinigamePlayer(matchedPlayer.getFirst());
            } else {
                MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAD_ERROR_NOTPLAYER,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), args[0]));
                return true;
            }

            if (args.length >= 4 && COORD_PATTERN.matcher(args[1]).matches() &&
                    COORD_PATTERN.matcher(args[2]).matches() &&
                    COORD_PATTERN.matcher(args[3]).matches()) {
                double x, y, z;
                float yaw = mgPlayer.getLocation().getYaw();
                float pitch = mgPlayer.getLocation().getPitch();

                final Player player = matchedPlayer.getFirst();
                if (args[1].contains("~")) {
                    if (args[1].equals("~")) {
                        x = player.getLocation().getX();
                    } else {
                        x = player.getLocation().getX() + Double.parseDouble(args[1].replace("~", ""));
                    }
                } else {
                    x = Double.parseDouble(args[1]);
                }

                if (args[2].contains("~")) {
                    if (args[2].equals("~")) {
                        y = player.getLocation().getY();
                    } else {
                        y = player.getLocation().getY() + Double.parseDouble(args[2].replace("~", ""));
                    }
                } else {
                    y = Double.parseDouble(args[2]);
                }

                if (args[3].contains("~")) {
                    if (args[3].equals("~")) {
                        z = player.getLocation().getZ();
                    } else {
                        z = player.getLocation().getZ() + Double.parseDouble(args[3].replace("~", ""));
                    }
                } else {
                    z = Double.parseDouble(args[3]);
                }

                if (args.length == 6 && COORD_PATTERN.matcher(args[4]).matches() && COORD_PATTERN.matcher(args[5]).matches()) {
                    if (args[4].contains("~")) {
                        if (args[4].equals("~")) {
                            yaw = player.getLocation().getYaw();
                        } else {
                            yaw = player.getLocation().getYaw() + Float.parseFloat(args[4].replace("~", ""));
                        }
                    } else {
                        yaw = Float.parseFloat(args[4]);
                    }

                    if (args[5].contains("~")) {
                        if (args[5].equals("~")) {
                            pitch = player.getLocation().getPitch();
                        } else {
                            pitch = player.getLocation().getPitch() + Float.parseFloat(args[5].replace("~", ""));
                        }
                    } else {
                        pitch = Float.parseFloat(args[5]);
                    }

                    if (pitch > 90) {
                        pitch = 90f;
                    } else if (pitch < -90) {
                        pitch = -90f;
                    }
                }

                MessageManager.sendMessage(sender, MinigameMessageType.SUCCESS, MgCommandLangKey.COMMAND_TELEPORT_TPCOORDS,
                        Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()));
                mgPlayer.teleport(new Location(player.getWorld(), x, y, z, yaw, pitch));
                return true;
            } else if (args.length >= 2 && args[1].equalsIgnoreCase("start")) {
                if (mgPlayer.isInMinigame()) {
                    int pos = 0;
                    Team team = null;
                    if (args.length == 3) {
                        team = TeamsModule.getMinigameModule(mgPlayer.getMinigame()).getTeam(TeamColor.matchColor(args[2]));
                    } else if (mgPlayer.getTeam() != null) {
                        team = mgPlayer.getTeam();
                    }

                    if (args.length >= 3 && args[2].matches("[0-9]+") && !args[2].equals("0")) {
                        pos = Integer.parseInt(args[2]) - 1;
                    }

                    if (team == null && pos >= mgPlayer.getMinigame().getStartLocations().size()) {
                        pos = mgPlayer.getMinigame().getStartLocations().size() - 1;
                    } else if (team != null && pos >= team.getStartLocations().size()) {
                        pos = team.getStartLocations().size() - 1;
                    }

                    if (team != null) {
                        mgPlayer.teleport(team.getStartLocations().get(pos));
                        MessageManager.sendMessage(sender, MinigameMessageType.SUCCESS, MgCommandLangKey.COMMAND_TELEPORT_TEAMSTARTPOS,
                                Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()),
                                Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), team.getColoredDisplayName()),
                                Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(pos + 1)));
                    } else {
                        mgPlayer.teleport(mgPlayer.getMinigame().getStartLocations().get(pos));
                        MessageManager.sendMessage(sender, MinigameMessageType.SUCCESS, MgCommandLangKey.COMMAND_TELEPORT_STARTPOS,
                                Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()),
                                Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(pos + 1)));
                    }
                } else {
                    MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTINMINIGAME_SELF,
                            Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()));
                }
                return true;
            } else if (args.length == 2 && args[1].equalsIgnoreCase("checkpoint")) {
                if (mgPlayer.isInMinigame()) {
                    mgPlayer.teleport(mgPlayer.getCheckpoint());
                    MessageManager.sendMessage(sender, MinigameMessageType.SUCCESS, MgCommandLangKey.COMMAND_TELEPORT_CHEKPOINT,
                            Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()));
                } else {
                    MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTINMINIGAME_PLAYER,
                            Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()));
                }
                return true;
            } else if (args.length == 2) {
                matchedPlayer = PLUGIN.getServer().matchPlayer(args[1]);
                final @NotNull MinigamePlayer mgPlayerSelected;

                if (!matchedPlayer.isEmpty()) {
                    mgPlayerSelected = PLUGIN.getPlayerManager().getMinigamePlayer(matchedPlayer.getFirst());
                } else {
                    MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAD_ERROR_NOTPLAYER,
                            Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), args[1]));
                    return true;
                }

                mgPlayer.teleport(mgPlayerSelected.getLocation());
                MessageManager.sendMessage(sender, MinigameMessageType.SUCCESS, MgCommandLangKey.COMMAND_TELEPORT_PLAYER2PLAYER,
                        Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()),
                        Placeholder.component(MinigamePlaceHolderKey.OTHER_PLAYER.getKey(), mgPlayerSelected.displayName()));
                return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender,
                                                         @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            List<String> pl = new ArrayList<>();
            for (Player p : PLUGIN.getServer().getOnlinePlayers()) {
                pl.add(p.getName());
            }
            return CommandDispatcher.tabCompleteMatch(pl, args[0]);
        } else if (args.length == 2) {
            List<String> playerNames = new ArrayList<>(PLUGIN.getServer().getOnlinePlayers().size() + 2);
            for (Player player : PLUGIN.getServer().getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            playerNames.add("Start");
            playerNames.add("Checkpoint");
            return CommandDispatcher.tabCompleteMatch(playerNames, args[1]);
        }
        return null;
    }
}
