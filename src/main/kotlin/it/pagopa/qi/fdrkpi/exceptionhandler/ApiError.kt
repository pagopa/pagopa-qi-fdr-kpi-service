package it.pagopa.qi.fdrkpi.exceptionhandler

import org.springframework.http.HttpStatus

abstract class ApiError(val httpStatus: HttpStatus, val title: String, val description: String) :
    RuntimeException(title) {
    fun toRestException() = RestApiException(httpStatus, title, description)
}

class DateTooRecentException(dateValue: String?) :
    ApiError(
        HttpStatus.BAD_REQUEST,
        "Date Too Recent",
        "The provided date '$dateValue' is too recent. Please provide an earlier date."
    )

class InvalidDateFormatException(dateValue: String?) :
    ApiError(
        HttpStatus.BAD_REQUEST,
        "Invalid Date Format",
        "The provided date '$dateValue' does not match the required format. Please use the correct format (e.g., yyyy-MM-dd)."
    )

class InvalidKpiTypeException(kpiTypeVal: String?) :
    ApiError(
        HttpStatus.BAD_REQUEST,
        "Invalid KPI Type",
        "The provided KPI type '$kpiTypeVal' is invalid."
    )

class InvalidPeriodException(periodValue: String?) :
    ApiError(
        HttpStatus.BAD_REQUEST,
        "Invalid Period",
        "The provided period '$periodValue' is invalid. Please ensure it follows the expected format."
    )

class PspNotFoundException(pspValue: String?) :
    ApiError(
        HttpStatus.NOT_FOUND,
        "PSP Not Found",
        "The requested PSP with value '$pspValue' was not found."
    )

class NoResultsFoundException(pspValue: String?) :
    ApiError(
        HttpStatus.BAD_REQUEST,
        "No Results Found",
        "No results returned for query with PSP code: $pspValue"
    )
