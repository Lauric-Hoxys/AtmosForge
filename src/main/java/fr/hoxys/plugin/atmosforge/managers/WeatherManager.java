package fr.hoxys.plugin.atmosforge.managers;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.api.events.WeatherChangeEvent;
import fr.hoxys.plugin.atmosforge.models.Season;
import fr.hoxys.plugin.atmosforge.models.WeatherType;
import fr.hoxys.plugin.atmosforge.models.Weather;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Gestionnaire des conditions météorologiques pour chaque monde.
 */
public class WeatherManager {

    private final Main plugin;
    private final Logger logger;
    private final Random random;

    // Stocke la météo actuelle pour chaque monde
    private final Map<UUID, WeatherType> currentWeathers;

    // Stocke la durée restante de la météo actuelle en minutes pour chaque monde
    private final Map<UUID, Integer> weatherDurations;

    // Durée par défaut d'une condition météo en minutes (configurable)
    private int defaultWeatherDuration;

    // Chance de changement météorologique par jour (0-100)
    private int weatherChangeChance;

    public WeatherManager(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.random = new Random();
        this.currentWeathers = new HashMap<>();
        this.weatherDurations = new HashMap<>();

        // Charger la configuration
        loadConfiguration();
    }

    /**
     * Charge la configuration du gestionnaire.
     */
    private void loadConfiguration() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        defaultWeatherDuration = config.getInt("weather.default_duration", 120); // 2 heures par défaut
        weatherChangeChance = config.getInt("weather.change_chance", 30); // 30% par défaut

