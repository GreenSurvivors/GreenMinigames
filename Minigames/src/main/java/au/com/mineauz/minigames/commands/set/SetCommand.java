package au.com.mineauz.minigames.commands.set;

import au.com.mineauz.minigames.commands.ACommand;
import au.com.mineauz.minigames.commands.CommandDispatcher;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SetCommand extends ACommand {
    private static final @NotNull Map<@NotNull String, @NotNull ASetCommand> parameterList = new TreeMap<>(); // sort by name for display in help

    static {
        registerSetCommand(new SetStartCommand());
        registerSetCommand(new SetEndLocationCommand());
        registerSetCommand(new SetQuitLocationCommand());
        registerSetCommand(new SetLobbyCommand());
        registerSetCommand(new SetRewardCommand());
        registerSetCommand(new SetSecondaryRewardCommand());
        registerSetCommand(new SetTypeCommand());
        registerSetCommand(new SetFloorDegeneratorCommand());
        registerSetCommand(new SetMaxPlayersCommand());
        registerSetCommand(new SetMinPlayersCommand());
        registerSetCommand(new SetLoadoutCommand());
        registerSetCommand(new SetEnabledCommand());
        registerSetCommand(new SetMaxRadiusCommand());
        registerSetCommand(new SetMinTreasureCommand());
        registerSetCommand(new SetMaxTreasureCommand());
        registerSetCommand(new SetFlagCommand());
        registerSetCommand(new SetLocationHintCommand());
        registerSetCommand(new SetUsePermissionsCommand());
        registerSetCommand(new SetMinScoreCommand());
        registerSetCommand(new SetMaxScoreCommand());
        registerSetCommand(new SetTimerCommand());
        registerSetCommand(new SetItemDropCommand());
        registerSetCommand(new SetItemPickupCommand());
        registerSetCommand(new SetBlockBreakCommand());
        registerSetCommand(new SetBlockPlaceCommand());
        registerSetCommand(new SetGamemodeCommand());
        registerSetCommand(new SetBlockWhitelistCommand());
        registerSetCommand(new SetBlocksDropCommand());
        registerSetCommand(new SetGameMechanicCommand());
        registerSetCommand(new SetPaintballCommand());
        registerSetCommand(new SetStoreCheckpointsCommand());
        registerSetCommand(new SetMaxHeightCommand());
        registerSetCommand(new SetPresetCommand());
        registerSetCommand(new SetLateJoinCommand());
        registerSetCommand(new SetUnlimitedAmmoCommand());
        registerSetCommand(new SetSpectateCommand());
        registerSetCommand(new SetRandomizeChestsCommand());
        registerSetCommand(new SetRegenAreaCommand());
        registerSetCommand(new SetLivesCommand());
        registerSetCommand(new SetDefaultWinnerCommand());
        registerSetCommand(new SetAllowEnderPearlsCommand());
        registerSetCommand(new SetStartTimeCommand());
        registerSetCommand(new SetMultiplayerCheckpointsCommand());
        registerSetCommand(new SetObjectiveCommand());
        registerSetCommand(new SetGametypeNameCommand());
        registerSetCommand(new SetSPMaxPlayersCommand());
        registerSetCommand(new SetDisplayNameCommand());
        registerSetCommand(new SetRegenDelayCommand());
        registerSetCommand(new SetTeamCommand());
        registerSetCommand(new SetFlightCommand());
        registerSetCommand(new SetHintDelayCommand());
        registerSetCommand(new SetRestartDelayCommand());
        registerSetCommand(new SetSpectatorSpawnLocationCommand());
        registerSetCommand(new SetInfectedPercentCommand());
        registerSetCommand(new SetGameOverCommand());
        registerSetCommand(new SetDisplayScoreboardCommand());
        registerSetCommand(new SetInfectedTeamCommand());
        registerSetCommand(new SetSurvivorTeamCommand());
    }

    public static void registerSetCommand(ASetCommand command) {
        parameterList.put(command.getName(), command);
    }

    public static @NotNull Collection<@NotNull ASetCommand> getSetCommands() {
        return parameterList.values();
    }

    public static @Nullable ASetCommand getSetCommand(@NotNull String name) {
        ASetCommand comd = null;
        if (parameterList.containsKey(name.toLowerCase())) {
            comd = parameterList.get(name.toLowerCase());
        } else {
            AliasCheck:
            for (ASetCommand com : parameterList.values()) {
                if (com.getAliases() != null) {
                    for (String alias : com.getAliases()) {
                        if (name.equalsIgnoreCase(alias)) {
                            comd = com;
                            break AliasCheck;
                        }
                    }
                }
            }
        }
        return comd;
    }

    @Override
    public @NotNull String getName() {
        return "set";
    }

    @Override
    public boolean canBeConsole() {
        return true;
    }

    @Override
    public @NotNull Component getDescription() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_SET_DESCRIPTION);
    }

    @Override
    public Component getUsage() {
        return MinigameMessageManager.getMgMessage(MgCommandLangKey.COMMAND_SET_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        ASetCommand comd = null;
        Minigame minigame = null;
        String[] shortArgs = null;

        if (args.length >= 1) {
            if (PLUGIN.getMinigameManager().hasMinigame(args[0])) {
                minigame = PLUGIN.getMinigameManager().getMinigame(args[0]);
            }
            if (args.length >= 2) {
                comd = getSetCommand(args[1]);
            }

            if (args.length > 2) {
                shortArgs = new String[args.length - 2];
                System.arraycopy(args, 2, shortArgs, 0, args.length - 2);
            }
        }

        if (comd != null && minigame != null) {
            if (sender instanceof Player || comd.canBeConsole()) {
                if (comd.getPermission() == null || sender.hasPermission(comd.getPermission())) {
                    boolean returnValue = comd.onCommand(sender, minigame, shortArgs);
                    if (!returnValue) {
                        MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.NONE, MgCommandLangKey.COMMAND_SET_HEADER);

                        MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.NONE, MgCommandLangKey.COMMAND_SET_SUBCOMMAND_DESCRIPTION,
                                Placeholder.component(MinigamePlaceHolderKey.TEXT.getKey(), comd.getDescription()));
                        MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.NONE, MgCommandLangKey.COMMAND_SET_SUBCOMMAND_USAGE,
                                Placeholder.component(MinigamePlaceHolderKey.TEXT.getKey(), comd.getUsage()));
                        if (comd.getAliases() != null) {
                            String aliases = String.join("<gray>, </gray>", comd.getAliases());

                            MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.NONE, MgCommandLangKey.COMMAND_SET_SUBCOMMAND_ALIASES,
                                    Placeholder.parsed(MinigamePlaceHolderKey.TEXT.getKey(), aliases));
                        }
                    }
                } else {
                    MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOPERMISSION);
                }
            } else {
                MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_SENDERNOTAPLAYER);
            }
            return true;
        } else if (minigame == null) {
            MinigameMessageManager.sendMgMessage(sender, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOMINIGAME,
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), args[0]));
        }
        return false;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(@NotNull CommandSender sender, @NotNull String @Nullable [] args) {
        if (args != null && args.length > 0) {
            ASetCommand comd = null;
            String[] shortArgs;
            Minigame mgm = null;

            if (PLUGIN.getMinigameManager().hasMinigame(args[0])) {
                mgm = PLUGIN.getMinigameManager().getMinigame(args[0]);
            }

            if (args.length > 1 && mgm != null) {
                if (parameterList.containsKey(args[1].toLowerCase())) {
                    comd = parameterList.get(args[1].toLowerCase());
                }

                shortArgs = new String[args.length - 2];
                System.arraycopy(args, 2, shortArgs, 0, args.length - 2);

                if (comd != null) {
                    if (sender instanceof Player) {
                        List<String> l = comd.onTabComplete(sender, mgm, shortArgs);
                        return Objects.requireNonNullElseGet(l, () -> List.of(""));
                    }
                } else {
                    List<String> ls = new ArrayList<>(parameterList.keySet());
                    return CommandDispatcher.tabCompleteMatch(ls, args[1]);
                }
            } else if (args.length == 1) {
                List<String> ls = new ArrayList<>(PLUGIN.getMinigameManager().getAllMinigames().keySet());
                return CommandDispatcher.tabCompleteMatch(ls, args[0]);
            }
        }
        return null;
    }

}
