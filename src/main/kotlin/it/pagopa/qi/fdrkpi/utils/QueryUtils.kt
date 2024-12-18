package it.pagopa.qi.fdrkpi.utils

import com.microsoft.azure.kusto.data.Client
import java.time.LocalDate
import java.time.YearMonth

fun executeQuery(
    query: String,
    dateRange: Pair<LocalDate, LocalDate>,
    xPspCode: String,
    reKustoClient: Client
): List<Any> {
    val result =
        reKustoClient.executeQuery(
            preparePspQuery(query, dateRange.first, dateRange.second, xPspCode)
        )
    val primaryResult = result.primaryResults
    while (primaryResult.next()) {
        return primaryResult.currentRow
    }
    throw IllegalArgumentException("error")
}

fun preparePspQuery(query: String, startDate: LocalDate, endDate: LocalDate, psp: String): String {
    return query
        .replace("\$START_DATE", startDate.toString())
        .replace("\$END_DATE", endDate.toString())
        .replace("\$PSP", psp)
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
