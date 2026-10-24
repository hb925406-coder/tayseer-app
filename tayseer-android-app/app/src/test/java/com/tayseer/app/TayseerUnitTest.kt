package com.tayseer.app

import com.tayseer.app.data.entity.AttendanceStatus
import com.tayseer.app.data.entity.EvaluationTagType
import com.tayseer.app.utils.HijriCalendarHelper
import com.tayseer.app.utils.SecurityHelper
import org.junit.Assert.*
import org.junit.Test

class TayseerUnitTest {

    @Test
    fun testPasswordHashingAndVerification() {
        val password = "secretPassword123"
        val salt = SecurityHelper.generateSalt()
        assertNotNull(salt)
        assertTrue(salt.isNotBlank())

        val hash = SecurityHelper.hashPassword(password, salt)
        assertNotNull(hash)
        assertEquals(64, hash.length)

        assertTrue(SecurityHelper.verifyPassword(password, salt, hash))
        assertFalse(SecurityHelper.verifyPassword("wrongPassword", salt, hash))
    }

    @Test
    fun testParentAccessCodeGeneration() {
        val code = SecurityHelper.generateAccessCode(6)
        assertEquals(6, code.length)
        assertTrue(code.all { it.isDigit() })
    }

    @Test
    fun testHijriCalendarConversion() {
        val isoDate = "2024-10-22"
        val hijriDate = HijriCalendarHelper.getHijriDate(isoDate, 0)
        assertNotNull(hijriDate)
        assertTrue("Hijri date should contain Arabic characters and year", hijriDate.contains("هـ"))

        val hijriOffsetPlus = HijriCalendarHelper.getHijriDate(isoDate, 1)
        assertNotNull(hijriOffsetPlus)
    }

    @Test
    fun testAttendanceStatusLabels() {
        assertEquals("حاضر", AttendanceStatus.PRESENT.arabicLabel)
        assertEquals("غائب", AttendanceStatus.ABSENT.arabicLabel)
        assertEquals("متأخر", AttendanceStatus.LATE.arabicLabel)
        assertEquals("مستأذن", AttendanceStatus.PERMITTED.arabicLabel)
        assertEquals("غائب بعذر", AttendanceStatus.EXCUSED_ABSENCE.arabicLabel)
        assertEquals("هارب", AttendanceStatus.TRUANT.arabicLabel)
    }

    @Test
    fun testEvaluationTagPositivity() {
        assertTrue(EvaluationTagType.MEMORIZED.isPositive)
        assertFalse(EvaluationTagType.NOT_MEMORIZED.isPositive)
        assertTrue(EvaluationTagType.DISTINGUISHED.isPositive)
        assertTrue(EvaluationTagType.WELL_BEHAVED.isPositive)
        assertFalse(EvaluationTagType.FORGOT_BOOKS.isPositive)
    }
}
