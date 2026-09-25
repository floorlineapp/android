package com.thefloor.app.core.model

/** The Recognition ladder. */
enum class RecognitionLevel(val label: String, val threshold: Int) {
    MEMBER("Member", 0),
    CONTRIBUTOR("Contributor", 1_000),
    RECOGNISED_MEMBER("Recognised Member", 5_000),
    FLOOR_VOICE("Floor Voice", 10_000),
    WORKPLACE_AMBASSADOR("Workplace Ambassador", 15_000),
}

/** Points alone are not enough above Contributor: Recognised Member also needs a verified workplace, and… */
fun recognitionLevel(
    floorPoints: Int,
    workplaceVerified: Boolean,
    trustedHistory: Boolean,
): RecognitionLevel = when {
    floorPoints >= RecognitionLevel.WORKPLACE_AMBASSADOR.threshold && workplaceVerified && trustedHistory ->
        RecognitionLevel.WORKPLACE_AMBASSADOR
    floorPoints >= RecognitionLevel.FLOOR_VOICE.threshold && workplaceVerified ->
        RecognitionLevel.FLOOR_VOICE
    floorPoints >= RecognitionLevel.RECOGNISED_MEMBER.threshold && workplaceVerified ->
        RecognitionLevel.RECOGNISED_MEMBER
    floorPoints >= RecognitionLevel.CONTRIBUTOR.threshold ->
        RecognitionLevel.CONTRIBUTOR
    else -> RecognitionLevel.MEMBER
}

/** The rung above [level], or null at the top of the ladder. */
fun nextRecognitionLevel(level: RecognitionLevel): RecognitionLevel? =
    RecognitionLevel.entries.getOrNull(level.ordinal + 1)

/** What each rung asks for beyond the points, in the member's own words. */
fun recognitionRequirement(level: RecognitionLevel): String = when (level) {
    RecognitionLevel.MEMBER -> "Join a Floor and start taking part."
    RecognitionLevel.CONTRIBUTOR -> "1,000 Floor Points."
    RecognitionLevel.RECOGNISED_MEMBER -> "5,000 Floor Points and a verified workplace."
    RecognitionLevel.FLOOR_VOICE -> "10,000 Floor Points and a verified workplace. Unlocks Workplace Spotlight."
    RecognitionLevel.WORKPLACE_AMBASSADOR -> "15,000 Floor Points, a verified workplace and a trusted history."
}
