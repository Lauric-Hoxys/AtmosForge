package fr.hoxys.plugin.atmosforge.api;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.models.Season;
import fr.hoxys.plugin.atmosforge.models.WeatherType;

import org.bukkit.World;

/**
 * API publique pour interagir avec AtmosForge.
 * Cette classe permet à d'autres plugins d'accéder aux fonctionnalités d'AtmosForge.
 */
public class AtmosForgeAPI {

    private static Main plugin;

    /**
     * Initialise l'API avec l'instance du plugin principal.
     * Cette méthode est appelée automatiquement par le plugin.
     *
     * @param main L'instance du plugin principal.
     */
    public static void init(Main main) {
        plugin = main;
    }

    /**
     * Vérifie si AtmosForge est activé pour un monde spécifique.
     *
     * @param world Le monde à vérifier.
     * @return true si AtmosForge est activé pour ce monde, false sinon.
     */
    public static boolean isWorldEnabled(World world) {
        if (plugin == null || world == null) return false;
        return plugin.getConfigManager().isWorldEnabled(world.getName());
    }

    /**
     * Obtient la saison actuelle pour un monde.
     *
     * @param world Le monde pour lequel obtenir la saison.
     * @return La saison actuelle, ou null si AtmosForge n'est pas activé pour ce monde.
     */
    public static Season getCurrentSeason(World world) {
        if (plugin == null || world == null || !isWorldEnabled(world)) return null;
        return plugin.getSeasonManager().getCurrentSeason(world);
    }

    /**
     * Obtient le jour actuel de la saison pour un monde.
     *
     * @param world Le monde pour lequel obtenir le jour de la saison.
     * @return Le jour actuel de la saison, ou -1 si AtmosForge n'est pas activé pour ce monde.
     */
    public static int getCurrentSeasonDay(World world) {
        if (plugin == null || world == null || !isWorldEnabled(world)) return -1;
        return plugin.getSeasonManager().getCurrentSeasonDay(world);
    }

    /**
     * Obtient le nombre de jours dans une saison.
     *
     * @return Le nombre de jours dans une saison.
     */
    public static int getDaysPerSeason() {
        if (plugin == null) return 30; // Valeur par défaut
        return plugin.getSeasonManager().getDaysPerSeason();
    }

    /**
     * Obtient le type de météo actuel pour un monde.
     *
     * @param world Le monde pour lequel obtenir la météo.
     * @return Le type de météo actuel, ou null si AtmosForge n'est pas activé pour ce monde.
     */
    public static WeatherType getCurrentWeather(World world) {
        if (plugin == null || world == null || !isWorldEnabled(world)) return null;
        return plugin.getWeatherManager().getCurrentWeather(world);
    }

    /**
     * Vérifie si le cycle de nuit est actif pour un monde.
     *
     * @param world Le monde à vérifier.
     * @return true si le cycle de nuit est actif, false sinon.
     */
    public static boolean isNightCycleActive(World world) {
        if (plugin == null || world == null || !isWorldEnabled(world)) return false;
        return plugin.getTimeManager().isNightCycleActive(world);
    }

    /**
     * Définit la saison pour un monde.
     *
     * @param world Le monde pour lequel définir la saison.
     * @param season La nouvelle saison.
     * @param day Le jour de la saison (optionnel, 1 par défaut).
     * @return true si le changement a réussi, false sinon.
     */
    public static boolean setSeason(World world, Season season, int day) {
        if (plugin == null || world == null || season == null || !isWorldEnabled(world)) return false;
        return plugin.getSeasonManager().setSeason(world, season, day);
    }

    /**
     * Définit la saison pour un monde (jour 1).
     *
     * @param world Le monde pour lequel définir la saison.
     * @param season La nouvelle saison.
     * @return true si le changement a réussi, false sinon.
     */
    public static boolean setSeason(World world, Season season) {
        return setSeason(world, season, 1);
    }

    /**
     * Définit la météo pour un monde.
     *
     * @param world Le monde pour lequel définir la météo.
     * @param weatherType Le nouveau type de météo.
     * @param duration La durée en minutes.
     * @return true si le changement a réussi, false sinon.
     */
    public static boolean setWeather(World world, WeatherType weatherType, int duration) {
        if (plugin == null || world == null || weatherType == null || !isWorldEnabled(world)) return false;
        return plugin.getWeatherManager().setWeather(world, weatherType, duration);
    }

    /**
     * Définit la météo pour un monde (durée par défaut).
     *
     * @param world Le monde pour lequel définir la météo.
     * @param weatherType Le nouveau type de météo.
     * @return true si le changement a réussi, false sinon.
     */
    public static boolean setWeather(World world, WeatherType weatherType) {
        if (plugin == null || world == null || weatherType == null || !isWorldEnabled(world)) return false;
        return plugin.getWeatherManager().setWeather(world, weatherType);
    }

    /**
     * Force le jour ou la nuit dans un monde.
     *
     * @param world Le monde dans lequel forcer le temps.
     * @param day true pour forcer le jour, false pour forcer la nuit.
     * @return true si le changement a réussi, false sinon.
     */
    public static boolean forceTimeOfDay(World world, boolean day) {
        if (plugin == null || world == null || !isWorldEnabled(world)) return false;
        plugin.getTimeManager().forceTimeOfDay(world, day);
        return true;
    }

    /**
     * Obtient le nom localisé d'un type de météo.
     *
     * @param weatherType Le type de météo.
     * @return Le nom localisé du type de météo.
     */
    public static String getWeatherName(WeatherType weatherType) {
        if (plugin == null || weatherType == null) return weatherType != null ? weatherType.getId() : "null";
        return plugin.getLanguageManager().getWeatherName(weatherType);
    }

    /**
     * Obtient le nom localisé d'une saison.
     *
     * @param season La saison.
     * @return Le nom localisé de la saison.
     */
    public static String getSeasonName(Season season) {
        if (plugin == null || season == null) return season != null ? season.getId() : "null";
        return plugin.getLanguageManager().getSeasonName(season);
    }
}