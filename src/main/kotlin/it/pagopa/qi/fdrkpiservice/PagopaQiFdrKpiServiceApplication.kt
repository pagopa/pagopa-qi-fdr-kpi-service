package it.pagopa.qi.fdrkpiservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class PagopaQiFdrKpiServiceApplication

fun main(args: Array<String>) {
    runApplication<PagopaQiFdrKpiServiceApplication>(*args)
}
