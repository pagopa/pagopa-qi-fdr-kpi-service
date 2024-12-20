package it.pagopa.qi.fdrkpi.utils

import it.pagopa.qi.fdrkpi.exceptionhandler.DateTooRecentException
import it.pagopa.qi.fdrkpi.exceptionhandler.InvalidPeriodException
import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries.generateIdFilter
import java.time.LocalDate
import java.time.YearMonth

fun prepareQuery(
    query: String,
    startDate: LocalDate,
    endDate: LocalDate,
    brokerFiscalCode: String? = null,
    pspId: String? = null
): String {
    return query
        .replace("\$START_DATE", startDate.toString())
        .replace("\$END_DATE", endDate.toString())
        .replace("\$FILTER", generateIdFilter(brokerFiscalCode, pspId))
}

fun getDateRange(period: FdrKpiPeriod, date: String): Pair<LocalDate, LocalDate> {
    return when (period) {
        FdrKpiPeriod.daily -> Pair(LocalDate.parse(date), LocalDate.parse(date))
        FdrKpiPeriod.monthly -> {
            val ym = YearMonth.parse(date)
            Pair(ym.atDay(1), ym.atEndOfMonth())
        }
    }
}

fun validateDate(period: String, date: String) {
    try {
        when (period) {
            "daily" -> LocalDate.parse(date)
            "monthly" -> YearMonth.parse(date).atDay(1)
            else -> throw InvalidPeriodException(period)
        }.also { parsedDate ->
            if (parsedDate.isAfter(LocalDate.now().minusDays(10))) {
                throw DateTooRecentException(date)
            }
        }
    } catch (e: DateTooRecentException) {
        throw e
    } catch (e: InvalidPeriodException) {
        throw e
    }
}
