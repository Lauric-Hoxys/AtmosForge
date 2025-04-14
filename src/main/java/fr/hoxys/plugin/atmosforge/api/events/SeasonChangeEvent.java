package fr.hoxys.plugin.atmosforge.api.events;

import fr.hoxys.plugin.atmosforge.models.Season;

import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Événement lancé lorsque la saison change dans un monde.
 */
public class SeasonChangeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final World world;
    private final Season oldSeason;
    private final Season newSeason;

    /**
     * Constructeur de l'événement de changement de saison.
     *
     * @param world Le monde dans lequel la saison change.
     * @param oldSeason L'ancienne saison.
     * @param newSeason La nouvelle saison.
     */
    public SeasonChangeEvent(World world, Season oldSeason, Season newSeason) {
        this.world = world;
        this.oldSeason = oldSeason;
        this.newSeason = newSeason;
        this.cancelled = false;
    }

    /**
     * Obtient le monde concerné par le changement de saison.
     *
     * @return Le monde concerné.
     */
    public World getWorld() {
        return world;
    }

    /**
     * Obtient l'ancienne saison.
     *
     * @return L'ancienne saison.
     */
    public Season getOldSeason() {
        return oldSeason;
    }

    /**
     * Obtient la nouvelle saison.
     *
     * @return La nouvelle saison.
     */
    public Season getNewSeason() {
        return newSeason;
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