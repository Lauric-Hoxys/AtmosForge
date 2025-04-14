package fr.hoxys.plugin.atmosforge.listeners;

import fr.hoxys.plugin.atmosforge.Main;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 * Écouteur d'événements liés aux mondes.
 */
public class WorldListener implements Listener {

    private final Main plugin;

    public WorldListener(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Gère l'événement quand un monde est initialisé.
     */
    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        // Nous n'avons pas besoin de faire quoi que ce soit lors de l'initialisation
    }

    /**
     * Gère l'événement quand un monde est chargé.
     */
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        // Vérifier si ce monde doit être activé pour AtmosForge
        if (plugin.getConfigManager().isWorldEnabled(event.getWorld().getName())) {
            // Initialiser la météo et la saison pour ce monde
            plugin.getWeatherManager().initializeWeather(event.getWorld());
            plugin.getSeasonManager().initializeSeason(event.getWorld());

            plugin.getLogger().info("Initialized weather and season for newly loaded world: " +
                    event.getWorld().getName());
        }
    }

    /**
     * Gère l'événement quand un monde est déchargé.
     */
    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        // Vérifier si ce monde était activé pour AtmosForge
        if (plugin.getConfigManager().isWorldEnabled(event.getWorld().getName())) {
            // Sauvegarder les données de ce monde avant qu'il ne soit déchargé
            plugin.getWorldManager().saveWorldData(event.getWorld().getName();

            // Annuler tous les effets météorologiques pour ce monde
            plugin.getEffectManager().cancelWorldEffects(event.getWorld());

            plugin.getLogger().info("Saved data and cancelled effects for unloaded world: " +
                    event.getWorld().getName());
        }
    }
}