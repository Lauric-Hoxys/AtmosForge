package fr.hoxys.plugin.atmosforge.listeners;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.models.WeatherType;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Écouteur d'événements liés aux joueurs.
 */
public class PlayerListener implements Listener {

    private final Main plugin;

    public PlayerListener(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Gère l'événement quand un joueur rejoint le serveur.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Vérifier si le monde du joueur est activé
        if (plugin.getConfigManager().isWorldEnabled(player.getWorld().getName())) {
            // Notifier le joueur de la météo et saison actuelles
            sendWeatherAndSeasonInfo(player);
        }
    }

    /**
     * Gère l'événement quand un joueur quitte le serveur.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Annuler tous les effets météorologiques appliqués au joueur
        // Ceci est optionnel, car les tâches sont annulées automatiquement quand le joueur se déconnecte
    }

    /**
     * Gère l'événement quand un joueur change de monde.
     */
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        // Vérifier si le nouveau monde du joueur est activé
        if (plugin.getConfigManager().isWorldEnabled(player.getWorld().getName())) {
            // Notifier le joueur de la météo et saison actuelles
            sendWeatherAndSeasonInfo(player);
        }
    }

    /**
     * Envoie les informations de météo et de saison à un joueur.
     */
    private void sendWeatherAndSeasonInfo(Player player) {
        // Obtenir les informations de météo et de saison
        WeatherType currentWeather = plugin.getWeatherManager().getCurrentWeather(player.getWorld());
        String weatherName = plugin.getLanguageManager().getWeatherName(currentWeather);

        String seasonName = plugin.getLanguageManager().getSeasonName(
                plugin.getSeasonManager().getCurrentSeason(player.getWorld()));

        int seasonDay = plugin.getSeasonManager().getCurrentSeasonDay(player.getWorld());
        int daysPerSeason = plugin.getSeasonManager().getDaysPerSeason();

        // Envoyer un message au joueur
        player.sendMessage(plugin.getLanguageManager().getMessage("general.prefix") +
                "§6Saison actuelle: §f" + seasonName + " §6(Jour §f" + seasonDay + "/" + daysPerSeason + "§6)");

        player.sendMessage(plugin.getLanguageManager().getMessage("general.prefix") +
                "§6Météo actuelle: §f" + weatherName);

        // Si la météo est dangereuse, avertir le joueur
        if (currentWeather.isDangerous()) {
            player.sendMessage(plugin.getLanguageManager().getMessage("general.prefix") +
                    "§cAttention : Cette météo peut être dangereuse !");
        }
    }
}