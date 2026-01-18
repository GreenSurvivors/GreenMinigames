package au.com.mineauz.minigames.objects;

public interface ScoreHolder {
    void setScore(int amount);

    int addScore();

    int addScore(int amount);

    void resetScore();

    int getScore();
}
