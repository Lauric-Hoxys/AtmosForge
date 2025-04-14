package fr.hoxys.plugin.atmosforge.managers;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.api.events.SeasonChangeEvent;
import fr.hoxys.plugin.atmosforge.models.Season;
import fr.hoxys.plugin.atmosforge.models.WeatherType;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Gestionnaire des saisons pour chaque monde.
 */
public class SeasonManager {

    private final Main plugin;
    private final Logger logger;

    // Stocke la saison actuelle pour chaque monde
    private final Map<UUID, Season> currentSeasons;

    // Stocke le jour actuel de la saison pour chaque monde (de 1 à N)
    private final Map<UUID, Integer> currentSeasonDays;

    // Nombre de jours dans chaque saison (configurable)
    private int daysPerSeason;

    public SeasonManager(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.currentSeasons = new HashMap<>();
        this.currentSeasonDays = new HashMap<>();

        // Charger la configuration
        loadConfiguration();
    }

    /**
     * Charge la configuration du gestionnaire.
     */
    private void loadConfiguration() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        daysPerSeason = config.getInt("seasons.days_per_season", 30);

        logger.info("Days per season set to: " + daysPerSeason);
    }

    /**
     * Initialise la saison pour un monde.
     * Utilise les données sauvegardées si disponibles, sinon définit par défaut sur le printemps.
     *
     * @param world Le monde à initialiser.
     */
    public void initializeSeason(World world) {
        UUID worldId = world.getUID();
        Map<String, Object> worldData = plugin.getWorldManager().getWorldData(world.getName());

        if (worldData != null && worldData.containsKey("season") && worldData.containsKey("season_day")) {
            String seasonId = (String) worldData.get("season");
            int seasonDay = (int) worldData.get("season_day");

            Season season = Season.fromId(seasonId);
            if (season != null) {
                currentSeasons.put(worldId, season);
                currentSeasonDays.put(worldId, seasonDay);
                logger.info("Loaded saved season for world " + world.getName() + ": " + season.getDisplayName() + ", Day " + seasonDay);
                return;
            }
        }

        // Si aucune donnée sauvegardée n'est trouvée ou si les données sont invalides, initialiser avec les valeurs par défaut
        currentSeasons.put(worldId, Season.SPRING);
        currentSeasonDays.put(worldId, 1);
        logger.info("Initialized default season for world " + world.getName() + ": " + Season.SPRING.getDisplayName() + ", Day 1");
    }

    /**
     * Obtient la saison actuelle pour un monde spécifique.
     *
     * @param world Le monde pour lequel obtenir la saison.
     * @return La saison actuelle du monde.
     */
    public Season getCurrentSeason(World world) {
        UUID worldId = world.getUID();
        if (!currentSeasons.containsKey(worldId)) {
            initializeSeason(world);
        }
        return currentSeasons.get(worldId);
    }

    /**
     * Obtient le jour actuel de la saison pour un monde spécifique.
     *
     * @param world Le monde pour lequel obtenir le jour de la saison.
     * @return Le jour actuel de la saison.
     */
    public int getCurrentSeasonDay(World world) {
        UUID worldId = world.getUID();
        if (!currentSeasonDays.containsKey(worldId)) {
            initializeSeason(world);
        }
        return currentSeasonDays.get(worldId);
    }

    /**
     * Change manuellement la saison d'un monde.
     *
     * @param world Le monde pour lequel changer la saison.
     * @param season La nouvelle saison.
     * @param day Le jour de la saison (optionnel, 1 par défaut).
     * @return true si le changement a réussi, false sinon.
     */
    public boolean setSeason(World world, Season season, int day) {
        if (day < 1 || day > daysPerSeason) {
            day = 1;
        }

        UUID worldId = world.getUID();
        Season oldSeason = currentSeasons.getOrDefault(worldId, Season.SPRING);

        // Déclencher l'événement de changement de saison
        SeasonChangeEvent event = new SeasonChangeEvent(world, oldSeason, season);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        currentSeasons.put(worldId, season);
        currentSeasonDays.put(worldId, day);

        // Mettre à jour la météo pour correspondre à la nouvelle saison
        updateWeatherForSeason(world, season);

        // Sauvegarder les données du monde
        saveWorldData(world);

        logger.info("Season manually changed for world " + world.getName() + " to " + season.getDisplayName() + ", Day " + day);
        return true;
    }

    /**
     * Avance d'un jour la saison pour un monde spécifique.
     * Si le dernier jour de la saison est atteint, passe à la saison suivante.
     *
     * @param world Le monde pour lequel avancer la saison.
     */
    public void advanceDay(World world) {
        UUID worldId = world.getUID();
        if (!currentSeasons.containsKey(worldId) || !currentSeasonDays.containsKey(worldId)) {
            initializeSeason(world);
        }

        Season currentSeason = currentSeasons.get(worldId);
        int currentDay = currentSeasonDays.get(worldId);

        // Augmenter le jour actuel
        currentDay++;

        // Si nous avons atteint la fin de la saison, passer à la suivante
        if (currentDay > daysPerSeason) {
            Season nextSeason = currentSeason.getNext();

            // Déclencher l'événement de changement de saison
            SeasonChangeEvent event = new SeasonChangeEvent(world, currentSeason, nextSeason);
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                currentSeason = nextSeason;
                currentDay = 1;

                // Mettre à jour la météo pour correspondre à la nouvelle saison
                updateWeatherForSeason(world, currentSeason);

                logger.info("Season changed for world " + world.getName() + " to " + currentSeason.getDisplayName());
            }
        }

        currentSeasons.put(worldId, currentSeason);
        currentSeasonDays.put(worldId, currentDay);

        // Sauvegarder les données du monde
        saveWorldData(world);

        logger.fine("Advanced day for world " + world.getName() + " to " + currentSeason.getDisplayName() + ", Day " + currentDay);
    }

    /**
     * Met à jour la météo pour correspondre à la saison spécifiée.
     * Cela définit une météo aléatoire parmi les types courants pour cette saison.
     *
     * @param world Le monde pour lequel mettre à jour la météo.
     * @param season La saison actuelle.
     */
    private void updateWeatherForSeason(World world, Season season) {
        WeatherManager weatherManager = plugin.getWeatherManager();

        // Sélectionner un type de météo aléatoire parmi ceux communs à cette saison
        java.util.List<WeatherType> seasonalWeathers = season.getCommonWeatherTypes();
        if (!seasonalWeathers.isEmpty()) {
            int randomIndex = (int) (Math.random() * seasonalWeathers.size());
            WeatherType randomWeather = seasonalWeathers.get(randomIndex);

            // Définir la météo
            weatherManager.setWeather(world, randomWeather);
        }
    }

    /**
     * Sauvegarde les données de saison pour un monde spécifique.
     *
     * @param world Le monde pour lequel sauvegarder les données.
     */
    private void saveWorldData(World world) {
        UUID worldId = world.getUID();
        if (currentSeasons.containsKey(worldId) && currentSeasonDays.containsKey(worldId)) {
            Season season = currentSeasons.get(worldId);
            int day = currentSeasonDays.get(worldId);

            Map<String, Object> worldData = plugin.getWorldManager().getWorldData(world.getName());
            if (worldData == null) {
                worldData = new HashMap<>();
            }

            worldData.put("season", season.getId());
            worldData.put("season_day", day);

            plugin.getWorldManager().saveWorldData(world.getName(), worldData);
        }
    }

    /**
     * Sauvegarde les données de saison pour tous les mondes.
     */
    public void saveAllWorldData() {
        for (World world : Bukkit.getWorlds()) {
            if (plugin.getConfigManager().isWorldEnabled(world.getName())) {
                saveWorldData(world);
            }
        }
    }

    /**
     * Obtient le nombre de jours dans une saison.
     *
     * @return Le nombre de jours dans une saison.
     */
    public int getDaysPerSeason() {
        return daysPerSeason;
    }

    /**
     * Définit le nombre de jours dans une saison.
     *
     * @param days Le nouveau nombre de jours dans une saison.
     */
    public void setDaysPerSeason(int days) {
        if (days > 0) {
            this.daysPerSeason = days;

            // Mettre à jour la configuration
            FileConfiguration config = plugin.getConfigManager().getConfig();
            config.set("seasons.days_per_season", days);
            plugin.getConfigManager().saveConfig();

            logger.info("Days per season updated to: " + days);
        }
    }
}