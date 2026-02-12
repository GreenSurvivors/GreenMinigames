package au.com.mineauz.minigames.commands.set;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.commands.CommandDispatcher;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MgRegion;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.RegenRegionChangeResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SetRegenAreaCommand extends ASetCommand {
    private final int REGIONS_PER_PAGE = 5;

    @Override
    public @NotNull String getName() {
        return "regenarea";
    }

    @Override
    public boolean canBeConsole() {
        return false;
    }

    @Override
    public @NotNull Component getDescription() {
        return MessageManager.getMessage(MgCommandLangKey.COMMAND_SET_REGENAREA_DESCRIPTION);
    }

    @Override
    public @NotNull Component getUsage() {
        return MessageManager.getMessage(MgCommandLangKey.COMMAND_SET_REGENAREA_USAGE);
    }

    @Override
    public @Nullable String getPermission() {
        return "minigame.set.regenarea";
    }

    /**
     * helper methode for creating the list-component for /minigame set <Minigame> regenarea list <page>
     *
     * @param minigame the minigame of the command
     * @param page     the given page
     * @return returns a component containing max 5 regen regions with their coordinates and volume
     */
    private @NotNull Component makeList(final @NotNull Minigame minigame, final int page) {
        //get all currently active regions
        final @NotNull List<@NotNull MgRegion> regions = new ArrayList<>(minigame.getRegenRegions());
        //how many regions are known. Needed to calculate how many pages there are and
        //how many there should be on the given page (if the page is not full)
        final int NUM_OF_REGIONS = regions.size();
        //how many pages of regions are there? - needed in header and limit page to how many exits
        final int NUM_OF_PAGES = (int) Math.ceil((double) NUM_OF_REGIONS / (double) REGIONS_PER_PAGE);

        //limit page to range of possible pages
        final int PAGE = Math.max(Math.min(page, NUM_OF_PAGES), 1);
        //don't try to access more books than exits
        final int MAX_BOOKS_THIS_PAGE = Math.min(NUM_OF_REGIONS, PAGE * REGIONS_PER_PAGE);

        TextComponent.Builder listBuilder = Component.text();
        listBuilder.append(MessageManager.getMessage(MgCommandLangKey.COMMAND_SET_REGENAREA_LIST_HEADER,
                Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(PAGE)),
                Placeholder.unparsed(MinigamePlaceHolderKey.MAX.getKey(), String.valueOf(NUM_OF_PAGES))));

        //add the books for the page
        for (int id = (PAGE - 1) * REGIONS_PER_PAGE; id < MAX_BOOKS_THIS_PAGE; id++) {
            MgRegion region = regions.get(id);
            listBuilder.appendNewline();

            listBuilder.append(region.describe());
        }

        //todo footer in messages and not legacy formatting
        //add footer
        listBuilder.appendNewline();
        listBuilder.append(LegacyComponentSerializer.legacySection().deserialize("&2--"));

        //back button or none
        if (PAGE > 1) {
            listBuilder.append(LegacyComponentSerializer.legacySection().deserialize(String.format("&6<<( &e%s&6 ) ", PAGE - 1)).
                    clickEvent(ClickEvent.runCommand("/minigame set " + minigame.getName() + " regenarea list " + (PAGE - 1))));
        } else {
            listBuilder.append(Component.text("-------"));
        }

        //inner part, separating both buttons
        listBuilder.append(LegacyComponentSerializer.legacySection().deserialize("&2---<*>---"));

        //next button
        if (PAGE < NUM_OF_PAGES) {
            listBuilder.append(LegacyComponentSerializer.legacySection().deserialize(String.format("&6 ( &e%s&6 )>>", PAGE + 1)).
                    clickEvent(ClickEvent.runCommand("/minigame set " + minigame.getName() + " regenarea list " + (PAGE + 1))));
        } else {
            listBuilder.append(Component.text("-------"));
        }
        listBuilder.append(LegacyComponentSerializer.legacySection().deserialize("&2--"));

        return listBuilder.build();
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Minigame minigame,
                             final @NotNull String @Nullable [] args) {
        if (args != null) {
            if (sender instanceof final @NotNull Player player) {
                final @NotNull MinigamePlayer mgPlayer = Minigames.getPlugin().getPlayerManager().getMinigamePlayer(player);

                if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
                    MessageManager.sendMessage(sender, MinigameMessageType.NONE, makeList(minigame, 1));

                } else if (args.length == 2) {
                    switch (args[0].toLowerCase()) {
                        case "create" -> {
                            if (mgPlayer.hasSelection()) {
                                final @NotNull String name = args[1];
                                final @Nullable MgRegion region = minigame.getRegenRegion(name);

                                final @NotNull RegenRegionChangeResult result = minigame.setRegenRegion(new MgRegion(name, mgPlayer.getSelectionLocations()[0], mgPlayer.getSelectionLocations()[1]));

                                if (result.success()) {
                                    if (region == null) {
                                        MessageManager.sendMessage(mgPlayer, MinigameMessageType.SUCCESS, MgMiscLangKey.REGION_REGENREGION_CREATED,
                                                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                                                Placeholder.unparsed(MinigamePlaceHolderKey.REGION.getKey(), name),
                                                Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(result.numOfBlocksTotal())),
                                                Placeholder.unparsed(MinigamePlaceHolderKey.MAX.getKey(), String.valueOf(minigame.getRegenBlocklimit())));
                                    } else {
                                        MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.REGION_REGENREGION_UPDATED,
                                                Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getDisplayName()),
                                                Placeholder.unparsed(MinigamePlaceHolderKey.REGION.getKey(), name),
                                                Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(result.numOfBlocksTotal())),
                                                Placeholder.unparsed(MinigamePlaceHolderKey.MAX.getKey(), String.valueOf(minigame.getRegenBlocklimit())));
                                    }

                                    mgPlayer.clearSelection();
                                } else {
                                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.REGION_REGENREGION_ERROR_LIMIT,
                                            Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(result.numOfBlocksTotal())),
                                            Placeholder.unparsed(MinigamePlaceHolderKey.MAX.getKey(), String.valueOf(minigame.getRegenBlocklimit())));
                                }
                            } else {
                                MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_SET_REGENAREA_ERROR_NOTSELECTED);
                            }

                            return true;
                        }
                        case "list" -> {
                            if (args[1].matches("\\d+")) {
                                MessageManager.sendMessage(mgPlayer, MinigameMessageType.NONE, makeList(minigame, Integer.parseInt(args[1])));
                            } else {
                                MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTNUMBER,
                                        Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), args[0]));
                            }
                        }
                        case "remove" -> {
                            final @NotNull RegenRegionChangeResult result = minigame.removeRegenRegion(args[1]);

                            if (result.success()) {
                                MessageManager.sendMessage(mgPlayer, MinigameMessageType.WARNING, MgMiscLangKey.REGION_REGENREGION_REMOVED,
                                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                                        Placeholder.unparsed(MinigamePlaceHolderKey.REGION.getKey(), args[1]),
                                        Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(result.numOfBlocksTotal())),
                                        Placeholder.unparsed(MinigamePlaceHolderKey.MAX.getKey(), String.valueOf(minigame.getRegenBlocklimit())));
                            } else {
                                MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.REGION_ERROR_NOREGENREION,
                                        Placeholder.unparsed(MinigamePlaceHolderKey.REGION.getKey(), args[1]));
                            }
                            return true;
                        }
                    }
                }
            } else {
                MessageManager.sendMessage(sender, MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_SENDERNOTAPLAYER);
                return false;
            }

            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<@NotNull String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Minigame minigame,
                                                         final @NotNull String @NotNull [] args) {

        if (args.length == 1) {
            final @NotNull List<@NotNull String> tab = new ArrayList<>();
            tab.add("create");
            tab.add("list");
            tab.add("remove");
            return CommandDispatcher.tabCompleteMatch(tab, args[0]);
        } else if (args.length == 2) {
            final @NotNull List<@NotNull String> tab = new ArrayList<>();
            if (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("remove")) {
                for (final @NotNull MgRegion region : minigame.getRegenRegions()) {
                    tab.add(region.getName());
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                //cache number of pages to not recalculate every loop
                final int PAGES = (int) Math.ceil((double) minigame.getRegenRegions().size() / (double) REGIONS_PER_PAGE);

                //make list of all known pages
                for (int page = 1; page <= PAGES; page++) {
                    tab.add(String.valueOf(page));
                }
            }
            return CommandDispatcher.tabCompleteMatch(tab, args[1]);
        }
        return null;
    }
}
