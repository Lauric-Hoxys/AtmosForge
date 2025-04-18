﻿# AtmosForge (En cours de dev..)

Un système avancé de météo et de saisons pour Minecraft avec des conditions météorologiques personnalisées et réalistes.

## Fonctionnalités

### Saisons exclusives par monde
- Chaque monde possède son propre cycle de saisons unique, permettant d'avoir une infinité de mondes fonctionnant avec des saisons et des conditions météorologiques différentes.

### Cycle de nuit
- Après une dure journée de jeu, le système entre en cycle de nuit qui compte comme une météo qui ne fait rien. Paisible pour les joueurs !

### Cycles de saisons configurables
- Modifiez le nombre de jours Minecraft dans une saison via le fichier de configuration généré. La valeur par défaut est de 30 jours, simulant un mois de la vie réelle !

### Météos aléatoires
- Chaque météo a une chance égale de se produire à la fin de la journée. Vous pouvez configurer cela dans le fichier de configuration.

### Configuration forcée
- En jeu, les personnes ayant les bonnes permissions peuvent changer la saison, le jour ou la météo, contredisant ainsi le randomiseur. Si vous voulez un temps brûlant en hiver, vous pouvez l'avoir !

### Sauvegarde lors de l'arrêt
- Même lorsque votre serveur s'arrête, le plugin enregistrera où en est chaque monde et le rechargera lorsque vous redémarrerez. Redémarrez donc à loisir !

### Open Source
- Je ne vais pas cacher ce code derrière des paywalls premium ou quoi que ce soit d'autre. Si vous êtes développeur, vous pouvez trouver le code source et interagir avec le plugin sous licence Apache 2.0 !

### Personnalisation des intervalles de dégâts
- Vous pouvez modifier le nombre de ticks qui s'écoulent entre les dégâts du Soldering Iron ou du Frostbite (les deux principaux debuffs infligeant des dégâts) via le fichier de configuration !

### Fichier de langue
- Personnalisez les messages que AtmosForge affiche, traduisez-les dans une autre langue ou mettez-y votre parler pirate. Les possibilités sont infinies !

## Types de météo inclus

### Précipitations
- Pluie (légère, modérée, forte)
- Bruine
- Averse
- Pluie verglaçante
- Grêle (petite, moyenne, grosse)
- Neige (légère, modérée, forte)
- Neige mouillée/fondue
- Tempête de neige/blizzard
- Grésil
- Givre
- Pluie de boue/sable

### Phénomènes liés à la température
- Vague de chaleur
- Vague de froid
- Gel
- Dégel
- Canicule

### Vents
- Brise légère
- Vent modéré
- Vent fort
- Rafales
- Tempête
- Ouragan/Cyclone/Typhon
- Tornade
- Trombe/Tuba
- Vent de sable/tempête de sable
- Tempête de poussière
- Blizzard (vent violent avec neige)
- Bourrasque
- Chinook/Foehn (vents chauds descendants)
- Mousson

### Phénomènes électriques et lumineux
- Orage
- Éclair/Foudre
- Tonnerre
- Feu de Saint-Elme
- Aurore polaire (boréale/australe)

### Phénomènes de visibilité
- Brouillard
- Brume
- Smog/Brume sèche
- Brume de mer
- Brouillard givrant
- Fumée (d'incendies)
- Brume de sable/poussière
- Obscurcissement par cendres volcaniques

### Phénomènes optiques
- Arc-en-ciel
- Arc-en-ciel de lune
- Halo solaire/lunaire
- Parhélie/soleil factice
- Pilier lumineux
- Couronne solaire/lunaire
- Rayon crépusculaire/anticrepusculaire
- Éclair vert (rare, au coucher du soleil)

### Conditions de ciel
- Ciel dégagé/Ensoleillé
- Partiellement nuageux
- Nuageux
- Couvert
- Ciel menaçant

### Phénomènes extrêmes ou rares
- Cyclone tropical/Ouragan/Typhon
- Tornade
- Trombe marine
- Grêle géante
- Tempête de feu/Orage pyroconvectif
- Tempête de poussière
- Tempête de neige
- Onde de tempête
- Avalanche
- Inondation éclair
- Mousson
- Tempête de grésil
- Pluie acide
- Brouillard glacé
- Neige rouge/colorée (due à des algues ou pollens)

## Installation

1. Téléchargez le fichier JAR et placez-le dans votre dossier `plugins`.
2. Redémarrez votre serveur.
3. Configurez le plugin via le fichier `config.yml` généré.

## Commandes

- `/atmosforge` ou `/af` - Affiche les informations sur le plugin
- `/atmosforge help` - Affiche l'aide des commandes
- `/atmosforge weather info [monde]` - Affiche la météo actuelle
- `/atmosforge weather set <monde> <météo> [durée]` - Définit la météo
- `/atmosforge season info [monde]` - Affiche la saison actuelle
- `/atmosforge season set <monde> <saison> [jour]` - Définit la saison
- `/atmosforge time <day|night> [monde]` - Force le jour ou la nuit
- `/atmosforge config get <option>` - Obtient une valeur de configuration
- `/atmosforge config set <option> <valeur>` - Définit une valeur de configuration
- `/atmosforge info [monde]` - Affiche des informations détaillées

## Permissions

- `atmosforge.use` - Permet d'utiliser la commande de base AtmosForge
- `atmosforge.admin.reload` - Permet de recharger la configuration
- `atmosforge.admin.weather` - Permet de changer la météo
- `atmosforge.admin.season` - Permet de changer la saison
- `atmosforge.admin.time` - Permet de forcer le jour ou la nuit
- `atmosforge.admin.config` - Permet de modifier la configuration en jeu
- `atmosforge.admin.*` - Toutes les permissions administratives

## Intégrations

### PlaceholderAPI
AtmosForge s'intègre avec PlaceholderAPI pour fournir des placeholders utilisables dans d'autres plugins.

Exemples de placeholders:
- `%atmosforge_season%` - Nom de la saison actuelle
- `%atmosforge_weather%` - Nom de la météo actuelle
- `%atmosforge_season_day%` - Jour actuel de la saison
- `%atmosforge_world_worldname_season%` - Saison dans un monde spécifique

### ProtocolLib
AtmosForge utilise ProtocolLib pour créer des effets visuels avancés pour les différents types de météo.

## Configuration

Exemple de configuration (`config.yml`):
```yaml
# Langue du plugin (fr_FR, en_US, etc.)
language: "fr_FR"

# Mondes activés pour AtmosForge
enabled_worlds:
  - "all"

# Configuration des saisons
seasons:
  # Nombre de jours Minecraft dans chaque saison
  days_per_season: 30

# Configuration de la météo
weather:
  # Durée par défaut d'une condition météo en minutes
  default_duration: 120
  # Chance de changement météorologique par jour (0-100)
  change_chance: 30
```

## API pour les développeurs

AtmosForge fournit une API pour les développeurs souhaitant interagir avec le système de météo et de saisons.

Exemple d'utilisation:
```java
// Obtenir la saison actuelle d'un monde
Season currentSeason = AtmosForgeAPI.getCurrentSeason(world);

// Obtenir la météo actuelle d'un monde
WeatherType currentWeather = AtmosForgeAPI.getCurrentWeather(world);

// Définir la météo pour un monde
AtmosForgeAPI.setWeather(world, WeatherType.THUNDERSTORM, 60);

// Définir la saison pour un monde
AtmosForgeAPI.setSeason(world, Season.WINTER, 1);
```

## Licence

Ce projet est sous licence Apache 2.0. Vous êtes libre de l'utiliser, de le modifier et de le distribuer selon les termes de cette licence.
