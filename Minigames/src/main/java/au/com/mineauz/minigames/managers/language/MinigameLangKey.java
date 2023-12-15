package au.com.mineauz.minigames.managers.language;

import org.jetbrains.annotations.NotNull;

public enum MinigameLangKey implements LangKey {
    COMMAND_DIVIDER_LARGE("command.divider.large"),
    COMMAND_DIVIDER_SMALL("command.divider.small"),
    COMMAND_INFO_DESCRIPTION("command.info.description"),
    COMMAND_INFO_OUTPUT_DESCRIPTION("command.info.output.description"),
    COMMAND_INFO_OUTPUT_GAMETYPE("command.info.output.gameType"),
    COMMAND_INFO_OUTPUT_HEADER("command.info.output.header"),
    COMMAND_INFO_OUTPUT_NOMINIGAME("command.info.noMinigame"),
    COMMAND_INFO_OUTPUT_NOPLAYER("command.info.output.noPlayer"),
    COMMAND_INFO_OUTPUT_PLAYERDATA("command.info.output.playerData"),
    COMMAND_INFO_OUTPUT_PLAYERHEADER("command.info.output.playerHeader"),
    COMMAND_INFO_OUTPUT_TEAMDATA("command.info.output.teamData"),
    COMMAND_INFO_OUTPUT_TIMER("command.info.output.timer"),
    MINIGAME_ERROR_FULL("minigame.error.full"),
    MINIGAME_ERROR_INCORRECTSTART("minigame.error.incorrectStart"),
    MINIGAME_ERROR_INVALIDMECHANIC("minigame.error.invalidMechanic"),
    MINIGAME_ERROR_INVALIDTYPE("minigame.error.invalidType"),
    MINIGAME_ERROR_MECHANICSTARTFAIL("minigame.error.mechanicStartFail"),
    MINIGAME_ERROR_NODEFAULTTOOL("minigame.error.noDefaultTool"),
    MINIGAME_ERROR_NOEND("minigame.error.noEnd"),
    MINIGAME_ERROR_NOLOBY("minigame.error.noLobby"),
    MINIGAME_ERROR_NOMINIGAME("minigame.error.noMinigame"),
    MINIGAME_ERROR_NOQUIT("minigame.error.noQuit"),
    MINIGAME_ERROR_NOSPECTATEPOS("minigame.error.noSpectatePos"),
    MINIGAME_ERROR_NOSTART("minigame.error.noStart"),
    MINIGAME_ERROR_NOTEAM("minigame.error.noTeam"),
    MINIGAME_ERROR_NOTELEPORT("minigame.error.noTeleport"),
    MINIGAME_ERROR_NOTENABLED("minigame.error.notEnabled"),
    MINIGAME_ERROR_NOTSTARTED("minigame.error.notStarted"),
    MINIGAME_ERROR_REGENERATING("minigame.error.regenerating"),
    MINIGAME_ERROR_STARTED("minigame.error.started"),
    MINIGAME_FLAG_RETURNEDTEAM("minigame.flag.returnedTeam"),
    MINIGAME_INFO_SCORE("minigame.info.score"),
    MINIGAME_LATEJOIN("minigame.lateJoin"),
    MINIGAME_LATEJOINWAIT("minigame.lateJoinWait"),
    MINIGAME_LIVESLEFT("minigame.livesLeft"),
    MINIGAME_RESOURCE_DECLINED("minigame.resource.declined"), //todo
    MINIGAME_RESOURCE_FAILED("minigame.resource.failed"), //todo
    MINIGAME_RESSOURCEPACK_APPLY("minigame.resourcepack.apply"),
    MINIGAME_RESSOURCEPACK_REMOVE("minigames.resourcepack.remove"),
    MINIGAME_SCORETOWIN("minigame.scoreToWin"),
    MINIGAME_SKIPWAITTIME("minigame.skipWaitTime"),
    MINIGAME_STARTRANDOMIZED("minigame.startRandomized"),
    MINIGAME_WAITINGFORPLAYERS("minigame.waitingForPlayers"),
    MINIGAME_WARNING_TELEPORT_ACROSS_WORLDS("minigame.warning.TeleportAcrossWorlds"),
    PLAYER_BET_INCORRECTITEMAMOUNTINFO("player.bet.incorrectItemAmountInfo"),
    PLAYER_BET_INCORRECTMONEYAMOUNTINFO("player.bet.incorrectMoneyAmountInfo"),
    PLAYER_BET_NOTENOUGHMONEY("player.bet.notEnoughMoney"),
    PLAYER_BET_NOTENOUGHMONEYINFO("player.bet.notEnoughMoneyInfo"),
    PLAYER_BET_PLAYERMSG("player.bet.plyMsg"),
    PLAYER_BET_PLAYERNOBET("player.bet.plyNoBet"),
    PLAYER_BET_WINMONEY("player.bet.winMoney"),
    PLAYER_CHECKPOINT_DEATHREVERT("player.checkpoint.deathRevert"),
    PLAYER_CHECKPOINT_REVERT("player.checkpoint.revert"),
    PLAYER_COMPLETIONTIME("player.completionTime"),
    PLAYER_CTF_CAPTURE("player.ctf.capture"),
    PLAYER_CTF_CAPTUREFINAL("player.ctf.captureFinal"),
    PLAYER_CTF_DROPPED("player.ctf.dropped"),
    PLAYER_CTF_NEUTRAL_CAPTURE("player.ctf.captureNeutral"),
    PLAYER_CTF_NEUTRAL_CAPTUREFINAL("player.ctf.captureNeutralFinal"),
    PLAYER_CTF_NEUTRAL_DROPPED("player.ctf.droppedNeutral"),
    PLAYER_CTF_NEUTRAL_STOLE("player.ctf.stoleNeutral"),
    PLAYER_CTF_RETURNED("player.ctf.returned"),
    PLAYER_CTF_RETURNFAIL("player.ctf.returnFail"),
    PLAYER_CTF_STOLE("player.ctf.stole"),
    PLAYER_END_BROADCAST_NOBODY("player.end.broadcast.nobodyWon"),
    PLAYER_END_BROADCAST_WIN("player.end.broadcast.win"),
    PLAYER_END_TEAM_SCORE("player.end.team.score"),
    PLAYER_END_TEAM_TIE("player.end.team.tie"),
    PLAYER_END_TEAM_TIECOUNT("player.end.team.tieCount"),
    PLAYER_END_TEAM_WIN("player.end.team.win"),
    PLAYER_JOIN_OBJECTIVE("player.join.objective"),
    PLAYER_JOIN_PLAYERINFO("player.join.plyInfo"),
    PLAYER_JOIN_PLAYERMSG("player.join.plyMsg"),
    PLAYER_QUIT_PLAYERMSG("player.quit.plyMsg"),
    PLAYER_SPECTATE_JOIN_MINIGAMEMSG("player.spectate.join.minigameMsg"),
    PLAYER_SPECTATE_JOIN_PLAYERHELP("player.spectate.join.plyHelp"),
    PLAYER_SPECTATE_JOIN_PLAYERMSG("player.spectate.join.plyMsg"),
    PLAYER_SPECTATE_QUIT_MINIGAMEMSG("player.spectate.quit.minigameMsg"),
    PLAYER_SPECTATE_QUIT_PLAYERMSG("player.spectate.quit.plyMsg"),
    PLAYER_TEAM_ASSIGN_JOINTEAM("player.team.assign.joinTeam"),
    TIME_STARTUP_GO("time.startup.go"),
    TIME_STARTUP_MINIGAMESTARTS("time.startup.minigameStarts"),
    TIME_STARTUP_PAUSED("time.startup.timerPaused"),
    TIME_STARTUP_RESUMED("time.startup.timerResumed"),
    TIME_STARTUP_TIME("time.startup.time"),
    TIME_STARTUP_WAITINGFORPLAYERS("time.startup.waitingForPlayers"),
    TIME_TIMELEFT("time.timeLeft");

    private final @NotNull String path;

    MinigameLangKey(@NotNull String path) {
        this.path = path;
    }

    public @NotNull String getPath() {
        return path;
    }
}
