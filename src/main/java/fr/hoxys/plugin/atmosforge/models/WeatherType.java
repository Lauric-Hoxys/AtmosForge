package fr.hoxys.plugin.atmosforge.models;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Énumération de tous les types de météo disponibles dans AtmosForge.
 */
public enum WeatherType {
    // Précipitations
    LIGHT_RAIN("light_rain", WeatherCategory.PRECIPITATION, true, false),
    MODERATE_RAIN("moderate_rain", WeatherCategory.PRECIPITATION, true, false),
    HEAVY_RAIN("heavy_rain", WeatherCategory.PRECIPITATION, true, false),
    DRIZZLE("drizzle", WeatherCategory.PRECIPITATION, true, false),
    SHOWER("shower", WeatherCategory.PRECIPITATION, true, false),
    FREEZING_RAIN("freezing_rain", WeatherCategory.PRECIPITATION, true, true),
    SMALL_HAIL("small_hail", WeatherCategory.PRECIPITATION, true, false),
    MEDIUM_HAIL("medium_hail", WeatherCategory.PRECIPITATION, true, false),
    LARGE_HAIL("large_hail", WeatherCategory.PRECIPITATION, true, true),
    LIGHT_SNOW("light_snow", WeatherCategory.PRECIPITATION, true, false),
    MODERATE_SNOW("moderate_snow", WeatherCategory.PRECIPITATION, true, false),
    HEAVY_SNOW("heavy_snow", WeatherCategory.PRECIPITATION, true, true),
    SLEET("sleet", WeatherCategory.PRECIPITATION, true, false),
    BLIZZARD("blizzard", WeatherCategory.PRECIPITATION, true, true),
    SLUSH("slush", WeatherCategory.PRECIPITATION, true, false),
    FROST("frost", WeatherCategory.PRECIPITATION, false, false),
    MUD_RAIN("mud_rain", WeatherCategory.PRECIPITATION, true, false),
    SAND_RAIN("sand_rain", WeatherCategory.PRECIPITATION, true, false),

    // Phénomènes liés à la température
    HEAT_WAVE("heat_wave", WeatherCategory.TEMPERATURE, false, true),
    COLD_WAVE("cold_wave", WeatherCategory.TEMPERATURE, false, true),
    FROST_EVENT("frost_event", WeatherCategory.TEMPERATURE, false, false),
    THAW("thaw", WeatherCategory.TEMPERATURE, false, false),
    HEATSTROKE("heatstroke", WeatherCategory.TEMPERATURE, false, true),

    // Vents
    LIGHT_BREEZE("light_breeze", WeatherCategory.WIND, false, false),
    MODERATE_WIND("moderate_wind", WeatherCategory.WIND, false, false),
    STRONG_WIND("strong_wind", WeatherCategory.WIND, false, false),
    GUST("gust", WeatherCategory.WIND, false, false),
    STORM("storm", WeatherCategory.WIND, false, true),
    HURRICANE("hurricane", WeatherCategory.WIND, false, true),
    TORNADO("tornado", WeatherCategory.WIND, false, true),
    WATERSPOUT("waterspout", WeatherCategory.WIND, false, true),
    SAND_STORM("sand_storm", WeatherCategory.WIND, false, true),
    DUST_STORM("dust_storm", WeatherCategory.WIND, false, true),
    WIND_BLAST("wind_blast", WeatherCategory.WIND, false, false),
    CHINOOK("chinook", WeatherCategory.WIND, false, false),
    MONSOON("monsoon", WeatherCategory.WIND, true, true),

    // Phénomènes électriques et lumineux
    THUNDERSTORM("thunderstorm", WeatherCategory.ELECTRICAL, true, true),
    LIGHTNING("lightning", WeatherCategory.ELECTRICAL, false, true),
    THUNDER("thunder", WeatherCategory.ELECTRICAL, false, false),
    ST_ELMO_FIRE("st_elmo_fire", WeatherCategory.ELECTRICAL, false, false),
    AURORA("aurora", WeatherCategory.ELECTRICAL, false, false),

    // Phénomènes de visibilité
    FOG("fog", WeatherCategory.VISIBILITY, false, false),
    MIST("mist", WeatherCategory.VISIBILITY, false, false),
    SMOG("smog", WeatherCategory.VISIBILITY, false, false),
    SEA_MIST("sea_mist", WeatherCategory.VISIBILITY, false, false),
    FREEZING_FOG("freezing_fog", WeatherCategory.VISIBILITY, false, false),
    SMOKE("smoke", WeatherCategory.VISIBILITY, false, false),
    DUST_HAZE("dust_haze", WeatherCategory.VISIBILITY, false, false),
    VOLCANIC_ASH("volcanic_ash", WeatherCategory.VISIBILITY, false, true),

