package it.pagopa.qi.fdrkpi

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@TestPropertySource(locations = ["classpath:application-tests.properties"])
class PagopaQiFdrKpiServiceApplicationTests {

    @Test fun contextLoads() {}
}
