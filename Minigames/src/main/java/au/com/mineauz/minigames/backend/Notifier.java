package au.com.mineauz.minigames.backend;

import org.jetbrains.annotations.NotNull;

public interface Notifier {

    void onProgress(@NotNull String state, int count);

    void onComplete();

    void onError(@NotNull Exception e, @NotNull String state, int count);
}
