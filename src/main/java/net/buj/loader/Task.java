package net.buj.loader;

import net.buj.loader.Failible.NoPropagate;
import net.buj.rml.annotations.NotNull;
import net.buj.rml.annotations.Nullable;

public abstract class Task<T> {
    private @Nullable T data = null;
    private @Nullable NoPropagate error = null;

    protected abstract @NotNull T run(RosepadLoadingWindow log) throws Failible;

    public @NotNull T await(RosepadLoadingWindow log) throws NoPropagate {
        if (error != null) throw error;
        if (data != null) return data;
        try {
            return data = Failible.$(() -> run(log));
        } catch (Failible e) {
            throw error = new NoPropagate(e);
        }
    }
}
