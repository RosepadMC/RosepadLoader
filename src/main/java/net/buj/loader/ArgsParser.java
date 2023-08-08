package net.buj.loader;

import net.buj.rml.annotations.NotNull;
import net.buj.rml.annotations.Nullable;
import net.buj.rml.annotations.PublicAPI;

import java.util.*;

// Many annotations make you feel smart

/**
 * Command line argument parser
 */
public class ArgsParser {
    /**
     * {@link ArgsParser} builder class
     */
    public static class Builder {
        private final @NotNull ArgsParser parser = new ArgsParser();
        private final @NotNull Map<String, Boolean> params = new HashMap<>();

        /**
         * Register a parameter
         */
        @PublicAPI
        public @NotNull Builder param(final @NotNull String name, final boolean hasParam) {
            this.params.put(name, hasParam);
            return this;
        }

        /**
         * Finalize {@link ArgsParser}
         */
        @PublicAPI
        public @NotNull ArgsParser build(final @NotNull String[] args) {
            @Nullable String paramName = null;

            for (final @NotNull String arg : args) {
                if (paramName != null) {
                    this.parser.params.put(paramName, arg);
                    paramName = null;
                }
                else if (arg.startsWith("--")) {
                    if (this.params.containsKey(arg.substring(2)) &&
                        this.params.get(arg.substring(2))) {
                        paramName = arg.substring(2);
                    }
                    else {
                        this.parser.params.put(arg, null);
                    }
                }
                else {
                    this.parser.args.add(arg);
                }
            }

            return this.parser;
        }
    }

    private final List<String> args = new ArrayList<>();
    private final Map<String, String> params = new HashMap<>();

    private ArgsParser() {}

    /**
     * Get a positional command line argument
     */
    @PublicAPI
    public @Nullable String arg(final int pos) {
        if (pos < 0 || pos >= this.args.size()) return null;
        return this.args.get(pos);
    }

    /**
     * Get parameter value
     */
    @PublicAPI
    public @Nullable String arg(final @NotNull String name) {
        return this.params.get(name);
    }

    /**
     * Check if flag is set
     */
    @PublicAPI
    public boolean has(final @NotNull String name) {
        return this.params.containsKey(name);
    }

    /**
     * Get a positional command line argument or default
     */
    @PublicAPI
    public @Nullable String or(final int pos, final @Nullable String default_) {
        if (pos < 0 || pos >= this.args.size()) return default_;
        return this.args.get(pos);
    }

    /**
     * Get parameter value or default
     */
    @PublicAPI
    public @Nullable String or(final @NotNull String name, final @Nullable String default_) {
        return this.params.getOrDefault(name, default_);
    }
}
