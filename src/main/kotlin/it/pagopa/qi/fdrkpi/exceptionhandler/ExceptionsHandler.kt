package it.pagopa.qi.fdrkpi.exceptionhandler

import it.pagopa.generated.qi.fdrkpi.v1.model.ProblemJsonDto
import jakarta.validation.ConstraintViolationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionsHandler {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    val invalidRequestDefaultMessage = "Input request is invalid."

    @ExceptionHandler(RestApiException::class)
    fun handleException(e: RestApiException): ResponseEntity<ProblemJsonDto> {
        logger.error("Exception processing request", e)
        return ResponseEntity.status(e.httpStatus).body(e.toProblemJsonDto())
    }

    /** ApiError exception handler */
    @ExceptionHandler(ApiError::class)
    fun handleException(e: ApiError): ResponseEntity<ProblemJsonDto> {
        return handleException(e.toRestException())
    }

    private fun extractValidationErrors(exception: Exception): String? {
        return when (exception) {
            is ConstraintViolationException ->
                exception.constraintViolations.joinToString(", ") { it.propertyPath.toString() }
            is MethodArgumentNotValidException ->
                exception.bindingResult.fieldErrors.joinToString(", ") { it.field }
            else -> null
        }
    }

    @ExceptionHandler(
        MethodArgumentNotValidException::class,
        // MethodArgumentTypeMismatchException::class,
        // ValidationException::class,
        // HttpMessageNotReadableException::class,
        ConstraintViolationException::class
    )
    fun handleRequestValidationException(exception: Exception): ResponseEntity<ProblemJsonDto> {
        logger.error(invalidRequestDefaultMessage)
        val validationErrorCause = extractValidationErrors(exception)
        val detail = "$invalidRequestDefaultMessage ${validationErrorCause ?: ""}"

        return ResponseEntity.badRequest()
            .body(
                ProblemJsonDto()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .title("Bad request")
                    .detail(detail)
            )
    }

    /** Handler for generic exception */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<ProblemJsonDto> {
        logger.error("Exception processing the request", e)
        return ResponseEntity.internalServerError()
            .body(
                ProblemJsonDto()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .title("Error processing the request")
                    .detail("Generic error occurred")
            )
    }
}
