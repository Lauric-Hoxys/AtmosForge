package fr.hoxys.plugin.atmosforge.commands;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.models.Season;

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
 * Classe de commande pour gérer les saisons dans AtmosForge.
 */
public class SeasonCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public SeasonCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /season <set|info|next|prev> [world] [season] [day]");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "set":
                if (!hasPermission(sender, "atmosforge.admin.season")) return true;
                handleSetCommand(sender, args);
                break;

            case "info":
                handleInfoCommand(sender, args);
                break;

            case "next":
                if (!hasPermission(sender, "atmosforge.admin.season")) return true;
                handleNextCommand(sender, args);
                break;

            case "prev":
            case "previous":
                if (!hasPermission(sender, "atmosforge.admin.season")) return true;
                handlePrevCommand(sender, args);
                break;

            case "list":
                handleListCommand(sender);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /season help for help.");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("set", "info", "next", "prev", "list");
            return filterCompletions(subCommands, args[0]);
        } else if (args.length >= 2) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("set")) {
                if (args.length == 2) {
                    return filterCompletions(getWorldNames(), args[1]);
                } else if (args.length == 3) {
                    return filterCompletions(getSeasonIds(), args[2]);
                } else if (args.length == 4) {
                    List<String> days = new ArrayList<>();
                    int daysPerSeason = plugin.getSeasonManager().getDaysPerSeason();
                    for (int i = 1; i <= daysPerSeason; i++) {
                        days.add(String.valueOf(i));
                    }
                    return filterCompletions(days, args[3]);
                }
            } else if (subCommand.equals("info") || subCommand.equals("next") || subCommand.equals("prev") ||
                    subCommand.equals("previous")) {
                if (args.length == 2) {
                    return filterCompletions(getWorldNames(), args[1]);
                }
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
     * Gère la commande de définition de la saison.
     *
     * @param sender L'expéditeur de la commande.
     * @param args Les arguments de la commande.
     */
    private void handleSetCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /season set <world> <season> [day]");
            return;
        }

        // Obtenir le monde
        String worldName = args[1];
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sender.sendMessage(ChatColor.RED + "World '" + worldName + "' not found.");
            return;
        }

        // Vérifier si le monde est activé
        if (!plugin.getConfigManager().isWorldEnabled(world.getName())) {
            sender.sendMessage(ChatColor.RED + "AtmosForge is not enabled for world '" + world.getName() + "'.");
            return;
        }

        // Obtenir la saison
        String seasonId = args[2].toLowerCase();
        Season season = Season.fromId(seasonId);
        if (season == null) {
            sender.sendMessage(ChatColor.RED + "Season '" + seasonId + "' not recognized.");
            sender.sendMessage(ChatColor.RED + "Available seasons: spring, summer, autumn, winter");
            return;
        }

        // Obtenir le jour (optionnel)
        int day = 1;
        if (args.length >= 4) {
            try {
                day = Integer.parseInt(args[3]);
                int daysPerSeason = plugin.getSeasonManager().getDaysPerSeason();
                if (day < 1 || day > daysPerSeason) {
                    sender.sendMessage(ChatColor.RED + "Day must be between 1 and " + daysPerSeason + ".");
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid day. Please enter a number.");
                return;
            }
        }

        // Définir la saison
        boolean success = plugin.getSeasonManager().setSeason(world, season, day);
        if (success) {
            String seasonName = plugin.getLanguageManager().getSeasonName(season);
            sender.sendMessage(plugin.getLanguageManager().getMessage("seasons.set_success",
                    "{season}", seasonName, "{day}", String.valueOf(day)));
        } else {
            sender.sendMessage(plugin.getLanguageManager().getMessage("seasons.set_error"));
        }
    }

    /**
     * Gère la commande d'information sur la saison.
     *
     * @param sender L'expéditeur de la commande.
     * @param args Les arguments de la commande.
     */
    private void handleInfoCommand(CommandSender sender, String[] args) {
        World world = null;

        if (args.length >= 2) {
            // Monde spécifié
            String worldName = args[1];
            world = Bukkit.getWorld(worldName);
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "World '" + worldName + "' not found.");
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

        // Afficher les informations sur la saison
        Season currentSeason = plugin.getSeasonManager().getCurrentSeason(world);
        int currentDay = plugin.getSeasonManager().getCurrentSeasonDay(world);
        int daysPerSeason = plugin.getSeasonManager().getDaysPerSeason();
        Season nextSeason = currentSeason.getNext();

        sender.sendMessage(ChatColor.GOLD + "=== Season Information for " + world.getName() + " ===");
        sender.sendMessage(ChatColor.YELLOW + "Current season: " + ChatColor.WHITE +
                plugin.getLanguageManager().getSeasonName(currentSeason));
        sender.sendMessage(ChatColor.YELLOW + "Season day: " + ChatColor.WHITE +
                currentDay + "/" + daysPerSeason);
        sender.sendMessage(ChatColor.YELLOW + "Days until next season: " + ChatColor.WHITE +
                (daysPerSeason - currentDay + 1));
        sender.sendMessage(ChatColor.YELLOW + "Next season: " + ChatColor.WHITE +
                plugin.getLanguageManager().getSeasonName(nextSeason));

        // Afficher la description
        String description = plugin.getLanguageManager().getSeasonDescription(currentSeason);
        sender.sendMessage(ChatColor.YELLOW + "Description: " + ChatColor.WHITE + description);

        // Afficher les types de météo communs
        List<String> commonWeatherNames = currentSeason.getCommonWeatherTypes().stream()
                .map(weatherType -> plugin.getLanguageManager().getWeatherName(weatherType))
                .collect(Collectors.toList());

        sender.sendMessage(ChatColor.YELLOW + "Common weather types: " + ChatColor.WHITE +
                String.join(", ", commonWeatherNames));
    }

    /**
     * Gère la commande de passage à la saison suivante.
     *
     * @param sender L'expéditeur de la commande.
     * @param args Les arguments de la commande.
     */
    private void handleNextCommand(CommandSender sender, String[] args) {
        World world = null;

        if (args.length >= 2) {
            // Monde spécifié
            String worldName = args[1];
            world = Bukkit.getWorld(worldName);
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "World '" + worldName + "' not found.");
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

        // Obtenir la saison actuelle et la suivante
        Season currentSeason = plugin.getSeasonManager().getCurrentSeason(world);
        Season nextSeason = currentSeason.getNext();

        // Définir la saison suivante
        boolean success = plugin.getSeasonManager().setSeason(world, nextSeason, 1);
        if (success) {
            String seasonName = plugin.getLanguageManager().getSeasonName(nextSeason);
            sender.sendMessage(plugin.getLanguageManager().getMessage("seasons.set_success",
                    "{season}", seasonName, "{day}", "1"));
        } else {
            sender.sendMessage(plugin.getLanguageManager().getMessage("seasons.set_error"));
        }
    }

    /**
     * Gère la commande de passage à la saison précédente.
     *
     * @param sender L'expéditeur de la commande.
     * @param args Les arguments de la commande.
     */
    private void handlePrevCommand(CommandSender sender, String[] args) {
        World world = null;

        if (args.length >= 2) {
            // Monde spécifié
            String worldName = args[1];
            world = Bukkit.getWorld(worldName);
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "World '" + worldName + "' not found.");
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

        // Obtenir la saison actuelle et la précédente
        Season currentSeason = plugin.getSeasonManager().getCurrentSeason(world);
        Season prevSeason = currentSeason.getPrevious();

        // Définir la saison précédente
        boolean success = plugin.getSeasonManager().setSeason(world, prevSeason, 1);
        if (success) {
            String seasonName = plugin.getLanguageManager().getSeasonName(prevSeason);
            sender.sendMessage(plugin.getLanguageManager().getMessage("seasons.set_success",
                    "{season}", seasonName, "{day}", "1"));
        } else {
            sender.sendMessage(plugin.getLanguageManager().getMessage("seasons.set_error"));
        }
    }

    /**
     * Gère la commande de liste des saisons.
     *
     * @param sender L'expéditeur de la commande.
     */
    private void handleListCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Available Seasons ===");

        for (Season season : Season.values()) {
            String seasonName = plugin.getLanguageManager().getSeasonName(season);
            String seasonId = season.getId();

            sender.sendMessage(ChatColor.YELLOW + seasonId + ChatColor.WHITE + " - " + seasonName);
        }

        sender.sendMessage(ChatColor.GOLD + "Use /season set <world> <season> [day] to set the season.");
    }
}