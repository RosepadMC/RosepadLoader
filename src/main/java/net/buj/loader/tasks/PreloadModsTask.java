package net.buj.loader.tasks;

import net.buj.loader.Failible;
import net.buj.loader.RosepadLoadingWindow;
import net.buj.loader.Task;
import net.buj.rml.Environment;
import net.buj.rml.Game;
import net.buj.rml.annotations.NotNull;
import net.buj.rml.events.EventLoop;
import net.buj.rml.loader.GameJar;
import net.buj.rml.loader.RosepadModLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class PreloadModsTask extends Task<GameJar> {
    private final CreateGameJarTask gameJarTask;
    private final Path modsPath;
    private final Environment environment;

    public PreloadModsTask(CreateGameJarTask gameJarTask, Environment environment, Path modsPath) {
        this.gameJarTask = gameJarTask;
        this.environment = environment;
        this.modsPath = modsPath;
    }

    @Override
    protected @NotNull GameJar run(RosepadLoadingWindow log) throws Failible {
        GameJar jar = Failible.$(() -> this.gameJarTask.await(log));

        log.setTask("Loading mods");

        Failible.ok(() -> Files.createDirectories(this.modsPath));
        Game.EVENT_LOOP = new EventLoop();
        Game.MOD_LOADER = new RosepadModLoader();
        Failible.$(() -> Game.MOD_LOADER.load(environment, this.modsPath.toFile(), jar));

        return jar;
    }
}
