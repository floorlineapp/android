package com.thefloor.app.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    @Test
    fun `email validation`() {
        assertNull(Validators.email("alex@example.com"))
        assertNotNull(Validators.email(""))
        assertNotNull(Validators.email("not-an-email"))
    }

    @Test
    fun `password requires length and complexity`() {
        assertNull(Validators.password("longenough1"))
        assertNull(Validators.password("longenough!"))
        assertNotNull(Validators.password("short1"))
        assertNotNull(Validators.password("justlettershere"))
    }

    @Test
    fun `referral code format mirrors server rules`() {
        assertTrue(Validators.referralCodeFormat("ABCDEFGH"))
        assertTrue(Validators.referralCodeFormat("abcdefgh")) // normalized to uppercase
        assertTrue(Validators.referralCodeFormat(" ABCDEFGH ")) // trimmed
        assertFalse(Validators.referralCodeFormat("ABC")) // too short
        assertFalse(Validators.referralCodeFormat("ABCDEFG0")) // 0 not in alphabet
        assertFalse(Validators.referralCodeFormat("ABCDEFGI")) // I not in alphabet
    }

    @Test
    fun `post body limits`() {
        assertNull(Validators.postBody("A real post"))
        assertNotNull(Validators.postBody(""))
        assertNotNull(Validators.postBody("x"))
        assertNotNull(Validators.postBody("y".repeat(5001)))
    }
}
