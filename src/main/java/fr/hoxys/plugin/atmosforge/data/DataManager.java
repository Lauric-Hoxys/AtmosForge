package fr.hoxys.plugin.atmosforge.data;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.models.Season;
import fr.hoxys.plugin.atmosforge.models.WeatherType;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestionnaire de données pour AtmosForge.
 * Cette classe s'occupe de la persistance des données de météo et de saison pour chaque monde.
 */
public class DataManager {

    private final Main plugin;
    private final Logger logger;
    private final FileManager fileManager;

    // Cache des données par monde
    private final Map<String, Map<String, Object>> worldDataCache;

    // Fichier de données
    private File dataFile;
    private FileConfiguration dataConfig;

    /**
     * Constructeur du gestionnaire de données.
     *
     * @param plugin L'instance du plugin principal.
     */
    public DataManager(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.fileManager = new FileManager(plugin);
        this.worldDataCache = new HashMap<>();

        // Initialiser le fichier de données
        initDataFile();
    }

    /**
     * Initialise le fichier de données.
     */
    private void initDataFile() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");

        // Créer le fichier s'il n'existe pas
        if (!dataFile.exists()) {
            try {
                if (dataFile.createNewFile()) {
                    logger.info("Created new data file.");
                } else {
                    logger.warning("Failed to create data file!");
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Could not create data file", e);
            }
        }

        // Charger la configuration
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        // Charger le cache des données
        loadCache();
    }

    /**
     * Charge les données depuis le fichier vers le cache.
     */
    private void loadCache() {
        worldDataCache.clear();

        // Obtenir la section des mondes
        ConfigurationSection worldsSection = dataConfig.getConfigurationSection("worlds");
        if (worldsSection == null) {
            return;
        }

        // Parcourir tous les mondes
        for (String worldName : worldsSection.getKeys(false)) {
            ConfigurationSection worldSection = worldsSection.getConfigurationSection(worldName);
            if (worldSection != null) {
                Map<String, Object> worldData = new HashMap<>();

                // Charger toutes les clés du monde
                for (String key : worldSection.getKeys(false)) {
                    worldData.put(key, worldSection.get(key));
                }

                worldDataCache.put(worldName, worldData);
            }
        }

        logger.info("Loaded data for " + worldDataCache.size() + " worlds.");
    }

    /**
     * Sauvegarde le cache des données dans le fichier.
     */
    public void saveCache() {
        // Réinitialiser la section des mondes
        dataConfig.set("worlds", null);

        // Sauvegarder les données de chaque monde
        for (Map.Entry<String, Map<String, Object>> entry : worldDataCache.entrySet()) {
            String worldName = entry.getKey();
            Map<String, Object> worldData = entry.getValue();

            for (Map.Entry<String, Object> dataEntry : worldData.entrySet()) {
                dataConfig.set("worlds." + worldName + "." + dataEntry.getKey(), dataEntry.getValue());
            }
        }

        // Sauvegarder dans le fichier
        try {
            dataConfig.save(dataFile);
            logger.fine("Saved data for " + worldDataCache.size() + " worlds.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save data to " + dataFile, e);
        }
    }

    /**
     * Obtient les données pour un monde spécifique.
     *
     * @param worldName Le nom du monde.
     * @return Une copie des données du monde, ou une nouvelle Map si aucune donnée n'existe.
     */
    public Map<String, Object> getWorldData(String worldName) {
        Map<String, Object> worldData = worldDataCache.get(worldName);

        // Créer une nouvelle Map si aucune donnée n'existe
        if (worldData == null) {
            return new HashMap<>();
        }

        // Retourner une copie pour éviter les modifications directes du cache
        return new HashMap<>(worldData);
    }

    /**
     * Met à jour les données pour un monde spécifique.
     *
     * @param worldName Le nom du monde.
     * @param worldData Les nouvelles données du monde.
     */
    public void setWorldData(String worldName, Map<String, Object> worldData) {
        worldDataCache.put(worldName, new HashMap<>(worldData));

        // Sauvegarder immédiatement les changements
        saveCache();
    }

    /**
     * Supprime les données pour un monde spécifique.
     *
     * @param worldName Le nom du monde.
     */
    public void removeWorldData(String worldName) {
        worldDataCache.remove(worldName);

        // Sauvegarder immédiatement les changements
        saveCache();

        logger.info("Removed data for world: " + worldName);
    }

    /**
     * Obtient une valeur spécifique pour un monde.
     *
     * @param worldName Le nom du monde.
     * @param key La clé de la valeur à obtenir.
     * @return La valeur associée à la clé, ou null si non trouvée.
     */
    public Object getWorldValue(String worldName, String key) {
        Map<String, Object> worldData = worldDataCache.get(worldName);
        if (worldData != null) {
            return worldData.get(key);
        }
        return null;
    }

    /**
     * Définit une valeur spécifique pour un monde.
     *
     * @param worldName Le nom du monde.
     * @param key La clé de la valeur à définir.
     * @param value La valeur à définir.
     */
    public void setWorldValue(String worldName, String key, Object value) {
        Map<String, Object> worldData = worldDataCache.get(worldName);
        if (worldData == null) {
            worldData = new HashMap<>();
            worldDataCache.put(worldName, worldData);
        }

        worldData.put(key, value);

        // Sauvegarder immédiatement les changements
        saveCache();
    }

