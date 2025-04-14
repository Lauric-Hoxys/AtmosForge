package fr.hoxys.plugin.atmosforge;

import fr.hoxys.plugin.atmosforge.commands.AtmosForgeCommand;
import fr.hoxys.plugin.atmosforge.config.ConfigManager;
import fr.hoxys.plugin.atmosforge.config.LanguageManager;
import fr.hoxys.plugin.atmosforge.config.WorldManager;
import fr.hoxys.plugin.atmosforge.integration.PlaceholderAPIHook;
import fr.hoxys.plugin.atmosforge.integration.ProtocolLibHook;
import fr.hoxys.plugin.atmosforge.managers.EffectManager;
import fr.hoxys.plugin.atmosforge.managers.SeasonManager;
import fr.hoxys.plugin.atmosforge.managers.TimeManager;
import fr.hoxys.plugin.atmosforge.managers.WeatherManager;
import fr.hoxys.plugin.atmosforge.listeners.PlayerListener;
import fr.hoxys.plugin.atmosforge.listeners.WorldListener;
import fr.hoxys.plugin.atmosforge.listeners.TimeListener;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Main extends JavaPlugin {

    private static Main instance;
    private Logger logger;

    // Managers
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private WorldManager worldManager;
    private WeatherManager weatherManager;
    private SeasonManager seasonManager;
    private EffectManager effectManager;
    private TimeManager timeManager;

    // Integrations
    private boolean placeholderAPIEnabled = false;
    private boolean protocolLibEnabled = false;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();

        logger.info("Initializing AtmosForge v" + getDescription().getVersion() + "...");

        // Initialize configuration
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Initialize language system
        languageManager = new LanguageManager(this);
        languageManager.loadLanguage();

        // Initialize world manager
        worldManager = new WorldManager(this);
        worldManager.loadWorldData();

        // Initialize managers
        weatherManager = new WeatherManager(this);
        seasonManager = new SeasonManager(this);
        effectManager = new EffectManager(this);
        timeManager = new TimeManager(this);

        // Register commands
        getCommand("atmosforge").setExecutor(new AtmosForgeCommand(this));

        // Register event listeners
        registerListeners();

        // Initialize integrations
        initializeIntegrations();

        // Initialize world weather and seasons
        initializeWorlds();

        // Start time cycle
        timeManager.startTimeCycle();

        logger.info("AtmosForge has been successfully enabled!");
    }

    @Override
    public void onDisable() {
        logger.info("Disabling AtmosForge...");

        // Save world data before shutdown
        worldManager.saveAllWorldData();

        // Stop time cycle
        timeManager.stopTimeCycle();

        logger.info("AtmosForge has been successfully disabled!");
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new WorldListener(this), this);
        pm.registerEvents(new TimeListener(this), this);
    }

    private void initializeIntegrations() {
        // Check for PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
            placeholderAPIEnabled = true;
            logger.info("PlaceholderAPI integration enabled!");
        }

        // Check for ProtocolLib
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            new ProtocolLibHook(this).initialize();
            protocolLibEnabled = true;
            logger.info("ProtocolLib integration enabled!");
        }
    }

    private void initializeWorlds() {
        for (World world : Bukkit.getWorlds()) {
            if (configManager.isWorldEnabled(world.getName())) {
                weatherManager.initializeWeather(world);
                seasonManager.initializeSeason(world);
                logger.info("Initialized weather and season for world: " + world.getName());
            }
        }
    }

    // Getter methods

    public static Main getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public WeatherManager getWeatherManager() {
        return weatherManager;
    }

    public SeasonManager getSeasonManager() {
        return seasonManager;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    public TimeManager getTimeManager() {
        return timeManager;
    }

    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }

    public boolean isProtocolLibEnabled() {
        return protocolLibEnabled;
    }
}