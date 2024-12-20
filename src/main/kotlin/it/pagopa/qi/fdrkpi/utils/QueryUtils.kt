package it.pagopa.qi.fdrkpi.utils

import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries.generateIdFilter
import java.time.LocalDate
import java.time.YearMonth

fun preparePspQuery(
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

fun getDateRange(period: String, date: String): Pair<LocalDate, LocalDate> {
    return when (FdrKpiPeriod.valueOf(period)) {
        FdrKpiPeriod.daily -> {
            Pair(LocalDate.parse(date), LocalDate.parse(date))
        }
        FdrKpiPeriod.monthly -> {
            val ym = YearMonth.parse(date)
            Pair(ym.atDay(1), ym.atEndOfMonth())
        }
    }
}
