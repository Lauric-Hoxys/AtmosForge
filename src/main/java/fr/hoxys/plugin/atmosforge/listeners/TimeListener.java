package fr.hoxys.plugin.atmosforge.listeners;

import fr.hoxys.plugin.atmosforge.Main;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.TimeSkipEvent;

/**
 * Écouteur d'événements liés au temps dans le jeu.
 */
public class TimeListener implements Listener {

    private final Main plugin;

    public TimeListener(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Gère l'événement quand le temps est sauté (par exemple, avec la commande /time set).
     */
    @EventHandler
    public void onTimeSkip(TimeSkipEvent event) {
        World world = event.getWorld();

        // Vérifier si le monde est activé pour AtmosForge
        if (!plugin.getConfigManager().isWorldEnabled(world.getName())) {
            return;
        }

        // Déterminer si le saut de temps est un cycle jour/nuit
        // Si le temps est défini sur day ou night, on met à jour notre système
        if (event.getSkipReason() == TimeSkipEvent.SkipReason.COMMAND) {
            long newTime = world.getTime();

            // Vérifier si c'est le jour ou la nuit après le saut
            boolean isNightAfterSkip = (newTime >= 13000 && newTime <= 23000);
            boolean isNightBeforeSkip = plugin.getTimeManager().isNightCycleActive(world);

            // Si le statut jour/nuit a changé, mettre à jour notre système
            if (isNightAfterSkip != isNightBeforeSkip) {
                plugin.getTimeManager().forceTimeOfDay(world, !isNightAfterSkip);

                plugin.getLogger().info("Time was skipped in world " + world.getName() +
                        ", updated AtmosForge time cycle accordingly.");
            }
        }
    }
}