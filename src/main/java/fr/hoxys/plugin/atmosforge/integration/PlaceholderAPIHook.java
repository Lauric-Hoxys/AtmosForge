package fr.hoxys.plugin.atmosforge.integration;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.models.Season;
import fr.hoxys.plugin.atmosforge.models.WeatherType;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

/**
 * Classe d'intégration avec PlaceholderAPI.
 * Permet d'utiliser des placeholders pour accéder aux informations d'AtmosForge.
 */
public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final Main plugin;
    private final Logger logger;

    /**
     * Constructeur de l'intégration PlaceholderAPI.
     *
     * @param plugin L'instance du plugin principal.
     */
    public PlaceholderAPIHook(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    @Override
    public String getIdentifier() {
        return "atmosforge";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // Reste enregistré jusqu'à ce que le serveur s'arrête
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        // Placeholders qui nécessitent un joueur
        if (player == null) {
            // Certains placeholders peuvent fonctionner sans joueur
            if (identifier.startsWith("global_")) {
                return handleGlobalPlaceholder(identifier.substring(7));
            }
            return ""; // Pas de joueur, pas de valeur
        }

        // Obtenir le monde du joueur
        World world = player.getWorld();

        // Vérifier si le monde est activé
        if (!plugin.getConfigManager().isWorldEnabled(world.getName())) {
            // Si le monde n'est pas activé, essayer de voir si c'est un placeholder global
            if (identifier.startsWith("global_")) {
                return handleGlobalPlaceholder(identifier.substring(7));
            }
            return ""; // Monde non activé, pas de valeur
        }

        // Traiter les placeholders standard
        switch (identifier.toLowerCase()) {
            case "season":
                Season season = plugin.getSeasonManager().getCurrentSeason(world);
                return plugin.getLanguageManager().getSeasonName(season);

            case "season_id":
                Season seasonId = plugin.getSeasonManager().getCurrentSeason(world);
                return seasonId.getId();

            case "season_day":
                return String.valueOf(plugin.getSeasonManager().getCurrentSeasonDay(world));

            case "season_day_total":
                return String.valueOf(plugin.getSeasonManager().getDaysPerSeason());

            case "season_day_remaining":
                int currentDay = plugin.getSeasonManager().getCurrentSeasonDay(world);
                int totalDays = plugin.getSeasonManager().getDaysPerSeason();
                return String.valueOf(totalDays - currentDay + 1);

            case "next_season":
                Season currentSeason = plugin.getSeasonManager().getCurrentSeason(world);
                Season nextSeason = currentSeason.getNext();
                return plugin.getLanguageManager().getSeasonName(nextSeason);

            case "next_season_id":
                Season currentSeasonId = plugin.getSeasonManager().getCurrentSeason(world);
                Season nextSeasonId = currentSeasonId.getNext();
                return nextSeasonId.getId();

            case "weather":
                WeatherType weather = plugin.getWeatherManager().getCurrentWeather(world);
                return plugin.getLanguageManager().getWeatherName(weather);

            case "weather_id":
                WeatherType weatherId = plugin.getWeatherManager().getCurrentWeather(world);
                return weatherId.getId();

            case "weather_duration":
                return String.valueOf(plugin.getWeatherManager().getWeatherDuration(world));

            case "weather_has_precipitation":
                WeatherType precipWeather = plugin.getWeatherManager().getCurrentWeather(world);
                return precipWeather.hasPrecipitation() ? "Yes" : "No";

            case "weather_is_dangerous":
                WeatherType dangerWeather = plugin.getWeatherManager().getCurrentWeather(world);
                return dangerWeather.isDangerous() ? "Yes" : "No";

            case "is_night_cycle":
                return plugin.getTimeManager().isNightCycleActive(world) ? "Yes" : "No";

            default:
                // Vérifier si c'est un placeholder pour un monde spécifique
                if (identifier.startsWith("world_")) {
                    // Format: world_worldname_placeholder
                    String[] parts = identifier.split("_", 3);
                    if (parts.length == 3) {
                        String worldName = parts[1];
                        String worldPlaceholder = parts[2];
                        return handleWorldPlaceholder(worldName, worldPlaceholder);
                    }
                }

                // Vérifier si c'est un placeholder global
                if (identifier.startsWith("global_")) {
                    return handleGlobalPlaceholder(identifier.substring(7));
                }

                break;
        }

        return ""; // Placeholder non reconnu
    }

    /**
     * Gère les placeholders spécifiques à un monde.
     *
     * @param worldName Le nom du monde.
     * @param placeholder Le placeholder à traiter.
     * @return La valeur du placeholder, ou une chaîne vide si non reconnu.
     */
    private String handleWorldPlaceholder(String worldName, String placeholder) {
        World world = Bukkit.getWorld(worldName);
        if (world == null || !plugin.getConfigManager().isWorldEnabled(worldName)) {
            return ""; // Monde non trouvé ou non activé
        }

        switch (placeholder.toLowerCase()) {
            case "season":
                Season season = plugin.getSeasonManager().getCurrentSeason(world);
                return plugin.getLanguageManager().getSeasonName(season);

            case "season_id":
                Season seasonId = plugin.getSeasonManager().getCurrentSeason(world);
                return seasonId.getId();

            case "season_day":
                return String.valueOf(plugin.getSeasonManager().getCurrentSeasonDay(world));

            case "weather":
                WeatherType weather = plugin.getWeatherManager().getCurrentWeather(world);
                return plugin.getLanguageManager().getWeatherName(weather);

            case "weather_id":
                WeatherType weatherId = plugin.getWeatherManager().getCurrentWeather(world);
                return weatherId.getId();

            case "is_night_cycle":
                return plugin.getTimeManager().isNightCycleActive(world) ? "Yes" : "No";

            default:
                return ""; // Placeholder non reconnu
        }
    }

    /**
     * Gère les placeholders globaux.
     *
     * @param placeholder Le placeholder à traiter.
     * @return La valeur du placeholder, ou une chaîne vide si non reconnu.
     */
    private String handleGlobalPlaceholder(String placeholder) {
        switch (placeholder.toLowerCase()) {
            case "days_per_season":
                return String.valueOf(plugin.getSeasonManager().getDaysPerSeason());

            case "weather_duration_default":
                return String.valueOf(plugin.getWeatherManager().getDefaultWeatherDuration());

            case "weather_change_chance":
                return String.valueOf(plugin.getWeatherManager().getWeatherChangeChance());

            case "version":
                return plugin.getDescription().getVersion();

            default:
                return ""; // Placeholder non reconnu
        }
    }

    /**
     * Enregistre l'expansion de PlaceholderAPI.
     *
     * @return true si l'enregistrement a réussi, false sinon.
     */
    public boolean register() {
        boolean registered = this.register();
        if (registered) {
            logger.info("PlaceholderAPI integration enabled.");
        } else {
            logger.warning("Failed to register PlaceholderAPI expansion.");
        }
        return registered;
    }
}