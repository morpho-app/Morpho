package radiant.nimbus.util

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import radiant.nimbus.model.Moment

fun getFormattedDateTimeSince(moment: Moment): String {
    val d = Clock.System.now() - moment.instant
    d.toComponents { days, hours, minutes, seconds ->
        return when {
            days >= 180 -> {
                moment.instant.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            }
            days >= 1 -> {
                val date = Clock.System.todayIn(TimeZone.currentSystemDefault()) - moment.instant.toLocalDateTime(
                    TimeZone.currentSystemDefault()).date
                "${if(date.years > 0) "${date.years} yrs " else ""}${if(date.months > 0) "${date.months} months " else ""}${if(date.days > 0 && date.months == 0) "${date.days} days " else ""}ago"
            }
            (days == 0.toLong() && hours >= 12)-> {
                "$hours h ago"
            }
            (days == 0.toLong() && hours >= 1)-> {
                "${hours}:${minutes} ago"
            }
            (days == 0.toLong() && hours == 0 && minutes > 1) -> {
                "$minutes m ago"
            }
            (days == 0.toLong() && hours == 0 && minutes == 0) -> {
                "$seconds s ago"
            }
            else -> {
                d.toString().substringBeforeLast('m') + "m"
            }
        }
    }
}