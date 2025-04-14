package fr.hoxys.plugin.atmosforge.config;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.models.Season;
import fr.hoxys.plugin.atmosforge.models.WeatherType;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestionnaire de langue pour AtmosForge.
 */
public class LanguageManager {

    private final Main plugin;
    private final Logger logger;

    private FileConfiguration langConfig;
    private File langFile;
    private String language;

    /**
     * Constructeur du gestionnaire de langue.
     *
     * @param plugin L'instance du plugin principal.
     */
    public LanguageManager(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * Charge le fichier de langue.
     */
    public void loadLanguage() {
        // Obtenir la langue configurée
        String configLang = plugin.getConfigManager().getConfig().getString("language", "en_US");
        this.language = configLang;

        // Dossier des langues
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            boolean created = langDir.mkdirs();
            if (!created) {
                logger.warning("Failed to create language directory!");
            }
        }

        // Fichier de langue
        langFile = new File(langDir, language + ".yml");

        if (!langFile.exists()) {
            // Si le fichier n'existe pas, essayer de l'extraire des ressources
            if (plugin.getResource("lang/" + language + ".yml") != null) {
                plugin.saveResource("lang/" + language + ".yml", false);
                logger.info("Created " + language + " language file from resources.");
            } else {
                // Si la langue demandée n'existe pas, utiliser l'anglais par défaut
                if (!language.equals("en_US")) {
                    logger.warning("Language file for " + language + " not found, using en_US instead.");
                    language = "en_US";
                    langFile = new File(langDir, "en_US.yml");

                    if (!langFile.exists()) {
                        plugin.saveResource("lang/en_US.yml", false);
                        logger.info("Created en_US language file from resources.");
                    }
                }
            }
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);

        // Vérifier si toutes les clés sont présentes en comparant avec le fichier par défaut
        InputStream defaultLangStream = plugin.getResource("lang/" + language + ".yml");
        if (defaultLangStream != null) {
            YamlConfiguration defaultLang = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultLangStream));

            for (String key : defaultLang.getKeys(true)) {
                if (!langConfig.contains(key)) {
                    langConfig.set(key, defaultLang.get(key));
                    logger.info("Added missing language key: " + key);
                }
            }

            // Sauvegarder les modifications
            try {
                langConfig.save(langFile);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Could not save updated language file to " + langFile, e);
            }
        }

        logger.info("Language " + language + " loaded successfully.");
    }

    /**
     * Recharge le fichier de langue.
     */
    public void reloadLanguage() {
        if (langFile == null) {
            loadLanguage();
            return;
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);

        // Recharger les valeurs par défaut
        InputStream defaultLangStream = plugin.getResource("lang/" + language + ".yml");
        if (defaultLangStream != null) {
            YamlConfiguration defaultLang = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultLangStream));
            langConfig.setDefaults(defaultLang);
        }

        logger.info("Language reloaded successfully.");
    }

    /**
     * Obtient un message à partir de la clé de langue.
     *
     * @param key La clé du message.
     * @param replacements Les paires clé-valeur pour les remplacements dans le message.
     * @return Le message traduit.
     */
    public String getMessage(String key, String... replacements) {
        if (langConfig == null) {
            loadLanguage();
        }

        String message = langConfig.getString(key);

        if (message == null) {
            logger.warning("Missing language key: " + key);
            return "Missing language key: " + key;
        }

        // Effectuer les remplacements
        if (replacements != null && replacements.length > 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 < replacements.length) {
                    message = message.replace(replacements[i], replacements[i + 1]);
                }
            }
        }

        // Traduire les codes de couleur
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Obtient le nom localisé d'un type de météo.
     *
     * @param weatherType Le type de météo.
     * @return Le nom localisé du type de météo.
     */
    public String getWeatherName(WeatherType weatherType) {
        if (langConfig == null) {
            loadLanguage();
        }

        String key = "weather." + weatherType.getId() + ".name";
        String name = langConfig.getString(key);

        if (name == null) {
            // Si la clé n'existe pas, retourner l'ID formaté
            return formatId(weatherType.getId());
        }

        return ChatColor.translateAlternateColorCodes('&', name);
    }

    /**
     * Obtient la description localisée d'un type de météo.
     *
     * @param weatherType Le type de météo.
     * @return La description localisée du type de météo.
     */
    public String getWeatherDescription(WeatherType weatherType) {
        if (langConfig == null) {
            loadLanguage();
        }

        String key = "weather." + weatherType.getId() + ".description";
        String description = langConfig.getString(key);

        if (description == null) {
            // Si la clé n'existe pas, retourner un message par défaut
            return "No description available.";
        }

        return ChatColor.translateAlternateColorCodes('&', description);
    }

    /**
     * Obtient le nom localisé d'une saison.
     *
     * @param season La saison.
     * @return Le nom localisé de la saison.
     */
    public String getSeasonName(Season season) {
        if (langConfig == null) {
            loadLanguage();
        }

        String key = "seasons." + season.getId() + ".name";
        String name = langConfig.getString(key);

        if (name == null) {
            // Si la clé n'existe pas, retourner l'ID formaté
            return formatId(season.getId());
        }

        return ChatColor.translateAlternateColorCodes('&', name);
    }

    /**
     * Obtient la description localisée d'une saison.
     *
     * @param season La saison.
     * @return La description localisée de la saison.
     */
    public String getSeasonDescription(Season season) {
        if (langConfig == null) {
            loadLanguage();
        }

        String key = "seasons." + season.getId() + ".description";
        String description = langConfig.getString(key);

        if (description == null) {
            // Si la clé n'existe pas, retourner un message par défaut
            return "No description available.";
        }

        return ChatColor.translateAlternateColorCodes('&', description);
    }

    /**
     * Formate un ID en texte lisible.
     *
     * @param id L'ID à formater.
     * @return L'ID formaté.
     */
    private String formatId(String id) {
        String[] words = id.split("_");
        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                formatted.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return formatted.toString().trim();
    }

    /**
     * Obtient la langue actuellement utilisée.
     *
     * @return Le code de langue actuel.
     */
    public String getLanguage() {
        return language;
    }
}