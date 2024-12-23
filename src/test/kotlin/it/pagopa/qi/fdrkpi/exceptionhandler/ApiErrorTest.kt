package it.pagopa.qi.fdrkpi.exceptionhandler

import kotlin.test.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.http.HttpStatus

class ApiErrorTest {

    @Test
    fun `ApiError subclasses convert to RestApiException`() {
        val apiError = DateTooRecentException("2024-12-20")
        val restException = apiError.toRestException()

        assertEquals(HttpStatus.BAD_REQUEST, restException.httpStatus)
        assertEquals("Date Too Recent", restException.title)
        assertEquals(
            "The provided date '2024-12-20' is too recent. Please provide an earlier date.",
            restException.description
        )
    }

    @Test
    fun `InvalidDateFormatException sets correct properties`() {
        val exception = InvalidDateFormatException("2024-12-20")
        assertEquals(HttpStatus.BAD_REQUEST, exception.httpStatus)
        assertEquals("Invalid Date Format", exception.title)
        assertEquals(
            "The provided date '2024-12-20' does not match the required format. Please use the correct format (e.g., yyyy-MM-dd).",
            exception.description
        )
    }

    @Test
    fun `PspNotFoundException sets correct properties`() {
        val exception = PspNotFoundException("PSP123")
        assertEquals(HttpStatus.NOT_FOUND, exception.httpStatus)
        assertEquals("PSP Not Found", exception.title)
        assertEquals("The requested PSP with value 'PSP123' was not found.", exception.description)
    }
}
