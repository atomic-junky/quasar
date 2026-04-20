package com.childwax.quasar.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalRegistry {
    private static final Path FILE = FabricLoader.getInstance()
            .getConfigDir().resolve("quasar_portals.json");

    public record Vec3Data(double x, double y, double z) {
        public Vec3 toVec3() { return new Vec3(x, y, z); }
        public static Vec3Data of(Vec3 v) { return new Vec3Data(v.x, v.y, v.z); }
    }

    public record Entry(String server, Vec3Data pos1, Vec3Data pos2) {
        public AABB aabb() {
            return new AABB(pos1.toVec3(), pos2.toVec3()).inflate(0.1);
        }
    }

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

    public static String register(String server, Vec3 pos1, Vec3 pos2) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        data.put(id, new Entry(server, Vec3Data.of(pos1), Vec3Data.of(pos2)));
        save();
        return id;
    }

    public static void update(String id, Entry entry) {
        data.put(id, entry);
        save();
    }

    public static Entry get(String id) {
        return data.get(id);
    }

    public static void remove(String id) {
        data.remove(id);
        save();
    }

    public static Map<String, Entry> getAll() {
        return Collections.unmodifiableMap(data);
    }

    public static String findPortalIntersecting(AABB playerBox) {
        for (Map.Entry<String, Entry> e : data.entrySet()) {
            if (e.getValue().aabb().intersects(playerBox)) return e.getKey();
        }
        return null;
    }
}