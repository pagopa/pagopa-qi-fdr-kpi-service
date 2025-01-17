package it.pagopa.qi.fdrkpi.utils

import it.pagopa.qi.fdrkpi.dataprovider.kusto.v1.KustoQueries.generateIdFilter
import it.pagopa.qi.fdrkpi.exceptionhandler.DateTooRecentException
import it.pagopa.qi.fdrkpi.exceptionhandler.InvalidPeriodException
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("it.pagopa.qi.fdrkpi.utils.QueryUtils")

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
    logger.info("Calculating date range for period [{}] and date [{}]", period, date)
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

/**
 * Calculates the daily KPI responses values for NRFDR, WPNFDR, WAFDR types (to avoid editing the
 * provided query). The rounding is necessary to handle small amounts, e.g. 3 * 33 / 100 would be
 * less than 1 and would display 0
 */
fun calculateDailyKPIValues(result: List<Any>, totalReports: Int): Int {
    val percentage =
        when (val value = result[0]) {
            is Int -> value
            is Long -> value.toInt()
            else -> throw ClassCastException("Expected Int or Long, got ${value::class.simpleName}")
        }

    return when {
        percentage < 0 ->
            0 // handle the -1 case from query (e.g. no flows for specified psp/broker)
        else -> ((totalReports.toDouble() * percentage) / 100).roundToInt()
    }
}
