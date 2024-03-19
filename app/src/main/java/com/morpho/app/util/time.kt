package com.morpho.app.util

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import com.morpho.app.model.Moment

fun getFormattedDateTimeSince(moment: Moment): String {
    val postDate = moment.instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val dateDiff = currentDate.minus(postDate)
    val deltaDays = currentDate.toEpochDays() - postDate.toEpochDays()
    val postTime = moment.instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val deltaTime = (currentTime.toInstant(TimeZone.currentSystemDefault()).minus(postTime.toInstant(TimeZone.currentSystemDefault())))
    deltaTime.toComponents { hours, minutes, seconds, nanoseconds ->
    return when {
            deltaDays >= 180 -> {
                moment.instant.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            }
            deltaDays >= 1 -> {

                "${if(dateDiff.years > 0) "${dateDiff.years} yrs " else ""}${if(dateDiff.months > 0) "${dateDiff.months} months " else ""}${if(dateDiff.days > 0 && dateDiff.months == 0) "${dateDiff.days} days " else ""}ago"
            }
            (deltaDays == 0 && hours >= 12)-> {
                "$hours h ago"
            }
            (deltaDays == 0 && hours >= 1)-> {
                "${hours}:${"%02d".format(minutes)} ago"
            }
            (deltaDays == 0 && hours.toInt() == 0 && minutes > 1) -> {
                "$minutes m ago"
            }
            (deltaDays == 0 && hours.toInt() == 0 && minutes == 0) -> {
                "$seconds s ago"
            }
            else -> {
                deltaTime.toString().substringBeforeLast('m') + "m"
            }
        }
    }

}