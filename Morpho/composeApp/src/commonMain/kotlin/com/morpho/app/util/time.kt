package com.morpho.app.util

import com.morpho.app.model.uidata.Moment
import kotlinx.datetime.*

fun getFormattedDateTimeSince(moment: Moment): String {
    val postDate = moment.instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val dateDiff = currentDate.minus(postDate)
    val deltaDays = currentDate.toEpochDays() - postDate.toEpochDays()
    val postTime = moment.instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val deltaTime = (currentTime.toInstant(TimeZone.currentSystemDefault()).minus(postTime.toInstant(TimeZone.currentSystemDefault())))
    deltaTime.toComponents { hours, minutes, seconds, _ ->
    return when {
            deltaDays >= 180 -> {
                postTime.date.toString()
            }
            dateDiff.months > 0 -> {
                "${dateDiff.months} months ago"
            }
            dateDiff.days > 0 -> {
                "${dateDiff.days} days ago"
            }
            (deltaDays == 0 && hours >= 12)-> {
                "$hours h ago"
            }
            (deltaDays == 0 && hours >= 2)-> {
                "$hours h $minutes m ago"
            }
            (deltaDays == 0 && hours.toInt() <= 1 && minutes > 1) -> {
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