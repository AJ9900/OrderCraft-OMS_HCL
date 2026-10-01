package com.ordercraft.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class UserSerializationTest {

    @Test
    void doesNotExposePasswordHash() throws Exception {
        User user = new User("admin", "$2a$12$hashed", "admin@example.com", "Admin", Role.ADMIN);

        String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(user);

        assertFalse(json.contains("password"));
        assertFalse(json.contains("$2a$12$hashed"));
    }
}