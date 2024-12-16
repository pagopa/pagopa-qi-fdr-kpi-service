package it.pagopa.qi.fdrkpi

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(locations = ["classpath:application-test.properties"])
class PagopaQiFdrKpiServiceApplicationTests {

    @Test fun contextLoads() {}
}
