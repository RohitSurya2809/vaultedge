package com.rohitsurya2809.vaultedge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohitsurya2809.vaultedge.model.IdempotencyKey;
import com.rohitsurya2809.vaultedge.repository.IdempotencyKeyRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IdempotencyService {

    private final IdempotencyKeyRepository repo;
    private final ObjectMapper mapper;

    public IdempotencyService(IdempotencyKeyRepository repo, ObjectMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public <T> T getIfExists(String key, Class<T> clazz) {
        Optional<IdempotencyKey> opt = repo.findById(key);
        if (opt.isEmpty()) return null;

        try {
            return mapper.readValue(opt.get().getResponseJson(), clazz);
        } catch (Exception e) {
            return null;
        }
    }

    public <T> void save(String key, T responseObj) {
        try {
            String json = mapper.writeValueAsString(responseObj);
            repo.save(new IdempotencyKey(key, json));
        } catch (Exception ignored) {}
    }
}
