package it.pagopa.qi.fdrkpi.utils

import java.time.LocalDate
import java.time.YearMonth
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("it.pagopa.qi.fdrkpi.utils.QueryUtils")

fun preparePspQuery(query: String, startDate: LocalDate, endDate: LocalDate, psp: String): String {
    return query
        .replace("\$START_DATE", startDate.toString())
        .replace("\$END_DATE", endDate.toString())
        .replace("\$PSP", psp)
}

fun getDateRange(period: String, date: String): Pair<LocalDate, LocalDate> {
    logger.info("Calculating date range for period [{}] and date [{}]", period, date)

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
