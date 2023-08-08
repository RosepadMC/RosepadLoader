package net.buj.loader;

import net.buj.rml.annotations.NotNull;
import net.buj.rml.annotations.Nullable;

public class Failible extends Exception {
    public interface FailibleOperation<T> {
        T run() throws Exception;
    }

    public interface FailibleVoidOperation {
        void run() throws Exception;
    }

    public static class NoPropagate extends Exception {
        private final @NotNull Failible failible;

        public NoPropagate(@NotNull Failible failible) {
            this.failible = failible;
        }

        public @NotNull Failible unwrap() {
            return this.failible;
        }

        public @NotNull Exception exception() {
            return this.failible.inner;
        }
    }

    private final @NotNull Exception inner;

    private Failible(@NotNull Exception exception) {
        this.inner = exception;
    }

    public static <T> void ok(@NotNull FailibleOperation<T> operation) {
        try {
            operation.run();
        } catch (Exception ignored) {}
    }

    public static void ok(@NotNull FailibleVoidOperation operation) {
        try {
            operation.run();
        } catch (Exception ignored) {}
    }

    public static <T> T $(@NotNull FailibleOperation<T> operation, T _default, @NotNull Runnable onFail) {
        try {
            return operation.run();
        } catch (Exception ignored) {
            onFail.run();
            return _default;
        }
    }

    public static void $(@NotNull FailibleVoidOperation operation) throws Failible {
        try {
            operation.run();
        } catch (NoPropagate p) {
            throw p.failible;
        } catch (Failible f) {
            throw f;
        } catch (Exception e) {
            throw new Failible(e);
        }
    }

    public static <T> T $(@NotNull FailibleOperation<T> operation, T _default) {
        try {
            return operation.run();
        } catch (Exception ignored) {
            return _default;
        }
    }

    public static <T> T $(@NotNull FailibleOperation<T> operation) throws Failible {
        try {
            return operation.run();
        } catch (NoPropagate p) {
            throw p.failible;
        } catch (Failible f) {
            throw f;
        } catch (Exception e) {
            throw new Failible(e);
        }
    }

    public static void $(@NotNull Exception e) throws Failible {
        if (e instanceof NoPropagate) {
            throw ((NoPropagate) e).unwrap();
        }
        if (e instanceof Failible) {
            throw (Failible) e;
        }
        throw new Failible(e);
    }
}
