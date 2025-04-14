package fr.hoxys.plugin.atmosforge.integration;

import fr.hoxys.plugin.atmosforge.Main;
import fr.hoxys.plugin.atmosforge.models.WeatherType;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.Particle;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.logging.Logger;

/**
 * Classe d'intégration avec ProtocolLib.
 * Permet de créer des effets visuels avancés pour les différents types de météo.
 */
public class ProtocolLibHook {

    private final Main plugin;
    private final Logger logger;
    private ProtocolManager protocolManager;
    private final Random random;

    /**
     * Constructeur de l'intégration ProtocolLib.
     *
     * @param plugin L'instance du plugin principal.
     */
    public ProtocolLibHook(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.random = new Random();
    }

    /**
     * Initialise l'intégration avec ProtocolLib.
     */
    public void initialize() {
        protocolManager = ProtocolLibrary.getProtocolManager();

        // Enregistrer les écouteurs de paquets si nécessaire
        registerPacketListeners();

        logger.info("ProtocolLib integration initialized.");
    }

    /**
     * Enregistre les écouteurs de paquets.
     */
    private void registerPacketListeners() {
        // Écouter les paquets de changement de temps pour potentiellement les modifier
        protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.GAME_STATE_CHANGE) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                // Identifier si c'est un changement de météo
                int reason = packet.getIntegers().read(0);

