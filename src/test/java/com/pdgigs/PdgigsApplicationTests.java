package com.pdgigs;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.data.mongodb.auto-index-creation=false",
                "spring.mongodb.embedded.version=7.0.0"
        }
)
class PdgigsApplicationTests {

    @Test
    void contextLoads() {

    }
}