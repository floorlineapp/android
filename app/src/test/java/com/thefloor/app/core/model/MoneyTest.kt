package com.thefloor.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {

    @Test
    fun `formats euros from minor units`() {
        assertEquals("€25", Money(2500, "EUR").format())
        assertEquals("€1000", Money(100000, "EUR").format())
        assertEquals("€12.50", Money(1250, "EUR").format())
        assertEquals("€0", Money(0, "EUR").format())
    }

    @Test
    fun `formats unknown currencies with code prefix`() {
        assertEquals("PHP 150", Money(15000, "PHP").format())
    }
}
