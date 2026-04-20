package com.childwax.quasar.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MannequinRegistry {
    private static final Path FILE = FabricLoader.getInstance()
            .getConfigDir().resolve("quasar_mannequins.json");

    public record Entry(String server, String textDisplayUUID) {}

    private static Map<String, Entry> data = new HashMap<>();

    public static void load() {
        if (!Files.exists(FILE)) return;
        try (Reader r = Files.newBufferedReader(FILE)) {
            Type type = new TypeToken<Map<String, Entry>>(){}.getType();
            data = new Gson().fromJson(r, type);
            if (data == null) data = new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(FILE)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(data, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void register(UUID mannequin, String server, UUID textDisplay) {
        data.put(mannequin.toString(), new Entry(server, textDisplay != null ? textDisplay.toString() : null));
        save();
    }

    public static String get(UUID uuid) {
        Entry e = data.get(uuid.toString());
        return e != null ? e.server() : null;
    }

    public static UUID getTextDisplay(UUID uuid) {
        Entry e = data.get(uuid.toString());
        return e != null && e.textDisplayUUID() != null ? UUID.fromString(e.textDisplayUUID()) : null;
    }

    public static Entry getEntry(UUID uuid) {
        return data.get(uuid.toString());
    }

    public static void remove(UUID uuid) {
        data.remove(uuid.toString());
        save();
    }

    public static Map<String, Entry> getAll() {
        return Collections.unmodifiableMap(data);
    }
}