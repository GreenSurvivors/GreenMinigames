package au.com.mineauz.minigames.managers.language;

import org.intellij.lang.annotations.Subst;

public enum MinigamePlaceHolderKey implements PlaceHolderKey {
    BIOME("biome"),
    FLAG("flag"),
    COMMAND("command"),
    DEATHS("deaths"),
    DIRECTION("direction"),
    KILLS("kills"),
    LOADOUT("loadout"),
    MATERIAL("material"),
    MAX("max"),
    MECHANIC("mechanic"),
    MIN("min"),
    MINIGAME("minigame"),
    MONEY("money"),
    NUMBER("number"),
    OBJECTIVE("objective"),
    OTHER_PLAYER("other_player"),
    OTHER_TEAM("other_team"),
    PERMISSION("permission"),
    PLAYER("player"),
    LOCATION("location"),
    REGION("region"),
    REVERTS("reverts"),
    SCORE("score"),
    STATE("state"),
    TEAM("team"),
    TEXT("text"),
    TIME("time"),
    TYPE("type"),
    WORLD("world");

    private final String placeHolder;

    MinigamePlaceHolderKey(String placeHolder) {
        this.placeHolder = placeHolder;
    }

    @Subst("number")
    public String getKey() {
        return placeHolder;
    }
}
