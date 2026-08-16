package com.thefloor.app.core.common

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class ShiftGreetingTest {

    @Test
    fun `morning shift 5am to noon`() {
        assertEquals("Good morning shift.", ShiftGreeting.greetingFor(LocalTime.of(5, 0)))
        assertEquals("Good morning shift.", ShiftGreeting.greetingFor(LocalTime.of(11, 59)))
    }

    @Test
    fun `afternoon shift noon to 6pm`() {
        assertEquals("Good afternoon shift.", ShiftGreeting.greetingFor(LocalTime.of(12, 0)))
        assertEquals("Good afternoon shift.", ShiftGreeting.greetingFor(LocalTime.of(17, 59)))
    }

    @Test
    fun `evening shift 6pm to 11pm`() {
        assertEquals("Good evening shift.", ShiftGreeting.greetingFor(LocalTime.of(18, 0)))
        assertEquals("Good evening shift.", ShiftGreeting.greetingFor(LocalTime.of(22, 59)))
    }

    @Test
    fun `night shift 11pm to 5am`() {
        assertEquals("Good night shift.", ShiftGreeting.greetingFor(LocalTime.of(23, 0)))
        assertEquals("Good night shift.", ShiftGreeting.greetingFor(LocalTime.of(2, 30)))
        assertEquals("Good night shift.", ShiftGreeting.greetingFor(LocalTime.of(4, 59)))
    }
}
