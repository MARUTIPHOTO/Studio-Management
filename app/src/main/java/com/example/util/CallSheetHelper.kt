package com.example.util

import com.example.data.model.CrewMember
import com.example.data.model.EventBooking
import com.example.data.model.StudioProfile
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object CallSheetHelper {

    fun generateEventCallSheet(
        profile: StudioProfile,
        mainEvent: EventBooking,
        allCustomerEvents: List<EventBooking>,
        allCrew: List<CrewMember>
    ): String {
        val sb = StringBuilder()

        // 1. Studio Header
        val studioUpper = profile.studioName.trim().uppercase()
        sb.appendLine("📸 $studioUpper - EVENT CALL SHEET 📸")
        sb.appendLine("===============================")
        sb.appendLine("🏢 Studio: ${profile.studioName.trim()}")
        sb.appendLine("👤 Owner / Lead: ${profile.ownerName.trim()}")
        sb.appendLine("📞 Studio Contact: ${profile.mobileNumber.trim()}")
        sb.appendLine("📍 Studio Office: ${profile.address.trim().ifBlank { "India" }}")
        sb.appendLine("===============================")
        sb.appendLine()

        // 2. Main Event Details
        val mainDay = getDayOfWeek(mainEvent.eventDateMillis, mainEvent.eventDate)
        val dateDisplay = if (mainDay.isNotBlank()) "${mainEvent.eventDate} ($mainDay)" else mainEvent.eventDate
        sb.appendLine("🎉 Main Event: ${mainEvent.eventName}")
        sb.appendLine("👤 Party / Customer: ${mainEvent.customerName}")
        sb.appendLine("📞 Customer Phone: ${mainEvent.customerMobile}")
        sb.appendLine("📅 Main Date: $dateDisplay")
        sb.appendLine("⏰ Time: ${mainEvent.eventTime}")
        sb.appendLine("📍 Main Venue / Location: ${mainEvent.eventLocation}")
        if (mainEvent.eventLocation.isNotBlank()) {
            val encodedLoc = try {
                URLEncoder.encode(mainEvent.eventLocation.trim(), "UTF-8")
            } catch (e: Exception) {
                mainEvent.eventLocation.trim()
            }
            sb.appendLine("🗺️ Google Maps: https://maps.google.com/?q=$encodedLoc")
        }
        sb.appendLine()

        // 3. Sub-Events / Functions Schedule
        val relatedEvents = if (allCustomerEvents.isNotEmpty()) {
            allCustomerEvents.sortedBy { it.eventDateMillis }
        } else {
            listOf(mainEvent)
        }

        val functionNumberEmojis = listOf("1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🔟")

        sb.appendLine("-------------------------------")
        val fnCount = relatedEvents.size
        sb.appendLine("🎊 FUNCTIONS & SUB-EVENTS SCHEDULE ($fnCount FUNCTION${if (fnCount > 1) "S" else ""}):")
        sb.appendLine()

        relatedEvents.forEachIndexed { index, ev ->
            val numEmoji = functionNumberEmojis.getOrElse(index) { "#${index + 1}" }
            val themeEmoji = getEventThemeEmoji(ev.eventName)
            val evDay = getDayOfWeek(ev.eventDateMillis, ev.eventDate)
            val evDateDisplay = if (evDay.isNotBlank()) "${ev.eventDate} ($evDay)" else ev.eventDate

            sb.appendLine("$numEmoji $themeEmoji ${ev.eventName}")
            sb.appendLine("   📅 Date: $evDateDisplay")
            sb.appendLine("   ⏰ Time: ${ev.eventTime}")
            sb.appendLine("   📍 Venue: ${ev.eventLocation}")
            if (ev.eventLocation.isNotBlank()) {
                val enc = try {
                    URLEncoder.encode(ev.eventLocation.trim(), "UTF-8")
                } catch (e: Exception) {
                    ev.eventLocation.trim()
                }
                sb.appendLine("   🗺️ Google Maps: https://maps.google.com/?q=$enc")
            }

            // Crew assigned to this specific function
            val assignedIds = ev.crewIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
            val assignedCrew = allCrew.filter { it.id in assignedIds }
            if (assignedCrew.isNotEmpty()) {
                val reportTime = getReportingTime(ev.eventTime)
                sb.appendLine("   👥 Assigned Crew:")
                assignedCrew.forEach { c ->
                    val rEmoji = getRoleEmoji(c.role)
                    sb.appendLine("      • $rEmoji ${c.name} (${c.role}) ⏰ Report: $reportTime 📞 ${c.mobile}")
                }
            } else if (ev.crewNames.isNotBlank()) {
                sb.appendLine("   👥 Assigned Crew: ${ev.crewNames}")
            }
            sb.appendLine()
        }

        // 4. Overall Assigned Crew & Reporting Schedule
        val allAssignedIds = relatedEvents.flatMap { ev ->
            ev.crewIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
        }.distinct()
        val allAssignedCrew = allCrew.filter { it.id in allAssignedIds }

        if (allAssignedCrew.isNotEmpty()) {
            sb.appendLine("-------------------------------")
            sb.appendLine("👥 ASSIGNED CREW & REPORTING SCHEDULE:")
            allAssignedCrew.forEachIndexed { index, c ->
                val rEmoji = getRoleEmoji(c.role)
                val crewEvents = relatedEvents.filter { ev ->
                    ev.crewIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }.contains(c.id)
                }
                val firstEvent = crewEvents.firstOrNull() ?: mainEvent
                val repTime = getReportingTime(firstEvent.eventTime)
                val gear = c.cameraEquipment.trim().ifBlank { "Standard Kit" }

                sb.appendLine("${index + 1}. $rEmoji ${c.name} (${c.role})")
                sb.appendLine("   ⏰ Reporting: $repTime")
                sb.appendLine("   📱 📞 ${c.mobile}")
                sb.appendLine("   🎒 Gear: $gear")
            }
            sb.appendLine()
        }

        // 5. Special Notes / Package
        val note = if (mainEvent.dressCodeNote.isNotBlank()) {
            mainEvent.dressCodeNote.trim()
        } else {
            relatedEvents.firstOrNull { it.dressCodeNote.isNotBlank() }?.dressCodeNote?.trim() ?: "No special notes"
        }
        sb.appendLine("-------------------------------")
        sb.appendLine("📌 Special Notes & Package:")
        sb.appendLine(note)
        sb.appendLine()

        // 6. Instructions & Footer
        sb.appendLine("===============================")
        sb.appendLine("⚠️ Instructions: Please arrive 15 minutes prior to reporting time with fully charged batteries and formatted SD cards.")
        sb.appendLine("🙏 Thank you - ${profile.studioName.trim()} (Helpline: ${profile.mobileNumber.trim()})")

        return sb.toString().trimEnd()
    }

    private fun getEventThemeEmoji(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("haldi") || lower.contains("પીઠી") -> "💛"
            lower.contains("wedding") || lower.contains("લગ્ન") || lower.contains("shaadi") || lower.contains("vivah") -> "💍"
            lower.contains("sangeet") || lower.contains("સંગીત") || lower.contains("garba") || lower.contains("રાસ") -> "🎶"
            lower.contains("mehendi") || lower.contains("મહેંદી") || lower.contains("mehndi") -> "🌿"
            lower.contains("reception") || lower.contains("રીસેપ્શન") -> "🥂"
            lower.contains("engagement") || lower.contains("સગાઈ") || lower.contains("ring") -> "💍"
            lower.contains("mamera") || lower.contains("મોસાળું") -> "🎁"
            lower.contains("pre-wedding") || lower.contains("pre wedding") -> "📸"
            lower.contains("baby") || lower.contains("સીમંત") -> "👶"
            lower.contains("birthday") || lower.contains("જન્મદિવસ") -> "🎂"
            else -> "🎉"
        }
    }

    private fun getDayOfWeek(millis: Long, dateString: String = ""): String {
        var time = millis
        if (time <= 0L && dateString.isNotBlank()) {
            try {
                val sdf1 = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                val d = sdf1.parse(dateString.trim())
                if (d != null) time = d.time
            } catch (e: Exception) {
                try {
                    val sdf2 = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
                    val d2 = sdf2.parse(dateString.trim())
                    if (d2 != null) time = d2.time
                } catch (e2: Exception) {}
            }
        }
        if (time <= 0L) return ""
        return try {
            val sdf = SimpleDateFormat("EEEE", Locale.ENGLISH)
            sdf.format(Date(time))
        } catch (e: Exception) {
            ""
        }
    }

    private fun getReportingTime(eventTime: String): String {
        if (eventTime.isBlank()) return "30 mins prior"
        return try {
            val trimmed = eventTime.trim()
            val parser = if (trimmed.contains("AM", ignoreCase = true) || trimmed.contains("PM", ignoreCase = true)) {
                SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            } else {
                SimpleDateFormat("HH:mm", Locale.ENGLISH)
            }
            val date = parser.parse(trimmed)
            if (date != null) {
                val cal = Calendar.getInstance()
                cal.time = date
                cal.add(Calendar.MINUTE, -30)
                val outFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
                outFormat.format(cal.time)
            } else {
                eventTime
            }
        } catch (e: Exception) {
            eventTime
        }
    }

    private fun getRoleEmoji(role: String): String {
        val lower = role.lowercase()
        return when {
            lower.contains("drone") -> "🚁"
            lower.contains("candid") -> "🌟"
            lower.contains("video") || lower.contains("cinema") -> "🎥"
            lower.contains("photo") -> "📷"
            lower.contains("edit") -> "💻"
            lower.contains("light") || lower.contains("crane") -> "💡"
            else -> "👤"
        }
    }
}
