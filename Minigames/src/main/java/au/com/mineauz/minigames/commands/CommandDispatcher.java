package au.com.mineauz.minigames.commands;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.commands.set.SetCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CommandDispatcher implements CommandExecutor, TabCompleter {
    private static final Map<String, ICommand> commands = new HashMap<>();
    private static final Minigames plugin = Minigames.getPlugin();
    private static BufferedWriter cmdFile;

    static {
        if (plugin.getConfig().getBoolean("outputCMDToFile")) {
            try {
                cmdFile = new BufferedWriter(new FileWriter(plugin.getDataFolder() + "/cmds.txt"));
                cmdFile.write("{| class=\"wikitable\"");
                cmdFile.newLine();
                cmdFile.write("! Command");
                cmdFile.newLine();
                cmdFile.write("! Syntax");
                cmdFile.newLine();
                cmdFile.write("! Description");
                cmdFile.newLine();
                cmdFile.write("! Permission");
                cmdFile.newLine();
                cmdFile.write("! Alias");
                cmdFile.newLine();
            } catch (IOException e) {
                Minigames.getCmpnntLogger().warn("Couldn't output cmds to file!", e);
            }
        }
        registerCommand(new CreateCommand());
        registerCommand(new SetCommand());
        registerCommand(new JoinCommand());
        registerCommand(new StartCommand());
        registerCommand(new StopCommand());
        registerCommand(new QuitCommand());
        registerCommand(new RevertCommand());
        registerCommand(new HintCommand());
        registerCommand(new EndCommand());
        registerCommand(new HelpCommand());
//        registerCommand(new ReloadCommand());
        registerCommand(new ListCommand());
        registerCommand(new ListPlaceholder());
        registerCommand(new ToggleTimerCommand());
        registerCommand(new DeleteCommand());
        registerCommand(new PartyModeCommand());
        registerCommand(new DeniedCommandCommand());
        registerCommand(new GlobalLoadoutCommand());
        registerCommand(new SpectateCommand());
        registerCommand(new PlayerCommand());
        registerCommand(new ScoreCommand());
        registerCommand(new TeleportCommand());
        registerCommand(new EditCommand());
        registerCommand(new ToolCommand());
        registerCommand(new ScoreboardCommand());
        registerCommand(new EnableAllCommand());
        registerCommand(new DisableAllCommand());
        registerCommand(new SaveCommand());
        registerCommand(new LoadoutCommand());
        registerCommand(new BackupCommand());
        registerCommand(new DebugCommand());
        registerCommand(new BackendCommand());
        registerCommand(new InfoCommand());
        registerCommand(new ResourcePackCommand());

        if (plugin.getConfig().getBoolean("outputCMDToFile")) {
            try {
                cmdFile.write("|}");
                cmdFile.close();
            } catch (IOException e) {
                Minigames.getCmpnntLogger().warn("Couldn't save cmds file!", e);
            }
        }
    }

    public static void registerCommand(ICommand command) {
        commands.put(command.getName(), command);

        if (plugin.getConfig().getBoolean("outputCMDToFile")) {
            try {
                cmdFile.write("|-");
                cmdFile.newLine();
                cmdFile.write("| '''" + command.getName() + "'''");
                cmdFile.newLine();
                if (command.getUsage() != null) {
                    cmdFile.write("| " + command.getUsage());
                } else {
                    cmdFile.write("| N/A");
                }
                cmdFile.newLine();
                command.getDescription();
                cmdFile.write("| " + command.getDescription());
                cmdFile.newLine();
                if (command.getPermission() != null)
                    cmdFile.write("| " + command.getPermission());
                else
                    cmdFile.write("| N/A");
                cmdFile.newLine();
                if (command.getAliases() != null) {
                    int count = 0;
                    cmdFile.write("| ");
                    for (String alias : command.getAliases()) {
                        cmdFile.write(alias);
                        count++;
                        if (count != command.getAliases().length) {
                            cmdFile.write("\n\n");
                        }
                    }
                } else
                    cmdFile.write("| N/A");
                cmdFile.newLine();
            } catch (IOException e) {
                //Failed to write
            }
        }
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        Player ply = null;
        if (sender instanceof Player) {
            ply = (Player) sender;
        }

        if (args != null && args.length > 0) {
            ICommand comd = null;
            String[] shortArgs = null;

            if (commands.containsKey(args[0].toLowerCase())) {
                comd = commands.get(args[0].toLowerCase());
            } else {
                AliasCheck:
                for (ICommand com : commands.values()) {
                    if (com.getAliases() != null) {
                        for (String alias : com.getAliases()) {
                            if (args[0].equalsIgnoreCase(alias)) {
                                comd = com;
                                break AliasCheck;
                            }
                        }
                    }
                }
            }

            if (args.length > 1) {
                shortArgs = new String[args.length - 1];
                System.arraycopy(args, 1, shortArgs, 0, args.length - 1);
            }

            if (comd != null) {
                if (ply != null || comd.canBeConsole()) {
                    if (ply == null || (comd.getPermission() == null || ply.hasPermission(comd.getPermission()))) {
                        boolean returnValue = comd.onCommand(sender, null, shortArgs);
                        if (!returnValue) {
                            sender.sendMessage(ChatColor.GREEN + "------------------Command Info------------------");
                            sender.sendMessage(ChatColor.BLUE + "Description: " + ChatColor.WHITE + comd.getDescription());
                            sender.sendMessage(ChatColor.BLUE + "Usage: " + ChatColor.WHITE + "<newline>" + comd.getUsage());
                            if (comd.getAliases() != null) {
                                sender.sendMessage(ChatColor.BLUE + "Aliases: " + ChatColor.WHITE + String.join(", ", comd.getAliases()));
                            }
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + comd.getPermissionMessage());
                        sender.sendMessage(ChatColor.RED + comd.getPermission());
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You must be a player to execute this command!");
                }
                return true;
            }
        } else {
            sender.sendMessage(ChatColor.GREEN + "Minigames");
            sender.sendMessage(ChatColor.GRAY + "By: " + plugin.getDescription().getAuthors().get(0));
            sender.sendMessage(ChatColor.GRAY + "Version: " + plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.GRAY + "Type /minigame help for help");
            return true;
        }
        return false;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args != null && args.length > 0) {
            ICommand comd = null;
            String[] shortArgs = null;

            if (commands.containsKey(args[0].toLowerCase())) {
                comd = commands.get(args[0].toLowerCase());
            }

            if (args.length > 1) {
                shortArgs = new String[args.length - 1];
                System.arraycopy(args, 1, shortArgs, 0, args.length - 1);
            }

            if (comd != null) {
                if (sender instanceof Player) { // todo ok, but why? let the commands test if a player was needed.
                    if (args.length > 1) {
                        List<String> l = comd.onTabComplete(sender, null, shortArgs);
                        return Objects.requireNonNullElseGet(l, () -> List.of(""));
                    }
                }
            } else {
                List<String> ls = new ArrayList<>(commands.keySet());
                return MinigameUtils.tabCompleteMatch(ls, args[0]);
            }
        }
        return null;
    }
}
