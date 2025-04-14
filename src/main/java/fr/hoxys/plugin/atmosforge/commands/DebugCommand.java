package fr.hoxys.plugin.atmosforge.commands;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.models.Season;
import fr.hoxys.plugin.atmosforge.models.WeatherType;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Classe de commande pour le débogage d'AtmosForge.
 * Cette commande est destinée aux administrateurs et aux développeurs.
 */
public class DebugCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public DebugCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!hasPermission(sender, "atmosforge.admin.debug")) return true;

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /atmosforge-debug <status|toggle|dump|reset|reload-config|test> [options]");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "status":
                handleStatusCommand(sender);
                break;

            case "toggle":
                handleToggleCommand(sender, args);
                break;

            case "dump":
                handleDumpCommand(sender, args);
                break;

            case "reset":
                handleResetCommand(sender, args);
                break;

            case "reload-config":
                handleReloadConfigCommand(sender);
                break;

            case "test":
                handleTestCommand(sender, args);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown debug subcommand. Use /atmosforge-debug for help.");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!sender.hasPermission("atmosforge.admin.debug")) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("status", "toggle", "dump", "reset", "reload-config", "test");
            return filterCompletions(subCommands, args[0]);
        } else if (args.length >= 2) {
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "toggle":
                    if (args.length == 2) {
                        return filterCompletions(Arrays.asList("debug", "effects", "particles", "sounds", "damage", "messages"), args[1]);
                    }
                    break;

                case "dump":
                    if (args.length == 2) {
                        return filterCompletions(Arrays.asList("worlds", "seasons", "weather", "effects", "config", "all"), args[1]);
                    }
                    break;

                case "reset":
                    if (args.length == 2) {
                        return filterCompletions(Arrays.asList("world", "effects", "tasks"), args[1]);
                    } else if (args.length == 3 && args[1].equalsIgnoreCase("world")) {
                        return filterCompletions(getWorldNames(), args[2]);
                    }
                    break;

                case "test":
                    if (args.length == 2) {
                        return filterCompletions(Arrays.asList("weather", "season", "effect", "particles", "sounds", "tps"), args[1]);
                    } else if (args.length == 3) {
                        if (args[1].equalsIgnoreCase("weather")) {
                            return filterCompletions(getWeatherTypeIds(), args[2]);
                        } else if (args[1].equalsIgnoreCase("season")) {
                            return filterCompletions(getSeasonIds(), args[2]);
                        }
                    }
                    break;
            }
        }

        return completions;
    }

    /**
     * Filtre les complétions en fonction de l'entrée partielle.
     *
     * @param options Les options disponibles.
     * @param input L'entrée partielle.
     * @return Une liste filtrée des options.
     */
    private List<String> filterCompletions(List<String> options, String input) {
        String lowercaseInput = input.toLowerCase();
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(lowercaseInput))
                .collect(Collectors.toList());
    }

    /**
     * Obtient la liste des noms de monde.
     *
     * @return Une liste des noms de monde.
     */
    private List<String> getWorldNames() {
        return Bukkit.getWorlds().stream()
                .filter(world -> plugin.getConfigManager().isWorldEnabled(world.getName()))
                .map(World::getName)
                .collect(Collectors.toList());
    }

    /**
     * Obtient la liste des identifiants de type de météo.
     *
     * @return Une liste des identifiants de type de météo.
     */
    private List<String> getWeatherTypeIds() {
        return Arrays.stream(WeatherType.values())
                .map(WeatherType::getId)
                .collect(Collectors.toList());
    }

    /**
     * Obtient la liste des identifiants de saison.
     *
     * @return Une liste des identifiants de saison.
     */
    private List<String> getSeasonIds() {
        return Arrays.stream(Season.values())
                .map(Season::getId)
                .collect(Collectors.toList());
    }

    /**
     * Vérifie si un expéditeur a une permission spécifique.
     *
     * @param sender L'expéditeur de la commande.
     * @param permission La permission à vérifier.
     * @return true si l'expéditeur a la permission, false sinon.
     */
    private boolean hasPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return false;
        }
        return true;
    }

    /**
     * Gère la commande d'affichage de l'état du débogage.
     *
     * @param sender L'expéditeur de la commande.
     */
    private void handleStatusCommand(CommandSender sender) {
        boolean debugEnabled = plugin.getConfigManager().getConfig().getBoolean("debug.enabled", false);
        int maxParticlesPerPlayer = plugin.getConfigManager().getConfig().getInt("debug.max_particles_per_player", 500);

        sender.sendMessage(ChatColor.GOLD + "=== AtmosForge Debug Status ===");
        sender.sendMessage(ChatColor.YELLOW + "Debug mode: " +
                (debugEnabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        sender.sendMessage(ChatColor.YELLOW + "Max particles per player: " + ChatColor.WHITE + maxParticlesPerPlayer);

        // Afficher le nombre de mondes activés
        List<String> enabledWorlds = plugin.getConfigManager().getEnabledWorlds();
        if (enabledWorlds.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Enabled worlds: " + ChatColor.WHITE + "All worlds");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Enabled worlds: " + ChatColor.WHITE +
                    String.join(", ", enabledWorlds));
        }

        // Afficher les intégrations
        sender.sendMessage(ChatColor.YELLOW + "PlaceholderAPI integration: " +
                (plugin.isPlaceholderAPIEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        sender.sendMessage(ChatColor.YELLOW + "ProtocolLib integration: " +
                (plugin.isProtocolLibEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));

        // Afficher des informations sur les tâches
        sender.sendMessage(ChatColor.YELLOW + "Time check interval: " + ChatColor.WHITE +
                plugin.getTimeManager().getTimeCheckInterval() + " ticks");
        sender.sendMessage(ChatColor.YELLOW + "Weather change chance: " + ChatColor.WHITE +
                plugin.getWeatherManager().getWeatherChangeChance() + "%");
        sender.sendMessage(ChatColor.YELLOW + "Default weather duration: " + ChatColor.WHITE +
                plugin.getWeatherManager().getDefaultWeatherDuration() + " minutes");
        sender.sendMessage(ChatColor.YELLOW + "Days per season: " + ChatColor.WHITE +
                plugin.getSeasonManager().getDaysPerSeason() + " days");
    }

    /**
     * Gère la commande d'activation/désactivation du débogage.
     *
     * @param sender L'expéditeur de la commande.
     * @param args Les arguments de la commande.
     */
    private void handleToggleCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /atmosforge-debug toggle <option>");
            sender.sendMessage(ChatColor.RED + "Options: debug, effects, particles, sounds, damage, messages");
            return;
        }

        String option = args[1].toLowerCase();
        boolean currentValue;

        switch (option) {
            case "debug":
                currentValue = plugin.getConfigManager().getConfig().getBoolean("debug.enabled", false);
                plugin.getConfigManager().getConfig().set("debug.enabled", !currentValue);
                sender.sendMessage(ChatColor.YELLOW + "Debug mode " +
                        (!currentValue ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
                break;

            case "effects":
                currentValue = plugin.getConfigManager().getConfig().getBoolean("weather.visual_effects", true);
                plugin.getConfigManager().getConfig().set("weather.visual_effects", !currentValue);
                sender.sendMessage(ChatColor.YELLOW + "Visual effects " +
                        (!currentValue ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
                break;

            case "particles":
                currentValue = plugin.getConfigManager().getConfig().getBoolean("weather.visual_effects", true);
                plugin.getConfigManager().getConfig().set("weather.visual_effects", !currentValue);
                sender.sendMessage(ChatColor.YELLOW + "Particle effects " +
                        (!currentValue ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
                break;

            case "sounds":
                currentValue = plugin.getConfigManager().getConfig().getBoolean("weather.sound_effects", true);
                plugin.getConfigManager().getConfig().set("weather.sound_effects", !currentValue);
                sender.sendMessage(ChatColor.YELLOW + "Sound effects " +
                        (!currentValue ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
                break;

            case "damage":
                currentValue = plugin.getConfigManager().getConfig().getBoolean("weather.player_effects", true);
                plugin.getConfigManager().getConfig().set("weather.player_effects", !currentValue);
                sender.sendMessage(ChatColor.YELLOW + "Player effects (damage) " +
                        (!currentValue ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
                break;

            case "messages":
                currentValue = true; // Pas d'option de configuration pour cela, mais pourrait être ajouté
                sender.sendMessage(ChatColor.YELLOW + "Debug messages are always enabled in debug mode");
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown toggle option: " + option);
                sender.sendMessage(ChatColor.RED + "Options: debug, effects, particles, sounds, damage, messages");
                return;
        }

        // Sauvegarder la configuration
        plugin.getConfigManager().saveConfig();
    }

    /**
     * Gère la commande de dump des informations de débogage.
     *
     * @param sender L'expéditeur de la commande.
     * @param args Les arguments de la commande.
     */
    private void handleDumpCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /atmosforge-debug dump <target>");
            sender.sendMessage(ChatColor.RED + "Targets: worlds, seasons, weather, effects, config, all");
            return;
        }

        String target = args[1].toLowerCase();

        switch (target) {
            case "worlds":
                dumpWorlds(sender);
                break;

            case "seasons":
                dumpSeasons(sender);
                break;

            case "weather":
                dumpWeather(sender);
                break;

            case "effects":
                dumpEffects(sender);
                break;

            case "config":
                dumpConfig(sender);
                break;

            case "all":
                dumpWorlds(sender);
                dumpSeasons(sender);
                dumpWeather(sender);
                dumpEffects(sender);
                dumpConfig(sender);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown dump target: " + target);
                sender.sendMessage(ChatColor.RED + "Targets: worlds, seasons, weather, effects, config, all");
                break;
        }
    }

    /**
     * Dump des informations sur les mondes.
     *
     * @param sender L'expéditeur de la commande.
     */
    private void dumpWorlds(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Worlds Dump ===");

        // Parcourir tous les mondes
        for (World world : Bukkit.getWorlds()) {
            boolean enabled = plugin.getConfigManager().isWorldEnabled(world.getName());
            ChatColor worldColor = enabled ? ChatColor.GREEN : ChatColor.RED;

            sender.sendMessage(worldColor + "World: " + world.getName());
            if (enabled) {
                Season currentSeason = plugin.getSeasonManager().getCurrentSeason(world);
                int currentDay = plugin.getSeasonManager().getCurrentSeasonDay(world);
                WeatherType currentWeather = plugin.getWeatherManager().getCurrentWeather(world);
                int weatherDuration = plugin.getWeatherManager().getWeatherDuration(world);
                boolean isNightCycle = plugin.getTimeManager().isNightCycleActive(world);

                sender.sendMessage(ChatColor.YELLOW + "  Season: " + ChatColor.WHITE +
                        currentSeason.getId() + " (Day " + currentDay + ")");
                sender.sendMessage(ChatColor.YELLOW + "  Weather: " + ChatColor.WHITE +
                        currentWeather.getId() + " (Duration: " + weatherDuration + " minutes)");
                sender.sendMessage(ChatColor.YELLOW + "  Night Cycle: " + ChatColor.WHITE +
                        (isNightCycle ? "Active" : "Inactive"));
                sender.sendMessage(ChatColor.YELLOW + "  Time: " + ChatColor.WHITE +
                        world.getTime() + " ticks");
            } else {
                sender.sendMessage(ChatColor.RED + "  AtmosForge disabled for this world");
            }
        }
    }

    /**
     * Dump des informations sur les saisons.
     *
     * @param sender L'expéditeur de la commande.
     */
    private void dumpSeasons(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Seasons Dump ===");

        int daysPerSeason = plugin.getSeasonManager().getDaysPerSeason();
        sender.sendMessage(ChatColor.YELLOW + "Days per season: " + ChatColor.WHITE + daysPerSeason);

        for (Season season : Season.values()) {
            sender.sendMessage(ChatColor.GREEN + "Season: " + season.getId());
            sender.sendMessage(ChatColor.YELLOW + "  Display Name: " + ChatColor.WHITE +
                    plugin.getLanguageManager().getSeasonName(season));
            sender.sendMessage(ChatColor.YELLOW + "  Order: " + ChatColor.WHITE + season.getOrder());

            // Afficher les types de météo communs
            List<String> commonWeatherNames = season.getCommonWeatherTypes().stream()
                    .map(WeatherType::getId)
                    .collect(Collectors.toList());

            sender.sendMessage(ChatColor.YELLOW + "  Common weather types: " + ChatColor.WHITE +
                    String.join(", ", commonWeatherNames));
        }
    }

    /**
     * Dump des informations sur la météo.
     *
     * @param sender L'expéditeur de la commande.
     */
    private void dumpWeather(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Weather Dump ===");

        int defaultDuration = plugin.getWeatherManager().getDefaultWeatherDuration();
        int changeChance = plugin.getWeatherManager().getWeatherChangeChance();

        sender.sendMessage(ChatColor.YELLOW + "Default duration: " + ChatColor.WHITE + defaultDuration + " minutes");
        sender.sendMessage(ChatColor.YELLOW + "Change chance: " + ChatColor.WHITE + changeChance + "%");

        // Parcourir tous les mondes activés
        for (World world : Bukkit.getWorlds()) {
            if (plugin.getConfigManager().isWorldEnabled(world.getName())) {
                WeatherType currentWeather = plugin.getWeatherManager().getCurrentWeather(world);
                int duration = plugin.getWeatherManager().getWeatherDuration(world);

                sender.sendMessage(ChatColor.GREEN + "World: " + world.getName());
                sender.sendMessage(ChatColor.YELLOW + "  Current Weather: " + ChatColor.WHITE +
                        currentWeather.getId());
                sender.sendMessage(ChatColor.YELLOW + "  Duration: " + ChatColor.WHITE +
                        duration + " minutes remaining");
                sender.sendMessage(ChatColor.YELLOW + "  Has Precipitation: " + ChatColor.WHITE +
                        (currentWeather.hasPrecipitation() ? "Yes" : "No"));
                sender.sendMessage(ChatColor.YELLOW + "  Is Dangerous: " + ChatColor.WHITE +
                        (currentWeather.isDangerous() ? "Yes" : "No"));
                sender.sendMessage(ChatColor.YELLOW + "  Category: " + ChatColor.WHITE +
                        currentWeather.getCategory().name());
            }
        }
    }

    /**
     * Dump des informations sur les effets.
     *
     * @param sender L'expéditeur de la commande.
     */
    private void dumpEffects(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Effects Dump ===");

        boolean visualEffects = plugin.getConfigManager().getConfig().getBoolean("weather.visual_effects", true);
        boolean soundEffects = plugin.getConfigManager().getConfig().getBoolean("weather.sound_effects", true);
        boolean playerEffects = plugin.getConfigManager().getConfig().getBoolean("weather.player_effects", true);
        boolean blockEffects = plugin.getConfigManager().getConfig().getBoolean("weather.block_effects", true);

        sender.sendMessage(ChatColor.YELLOW + "Visual effects: " +
                (visualEffects ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        sender.sendMessage(ChatColor.YELLOW + "Sound effects: " +
                (soundEffects ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        sender.sendMessage(ChatColor.YELLOW + "Player effects: " +
                (playerEffects ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        sender.sendMessage(ChatColor.YELLOW + "Block effects: " +
                (blockEffects ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));

        int frostbiteDamageInterval = plugin.getEffectManager().getFrostbiteDamageInterval();
        int solderingIronDamageInterval = plugin.getEffectManager().getSolderingIronDamageInterval();

        sender.sendMessage(ChatColor.YELLOW + "Frostbite damage interval: " + ChatColor.WHITE +
                frostbiteDamageInterval + " ticks");
        sender.sendMessage(ChatColor.YELLOW + "Soldering Iron damage interval: " + ChatColor.WHITE +
                solderingIronDamageInterval + " ticks");
    }

    /**
     * Dump des informations sur la configuration.
     *
     * @param sender L'expéditeur de la commande.
     */
    private void dumpConfig(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Config Dump ===");

        // Obtenir la configuration
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfigManager().getConfig();

        // Afficher les clés principales
        for (String key : config.getKeys(false)) {
            sender.sendMessage(ChatColor.GREEN + key + ":");

            // Si la clé est une section, afficher ses sous-clés
            if (config.isConfigurationSection(key)) {
                org.bukkit.configuration.ConfigurationSection section = config.getConfigurationSection(key);
                for (String subKey : section.getKeys(true)) {
                    Object value = section.get(subKey);
                    sender.sendMessage(ChatColor.YELLOW + "  " + subKey + ": " + ChatColor.WHITE + value);
                }
            } else {
                Object value = config.get(key);
                sender.sendMessage(ChatColor.YELLOW + "  " + key + ": " + ChatColor.WHITE + value);
            }
        }
    }

    /**
     * Gère la commande de réinitialisation.
     *
     * @param sender L'expéditeur de la commande.
     * @param args Les arguments de la commande.
     */
    private void handleResetCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /atmosforge-debug reset <target> [options]");
            sender.sendMessage(ChatColor.RED + "Targets: world, effects, tasks");
            return;
        }

        String target = args[1].toLowerCase();

        switch (target) {
            case "world":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /atmosforge-debug reset world <world_name>");
                    return;
                }
                String worldName = args[2];
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    sender.sendMessage(ChatColor.RED + "World '" + worldName + "' not found.");
                    return;
                }
                resetWorld(sender, world);
                break;

            case "effects":
                resetEffects(sender);
                break;

            case "tasks":
                resetTasks(sender);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown reset target: " + target);
                sender.sendMessage(ChatColor.RED + "Targets: world, effects, tasks");
                break;
        }
    }

    /**
     * Réinitialise un monde.
     *
     * @param sender L'expéditeur de la commande.
     * @param world Le monde à réinitialiser.
     */
    private void resetWorld(CommandSender sender, World world) {
        // Vérifier si le monde est activé
        if (!plugin.getConfigManager().isWorldEnabled(world.getName())) {
            sender.sendMessage(ChatColor.RED + "AtmosForge is not enabled for world '" + world.getName() + "'.");
            return;
        }

        // Supprimer les données du monde
        plugin.getWorldManager().removeWorldData(world.getName());

        // Réinitialiser la météo et la saison
        plugin.getWeatherManager().initializeWeather(world);
        plugin.getSeasonManager().initializeSeason(world);

        // Annuler tous les effets pour ce monde
        plugin.getEffectManager().cancelWorldEffects(world);

        sender.sendMessage(ChatColor.GREEN + "Successfully reset world '" + world.getName() + "'.");
    }

    /**
     * Réinitialise tous les effets.
     *
     * @param sender L'expéditeur de la commande.
     */
    private void resetEffects(CommandSender sender) {
        // Parcourir tous les mondes activés
        for (World world : Bukkit.getWorlds()) {
            if (plugin.getConfigManager().isWorldEnabled(world.getName())) {
                // Annuler tous les effets pour ce monde
                plugin.getEffectManager().cancelWorldEffects(world);

                // Réappliquer les effets météorologiques
                WeatherType currentWeather = plugin.getWeatherManager().getCurrentWeather(world);
                plugin.getEffectManager().applyWeatherEffects(world, currentWeather);
            }
        }

        sender.sendMessage(ChatColor.GREEN + "Successfully reset all effects.");
    }

    /**
     * Réinitialise toutes les tâches.
     *
     * @param sender L'expéditeur de la commande.
     */
    private void resetTasks(CommandSender sender) {
        // Arrêter le cycle de temps
        plugin.getTimeManager().stopTimeCycle();

        // Redémarrer le cycle de temps
        plugin.getTimeManager().startTimeCycle();

        sender.sendMessage(ChatColor.GREEN + "Successfully reset all scheduled tasks.");
    }

    /**
     * Gère la commande de rechargement de la configuration.
     *
     * @param sender L'expéditeur de la commande.
     */
    private void handleReloadConfigCommand(CommandSender sender) {
        // Recharger la configuration
        plugin.getConfigManager().reloadConfig();
        plugin.getLanguageManager().reloadLanguage();

        sender.sendMessage(ChatColor.GREEN + "Successfully reloaded configuration and language files.");
    }

    /**
     * Gère la commande de test.
     *
     * @param sender L'expéditeur de la commande.
     * @param args Les arguments de la commande.
     */
    private void handleTestCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /atmosforge-debug test <target> [options]");
            sender.sendMessage(ChatColor.RED + "Targets: weather, season, effect, particles, sounds, tps");
            return;
        }

        String target = args[1].toLowerCase();

        switch (target) {
            case "weather":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /atmosforge-debug test weather <weather_id>");
                    return;
                }
                testWeather(sender, args[2]);
                break;

            case "season":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /atmosforge-debug test season <season_id>");
                    return;
                }
                testSeason(sender, args[2]);
                break;

            case "effect":
                testEffect(sender);
                break;

            case "particles":
                testParticles(sender);
                break;

            case "sounds":
                testSounds(sender);
                break;

            case "tps":
                testTPS(sender);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown test target: " + target);
                sender.sendMessage(ChatColor.RED + "Targets: weather, season, effect, particles, sounds, tps");
                break;
        }
    }

    /**
     * Teste un type de météo spécifique.
     *
     * @param sender L'expéditeur de la commande.
     * @param weatherId L'identifiant du type de météo à tester.
     */
    private void testWeather(CommandSender sender, String weatherId) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return;
        }

        Player player = (Player) sender;
        World world = player.getWorld();

        // Vérifier si le monde est activé
        if (!plugin.getConfigManager().isWorldEnabled(world.getName())) {
            sender.sendMessage(ChatColor.RED + "AtmosForge is not enabled for world '" + world.getName() + "'.");
            return;
        }

        // Obtenir le type de météo
        WeatherType weatherType = WeatherType.fromId(weatherId);
        if (weatherType == null) {
            sender.sendMessage(ChatColor.RED + "Weather type '" + weatherId + "' not recognized.");
            sender.sendMessage(ChatColor.RED + "Use /weather list to see all available weather types.");
            return;
        }

        // Définir la météo temporairement (5 minutes)
        boolean success = plugin.getWeatherManager().setWeather(world, weatherType, 5);
        if (success) {
            String weatherName = plugin.getLanguageManager().getWeatherName(weatherType);
            sender.sendMessage(ChatColor.GREEN + "Testing weather '" + weatherName +
                    "' for 5 minutes in world '" + world.getName() + "'.");
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to set test weather.");
        }
    }

    /**
     * Teste une saison spécifique.
     *
     * @param sender L'expéditeur de la commande.
     * @param seasonId L'identifiant de la saison à tester.
     */
    private void testSeason(CommandSender sender, String seasonId) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return;
        }

        Player player = (Player) sender;
        World world = player.getWorld();

        // Vérifier si le monde est activé
        if (!plugin.getConfigManager().isWorldEnabled(world.getName())) {
            sender.sendMessage(ChatColor.RED + "AtmosForge is not enabled for world '" + world.getName() + "'.");
            return;
        }

        // Obtenir la saison
        Season season = Season.fromId(seasonId);
        if (season == null) {
            sender.sendMessage(ChatColor.RED + "Season '" + seasonId + "' not recognized.");
            sender.sendMessage(ChatColor.RED + "Available seasons: spring, summer, autumn, winter");
            return;
        }

        // Sauvegarder la saison actuelle
        Season currentSeason = plugin.getSeasonManager().getCurrentSeason(world);
        int currentDay = plugin.getSeasonManager().getCurrentSeasonDay(world);

        // Définir la saison temporairement
        boolean success = plugin.getSeasonManager().setSeason(world, season, 15);
        if (success) {
            String seasonName = plugin.getLanguageManager().getSeasonName(season);
            sender.sendMessage(ChatColor.GREEN + "Testing season '" + seasonName +
                    "' (day 15) in world '" + world.getName() + "'.");

            // Planifier le retour à la saison précédente
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getSeasonManager().setSeason(world, currentSeason, currentDay);
                player.sendMessage(ChatColor.GREEN + "Restored original season '" +
                        plugin.getLanguageManager().getSeasonName(currentSeason) + "' (day " + currentDay + ").");
            }, 20 * 60 * 5); // 5 minutes

            sender.sendMessage(ChatColor.YELLOW + "The original season will be restored in 5 minutes.");
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to set test season.");
        }
    }

    /**
     * Teste les effets météorologiques.
     *
     * @param sender L'expéditeur de la commande.
     */
    private void testEffect(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return;
        }

        Player player = (Player) sender;
        World world = player.getWorld();

        // Vérifier si le monde est activé
        if (!plugin.getConfigManager().isWorldEnabled(world.getName())) {
            sender.sendMessage(ChatColor.RED + "AtmosForge is not enabled for world '" + world.getName() + "'.");
            return;
        }

        // Tester différents effets météorologiques
        WeatherType[] testWeathers = {
                WeatherType.HEAVY_RAIN,
                WeatherType.THUNDERSTORM,
                WeatherType.BLIZZARD,
                WeatherType.SAND_STORM,
                WeatherType.HEAT_WAVE
        };

        sender.sendMessage(ChatColor.GREEN + "Starting effect test cycle...");

        // Sauvegarder la météo actuelle
        WeatherType originalWeather = plugin.getWeatherManager().getCurrentWeather(world);

        // Exécuter un test pour chaque type de météo
        for (int i = 0; i < testWeathers.length; i++) {
            WeatherType weather = testWeathers[i];

            // Planifier le test
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getWeatherManager().setWeather(world, weather, 1);
                player.sendMessage(ChatColor.YELLOW + "Testing effects for " +
                        plugin.getLanguageManager().getWeatherName(weather));
            }, 20 * 60 * i); // i minutes
        }

        // Restaurer la météo originale après les tests
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getWeatherManager().setWeather(world, originalWeather);
            player.sendMessage(ChatColor.GREEN + "Effect test complete. Restored original weather.");
        }, 20 * 60 * testWeathers.length);

        sender.sendMessage(ChatColor.YELLOW + "The test will cycle through " + testWeathers.length +
                " weather types, one minute each.");
    }

    /**
     * Teste les effets de particules.
     *
     * @param sender L'expéditeur de la commande.
     */
    private void testParticles(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return;
        }

        Player player = (Player) sender;
        Location location = player.getLocation();

        sender.sendMessage(ChatColor.GREEN + "Testing particles...");

        // Tester différents types de particules
        org.bukkit.Particle[] particles = {
                Particle.FALLING_DRIPSTONE_WATER,
                Particle.SNOWFLAKE,
                org.bukkit.Particle.CLOUD,
                org.bukkit.Particle.FLAME,
                Particle.DUST
        };

        for (int i = 0; i < particles.length; i++) {
            org.bukkit.Particle particle = particles[i];

            // Planifier le test
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int j = 0; j < 10; j++) {
                    // Créer des particules autour du joueur
                    for (int k = 0; k < 50; k++) {
                        double x = location.getX() + (Math.random() * 10 - 5);
                        double y = location.getY() + (Math.random() * 10 - 5);
                        double z = location.getZ() + (Math.random() * 10 - 5);

                        player.getWorld().spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0);
                    }

                    // Petite pause entre les vagues
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                player.sendMessage(ChatColor.YELLOW + "Testing particle: " + particle.name());
            }, 20 * 5 * i); // i * 5 seconds
        }

        sender.sendMessage(ChatColor.YELLOW + "The test will cycle through " + particles.length +
                " particle types, 5 seconds each.");
    }

    /**
     * Teste les effets sonores.
     *
     * @param sender L'expéditeur de la commande.
     */
    private void testSounds(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return;
        }

        Player player = (Player) sender;
        Location location = player.getLocation();

        sender.sendMessage(ChatColor.GREEN + "Testing sounds...");

        // Tester différents types de sons
        org.bukkit.Sound[] sounds = {
                org.bukkit.Sound.WEATHER_RAIN,
                org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
                org.bukkit.Sound.BLOCK_FIRE_AMBIENT,
                org.bukkit.Sound.BLOCK_SNOW_PLACE,
                org.bukkit.Sound.ENTITY_PHANTOM_AMBIENT
        };

        for (int i = 0; i < sounds.length; i++) {
            org.bukkit.Sound sound = sounds[i];

            // Planifier le test
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.playSound(location, sound, 1.0f, 1.0f);
                player.sendMessage(ChatColor.YELLOW + "Testing sound: " + sound.name());
            }, 20 * 2 * i); // i * 2 seconds
        }

        sender.sendMessage(ChatColor.YELLOW + "The test will cycle through " + sounds.length +
                " sound types, 2 seconds each.");
    }

    /**
     * Teste les TPS (ticks par seconde) du serveur.
     *
     * @param sender L'expéditeur de la commande.
     */
    private void testTPS(CommandSender sender) {
        // Obtenir les TPS du serveur
        double[] tps = Bukkit.getServer().getTP;

        sender.sendMessage(ChatColor.GOLD + "=== TPS (Ticks Per Second) ===");
        sender.sendMessage(ChatColor.YELLOW + "Current (1m): " + ChatColor.WHITE + formatTPS(tps[0]));
        sender.sendMessage(ChatColor.YELLOW + "Average (5m): " + ChatColor.WHITE + formatTPS(tps[1]));
        sender.sendMessage(ChatColor.YELLOW + "Average (15m): " + ChatColor.WHITE + formatTPS(tps[2]));

        // Démarrer un test de charge
        if (sender instanceof Player) {
            Player player = (Player) sender;
            World world = player.getWorld();

            // Vérifier si le monde est activé
            if (plugin.getConfigManager().isWorldEnabled(world.getName())) {
                sender.sendMessage(ChatColor.YELLOW + "Starting TPS load test...");

                // Effectuer des changements de météo rapides pour tester la charge
                for (int i = 0; i < 5; i++) {
                    WeatherType[] testWeathers = WeatherType.values();
                    WeatherType weather = testWeathers[i % testWeathers.length];

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        plugin.getWeatherManager().setWeather(world, weather, 1);
                    }, 20 * 2 * i); // i * 2 seconds
                }

                // Vérifier à nouveau les TPS après le test
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    double[] newTps = Bukkit.getServer().getTPS();

                    sender.sendMessage(ChatColor.GOLD + "=== TPS After Load Test ===");
                    sender.sendMessage(ChatColor.YELLOW + "Current (1m): " + ChatColor.WHITE + formatTPS(newTps[0]));
                    sender.sendMessage(ChatColor.YELLOW + "Average (5m): " + ChatColor.WHITE + formatTPS(newTps[1]));
                    sender.sendMessage(ChatColor.YELLOW + "Average (15m): " + ChatColor.WHITE + formatTPS(newTps[2]));

                    // Calculer l'impact
                    double impact = tps[0] - newTps[0];
                    ChatColor impactColor = impact < 1.0 ? ChatColor.GREEN : (impact < 3.0 ? ChatColor.YELLOW : ChatColor.RED);

                    sender.sendMessage(ChatColor.YELLOW + "TPS Impact: " + impactColor + String.format("%.2f", impact));

                    // Suggérer des optimisations si nécessaire
                    if (impact > 3.0) {
                        sender.sendMessage(ChatColor.RED + "Significant TPS impact detected! Consider reducing weather effects or increasing check intervals.");
                    }
                }, 20 * 15); // 15 seconds
            } else {
                sender.sendMessage(ChatColor.RED + "AtmosForge is not enabled for this world. Cannot run load test.");
            }
        }
    }

    /**
     * Formate une valeur TPS pour l'affichage.
     *
     * @param tps La valeur TPS à formater.
     * @return La valeur TPS formatée avec une couleur.
     */
    private String formatTPS(double tps) {
        // Les valeurs optimales de TPS sont de 20.0, nous arrondissons à un maximum de 20.0
        tps = Math.min(20.0, tps);

        // Déterminer la couleur en fonction de la valeur
        ChatColor color;
        if (tps >= 18.0) {
            color = ChatColor.GREEN; // Excellent
        } else if (tps >= 15.0) {
            color = ChatColor.YELLOW; // Bon
        } else {
            color = ChatColor.RED; // Problématique
        }

        return color + String.format("%.2f", tps);
    }
}