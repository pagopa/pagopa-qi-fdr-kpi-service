package it.pagopa.qi.fdrkpi.exceptionhandler

import it.pagopa.generated.qi.fdrkpi.v1.model.ProblemJsonDto
import org.springframework.http.HttpStatus

class RestApiException(val httpStatus: HttpStatus, val title: String, val description: String) :
    RuntimeException(title) {
    fun toProblemJsonDto(): ProblemJsonDto {
        return ProblemJsonDto().status(httpStatus.value()).title(title).detail(description)
    }
}
