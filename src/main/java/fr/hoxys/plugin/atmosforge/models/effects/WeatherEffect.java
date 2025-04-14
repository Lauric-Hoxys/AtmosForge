package fr.hoxys.plugin.atmosforge.models.effects;

import fr.hoxys.plugin.atmosforge.models.WeatherType;

import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Interface définissant un effet météorologique.
 * Les effets peuvent être appliqués aux joueurs, au monde, ou les deux.
 */
public interface WeatherEffect {

    /**
     * Obtient le type de météo associé à cet effet.
     *
     * @return Le type de météo.
     */
    WeatherType getWeatherType();

    /**
     * Applique l'effet à un joueur.
     *
     * @param player Le joueur auquel appliquer l'effet.
     * @param world Le monde dans lequel le joueur se trouve.
     */
    void applyToPlayer(Player player, World world);

    /**
     * Applique l'effet au monde.
     *
     * @param world Le monde auquel appliquer l'effet.
     */
    void applyToWorld(World world);

    /**
     * Annule l'effet pour un joueur.
     *
     * @param player Le joueur pour lequel annuler l'effet.
     */
    void removeFromPlayer(Player player);

    /**
     * Annule l'effet pour un monde.
     *
     * @param world Le monde pour lequel annuler l'effet.
     */
    void removeFromWorld(World world);

    /**
     * Vérifie si l'effet est actif pour un joueur.
     *
     * @param player Le joueur à vérifier.
     * @return true si l'effet est actif, false sinon.
     */
    boolean isActiveForPlayer(Player player);

    /**
     * Vérifie si l'effet est actif pour un monde.
     *
     * @param world Le monde à vérifier.
     * @return true si l'effet est actif, false sinon.
     */
    boolean isActiveForWorld(World world);

    /**
     * Obtient l'intensité de l'effet (0.0 à 1.0).
     *
     * @return L'intensité de l'effet.
     */
    float getIntensity();

    /**
     * Définit l'intensité de l'effet (0.0 à 1.0).
     *
     * @param intensity La nouvelle intensité.
     */
    void setIntensity(float intensity);

    /**
     * Obtient la durée de l'effet en ticks.
     *
     * @return La durée de l'effet.
     */
    int getDuration();

    /**
     * Définit la durée de l'effet en ticks.
     *
     * @param duration La nouvelle durée.
     */
    void setDuration(int duration);
}