    /**
     * Sauvegarde la saison actuelle pour un monde.
     *
     * @param world Le monde.
     * @param season La saison.
     * @param day Le jour de la saison.
     */
    public void saveSeasonData(World world, Season season, int day) {
        String worldName = world.getName();
        Map<String, Object> worldData = getWorldData(worldName);

        worldData.put("season", season.getId());
        worldData.put("season_day", day);

        setWorldData(worldName, worldData);
    }

    /**
     * Sauvegarde la météo actuelle pour un monde.
     *
     * @param world Le monde.
     * @param weatherType Le type de météo.
     * @param duration La durée en minutes.
     */
    public void saveWeatherData(World world, WeatherType weatherType, int duration) {
        String worldName = world.getName();
        Map<String, Object> worldData = getWorldData(worldName);

        worldData.put("weather", weatherType.getId());
        worldData.put("weather_duration", duration);

        setWorldData(worldName, worldData);
    }

    /**
     * Obtient la saison sauvegardée pour un monde.
     *
     * @param world Le monde.
     * @return La saison sauvegardée, ou Spring par défaut si aucune saison n'est trouvée.
     */
    public Season getSavedSeason(World world) {
        String worldName = world.getName();
        String seasonId = (String) getWorldValue(worldName, "season");

        if (seasonId != null) {
            Season season = Season.fromId(seasonId);
            if (season != null) {
                return season;
            }
        }

        return Season.SPRING; // Valeur par défaut
    }

    /**
     * Obtient le jour de saison sauvegardé pour un monde.
     *
     * @param world Le monde.
     * @return Le jour de saison sauvegardé, ou 1 par défaut si aucun jour n'est trouvé.
     */
    public int getSavedSeasonDay(World world) {
        String worldName = world.getName();
        Object day = getWorldValue(worldName, "season_day");

        if (day instanceof Integer) {
            return (Integer) day;
        }

        return 1; // Valeur par défaut
    }

    /**
     * Obtient le type de météo sauvegardé pour un monde.
     *
     * @param world Le monde.
     * @return Le type de météo sauvegardé, ou CLEAR_SKY par défaut si aucun type n'est trouvé.
     */
    public WeatherType getSavedWeather(World world) {
        String worldName = world.getName();
        String weatherId = (String) getWorldValue(worldName, "weather");

        if (weatherId != null) {
            WeatherType weatherType = WeatherType.fromId(weatherId);
            if (weatherType != null) {
                return weatherType;
            }
        }

        return WeatherType.CLEAR_SKY; // Valeur par défaut
    }

    /**
     * Obtient la durée de météo sauvegardée pour un monde.
     *
     * @param world Le monde.
     * @param defaultDuration La durée par défaut à utiliser si aucune durée n'est trouvée.
     * @return La durée de météo sauvegardée, ou la durée par défaut si aucune durée n'est trouvée.
     */
    public int getSavedWeatherDuration(World world, int defaultDuration) {
        String worldName = world.getName();
        Object duration = getWorldValue(worldName, "weather_duration");

        if (duration instanceof Integer) {
            return (Integer) duration;
        }

        return defaultDuration; // Valeur par défaut
    }

    /**
     * Sauvegarde la météo précédente lors du cycle de nuit.
     *
     * @param world Le monde.
     * @param weatherType Le type de météo précédent.
     */
    public void savePreviousWeather(World world, WeatherType weatherType) {
        String worldName = world.getName();
        Map<String, Object> worldData = getWorldData(worldName);

        worldData.put("previous_weather", weatherType.getId());

        setWorldData(worldName, worldData);
    }

    /**
     * Obtient la météo précédente lors du cycle de nuit.
     *
     * @param world Le monde.
     * @return Le type de météo précédent, ou null si aucun n'est trouvé.
     */
    public WeatherType getPreviousWeather(World world) {
        String worldName = world.getName();
        String weatherId = (String) getWorldValue(worldName, "previous_weather");

        if (weatherId != null) {
            return WeatherType.fromId(weatherId);
        }

        return null;
    }

    /**
     * Supprime la météo précédente lors du cycle de nuit.
     *
     * @param world Le monde.
     */
    public void removePreviousWeather(World world) {
        String worldName = world.getName();
        Map<String, Object> worldData = getWorldData(worldName);

        worldData.remove("previous_weather");

        setWorldData(worldName, worldData);
    }

    /**
     * Sauvegarde toutes les données en cache.
     * Cette méthode doit être appelée lors de l'arrêt du serveur.
     */
    public void saveAll() {
        saveCache();
        logger.info("Saved all data to file.");
    }

    /**
     * Recharge toutes les données depuis le fichier.
     * Cette méthode peut être utilisée lors du rechargement du plugin.
     */
    public void reloadData() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadCache();
        logger.info("Reloaded all data from file.");
    }

    /**
     * Permet d'accéder directement au fichier de configuration des données.
     *
     * @return La configuration des données.
     */
    public FileConfiguration getDataConfig() {
        return dataConfig;
    }

    /**
     * Obtient le gestionnaire de fichiers.
     *
     * @return Le gestionnaire de fichiers.
     */
    public FileManager getFileManager() {
        return fileManager;
    }
}