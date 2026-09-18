package dev.pinaki.backstage.library;

public class Backstage {

    private static volatile Backstage INSTANCE = null;

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
