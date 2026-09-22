package com.example.rtcpos.data.model

enum class ServiceType(val code: String, val title: String, val baseMultiplier: Double) {
    PALLE_VELUGU("PV", "Palle Velugu (Rural Ordinary)", 1.0),
    EXPRESS("EXP", "Express (Semi-Luxury)", 1.35),
    DELUXE("DLX", "Deluxe (Non-AC Luxury)", 1.65),
    SUPER_LUXURY("SLX", "Super Luxury (Push Back)", 2.0)
}

enum class Direction(val label: String) {
    UP("Up (Origin → Terminus)"),
    DOWN("Down (Terminus → Origin)")
}

enum class PaymentMode {
    CASH,
    UPI
}

enum class VerificationStatus {
    VALID,
    INVALID_SIGNATURE,
    OVER_TRAVEL,
    EXPIRED,
    NOT_FOUND
}

data class Stage(
    val stageNumber: Int,
    val nameEn: String,
    val nameVernacular: String,
    val distanceKm: Double
)

data class RouteInfo(
    val routeCode: String,
    val routeName: String,
    val depotName: String,
    val stages: List<Stage>
)

object TransitPreloads {
    val sampleRoutes = listOf(
        RouteInfo(
            routeCode = "100M",
            routeName = "100M: MGBS ⇄ HitechCity (City Ordinary)",
            depotName = "Hyderabad Central Depot - 1",
            stages = listOf(
                Stage(1, "MGBS", "ఎం.జి.బి.ఎస్", 0.0),
                Stage(2, "Afzalgunj", "అఫ్జల్‌గంజ్", 3.0),
                Stage(3, "Abids", "అబిడ్స్", 5.0),
                Stage(4, "Nampally", "నాంపల్లి", 7.0),
                Stage(5, "Lakdikapul", "లక్డీకపూల్", 9.0),
                Stage(6, "Mehdipatnam", "మెహదీపట్నం", 13.0),
                Stage(7, "Tolichowki", "టోలిచౌకి", 16.0),
                Stage(8, "Gachibowli", "గచ్చిబౌలి", 19.0),
                Stage(9, "HitechCity", "హైటెక్ సిటీ", 21.0),
                Stage(10, "Kondapur", "కొండాపూర్", 25.0)
            )
        ),
        RouteInfo(
            routeCode = "100-HYD-VJA",
            routeName = "Hyderabad MGBS ⇄ Vijayawada PNBS",
            depotName = "MGBS Central Depot - 1",
            stages = listOf(
                Stage(1, "MGBS Terminal", "ఎం.జి.బి.ఎస్ టెర్మినల్", 0.0),
                Stage(2, "LB Nagar Ring Road", "ఎల్.బి. నగర్", 14.0),
                Stage(3, "Choutuppal Toll", "చౌటుప్పల్", 48.0),
                Stage(4, "Narketpally Bypass", "నార్కట్‌పల్లి", 82.0),
                Stage(5, "Suryapet Hi-Way Hub", "సూర్యాపేట", 132.0),
                Stage(6, "Kodad Junction", "కోదాడ", 175.0),
                Stage(7, "Nandigama Cross", "నందిగామ", 220.0),
                Stage(8, "Kanchikacherla", "కంచికచర్ల", 240.0),
                Stage(9, "Ibrahimpatnam Ring", "ఇబ్రహీంపట్నం", 258.0),
                Stage(10, "Vijayawada PNBS", "విజయవాడ పి.ఎన్.బి.ఎస్", 274.0)
            )
        ),
        RouteInfo(
            routeCode = "204-BLR-MYS",
            routeName = "Bangalore Majestic ⇄ Mysuru Suburb",
            depotName = "Majestic Central Bus Station",
            stages = listOf(
                Stage(1, "Majestic KSRTC", "మెజెస్టిక్", 0.0),
                Stage(2, "Kengeri Satellite", "కెంగేరి", 16.0),
                Stage(3, "Bidadi Industrial Cross", "బిడది", 34.0),
                Stage(4, "Ramanagara Tollway", "రామనగర", 49.0),
                Stage(5, "Channapatna Craft Town", "చన్నపట్న", 61.0),
                Stage(6, "Maddur Station", "మద్దూరు", 82.0),
                Stage(7, "Mandya Highway", "మాండ్య", 100.0),
                Stage(8, "Srirangapatna Heritage", "శ్రీరంగపట్న", 125.0),
                Stage(9, "Mysuru Suburb Bus Stand", "మైసూరు", 143.0)
            )
        ),
        RouteInfo(
            routeCode = "305-MAA-TPT",
            routeName = "Chennai CMBT ⇄ Tirupati Bus Station",
            depotName = "Koyambedu Inter-State Depot",
            stages = listOf(
                Stage(1, "Chennai CMBT", "చెన్నై సి.ఎం.బి.టి", 0.0),
                Stage(2, "Thirumazhisai", "తిరుమళిసై", 22.0),
                Stage(3, "Thiruvallur Ring", "తిరువళ్లూరు", 45.0),
                Stage(4, "Tirutani Temple Town", "తిరుత్తణి", 85.0),
                Stage(5, "Nagari Border", "నగరి", 100.0),
                Stage(6, "Puttur Cross", "పుత్తూరు", 115.0),
                Stage(7, "Renigunta Junction", "రేణిగుంట", 138.0),
                Stage(8, "Tirupati Central Stand", "తిరుపతి", 148.0)
            )
        )
    )

    /**
     * Calculates base fare between stages based on distance and service type multiplier.
     * RTC fares are rounded to the nearest ₹5 or ₹10 coin denomination for quick change handling.
     */
    fun calculateFare(
        origin: Stage,
        destination: Stage,
        serviceType: ServiceType,
        adults: Int,
        children: Int,
        concessions: Int
    ): Double {
        val distance = kotlin.math.abs(destination.distanceKm - origin.distanceKm)
        if (distance <= 0.0) return 0.0

        // Base rate per km (e.g. ₹0.95 base)
        val ratePerKm = 0.95 * serviceType.baseMultiplier
        val rawBase = (distance * ratePerKm).coerceAtLeast(10.0)

        // Round up to nearest ₹5 coin/currency denomination
        val singleAdultFare = (Math.ceil(rawBase / 5.0) * 5.0)
        val singleChildFare = (Math.ceil((rawBase * 0.5) / 5.0) * 5.0)
        val singleConcessionFare = (Math.ceil((rawBase * 0.25) / 5.0) * 5.0)

        return (singleAdultFare * adults) + (singleChildFare * children) + (singleConcessionFare * concessions)
    }
}
