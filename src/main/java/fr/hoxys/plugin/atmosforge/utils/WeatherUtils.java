package fr.hoxys.plugin.atmosforge.utils;

import fr.hoxys.plugin.atmosforge.models.Season;
import fr.hoxys.plugin.atmosforge.models.WeatherType;

import org.bukkit.World;

import java.util.List;
import java.util.Random;

/**
 * Classe utilitaire pour manipuler la météo.
 */
public class WeatherUtils {

    private static final Random random = new Random();

    /**
     * Obtient un type de météo aléatoire.
     *
     * @return Un type de météo aléatoire.
     */
    public static WeatherType getRandomWeather() {
        WeatherType[] types = WeatherType.values();
        return types[random.nextInt(types.length)];
    }

    /**
     * Obtient un type de météo aléatoire parmi une liste.
     *
     * @param types La liste des types de météo parmi lesquels choisir.
     * @return Un type de météo aléatoire de la liste.
     */
    public static WeatherType getRandomWeather(List<WeatherType> types) {
        if (types == null || types.isEmpty()) {
            return getRandomWeather();
        }
        return types.get(random.nextInt(types.size()));
    }

    /**
     * Obtient un type de météo aléatoire adapté à une saison.
     *
     * @param season La saison pour laquelle obtenir un type de météo.
     * @return Un type de météo aléatoire adapté à la saison.
     */
    public static WeatherType getRandomWeatherForSeason(Season season) {
        return getRandomWeather(season.getCommonWeatherTypes());
    }

    /**
     * Vérifie si un type de météo implique des précipitations.
     *
     * @param weatherType Le type de météo à vérifier.
     * @return true si le type de météo implique des précipitations, false sinon.
     */
    public static boolean hasPrecipitation(WeatherType weatherType) {
        return weatherType.hasPrecipitation();
    }

    /**
     * Vérifie si un type de météo est considéré comme dangereux.
     *
     * @param weatherType Le type de météo à vérifier.
     * @return true si le type de météo est dangereux, false sinon.
     */
    public static boolean isDangerous(WeatherType weatherType) {
        return weatherType.isDangerous();
    }

    /**
     * Vérifie si la météo actuelle du monde est une tempête.
     *
     * @param world Le monde à vérifier.
     * @return true si la météo actuelle est une tempête, false sinon.
     */
    public static boolean isStorming(World world) {
        return world.hasStorm();
    }

    /**
     * Vérifie si la météo actuelle du monde est un orage.
     *
     * @param world Le monde à vérifier.
     * @return true si la météo actuelle est un orage, false sinon.
     */
    public static boolean isThundering(World world) {
        return world.isThundering();
    }

    /**
     * Définit la météo d'un monde Minecraft.
     *
     * @param world Le monde dans lequel définir la météo.
     * @param storm true pour activer la pluie/neige, false pour désactiver.
     * @param thunder true pour activer le tonnerre, false pour désactiver.
     * @param duration La durée en ticks.
     */
    public static void setMinecraftWeather(World world, boolean storm, boolean thunder, int duration) {
        world.setStorm(storm);
        world.setThundering(thunder);
        world.setWeatherDuration(duration);
        world.setThunderDuration(duration);
    }

    /**
     * Définit le ciel dégagé dans un monde Minecraft.
     *
     * @param world Le monde dans lequel définir le ciel dégagé.
     */
    public static void setClearWeather(World world) {
        setMinecraftWeather(world, false, false, 24000);
    }

    /**
     * Définit la pluie dans un monde Minecraft.
     *
     * @param world Le monde dans lequel définir la pluie.
     */
    public static void setRainWeather(World world) {
        setMinecraftWeather(world, true, false, 24000);
    }

    /**
     * Définit l'orage dans un monde Minecraft.
     *
     * @param world Le monde dans lequel définir l'orage.
     */
    public static void setThunderstormWeather(World world) {
        setMinecraftWeather(world, true, true, 24000);
    }
}