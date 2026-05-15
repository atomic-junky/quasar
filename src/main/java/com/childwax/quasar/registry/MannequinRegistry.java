package com.childwax.quasar.registry;

import com.childwax.quasar.Quasar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

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

    private static void checkForValidMannequin() {
        if (Quasar.getServer() == null)
            return;

        for (String mUUID : data.keySet()) {
            UUID uuid = UUID.fromString(mUUID);

            if (!checkIfServerOwnEntity(uuid)) {
                remove(uuid);
                Quasar.LOGGER.debug("Removed stale mannequin {} from registry (entity no longer exists)", uuid);
            }
        }
    }

    private static boolean checkIfServerOwnEntity(UUID uuid) {
        for (Level level : Quasar.getServer().getAllLevels()) {
            Entity e = level.getEntity(uuid);
            if (e != null)
                return true;
        }

        return false;
    }

    public static void load() {
        if (!Files.exists(FILE)) {
            Quasar.LOGGER.info("No mannequin registry file found, starting fresh");
            return;
        }
        try (Reader r = Files.newBufferedReader(FILE)) {
            Type type = new TypeToken<Map<String, Entry>>(){}.getType();
            data = new Gson().fromJson(r, type);
            if (data == null) data = new HashMap<>();
        } catch (Exception e) {
            Quasar.LOGGER.error("Failed to load mannequin registry from {}", FILE, e);
        }

        checkForValidMannequin();
        Quasar.LOGGER.info("Mannequin registry loaded ({} entries)", data.size());
    }

    public static void save() {
        checkForValidMannequin();

        try (Writer w = Files.newBufferedWriter(FILE)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(data, w);
        } catch (Exception e) {
            Quasar.LOGGER.error("Failed to save mannequin registry to {}", FILE, e);
        }

        Quasar.LOGGER.debug("Mannequin registry saved ({} entries)", data.size());
    }

    public static void register(UUID mannequin, String server, UUID textDisplay) {
        data.put(mannequin.toString(), new Entry(server, textDisplay != null ? textDisplay.toString() : null));
        Quasar.LOGGER.debug("Registered mannequin {} -> server='{}', textDisplay={}", mannequin, server, textDisplay);
        save();
    }

    public static Entry get(UUID uuid) {
        checkForValidMannequin();

        return data.get(uuid.toString());
    }

    public static String getServerName(UUID uuid) {
        Entry e = get(uuid);
        return e != null ? e.server() : null;
    }

    public static UUID getTextDisplay(UUID uuid) {
        Entry e = get(uuid);
        return e != null && e.textDisplayUUID() != null ? UUID.fromString(e.textDisplayUUID()) : null;
    }

    public static void remove(UUID uuid) {
        if (data.remove(uuid.toString()) != null) {
            Quasar.LOGGER.debug("Unregistered mannequin {}", uuid);
        } else {
            Quasar.LOGGER.warn("Tried to remove mannequin {} but it wasn't in the registry", uuid);
        }
        save();
    }

    public static Map<String, Entry> getAll() {
        return Collections.unmodifiableMap(data);
    }
}