package dev.pinaki.backstage.library;

import android.content.SharedPreferences;

public class Backstage {

    private static volatile Backstage INSTANCE = null;

    private final Delegate delegate;

    public static Backstage getInstance(Delegate delegate) {
        if (INSTANCE == null) {
            synchronized (Backstage.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Backstage(delegate);
                }
            }
        }

        return INSTANCE;
    }

    public interface Delegate {
        void init();

        void addController(BasicController controller);

        void addController(KeyValueController controller);

        void addSharedPreferencesExplorer(String displayName,
                                          SharedPreferencesFactory factory);
    }


    private Backstage(Delegate delegate) {
        this.delegate = delegate;
    }

    public void init() {
        delegate.init();
    }

    public Backstage addController(BasicController controller) {
        delegate.addController(controller);
        return this;
    }

    public Backstage addController(KeyValueController controller) {
        delegate.addController(controller);
        return this;
    }

    public Backstage withSharedPrefExplorer(String displayName,
                                            SharedPreferencesFactory factory) {
        if (factory == null) throw new IllegalArgumentException("factory must not be null");
        delegate.addSharedPreferencesExplorer(displayName, factory);
        return this;
    }

    public interface SharedPreferencesFactory {
        SharedPreferences create();
    }

}
