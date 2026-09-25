package com.thefloor.app.feature.radio

/** The six fixed Floor Radio regions. */
data class RadioShow(val time: String, val name: String, val desc: String)

data class RadioRegion(
    val key: String,
    val label: String,
    val city: String,
    val timezone: String,
    val host: String,
    val nowPlaying: String,
    val tagline: String,
    val listeners: Int,
    val schedule: List<RadioShow>,
)

val RADIO_REGIONS: List<RadioRegion> = listOf(
    RadioRegion(
        key = "global",
        label = "Global",
        city = "Everywhere",
        timezone = "UTC",
        host = "The Floor Collective",
        nowPlaying = "The Global Handover",
        tagline = "One timezone signs off and another takes over.",
        listeners = 4_182,
        schedule = listOf(
            RadioShow("05:30", "Morning Login", "Original music, shout-outs and the five headlines worth knowing."),
            RadioShow("11:00", "The Floor Briefing", "Contact-centre news, AI, CX and jobs — sourced, credited, condensed."),
            RadioShow("14:00", "Made on The Floor", "Original tracks by agents and TLs who make music after the shift."),
            RadioShow("17:00", "The Global Handover", "Johannesburg clocks out. Manila takes The Floor."),
            RadioShow("23:00", "Night Shift", "Company for the people keeping the world awake."),
        ),
    ),
    RadioRegion(
        key = "africa",
        label = "Africa",
        city = "Johannesburg",
        timezone = "Africa/Johannesburg",
        host = "Lerato K.",
        nowPlaying = "Joburg Drive",
        tagline = "From Cape Town to Nairobi, the continent's floors on one feed.",
        listeners = 1_530,
        schedule = listOf(
            RadioShow("06:00", "First Login", "Waking up the early shift across SAST and EAT."),
            RadioShow("10:00", "The Rand & The Rota", "Pay, shifts and what the numbers actually mean."),
            RadioShow("15:00", "Joburg Drive", "The afternoon handover set."),
            RadioShow("19:00", "Continental", "Amapiano, afrobeats and everything in between."),
            RadioShow("22:00", "Lights On", "For the night-shift teams in Joburg, Cape Town and Nairobi."),
        ),
    ),
    RadioRegion(
        key = "philippines",
        label = "Philippines",
        city = "Manila",
        timezone = "Asia/Manila",
        host = "Joan D.",
        nowPlaying = "Graveyard Gold",
        tagline = "The heart of global outsourcing. Always on, always awake.",
        listeners = 2_940,
        schedule = listOf(
            RadioShow("07:00", "Umaga Shift", "Morning music for the teams clocking on."),
            RadioShow("12:00", "Break Room", "Short stories from real BPO life."),
            RadioShow("18:00", "Handover Manila", "Taking The Floor from Johannesburg."),
            RadioShow("21:00", "OPM Hour", "Original Pilipino Music, requested by the floor."),
            RadioShow("01:00", "Graveyard Gold", "Low-light energy for the night shift."),
        ),
    ),
    RadioRegion(
        key = "india",
        label = "India",
        city = "Bengaluru",
        timezone = "Asia/Kolkata",
        host = "Rohan M.",
        nowPlaying = "The Late Queue",
        tagline = "Talent. Technology. Tomorrow starts here.",
        listeners = 2_106,
        schedule = listOf(
            RadioShow("07:30", "Bangalore Boot", "Coffee, commute and the first queue of the day."),
            RadioShow("11:30", "Tech Support", "What automation is actually changing about the job."),
            RadioShow("16:00", "Between Calls", "Music and stories for the back half of the shift."),
            RadioShow("20:00", "Desi Sessions", "Community sets from agents who produce."),
            RadioShow("00:30", "The Late Queue", "For the teams covering US hours."),
        ),
    ),
    RadioRegion(
        key = "uk-europe",
        label = "UK & Europe",
        city = "Kraków",
        timezone = "Europe/Warsaw",
        host = "Ana P.",
        nowPlaying = "Multilingual Mornings",
        tagline = "European quality, global impact — in seven languages.",
        listeners = 1_247,
        schedule = listOf(
            RadioShow("08:00", "Multilingual Mornings", "One show, switching languages with the queue."),
            RadioShow("12:00", "The Regulated Hour", "Compliance, scripts and doing it properly."),
            RadioShow("16:00", "Kraków to Lisbon", "The European afternoon handover."),
            RadioShow("19:00", "Continental Drift", "New music from across the region."),
            RadioShow("22:00", "Closedown", "Winding down before the night teams take over."),
        ),
    ),
    RadioRegion(
        key = "americas",
        label = "Americas",
        city = "Bogotá",
        timezone = "America/Bogota",
        host = "Camila R.",
        nowPlaying = "Nearshore Nights",
        tagline = "Passion, resilience and world-class customer experience.",
        listeners = 1_688,
        schedule = listOf(
            RadioShow("06:30", "Buenos Días Floor", "Bilingual mornings across the nearshore belt."),
            RadioShow("11:00", "The Americas Briefing", "Industry news for LATAM and North America."),
            RadioShow("15:00", "Bogotá Sessions", "Live sets from the community."),
            RadioShow("19:00", "Cross-Border", "Mexico, Colombia, Brazil and the US, on one feed."),
            RadioShow("23:30", "Nearshore Nights", "For the teams holding the overnight lines."),
        ),
    ),
)

fun radioRegion(key: String): RadioRegion =
    RADIO_REGIONS.firstOrNull { it.key == key } ?: RADIO_REGIONS.first()
