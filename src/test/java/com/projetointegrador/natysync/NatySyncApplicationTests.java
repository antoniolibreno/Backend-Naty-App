package com.projetointegrador.natysync;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostgresTestcontainerConfiguration.class)
class NatySyncApplicationTests {

    @Test
    void contextLoads() {}
}
