package net.buj.loader;

import net.buj.rml.annotations.Nullable;

import java.util.*;

public class ArgsParser {
    private final List<String> args = new ArrayList<>();
    private final Map<String, List<String>> params = new HashMap<>();

    public ArgsParser(String[] args$1) {
        boolean onlyArgs = false;
        String param = null;
        for (String arg : args$1) {
            if (arg.equals("--")) {
                onlyArgs = true;
                continue;
            }
            if (onlyArgs) {
                args.add(arg);
                continue;
            }
            if (arg.startsWith("--")) {
                param = arg.substring(2);
                if (!params.containsKey(param)) {
                    params.put(param, new ArrayList<>());
                }
                continue;
            }
            if (param == null) {
                args.add(arg);
                continue;
            }
            params.get(param).add(arg);
        }
    }

    public @Nullable String arg(int index) {
        return index < 0 || index >= args.size() ? null : args.get(index);
    }
    public List<String> param(String name) {
        return params.getOrDefault(name, Collections.emptyList());
    }
}
