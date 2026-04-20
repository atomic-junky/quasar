package com.childwax.quasar.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Config {
    public Map<String, String> commands = new HashMap<>();
    public PlayerJoinBehaviour joinBehaviour = PlayerJoinBehaviour.NONE;

    public enum PlayerJoinBehaviour {
        NONE,
        WORLD_SPAWN,
        PLAYER_SPAWN
    }

    public static Config load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("quasar.json");

        if (!Files.exists(path)) {
            Config def = new Config();
            def.commands.put("lobby", "lobby");
            save(def, path);
            return def;
        }

        try (Reader r = Files.newBufferedReader(path)) {
            return new Gson().fromJson(r, Config.class);
        } catch (Exception e) {
            return new Config();
        }
    }

    public static void save(Config config, Path path) {
        try (Writer w = Files.newBufferedWriter(path)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(config, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
