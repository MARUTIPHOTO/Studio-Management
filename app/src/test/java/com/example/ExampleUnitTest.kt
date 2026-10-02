package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun callSheet_generatesProperFormat() {
    val profile = com.example.data.model.StudioProfile(
      studioName = "Maruti Photo Studio",
      ownerName = "Kishorbhai Patel",
      mobileNumber = "9825012345",
      address = "Ahmedabad, Gujarat"
    )
    val event = com.example.data.model.EventBooking(
      id = 1L,
      customerId = 1L,
      customerName = "Charvik",
      customerMobile = "9876543210",
      eventName = "Wedding",
      eventDate = "25 Dec 2026",
      eventDateMillis = 1798156800000L,
      eventTime = "09:00 AM",
      eventLocation = "Ahmedabad",
      crewIdsCsv = "1",
      crewNames = "Vikram Singh (Drone Pilot)"
    )
    val crew = com.example.data.model.CrewMember(
      id = 1L,
      name = "Vikram Singh",
      mobile = "9898989898",
      role = "Drone Pilot",
      cameraEquipment = "DJI Mavic 3 Pro"
    )
    val sheet = com.example.util.CallSheetHelper.generateEventCallSheet(
      profile = profile,
      mainEvent = event,
      allCustomerEvents = listOf(event),
      allCrew = listOf(crew)
    )
    assertTrue(sheet.contains("MARUTI PHOTO STUDIO - EVENT CALL SHEET"))
    assertTrue(sheet.contains("Wedding"))
    assertTrue(sheet.contains("Charvik"))
    assertTrue(sheet.contains("Ahmedabad"))
  }
}
