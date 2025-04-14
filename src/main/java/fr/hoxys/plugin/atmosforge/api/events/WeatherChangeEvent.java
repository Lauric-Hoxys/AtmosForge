package fr.hoxys.plugin.atmosforge.api.events;

import fr.hoxys.plugin.atmosforge.models.WeatherType;

import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Événement lancé lorsque la météo change dans un monde.
 */
public class WeatherChangeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final World world;
    private final WeatherType oldWeather;
    private final WeatherType newWeather;

    /**
     * Constructeur de l'événement de changement de météo.
     *
     * @param world Le monde dans lequel la météo change.
     * @param oldWeather L'ancien type de météo.
     * @param newWeather Le nouveau type de météo.
     */
    public WeatherChangeEvent(World world, WeatherType oldWeather, WeatherType newWeather) {
        this.world = world;
        this.oldWeather = oldWeather;
        this.newWeather = newWeather;
        this.cancelled = false;
    }

    /**
     * Obtient le monde concerné par le changement de météo.
     *
     * @return Le monde concerné.
     */
    public World getWorld() {
        return world;
    }

    /**
     * Obtient l'ancien type de météo.
     *
     * @return L'ancien type de météo.
     */
    public WeatherType getOldWeather() {
        return oldWeather;
    }

    /**
     * Obtient le nouveau type de météo.
     *
     * @return Le nouveau type de météo.
     */
    public WeatherType getNewWeather() {
        return newWeather;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Obtient la liste des gestionnaires de cet événement.
     *
     * @return La liste des gestionnaires.
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}