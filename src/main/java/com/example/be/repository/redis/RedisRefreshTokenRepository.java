package com.example.be.repository.redis;

import com.example.be.domain.redis.RedisRefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisRefreshTokenRepository extends CrudRepository<RedisRefreshToken, String> {
}
