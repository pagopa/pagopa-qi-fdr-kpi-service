package it.pagopa.qi.fdrkpi.exceptionhandler

import kotlin.test.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.http.HttpStatus

class RestApiExceptionTest {

    @Test
    fun `RestApiException converts to ProblemJsonDto`() {
        val exception = RestApiException(HttpStatus.BAD_REQUEST, "Test Title", "Test Description")
        val problemDto = exception.toProblemJsonDto()

        assertEquals(400, problemDto.status)
        assertEquals("Test Title", problemDto.title)
        assertEquals("Test Description", problemDto.detail)
    }
}
