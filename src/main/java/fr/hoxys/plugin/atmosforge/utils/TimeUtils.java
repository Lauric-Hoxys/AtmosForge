package fr.hoxys.plugin.atmosforge.utils;

import org.bukkit.World;

/**
 * Classe utilitaire pour manipuler le temps dans Minecraft.
 */
public class TimeUtils {

    /**
     * Constantes de temps en ticks.
     */
    public static final long DAY_START = 0;
    public static final long NOON = 6000;
    public static final long NIGHT_START = 13000;
    public static final long MIDNIGHT = 18000;
    public static final long TICKS_PER_DAY = 24000;

    /**
     * Vérifie s'il fait jour dans un monde.
     *
     * @param world Le monde à vérifier.
     * @return true s'il fait jour, false s'il fait nuit.
     */
    public static boolean isDay(World world) {
        long time = world.getTime();
        return time < NIGHT_START || time > 23000;
    }

    /**
     * Vérifie s'il fait nuit dans un monde.
     *
     * @param world Le monde à vérifier.
     * @return true s'il fait nuit, false s'il fait jour.
     */
    public static boolean isNight(World world) {
        return !isDay(world);
    }

    /**
     * Calcule le jour Minecraft actuel pour un monde.
     * Un jour Minecraft complet est de 24000 ticks.
     *
     * @param world Le monde pour lequel calculer le jour.
     * @return Le jour actuel (depuis la création du monde).
     */
    public static long getCurrentDay(World world) {
        return world.getFullTime() / TICKS_PER_DAY;
    }

    /**
     * Obtient l'heure du jour sous forme de chaîne.
     *
     * @param world Le monde pour lequel obtenir l'heure.
     * @return L'heure du jour (ex: "6:30 AM").
     */
    public static String getTimeOfDay(World world) {
        long time = world.getTime();

        // Convertir les ticks en heures et minutes
        double hours = (time / 1000.0 + 6) % 24; // +6 pour aligner sur l'heure réelle
        int wholeHours = (int) hours;
        int minutes = (int) ((hours - wholeHours) * 60);

        // Formater en AM/PM
        String ampm = wholeHours >= 12 ? "PM" : "AM";
        wholeHours = wholeHours % 12;
        if (wholeHours == 0) wholeHours = 12;

        return String.format("%d:%02d %s", wholeHours, minutes, ampm);
    }

    /**
     * Calcule le jour de la saison à partir du jour total et de la durée de la saison.
     *
     * @param totalDay Le jour total depuis la création du monde.
     * @param daysPerSeason Le nombre de jours dans une saison.
     * @return Le jour actuel de la saison (de 1 à daysPerSeason).
     */
    public static int getSeasonDay(long totalDay, int daysPerSeason) {
        return (int) ((totalDay % daysPerSeason) + 1);
    }

    /**
     * Calcule la saison actuelle à partir du jour total et de la durée de la saison.
     *
     * @param totalDay Le jour total depuis la création du monde.
     * @param daysPerSeason Le nombre de jours dans une saison.
     * @return L'ordre de la saison (0 = printemps, 1 = été, 2 = automne, 3 = hiver).
     */
    public static int getSeasonOrder(long totalDay, int daysPerSeason) {
        return (int) ((totalDay / daysPerSeason) % 4);
    }

    /**
     * Force le temps d'un monde à une valeur spécifique.
     *
     * @param world Le monde dans lequel forcer le temps.
     * @param time Le temps en ticks.
     */
    public static void setWorldTime(World world, long time) {
        world.setTime(time);
    }

    /**
     * Force le jour dans un monde.
     *
     * @param world Le monde dans lequel forcer le jour.
     */
    public static void setDay(World world) {
        world.setTime(1000); // Début de journée
    }

    /**
     * Force la nuit dans un monde.
     *
     * @param world Le monde dans lequel forcer la nuit.
     */
    public static void setNight(World world) {
        world.setTime(14000); // Début de nuit
    }
}