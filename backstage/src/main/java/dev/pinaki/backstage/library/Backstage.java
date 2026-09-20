package dev.pinaki.backstage.library;

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

}
