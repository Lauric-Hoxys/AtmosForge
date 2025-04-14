package fr.hoxys.plugin.atmosforge.commands;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.models.Season;
import fr.hoxys.plugin.atmosforge.models.WeatherType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe de commande principale pour AtmosForge.
 */
public class AtmosForgeCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public AtmosForgeCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            // Commande de base, montrer les informations du plugin
            showPluginInfo(sender);
            return true;
        }

        // Sous-commandes
        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                showHelp(sender);
                break;

            case "reload":
                if (!hasPermission(sender, "atmosforge.admin.reload")) return true;
                reloadPlugin(sender);
                break;

            case "weather":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /atmosforge weather <set|info> [world] [weather] [duration]");
                    return true;
                }
                handleWeatherCommand(sender, args);
                break;

            case "season":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /atmosforge season <set|info> [world] [season] [day]");
                    return true;
                }
                handleSeasonCommand(sender, args);
                break;

            case "time":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /atmosforge time <day|night> [world]");
                    return true;
                }
                handleTimeCommand(sender, args);
                break;

            case "config":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /atmosforge config <get|set> [option] [value]");
                    return true;
                }
                handleConfigCommand(sender, args);
                break;

            case "info":
                handleInfoCommand(sender, args);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /atmosforge help for help.");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Premières sous-commandes
            List<String> subCommands = Arrays.asList(
                    "help", "reload", "weather", "season", "time", "config", "info");
            return filterCompletions(subCommands, args[0]);
        } else if (args.length >= 2) {
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "weather":
                    if (args.length == 2) {
                        return filterCompletions(Arrays.asList("set", "info"), args[1]);
                    } else if (args.length == 3) {
                        return filterCompletions(getWorldNames(), args[2]);
                    } else if (args.length == 4 && args[1].equalsIgnoreCase("set")) {
                        return filterCompletions(getWeatherTypeIds(), args[3]);
                    }
                    break;

                case "season":
                    if (args.length == 2) {
                        return filterCompletions(Arrays.asList("set", "info"), args[1]);
                    } else if (args.length == 3) {
                        return filterCompletions(getWorldNames(), args[2]);
                    } else if (args.length == 4 && args[1].equalsIgnoreCase("set")) {
                        return filterCompletions(getSeasonIds(), args[3]);
                    }
                    break;

                case "time":
                    if (args.length == 2) {
                        return filterCompletions(Arrays.asList("day", "night"), args[1]);
                    } else if (args.length == 3) {
                        return filterCompletions(getWorldNames(), args[2]);
                    }
                    break;

                case "config":
                    if (args.length == 2) {
                        return filterCompletions(Arrays.asList("get", "set"), args[1]);
                    } else if (args.length == 3) {
                        List<String> configOptions = Arrays.asList(
                                "days_per_season", "weather_duration", "weather_change_chance",
                                "frostbite_interval", "soldering_iron_interval", "time_check_interval");
                        return filterCompletions(configOptions, args[2]);
                    }
                    break;

                case "info":
                    if (args.length == 2) {
                        return filterCompletions(getWorldNames(), args[1]);
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
     * Affiche les informations du plugin.
     *
     * @param sender Le destinataire du message.
     */
    private void showPluginInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== AtmosForge v" + plugin.getDescription().getVersion() + " ===");
        sender.sendMessage(ChatColor.YELLOW + "A comprehensive weather and seasons system for Minecraft.");
        sender.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.WHITE + "/atmosforge help" + ChatColor.YELLOW + " for commands.");
        sender.sendMessage(ChatColor.YELLOW + "Authors: " + String.join(", ", plugin.getDescription().getAuthors()));
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
     * Gère les commandes d'information détaillée.
     *
     * @param sender Le destinataire du message.
     * @param args Les arguments de la commande.
     */
    private void handleInfoCommand(CommandSender sender, String[] args) {
        World world = null;

        if (args.length >= 2) {
            // Monde spécifié
            world = Bukkit.getWorld(args[1]);
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "World '" + args[1] + "' not found.");
                return;
            }
        } else if (sender instanceof Player) {
            // Utiliser le monde du joueur
            world = ((Player) sender).getWorld();
        } else {
            sender.sendMessage(ChatColor.RED + "Please specify a world.");
            return;
        }

        // Vérifier si le monde est activé
        if (!plugin.getConfigManager().isWorldEnabled(world.getName())) {
            sender.sendMessage(ChatColor.RED + "AtmosForge is not enabled for world '" + world.getName() + "'.");
            return;
        }

        // Afficher les informations détaillées
        WeatherType currentWeather = plugin.getWeatherManager().getCurrentWeather(world);
        Season currentSeason = plugin.getSeasonManager().getCurrentSeason(world);
        int currentDay = plugin.getSeasonManager().getCurrentSeasonDay(world);
        int daysPerSeason = plugin.getSeasonManager().getDaysPerSeason();
        boolean isNightCycle = plugin.getTimeManager().isNightCycleActive(world);

        sender.sendMessage(ChatColor.GOLD + "=== AtmosForge Status for " + world.getName() + " ===");

        // Informations sur la saison
        sender.sendMessage(ChatColor.YELLOW + "Current season: " + ChatColor.WHITE + currentSeason.getDisplayName());
        sender.sendMessage(ChatColor.YELLOW + "Season day: " + ChatColor.WHITE + currentDay + "/" + daysPerSeason);
        sender.sendMessage(ChatColor.YELLOW + "Days until next season: " + ChatColor.WHITE + (daysPerSeason - currentDay + 1));

        // Informations sur la météo
        if (isNightCycle) {
            sender.sendMessage(ChatColor.YELLOW + "Weather status: " + ChatColor.WHITE + "Night Cycle (inactive during night)");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Current weather: " + ChatColor.WHITE +
                    plugin.getLanguageManager().getWeatherName(currentWeather));
            sender.sendMessage(ChatColor.YELLOW + "Weather duration: " + ChatColor.WHITE +
                    plugin.getWeatherManager().getWeatherDuration(world) + " minutes remaining");

            // Informations supplémentaires sur la météo
            if (currentWeather.hasPrecipitation()) {
                sender.sendMessage(ChatColor.YELLOW + "Precipitation: " + ChatColor.WHITE + "Yes");
            }

            if (currentWeather.isDangerous()) {
                sender.sendMessage(ChatColor.YELLOW + "Warning: " + ChatColor.RED + "This weather is potentially dangerous!");
            }
        }

        // Informations sur le temps
        sender.sendMessage(ChatColor.YELLOW + "Time of day: " + ChatColor.WHITE +
                (isNightCycle ? "Night" : "Day") + " (" + world.getTime() + " ticks)");

        // Informations sur la configuration
        sender.sendMessage(ChatColor.YELLOW + "Weather change chance: " + ChatColor.WHITE +
                plugin.getWeatherManager().getWeatherChangeChance() + "%");
    }
}