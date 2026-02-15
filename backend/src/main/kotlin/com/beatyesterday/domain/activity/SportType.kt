package com.beatyesterday.domain.activity

/**
 * Every sport type that Strava supports, mapped to our domain model.
 *
 * - [stravaValue] is the exact string returned by the Strava API (e.g., "MountainBikeRide").
 * - [displayName] is the human-readable label shown in the UI (e.g., "Mountain Bike Rides").
 * - [activityType] is a higher-level grouping used for charts (e.g., pie chart breakdowns).
 *
 * Unknown types from Strava should fall back to [WORKOUT] so the app never crashes
 * on new sport types that Strava adds before we update this enum.
 */
enum class SportType(val stravaValue: String, val displayName: String, val activityType: ActivityType) {
    // Cycling
    RIDE("Ride", "Rides", ActivityType.RIDE),
    MOUNTAIN_BIKE_RIDE("MountainBikeRide", "Mountain Bike Rides", ActivityType.RIDE),
    GRAVEL_RIDE("GravelRide", "Gravel Rides", ActivityType.RIDE),
    E_BIKE_RIDE("EBikeRide", "E-Bike Rides", ActivityType.RIDE),
    E_MOUNTAIN_BIKE_RIDE("EMountainBikeRide", "E-Mountain Bike Rides", ActivityType.RIDE),
    VIRTUAL_RIDE("VirtualRide", "Virtual Rides", ActivityType.RIDE),
    VELO_MOBILE("Velomobile", "Velo Mobiles", ActivityType.RIDE),

    // Running
    RUN("Run", "Runs", ActivityType.RUN),
    TRAIL_RUN("TrailRun", "Trail Runs", ActivityType.RUN),
    VIRTUAL_RUN("VirtualRun", "Virtual Runs", ActivityType.RUN),

    // Walking
    WALK("Walk", "Walks", ActivityType.WALK),
    HIKE("Hike", "Hikes", ActivityType.WALK),

    // Water Sports
    CANOEING("Canoeing", "Canoeing", ActivityType.WATER_SPORTS),
    KAYAKING("Kayaking", "Kayaking", ActivityType.WATER_SPORTS),
    KITE_SURF("Kitesurf", "Kite Surf", ActivityType.WATER_SPORTS),
    ROWING("Rowing", "Rowing", ActivityType.WATER_SPORTS),
    STAND_UP_PADDLING("StandUpPaddling", "Stand Up Paddling", ActivityType.WATER_SPORTS),
    SURFING("Surfing", "Surfing", ActivityType.WATER_SPORTS),
    SWIM("Swim", "Swim", ActivityType.WATER_SPORTS),
    WIND_SURF("Windsurf", "Wind Surf", ActivityType.WATER_SPORTS),

    // Winter Sports
    BACK_COUNTRY_SKI("BackcountrySki", "Back Country Ski", ActivityType.WINTER_SPORTS),
    ALPINE_SKI("AlpineSki", "Alpine Ski", ActivityType.WINTER_SPORTS),
    NORDIC_SKI("NordicSki", "Nordic Ski", ActivityType.WINTER_SPORTS),
    ICE_SKATE("IceSkate", "Ice Skate", ActivityType.WINTER_SPORTS),
    SNOWBOARD("Snowboard", "Snowboard", ActivityType.WINTER_SPORTS),
    SNOWSHOE("Snowshoe", "Snowshoe", ActivityType.WINTER_SPORTS),

    // Skating
    SKATEBOARD("Skateboard", "Skateboard", ActivityType.SKATING),
    INLINE_SKATE("InlineSkate", "Inline Skate", ActivityType.SKATING),
    ROLLER_SKI("RollerSki", "Roller Ski", ActivityType.SKATING),

    // Racquet & Paddle Sports
    BADMINTON("Badminton", "Badminton", ActivityType.RACQUET_PADDLE_SPORTS),
    PICKLE_BALL("Pickleball", "Pickle Ball", ActivityType.RACQUET_PADDLE_SPORTS),
    RACQUET_BALL("Racquetball", "Racquet Ball", ActivityType.RACQUET_PADDLE_SPORTS),
    SQUASH("Squash", "Squash", ActivityType.RACQUET_PADDLE_SPORTS),
    TABLE_TENNIS("TableTennis", "Table Tennis", ActivityType.RACQUET_PADDLE_SPORTS),
    TENNIS("Tennis", "Tennis", ActivityType.RACQUET_PADDLE_SPORTS),

    // Fitness
    CROSSFIT("Crossfit", "Crossfit", ActivityType.FITNESS),
    WEIGHT_TRAINING("WeightTraining", "Weight Training", ActivityType.FITNESS),
    WORKOUT("Workout", "Workout", ActivityType.FITNESS),
    STAIR_STEPPER("StairStepper", "Stair Stepper", ActivityType.FITNESS),
    VIRTUAL_ROW("VirtualRow", "Virtual Row", ActivityType.FITNESS),
    HIIT("HighIntensityIntervalTraining", "HIIT", ActivityType.FITNESS),
    ELLIPTICAL("Elliptical", "Elliptical", ActivityType.FITNESS),

    // Mind & Body
    PILATES("Pilates", "Pilates", ActivityType.MIND_BODY_SPORTS),
    YOGA("Yoga", "Yoga", ActivityType.MIND_BODY_SPORTS),

    // Outdoor Sports
    GOLF("Golf", "Golf", ActivityType.OUTDOOR_SPORTS),
    ROCK_CLIMBING("RockClimbing", "Rock Climbing", ActivityType.OUTDOOR_SPORTS),
    SAIL("Sail", "Sail", ActivityType.OUTDOOR_SPORTS),
    SOCCER("Soccer", "Soccer", ActivityType.OUTDOOR_SPORTS),

    // Adaptive & Inclusive
    HAND_CYCLE("Handcycle", "Hand Cycle", ActivityType.ADAPTIVE_INCLUSIVE_SPORTS),
    WHEELCHAIR("Wheelchair", "Wheelchair", ActivityType.ADAPTIVE_INCLUSIVE_SPORTS),
    ;

    companion object {
        fun fromStravaValue(value: String): SportType =
            entries.find { it.stravaValue == value }
                ?: throw IllegalArgumentException("Unknown sport type: $value")
    }
}

/**
 * Coarse activity grouping used for the pie chart and summary statistics.
 * Multiple [SportType]s roll up into a single [ActivityType] so the UI
 * isn't overwhelmed with dozens of slices.
 */
enum class ActivityType(val displayName: String) {
    RIDE("Cycling"),
    RUN("Running"),
    WALK("Walking"),
    WATER_SPORTS("Water Sports"),
    WINTER_SPORTS("Winter Sports"),
    SKATING("Skating"),
    RACQUET_PADDLE_SPORTS("Racquet & Paddle Sports"),
    FITNESS("Fitness"),
    MIND_BODY_SPORTS("Mind & Body"),
    OUTDOOR_SPORTS("Outdoor Sports"),
    ADAPTIVE_INCLUSIVE_SPORTS("Adaptive & Inclusive Sports"),
}
