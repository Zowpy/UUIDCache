package io.github.zowpy.uuid;

import io.github.zowpy.jedisapi.JedisAPI;
import io.github.zowpy.jedisapi.redis.RedisCredentials;
import io.github.zowpy.uuid.manager.UUIDManager;
import lombok.Getter;

/**
 * This Project is property of Zowpy © 2021
 *
 * @author Zowpy
 * Created: 8/24/2021
 * Project: UUIDCache
 */

@Getter
public class UUIDCache {

    private final RedisCredentials credentials;
    private final JedisAPI jedisAPI;

    private final UUIDManager uuidManager;

    public UUIDCache(RedisCredentials credentials) {
        this.credentials = credentials;

        this.jedisAPI = new JedisAPI(credentials);

        this.uuidManager = new UUIDManager(this);
    }

}
