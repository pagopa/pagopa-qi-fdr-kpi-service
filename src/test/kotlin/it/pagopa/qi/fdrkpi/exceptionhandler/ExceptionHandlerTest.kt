package it.pagopa.qi.fdrkpi.exceptionhandler

import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Path
import kotlin.test.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException

class ExceptionHandlerTest {
    @Test
    fun `ExceptionsHandler handles RestApiException`() {
        val handler = ExceptionsHandler()
        val restException =
            RestApiException(HttpStatus.BAD_REQUEST, "Test Title", "Test Description")
        val response = handler.handleException(restException)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Test Title", response.body?.title)
        assertEquals("Test Description", response.body?.detail)
    }

    @Test
    fun `ExceptionsHandler handles ApiError`() {
        val handler = ExceptionsHandler()
        val apiError = DateTooRecentException("2024-12-20")
        val response = handler.handleException(apiError)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Date Too Recent", response.body?.title)
        assertEquals(
            "The provided date '2024-12-20' is too recent. Please provide an earlier date.",
            response.body?.detail
        )
    }

    @Test
    fun `ExceptionsHandler handles MethodArgumentNotValidException`() {
        val bindingResult = mock(BindingResult::class.java)
        `when`(bindingResult.fieldErrors)
            .thenReturn(listOf(FieldError("object", "field", "error message")))

        val methodParameter = mock(MethodParameter::class.java) // Mock valido
        val exception = MethodArgumentNotValidException(methodParameter, bindingResult)

        val handler = ExceptionsHandler()

        val response = handler.handleRequestValidationException(exception)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Bad request", response.body?.title)
        assertEquals("Input request is invalid. field", response.body?.detail)
    }

    @Test
    fun `ExceptionsHandler handles ConstraintViolationException`() {
        val violation = mock(ConstraintViolation::class.java)

        val path = mock(Path::class.java)
        `when`(path.toString()).thenReturn("field")
        `when`(violation.propertyPath).thenReturn(path)
        `when`(violation.message).thenReturn("must not be null")

        val exception = ConstraintViolationException(setOf(violation))
        val handler = ExceptionsHandler()

        val response = handler.handleRequestValidationException(exception)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Bad request", response.body?.title)
        assertEquals("Input request is invalid. field", response.body?.detail)
    }

    @Test
    fun `ExceptionsHandler handles generic Exception`() {
        val handler = ExceptionsHandler()
        val exception = RuntimeException("Generic error occurred")

        val response = handler.handleGenericException(exception)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Error processing the request", response.body?.title)
        assertEquals("Generic error occurred", exception.message)
    }
}
