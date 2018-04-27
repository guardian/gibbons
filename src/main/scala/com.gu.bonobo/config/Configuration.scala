package com.gu.bonobo
package config

import java.time.Period
import model.Email

object Configuration {
    /** The amount of inactivity time after which a key may be removed */
    val inactivityPeriod = Period.ofMonths(30)
    /** The amount of gracing time we give users to let us know they still use their keys */
    val gracePeriod = Period.ofWeeks(2)
    /** The email address used in the From field of emails sent to API users */
    val origin = Email(Option(System.getenv("ORIGIN_EMAIL")).getOrElse("dev@domain.com"))
}