                // 7 = début de pluie, 8 = fin de pluie
                if (reason == 7 || reason == 8) {
                    Player player = event.getPlayer();
                    World world = player.getWorld();

                    // Vérifier si le monde est activé pour AtmosForge
                    if (ProtocolLibHook.this.plugin.getConfigManager().isWorldEnabled(world.getName())) {
                        // Obtenir la météo actuelle
                        WeatherType currentWeather = ProtocolLibHook.this.plugin.getWeatherManager().getCurrentWeather(world);

                        // Si c'est une météo spéciale, potentiellement annuler le paquet Minecraft standard
                        if (isSpecialWeather(currentWeather)) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        });
    }

    /**
     * Vérifie si un type de météo est considéré comme "spécial" et nécessite un traitement personnalisé.
     *
     * @param weatherType Le type de météo à vérifier.
     * @return true si c'est une météo spéciale, false sinon.
     */
    private boolean isSpecialWeather(WeatherType weatherType) {
        // Liste des types de météo qui nécessitent un traitement personnalisé
        switch (weatherType) {
            case FOG:
            case MIST:
            case FREEZING_FOG:
            case BLIZZARD:
            case SAND_STORM:
            case DUST_STORM:
            case AURORA:
            case RAINBOW:
            case HALO:
            case NIGHT_CYCLE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Envoie un paquet de particules à un joueur.
     *
     * @param player Le joueur auquel envoyer le paquet.
     * @param particle Le type de particule.
     * @param location L'emplacement des particules.
     * @param count Le nombre de particules.
     * @param offsetX Le décalage X.
     * @param offsetY Le décalage Y.
     * @param offsetZ Le décalage Z.
     * @param speed La vitesse des particules.
     * @param data Les données supplémentaires (dépend du type de particule).
     */
    public void sendParticlePacket(Player player, Particle particle, Location location, int count,
                                   float offsetX, float offsetY, float offsetZ, float speed, int[] data) {
        if (protocolManager == null) return;

        try {
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);

            // Définir les données du paquet
            packet.getParticles().write(0, particle);
            packet.getBooleans().write(0, true); // Longue distance
            packet.getFloat().write(0, (float) location.getX());
            packet.getFloat().write(1, (float) location.getY());
            packet.getFloat().write(2, (float) location.getZ());
            packet.getFloat().write(3, offsetX);
            packet.getFloat().write(4, offsetY);
            packet.getFloat().write(5, offsetZ);
            packet.getFloat().write(6, speed);
            packet.getIntegers().write(0, count);

            // Envoyer le paquet
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            logger.warning("Failed to send particle packet: " + e.getMessage());
        }
    }

    /**
     * Crée des particules de pluie personnalisées autour d'un joueur.
     *
     * @param player Le joueur autour duquel créer les particules.
     * @param intensity L'intensité des particules (nombre de particules).
     */
    public void createRainEffect(Player player, int intensity) {
        Location playerLoc = player.getLocation();

        // Créer des particules de pluie dans un rayon autour du joueur
        for (int i = 0; i < intensity; i++) {
            double x = playerLoc.getX() + (random.nextDouble() * 30 - 15);
            double z = playerLoc.getZ() + (random.nextDouble() * 30 - 15);
            double y = playerLoc.getY() + 10 + random.nextDouble() * 5;

            Location particleLoc = new Location(playerLoc.getWorld(), x, y, z);

            // Envoyer un paquet de particules de goutte d'eau
            sendParticlePacket(player, Particle.WATER_DROP, particleLoc, 1, 0, 0, 0, 0, null);
        }
    }

    /**
     * Crée des particules de neige personnalisées autour d'un joueur.
     *
     * @param player Le joueur autour duquel créer les particules.
     * @param intensity L'intensité des particules (nombre de particules).
     */
    public void createSnowEffect(Player player, int intensity) {
        Location playerLoc = player.getLocation();

        // Créer des particules de neige dans un rayon autour du joueur
        for (int i = 0; i < intensity; i++) {
            double x = playerLoc.getX() + (random.nextDouble() * 30 - 15);
            double z = playerLoc.getZ() + (random.nextDouble() * 30 - 15);
            double y = playerLoc.getY() + 10 + random.nextDouble() * 5;

            Location particleLoc = new Location(playerLoc.getWorld(), x, y, z);

            // Envoyer un paquet de particules de neige
            sendParticlePacket(player, Particle.SNOW_SHOVEL, particleLoc, 1, 0, 0, 0, 0, null);
        }
    }

    /**
     * Crée des particules de brouillard personnalisées autour d'un joueur.
     *
     * @param player Le joueur autour duquel créer les particules.
     */
    public void createFogEffect(Player player) {
        Location playerLoc = player.getLocation();

        // Créer des particules de brouillard dans un rayon autour du joueur
        for (int i = 0; i < 30; i++) {
            double x = playerLoc.getX() + (random.nextDouble() * 20 - 10);
            double z = playerLoc.getZ() + (random.nextDouble() * 20 - 10);
            double y = playerLoc.getY() + random.nextDouble() * 4 - 1;

            Location particleLoc = new Location(playerLoc.getWorld(), x, y, z);

            // Envoyer un paquet de particules de nuage
            sendParticlePacket(player, Particle.CLOUD, particleLoc, 1, 0.5f, 0.1f, 0.5f, 0, null);
        }
    }

    /**
     * Crée des particules d'arc-en-ciel personnalisées.
     *
     * @param player Le joueur pour lequel créer l'arc-en-ciel.
     */
    public void createRainbowEffect(Player player) {
        Location playerLoc = player.getLocation();

        // Déterminer la direction que le joueur regarde
        double yaw = Math.toRadians(playerLoc.getYaw());
        double dirX = -Math.sin(yaw);
        double dirZ = Math.cos(yaw);

        // Créer un arc-en-ciel à distance
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 30; j++) {
                // Calcul de position en arc
                double angle = j * (Math.PI / 30);
                double height = Math.sin(angle) * 20;
                double width = Math.cos(angle) * 20;

                double x = playerLoc.getX() + dirX * 50 + width;
                double z = playerLoc.getZ() + dirZ * 50;
                double y = playerLoc.getY() + height + 10 + i * 2;

                Location particleLoc = new Location(playerLoc.getWorld(), x, y, z);

                // Couleurs de l'arc-en-ciel
                Particle particle;
                switch (i) {
                    case 0: particle = Particle.REDSTONE; break; // Rouge
                    case 1: particle = Particle.FLAME; break; // Orange
                    case 2: particle = Particle.VILLAGER_HAPPY; break; // Jaune
                    case 3: particle = Particle.SLIME; break; // Vert
                    case 4: particle = Particle.DRIP_WATER; break; // Bleu
                    case 5: particle = Particle.SPELL_WITCH; break; // Indigo
                    default: particle = Particle.PORTAL; break; // Violet
                }

                // Envoyer les particules
                sendParticlePacket(player, particle, particleLoc, 1, 0, 0, 0, 0, null);
            }
        }
    }

    /**
     * Crée des particules d'aurore boréale personnalisées.
     *
     * @param player Le joueur pour lequel créer l'aurore.
     */
    public void createAuroraEffect(Player player) {
        Location playerLoc = player.getLocation();

        // Créer des ondulations d'aurore
        for (int wave = 0; wave < 3; wave++) {
            for (int i = 0; i < 50; i++) {
                double angle = i * (Math.PI * 2 / 50);
                double distance = 30 + wave * 10;
                double waveHeight = Math.sin(angle * 4 + System.currentTimeMillis() / 1000.0) * 5;

                double x = playerLoc.getX() + Math.cos(angle) * distance;
                double z = playerLoc.getZ() + Math.sin(angle) * distance;
                double y = playerLoc.getY() + 20 + waveHeight + wave * 3;

                Location particleLoc = new Location(playerLoc.getWorld(), x, y, z);

                // Alterner entre vert et bleu pour l'aurore
                Particle particle = (i % 2 == 0) ? Particle.SPELL_MOB : Particle.SPELL_INSTANT;

                // Envoyer les particules
                sendParticlePacket(player, particle, particleLoc, 1, 0, 0, 0, 0, null);
            }
        }
    }

    /**
     * Crée un effet visuel pour une tempête de sable.
     *
     * @param player Le joueur pour lequel créer la tempête.
     */
    public void createSandstormEffect(Player player) {
        Location playerLoc = player.getLocation();

        // Créer des particules de sable tourbillonnantes
        for (int i = 0; i < 40; i++) {
            double radius = 15;
            double angle = random.nextDouble() * Math.PI * 2;
            double height = random.nextDouble() * 10;

            double x = playerLoc.getX() + Math.cos(angle) * radius;
            double z = playerLoc.getZ() + Math.sin(angle) * radius;
            double y = playerLoc.getY() + height;

            Location particleLoc = new Location(playerLoc.getWorld(), x, y, z);

            // Envoyer des particules de bloc de sable
            sendParticlePacket(player, Particle.BLOCK_CRACK, particleLoc,
                    5, 0.5f, 0.5f, 0.5f, 0.2f, new int[]{12}); // 12 = ID du bloc de sable
        }
    }

    /**
     * Crée un effet visuel pour un blizzard.
     *
     * @param player Le joueur pour lequel créer le blizzard.
     */
    public void createBlizzardEffect(Player player) {
        Location playerLoc = player.getLocation();

        // Créer des particules de neige horizontales
        for (int i = 0; i < 50; i++) {
            double radius = 20;
            double angle = random.nextDouble() * Math.PI * 2;
            double height = random.nextDouble() * 10;

            double x = playerLoc.getX() + Math.cos(angle) * radius;
            double z = playerLoc.getZ() + Math.sin(angle) * radius;
            double y = playerLoc.getY() + height;

            Location particleLoc = new Location(playerLoc.getWorld(), x, y, z);

            // Envoyer des particules de neige avec vitesse horizontale
            sendParticlePacket(player, Particle.SNOW_SHOVEL, particleLoc,
                    3, 0.2f, 0.1f, 0.2f, 0.5f, null);
        }
    }
}