    // Phénomènes optiques
    RAINBOW("rainbow", WeatherCategory.OPTICAL, false, false),
    MOONBOW("moonbow", WeatherCategory.OPTICAL, false, false),
    HALO("halo", WeatherCategory.OPTICAL, false, false),
    SUNDOG("sundog", WeatherCategory.OPTICAL, false, false),
    LIGHT_PILLAR("light_pillar", WeatherCategory.OPTICAL, false, false),
    CORONA("corona", WeatherCategory.OPTICAL, false, false),
    CREPUSCULAR_RAY("crepuscular_ray", WeatherCategory.OPTICAL, false, false),
    GREEN_FLASH("green_flash", WeatherCategory.OPTICAL, false, false),

    // Conditions de ciel
    CLEAR_SKY("clear_sky", WeatherCategory.SKY_CONDITION, false, false),
    PARTLY_CLOUDY("partly_cloudy", WeatherCategory.SKY_CONDITION, false, false),
    CLOUDY("cloudy", WeatherCategory.SKY_CONDITION, false, false),
    OVERCAST("overcast", WeatherCategory.SKY_CONDITION, false, false),
    THREATENING_SKY("threatening_sky", WeatherCategory.SKY_CONDITION, false, false),

    // Phénomènes extrêmes ou rares
    TROPICAL_CYCLONE("tropical_cyclone", WeatherCategory.EXTREME, true, true),
    FIRESTORM("firestorm", WeatherCategory.EXTREME, false, true),
    STORM_SURGE("storm_surge", WeatherCategory.EXTREME, true, true),
    AVALANCHE("avalanche", WeatherCategory.EXTREME, false, true),
    FLASH_FLOOD("flash_flood", WeatherCategory.EXTREME, true, true),
    ACID_RAIN("acid_rain", WeatherCategory.EXTREME, true, true),
    ICE_FOG("ice_fog", WeatherCategory.EXTREME, false, true),
    COLORED_SNOW("colored_snow", WeatherCategory.EXTREME, true, false),

    // Cycle Nuit (spécial)
    NIGHT_CYCLE("night_cycle", WeatherCategory.SPECIAL, false, false);

    private final String id;
    private final WeatherCategory category;
    private final boolean precipitation;
    private final boolean dangerous;

    WeatherType(String id, WeatherCategory category, boolean precipitation, boolean dangerous) {
        this.id = id;
        this.category = category;
        this.precipitation = precipitation;
        this.dangerous = dangerous;
    }

    /**
     * Obtient l'identifiant unique du type de météo.
     *
     * @return L'identifiant unique du type de météo.
     */
    public String getId() {
        return id;
    }

    /**
     * Obtient la catégorie du type de météo.
     *
     * @return La catégorie du type de météo.
     */
    public WeatherCategory getCategory() {
        return category;
    }

    /**
     * Vérifie si ce type de météo implique des précipitations.
     *
     * @return true si ce type de météo implique des précipitations, false sinon.
     */
    public boolean hasPrecipitation() {
        return precipitation;
    }

    /**
     * Vérifie si ce type de météo est considéré comme dangereux.
     *
     * @return true si ce type de météo est dangereux, false sinon.
     */
    public boolean isDangerous() {
        return dangerous;
    }

    /**
     * Obtient un type de météo à partir de son ID.
     *
     * @param id L'identifiant du type de météo à rechercher.
     * @return Le type de météo correspondant, ou null si aucun ne correspond.
     */
    public static WeatherType fromId(String id) {
        for (WeatherType type : values()) {
            if (type.getId().equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Obtient tous les types de météo d'une catégorie spécifique.
     *
     * @param category La catégorie de météo à filtrer.
     * @return Une liste des types de météo appartenant à cette catégorie.
     */
    public static List<WeatherType> getByCategory(WeatherCategory category) {
        return Arrays.stream(values())
                .filter(type -> type.getCategory() == category)
                .collect(Collectors.toList());
    }

    /**
     * Obtient tous les types de météo qui ont des précipitations.
     *
     * @return Une liste des types de météo avec précipitations.
     */
    public static List<WeatherType> getPrecipitationTypes() {
        return Arrays.stream(values())
                .filter(WeatherType::hasPrecipitation)
                .collect(Collectors.toList());
    }

    /**
     * Obtient tous les types de météo considérés comme dangereux.
     *
     * @return Une liste des types de météo dangereux.
     */
    public static List<WeatherType> getDangerousTypes() {
        return Arrays.stream(values())
                .filter(WeatherType::isDangerous)
                .collect(Collectors.toList());
    }

    /**
     * Catégories de types de météo.
     */
    public enum WeatherCategory {
        PRECIPITATION,
        TEMPERATURE,
        WIND,
        ELECTRICAL,
        VISIBILITY,
        OPTICAL,
        SKY_CONDITION,
        EXTREME,
        SPECIAL
    }
}