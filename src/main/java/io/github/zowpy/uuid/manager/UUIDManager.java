package io.github.zowpy.uuid.manager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.zowpy.uuid.UUIDCache;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This Project is property of Zowpy © 2021
 *
 * @author Zowpy
 * Created: 8/24/2021
 * Project: UUIDCache
 */

@Getter @RequiredArgsConstructor
public class UUIDManager {

    private final UUIDCache uuidCache;

    /**
     * Saves a name & uuid to redis cache
     *
     * @param name name to save
     * @param uuid uuid to save
     */

    public void save(String name, UUID uuid) {
        CompletableFuture.runAsync(() -> {
            this.uuidCache.getJedisAPI().getJedisHandler().runCommand(jedis -> {
                jedis.hset("UUIDCache", name, uuid.toString());
            });
        });
    }

    /**
     * Deletes a name from redis cache
     *
     * @param name name to delete
     */

    public void delete(String name) {
        CompletableFuture.runAsync(() -> {
            this.uuidCache.getJedisAPI().getJedisHandler().runCommand(jedis -> {
                jedis.hdel("UUIDCache", name);
            });
        });
    }

    /**
     * Gets a uuid from the name
     *
     * @param name name to get the uuid
     * @return {@link CompletableFuture<UUID>}
     */

    public CompletableFuture<UUID> getByName(String name) {
        return CompletableFuture.supplyAsync(() -> {
           final AtomicReference<UUID> uuid = new AtomicReference<>();

            this.uuidCache.getJedisAPI().getJedisHandler().runCommand(jedis -> {
                uuid.set(UUID.fromString(jedis.hget("UUIDCache", name)));
            });

            if (uuid.get() == null) {
                final UUID mojang = getFromMojang(name);

                uuid.set(mojang);
            }

            return uuid.get();
        });
    }

    /**
     * Gets a uuid from mojang api
     *
     * @param name name of the player
     * @return {@link CompletableFuture<UUID>}
     */

    public UUID getFromMojang(String name) {
        try {
            final URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            final InputStreamReader reader = new InputStreamReader(url.openStream());
            final JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();

            final String id = object.get("id").getAsString();
            final String uuid1 = id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32);
            final UUID uuid = UUID.fromString(uuid1);

            save(name, uuid);

            return uuid;

        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }



    }
}
