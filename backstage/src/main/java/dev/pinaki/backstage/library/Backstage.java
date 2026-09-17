package dev.pinaki.backstage.library;

import org.jetbrains.annotations.NotNull;

public class Backstage {

    private static Backstage INSTANCE = null;

    @NotNull
    public static Backstage getInstance() {
        if (INSTANCE == null) {
            synchronized (Backstage.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Backstage();
                }
            }
        }

        return INSTANCE;
    }

    private Backstage() {

    }

    public void init() {
    }
}
