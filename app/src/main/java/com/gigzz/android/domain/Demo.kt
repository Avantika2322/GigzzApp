package com.gigzz.android.domain

class Demo : ArrayList<DemoItem>()

data class DemoItem(
    val crew: List<Any>,
    val details: String,
    val flight_number: Int,
    val is_tentative: Boolean,
    val last_date_update: String,
    val last_ll_launch_date: String,
    val last_ll_update: String,
    val last_wiki_launch_date: String,
    val last_wiki_revision: String,
    val last_wiki_update: String,
    val launch_date_local: String,
    val launch_date_source: String,
    val launch_date_unix: Int,
    val launch_date_utc: String,
    val launch_failure_details: LaunchFailureDetails,
    val launch_site: LaunchSite,
    val launch_success: Boolean,
    val launch_window: Int,
    val launch_year: String,
    val links: Links,
    val mission_id: List<String>,
    val mission_name: String,
    val rocket: Rocket,
    val ships: List<String>,
    val static_fire_date_unix: Int,
    val static_fire_date_utc: String,
    val tbd: Boolean,
    val telemetry: Telemetry,
    val tentative_max_precision: String,
    val timeline: Timeline,
    val upcoming: Boolean
)

data class LaunchFailureDetails(
    val altitude: Int,
    val reason: String,
    val time: Int
)

data class LaunchSite(
    val site_id: String,
    val site_name: String,
    val site_name_long: String
)

data class Links(
    val article_link: String,
    val flickr_images: List<String>,
    val mission_patch: String,
    val mission_patch_small: String,
    val presskit: String,
    val reddit_campaign: String,
    val reddit_launch: String,
    val reddit_media: String,
    val reddit_recovery: String,
    val video_link: String,
    val wikipedia: String,
    val youtube_id: String
)

data class Rocket(
    val fairings: Fairings,
    val first_stage: FirstStage,
    val rocket_id: String,
    val rocket_name: String,
    val rocket_type: String,
    val second_stage: SecondStage
)

data class Telemetry(
    val flight_club: String
)

data class Timeline(
    val beco: Int,
    val center_core_boostback: Int,
    val center_core_entry_burn: Int,
    val center_core_landing: Int,
    val center_stage_sep: Int,
    val dragon_bay_door_deploy: Int,
    val dragon_separation: Int,
    val dragon_solar_deploy: Int,
    val engine_chill: Int,
    val fairing_deploy: Int,
    val first_stage_boostback_burn: Int,
    val first_stage_entry_burn: Int,
    val first_stage_landing: Int,
    val first_stage_landing_burn: Int,
    val go_for_launch: Int,
    val go_for_prop_loading: Int,
    val ignition: Int,
    val liftoff: Int,
    val maxq: Int,
    val meco: Int,
    val payload_deploy: Int,
    val payload_deploy_1: Int,
    val payload_deploy_2: Int,
    val prelaunch_checks: Int,
    val propellant_pressurization: Int,
    val rp1_loading: Int,

    val second_stage_ignition: Int,
    val second_stage_restart: Int,
    val side_core_boostback: Int,
    val side_core_entry_burn: Int,
    val side_core_landing: Int,
    val side_core_sep: Int,
    val stage1_lox_loading: Int,
    val stage1_rp1_loading: Int,
    val stage2_lox_loading: Int,
    val stage2_rp1_loading: Int,
    val stage_sep: Int,
    val webcast_launch: Int,
    val webcast_liftoff: Int
)

data class Fairings(
    val recovered: Boolean,
    val recovery_attempt: Boolean,
    val reused: Boolean,
    val ship: String
)

data class FirstStage(
    val cores: List<Core>
)

data class SecondStage(
    val block: Int,
    val payloads: List<Payload>
)

data class Core(
    val block: Int,
    val core_serial: String,
    val flight: Int,
    val gridfins: Boolean,
    val land_success: Boolean,
    val landing_intent: Boolean,
    val landing_type: String,
    val landing_vehicle: String,
    val legs: Boolean,
    val reused: Boolean
)

data class Payload(
    val cap_serial: String,
    val cargo_manifest: String,
    val customers: List<String>,
    val flight_time_sec: Int,
    val manufacturer: String,
    val mass_returned_kg: Double,
    val mass_returned_lbs: Double,
    val nationality: String,
    val norad_id: List<Int>,
    val orbit: String,
    val orbit_params: OrbitParams,
    val payload_id: String,
    val payload_mass_kg: Double,
    val payload_mass_lbs: Double,
    val payload_type: String,
    val reused: Boolean,
    val uid: String
)

data class OrbitParams(
    val apoapsis_km: Double,
    val arg_of_pericenter: Double,
    val eccentricity: Double,
    val epoch: String,
    val inclination_deg: Double,
    val lifespan_years: Double,
    val longitude: Double,
    val mean_anomaly: Double,
    val mean_motion: Double,
    val periapsis_km: Double,
    val period_min: Double,
    val raan: Double,
    val reference_system: String,
    val regime: String,
    val semi_major_axis_km: Double
)