        logger.info("Default weather duration set to: " + defaultWeatherDuration + " minutes");
        logger.info("Weather change chance set to: " + weatherChangeChance + "%");
    }

    /**
     * Initialise la météo pour un monde.
     * Utilise les données sauvegardées si disponibles, sinon définit une météo aléatoire.
     *
     * @param world Le monde à initialiser.
     */
    public void initializeWeather(World world) {
        UUID worldId = world.getUID();
        Map<String, Object> worldData = plugin.getWorldManager().getWorldData(world.getName());

        if (worldData != null && worldData.containsKey("weather") && worldData.containsKey("weather_duration")) {
            String weatherId = (String) worldData.get("weather");
            int duration = (int) worldData.get("weather_duration");

            WeatherType weather = WeatherType.fromId(weatherId);
            if (weather != null) {
                currentWeathers.put(worldId, weather);
                weatherDurations.put(worldId, duration);
                logger.info("Loaded saved weather for world " + world.getName() + ": " + weather.getId() + ", Duration: " + duration + " minutes");
                return;
            }
        }

        // Si aucune donnée sauvegardée n'est trouvée ou si les données sont invalides, initialiser avec une météo aléatoire
        Season currentSeason = plugin.getSeasonManager().getCurrentSeason(world);
        setRandomWeatherForSeason(world, currentSeason);
    }

    /**
     * Obtient le type de météo actuel pour un monde spécifique.
     *
     * @param world Le monde pour lequel obtenir la météo.
     * @return Le type de météo actuel du monde.
     */
    public WeatherType getCurrentWeather(World world) {
        UUID worldId = world.getUID();
        if (!currentWeathers.containsKey(worldId)) {
            initializeWeather(world);
        }
        return currentWeathers.get(worldId);
    }

    /**
     * Obtient la durée restante de la météo actuelle pour un monde spécifique, en minutes.
     *
     * @param world Le monde pour lequel obtenir la durée de météo.
     * @return La durée restante de la météo en minutes.
     */
    public int getWeatherDuration(World world) {
        UUID worldId = world.getUID();
        if (!weatherDurations.containsKey(worldId)) {
            initializeWeather(world);
        }
        return weatherDurations.get(worldId);
    }

    /**
     * Change manuellement la météo d'un monde.
     *
     * @param world Le monde pour lequel changer la météo.
     * @param weatherType Le nouveau type de météo.
     * @param duration La durée en minutes (optionnel, utilise la valeur par défaut si non spécifiée).
     * @return true si le changement a réussi, false sinon.
     */
    public boolean setWeather(World world, WeatherType weatherType, int duration) {
        if (duration <= 0) {
            duration = defaultWeatherDuration;
        }

        UUID worldId = world.getUID();
        WeatherType oldWeatherType = currentWeathers.getOrDefault(worldId, WeatherType.CLEAR_SKY);

        // Déclencher l'événement de changement de météo
        WeatherChangeEvent event = new WeatherChangeEvent(world, oldWeatherType, weatherType);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        // Appliquer la nouvelle météo
        currentWeathers.put(worldId, weatherType);
        weatherDurations.put(worldId, duration);

        // Mettre à jour les effets de la météo
        applyWeatherEffects(world, weatherType);

        // Mettre à jour la météo de Minecraft si nécessaire
        updateMinecraftWeather(world, weatherType);

        // Sauvegarder les données du monde
        saveWorldData(world);

        logger.info("Weather manually changed for world " + world.getName() + " to " + weatherType.getId() + ", Duration: " + duration + " minutes");
        return true;
    }

    /**
     * Surcharge de la méthode setWeather pour utiliser la durée par défaut.
     *
     * @param world Le monde pour lequel changer la météo.
     * @param weatherType Le nouveau type de météo.
     * @return true si le changement a réussi, false sinon.
     */
    public boolean setWeather(World world, WeatherType weatherType) {
        return setWeather(world, weatherType, defaultWeatherDuration);
    }

    /**
     * Diminue la durée de la météo actuelle pour un monde et change la météo si nécessaire.
     *
     * @param world Le monde pour lequel mettre à jour la météo.
     * @param minutes Nombre de minutes à soustraire.
     */
    public void decreaseWeatherDuration(World world, int minutes) {
        UUID worldId = world.getUID();
        if (!currentWeathers.containsKey(worldId) || !weatherDurations.containsKey(worldId)) {
            initializeWeather(world);
            return;
        }

        int currentDuration = weatherDurations.get(worldId);
        currentDuration -= minutes;

        if (currentDuration <= 0) {
            // La météo a expiré, en choisir une nouvelle
            Season currentSeason = plugin.getSeasonManager().getCurrentSeason(world);
            setRandomWeatherForSeason(world, currentSeason);
        } else {
            weatherDurations.put(worldId, currentDuration);
            saveWorldData(world);
        }
    }

    /**
     * Définit une météo aléatoire adaptée à la saison actuelle.
     *
     * @param world Le monde pour lequel définir la météo.
     * @param season La saison actuelle.
     */
    public void setRandomWeatherForSeason(World world, Season season) {
        List<WeatherType> seasonalWeathers = season.getCommonWeatherTypes();

        if (seasonalWeathers.isEmpty()) {
            // Si aucune météo saisonnière n'est disponible, utiliser le ciel dégagé par défaut
            setWeather(world, WeatherType.CLEAR_SKY);
            return;
        }

        // Sélectionner un type de météo aléatoire
        int randomIndex = random.nextInt(seasonalWeathers.size());
        WeatherType randomWeather = seasonalWeathers.get(randomIndex);

        // Durée aléatoire entre 50% et 150% de la durée par défaut
        int randomDuration = (int) (defaultWeatherDuration * (0.5 + random.nextDouble()));

        setWeather(world, randomWeather, randomDuration);
    }

    /**
     * Tente de changer la météo en fonction de la chance de changement configurable.
     *
     * @param world Le monde pour lequel tenter de changer la météo.
     * @return true si la météo a changé, false sinon.
     */
    public boolean tryChangeWeather(World world) {
        if (random.nextInt(100) < weatherChangeChance) {
            Season currentSeason = plugin.getSeasonManager().getCurrentSeason(world);
            setRandomWeatherForSeason(world, currentSeason);
            return true;
        }
        return false;
    }

    /**
     * Vérifie si un monde a actuellement le cycle de nuit actif.
     *
     * @param world Le monde à vérifier.
     * @return true si le cycle de nuit est actif, false sinon.
     */
    public boolean isNightCycle(World world) {
        UUID worldId = world.getUID();
        if (currentWeathers.containsKey(worldId)) {
            return currentWeathers.get(worldId) == WeatherType.NIGHT_CYCLE;
        }
        return false;
    }

    /**
     * Applique les effets de la météo actuelle à un monde.
     *
     * @param world Le monde auquel appliquer les effets.
     * @param weatherType Le type de météo à appliquer.
     */
    private void applyWeatherEffects(World world, WeatherType weatherType) {
        // Obtenir le gestionnaire d'effets pour appliquer les effets spécifiques au type de météo
        EffectManager effectManager = plugin.getEffectManager();
        effectManager.applyWeatherEffects(world, weatherType);

        // Notifier tous les joueurs du monde du changement de météo
        String weatherMessage = plugin.getLanguageManager().getMessage("weather.change",
                "{weather}", plugin.getLanguageManager().getWeatherName(weatherType));

        for (Player player : world.getPlayers()) {
            player.sendMessage(weatherMessage);
        }
    }

    /**
     * Met à jour la météo Minecraft native pour correspondre au mieux à notre type de météo personnalisé.
     *
     * @param world Le monde à mettre à jour.
     * @param weatherType Le type de météo personnalisé.
     */
    private void updateMinecraftWeather(World world, WeatherType weatherType) {
        boolean hasStorm = false;
        boolean hasThunder = false;

        // Déterminer si la météo personnalisée correspond à une tempête ou à un orage dans Minecraft
        if (weatherType.hasPrecipitation()) {
            hasStorm = true;

            // Vérifier si c'est un orage
            if (weatherType == WeatherType.THUNDERSTORM ||
                    weatherType == WeatherType.LIGHTNING ||
                    weatherType == WeatherType.THUNDER) {
                hasThunder = true;
            }
        }

        // Mettre à jour la météo Minecraft
        world.setStorm(hasStorm);
        world.setThundering(hasThunder);

        // Définir une durée très longue pour éviter que Minecraft ne change la météo par lui-même
        int ticksDuration = 20 * 60 * 60; // 1 heure en ticks
        world.setWeatherDuration(ticksDuration);
        world.setThunderDuration(ticksDuration);
    }

    /**
     * Sauvegarde les données météorologiques pour un monde spécifique.
     *
     * @param world Le monde pour lequel sauvegarder les données.
     */
    private void saveWorldData(World world) {
        UUID worldId = world.getUID();
        if (currentWeathers.containsKey(worldId) && weatherDurations.containsKey(worldId)) {
            WeatherType weather = currentWeathers.get(worldId);
            int duration = weatherDurations.get(worldId);

            Map<String, Object> worldData = plugin.getWorldManager().getWorldData(world.getName());
            if (worldData == null) {
                worldData = new HashMap<>();
            }

            worldData.put("weather", weather.getId());
            worldData.put("weather_duration", duration);

            plugin.getWorldManager().saveWorldData(world.getName(), worldData);
        }
    }

    /**
     * Sauvegarde les données météorologiques pour tous les mondes.
     */
    public void saveAllWorldData() {
        for (World world : Bukkit.getWorlds()) {
            if (plugin.getConfigManager().isWorldEnabled(world.getName())) {
                saveWorldData(world);
            }
        }
    }

    /**
     * Obtient la durée par défaut d'une condition météo en minutes.
     *
     * @return La durée par défaut d'une condition météo.
     */
    public int getDefaultWeatherDuration() {
        return defaultWeatherDuration;
    }

    /**
     * Définit la durée par défaut d'une condition météo en minutes.
     *
     * @param duration La nouvelle durée par défaut.
     */
    public void setDefaultWeatherDuration(int duration) {
        if (duration > 0) {
            this.defaultWeatherDuration = duration;

            // Mettre à jour la configuration
            FileConfiguration config = plugin.getConfigManager().getConfig();
            config.set("weather.default_duration", duration);
            plugin.getConfigManager().saveConfig();

            logger.info("Default weather duration updated to: " + duration + " minutes");
        }
    }

    /**
     * Obtient la chance de changement météorologique par jour (0-100).
     *
     * @return La chance de changement météorologique.
     */
    public int getWeatherChangeChance() {
        return weatherChangeChance;
    }

    /**
     * Définit la chance de changement météorologique par jour (0-100).
     *
     * @param chance La nouvelle chance de changement.
     */
    public void setWeatherChangeChance(int chance) {
        if (chance >= 0 && chance <= 100) {
            this.weatherChangeChance = chance;

            // Mettre à jour la configuration
            FileConfiguration config = plugin.getConfigManager().getConfig();
            config.set("weather.change_chance", chance);
            plugin.getConfigManager().saveConfig();

            logger.info("Weather change chance updated to: " + chance + "%");
        }
    }
}