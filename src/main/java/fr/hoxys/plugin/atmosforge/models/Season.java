package fr.hoxys.plugin.atmosforge.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Énumération des saisons disponibles dans AtmosForge.
 */
public enum Season {
    SPRING("spring", "Printemps", 0, getSpringWeatherTypes()),
    SUMMER("summer", "Été", 1, getSummerWeatherTypes()),
    AUTUMN("autumn", "Automne", 2, getAutumnWeatherTypes()),
    WINTER("winter", "Hiver", 3, getWinterWeatherTypes());

    private final String id;
    private final String displayName;
    private final int order;
    private final List<WeatherType> commonWeatherTypes;

    Season(String id, String displayName, int order, List<WeatherType> commonWeatherTypes) {
        this.id = id;
        this.displayName = displayName;
        this.order = order;
        this.commonWeatherTypes = commonWeatherTypes;
    }

    /**
     * Obtient l'identifiant unique de la saison.
     *
     * @return L'identifiant unique de la saison.
     */
    public String getId() {
        return id;
    }

    /**
     * Obtient le nom d'affichage de la saison.
     *
     * @return Le nom d'affichage de la saison.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Obtient l'ordre de la saison dans l'année.
     *
     * @return L'ordre de la saison.
     */
    public int getOrder() {
        return order;
    }

    /**
     * Obtient les types de météo communs pour cette saison.
     *
     * @return Liste des types de météo communs pour cette saison.
     */
    public List<WeatherType> getCommonWeatherTypes() {
        return commonWeatherTypes;
    }

    /**
     * Obtient la saison suivante dans le cycle annuel.
     *
     * @return La saison suivante.
     */
    public Season getNext() {
        Season[] seasons = values();
        int nextIndex = (this.ordinal() + 1) % seasons.length;
        return seasons[nextIndex];
    }

    /**
     * Obtient la saison précédente dans le cycle annuel.
     *
     * @return La saison précédente.
     */
    public Season getPrevious() {
        Season[] seasons = values();
        int prevIndex = (this.ordinal() - 1 + seasons.length) % seasons.length;
        return seasons[prevIndex];
    }

    /**
     * Obtient une saison à partir de son ID.
     *
     * @param id L'identifiant de la saison à rechercher.
     * @return La saison correspondante, ou null si aucune ne correspond.
     */
    public static Season fromId(String id) {
        for (Season season : values()) {
            if (season.getId().equalsIgnoreCase(id)) {
                return season;
            }
        }
        return null;
    }

    /**
     * Obtient une saison à partir de son ordre.
     *
     * @param order L'ordre de la saison (0-3).
     * @return La saison correspondante, ou SPRING si l'ordre est invalide.
     */
    public static Season fromOrder(int order) {
        for (Season season : values()) {
            if (season.getOrder() == order) {
                return season;
            }
        }
        return SPRING; // Valeur par défaut
    }

    // Définition des types de météo communs par saison

    /**
     * Obtient les types de météo communs au printemps.
     *
     * @return Liste des types de météo communs au printemps.
     */
    private static List<WeatherType> getSpringWeatherTypes() {
        return Arrays.asList(
                WeatherType.LIGHT_RAIN,
                WeatherType.MODERATE_RAIN,
                WeatherType.DRIZZLE,
                WeatherType.SHOWER,
                WeatherType.LIGHT_BREEZE,
                WeatherType.MODERATE_WIND,
                WeatherType.RAINBOW,
                WeatherType.CLEAR_SKY,
                WeatherType.PARTLY_CLOUDY,
                WeatherType.CLOUDY,
                WeatherType.FOG,
                WeatherType.MIST,
                WeatherType.THUNDERSTORM
        );
    }

    /**
     * Obtient les types de météo communs en été.
     *
     * @return Liste des types de météo communs en été.
     */
    private static List<WeatherType> getSummerWeatherTypes() {
        return Arrays.asList(
                WeatherType.CLEAR_SKY,
                WeatherType.PARTLY_CLOUDY,
                WeatherType.HEAT_WAVE,
                WeatherType.HEATSTROKE,
                WeatherType.THUNDERSTORM,
                WeatherType.LIGHTNING,
                WeatherType.HEAVY_RAIN,
                WeatherType.SHOWER,
                WeatherType.LIGHT_BREEZE,
                WeatherType.MONSOON,
                WeatherType.RAINBOW,
                WeatherType.TORNADO,
                WeatherType.HURRICANE
        );
    }

    /**
     * Obtient les types de météo communs en automne.
     *
     * @return Liste des types de météo communs en automne.
     */
    private static List<WeatherType> getAutumnWeatherTypes() {
        return Arrays.asList(
                WeatherType.MODERATE_RAIN,
                WeatherType.HEAVY_RAIN,
                WeatherType.DRIZZLE,
                WeatherType.LIGHT_SNOW,
                WeatherType.FROST,
                WeatherType.MODERATE_WIND,
                WeatherType.STRONG_WIND,
                WeatherType.GUST,
                WeatherType.FOG,
                WeatherType.CLOUDY,
                WeatherType.OVERCAST,
                WeatherType.THREATENING_SKY,
                WeatherType.MODERATE_SNOW
        );
    }

    /**
     * Obtient les types de météo communs en hiver.
     *
     * @return Liste des types de météo communs en hiver.
     */
    private static List<WeatherType> getWinterWeatherTypes() {
        return Arrays.asList(
                WeatherType.LIGHT_SNOW,
                WeatherType.MODERATE_SNOW,
                WeatherType.HEAVY_SNOW,
                WeatherType.BLIZZARD,
                WeatherType.SLEET,
                WeatherType.FREEZING_RAIN,
                WeatherType.FROST,
                WeatherType.COLD_WAVE,
                WeatherType.FREEZING_FOG,
                WeatherType.STRONG_WIND,
                WeatherType.OVERCAST,
                WeatherType.CLEAR_SKY,
                WeatherType.ICE_FOG
        );
    }
}