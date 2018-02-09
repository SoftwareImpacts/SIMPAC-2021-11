## Changelog

##### version 2.2.3 (09/02/2018)
- Linkset creation : negative cost is now checked and throw an exception

##### version 2.2.2 (20/12/2017)
- Corridor : throws Coordinate Out of bounds exception in rare cases

##### version 2.2.1 (12/12/2017)
- CLI : --linkset command throws ConcurrentModificationException when used with several costs

##### version 2.2 (24/10/2017)
- Final version

##### version 2.1.11 (11/10/2017)
- UI : distance conversion, show plot below
- UI : Pointset distance dialog locks UI during calculation

##### version 2.1.10 (09/10/2017)
- UI : number of components in graph properties
- UI : add point set dialog, disable attribute import by default

##### version 2.1.9 (22/09/2017)
- CLI : --graph name option does not work correctly
- Circuit linkset : throw exception for one special case in optim mode

##### version 2.1.8 (21/09/2017)
- Metric : remove beta parameter from PC and dPC
- Metric : remove FPC metric (replaced by IF metric)
- Metric : add EC global metric and IF local metric
- Metric : PC and IIC metrics are available only if patch capacities represent area
- UI : move "Remove attributes" menu entry in patch and linkset contextual menu

##### version 2.1.7 (13/09/2017)
- UI : add Corridor calculation on linkset contextual menu
- UI : DEM is visible in layers
- UI : in most input distance fields, shows the unit: cost or meter
- UI : default probability sets to 0.5
- CLI : --corridor command supports distance conversion
- CLI : --graph command add name option
- CLI : add --cluster command
- Full support of ascii grid format (.asc)

##### version 2.1.6 (13/07/2017)
- Project migration to sourcesup

##### version 2.1.5 (19/06/2017)
- Add circuit flow matrix distance for pointset

##### version 2.1.4 (13/06/2017) - from 2.0.6
- Patch addition : error when capacity coverage is used in multi patch

##### version 2.0.6 (12/06/2017)
- Patch addition : error when capacity coverage is used in multi patch

##### version 2.1.3 (03/11/2016)
- CLI : add sel option to --landmod command
- Add F6 metric in plugin
- UI : few enhancements on styling layer dialog

##### version 2.1.2 (12/05/2016)
- UI patch addition : remove dPC from metric combobox after changing the graph
- UI : bug while exporting the map with raster data (null pointer exception)
- UI patch addition : add metric parameter button
- Patch addition : cost of the first pixel of added links are slightly different between testing and adding patches -> throw Metric precision exception

##### version 2.0.5 (12/05/2016)
- UI patch addition : remove dPC from metric combobox after changing the graph

##### version 2.0.4 (26/04/2016)
- UI : bug while exporting the map with raster data (null pointer exception)

##### version 2.0.3 (05/04/2016)
- UI patch addition : add metric parameter button
- Patch addition : cost of the first pixel of added links are slightly different between testing and adding patches -> throw Metric precision exception

##### version 2.1.1 (15/03/2016)
- UI patch addition : remove dPC from metric combobox
- Patch addition : stop the process when no more patches can be added to avoid null pointer exception

##### version 2.0.2 (15/03/2016)
- UI patch addition : remove dPC from metric combobox
- Patch addition : stop the process when no more patches can be added to avoid null pointer exception

##### version 2.1.0 (04/03/2016)
- Metric : add Wilks metric
- Modularity : keep all clustering

##### version 2.0.1 (01/03/2016)
- UI : pointset export all menu does not work when some pointset attributes is null
- Project creation : save raster with .tfw world file when CRS is undefined, to avoid unsupported CRS error

##### version 2.0
- Final version

##### version 1.3.34 (14/01/2016)
- UI : move "Remove patches" menu entry to "Data" menu
- CLI : remove the command --ltest
- CLI : merge the commands --rempatch and --remlink in --remelem

##### version 1.3.33 (08/01/2016)
- Metric : move BCCirc metric in plugin
- CLI : move to normal mode the command --gtest

##### version 1.3.32 (15/12/2015)
- UI enable add patch menu 
- CLI : add fsel parameter for --ltest and --gtest command
- UI metric interpolation : add sum option for multiple connections
- Metric : include CF and BCCirc metrics
- CLI : move to advanced mode the commands --circuit, --landmod, --gtest, --ltest and --linkset circuit

##### version 1.3.31 (30/10/2015)
- CLI : --landmod command uses too much threads in threaded mode
- UI linkset creation : "Remove crossing patch" is now unchecked by default

##### version 1.3.30 (02/09/2015)
- add menu "Remove patches" : create a sub project in just retaining patches with a minimal capacity
- remove Diff button in SDM dialog
- UI : merge menu "Import" and "Calculate" patch capacity
- MPI : undo the previous change ie. RasterPathfinder is again in double precision

##### version 1.3.29 (30/07/2015)
WARNING : project is not compatible with previous versions

- Project creation : manage several habitat codes
- CLI : add command --interp for metric interpolation
- CLI : add command --landmod for batching landuse changes
- Metric interpolation is greatly optimized in some cases
- Values calculated for component metric are not set to the good component after reloading the project
- Project is no more static, the program can load or create several projects
- MPI : to decrease memory consumption RasterPathFinder uses simple precision in place of double precision
- Upgrade parallel lib

##### version 1.3.27 (25/06/2015)
- CLI : add command --capa [maxcost=[{]valcost[}] codes=code1,code2,...,coden [weight]] 
- CLI : add command --metapatch [mincapa=value] 
- CLI : --linkset add distance option for euclidean linkset
- CLI : add auto conversion from distance to cost with {}
- CLI : change order of global options -proc and -nosave
- CLI : change --mpi to -mpi as other global options
- metapatch : can create project with one patch only
- metapatch : creating cost linkset crashes when some patches have been removed (and recoded)
- linkset : can create linkset with no links

##### version 1.3.26 (02/06/2015)
- Shapefile export : correct geotools regression with fieldname longer than 10 character

##### version 1.3.25 (29/05/2015)
- Graph modularity execution has been optimized
- Point set : distance matrix can be calculated for raster circuit
- Point set : works with circuit linkset (attach to the nearest patch in least cost and set cost to 0)
- CLI : rempatch and remlink add id selection
- CLI : add command --create for creating new project
- CLI : --linkset command : add parameter name, remcrosspatch and extcost

##### version 1.3.24 (13/04/2015)
- remove CBC metric from plugin metric

##### version 1.3.23 (30/03/2015)
- graph clustering : create graph from the clustering
- graph :  add context menu entry "Set component id"

##### version 1.3.22 (16/03/2015)
- graph clustering : add cluster id in the patch attributes

##### version 1.3.21 (19/02/2015)
- graph clustering with modularity

##### version 1.3.20 (12/02/2015)
- poinset : add flow distance matrix (edge impedance is : -ln(ai*aj/A^2) + alpha*cost)

##### version 1.3.19 (05/02/2015)
- meta patch : add minimal capacity parameter

##### version 1.3.18 (31/01/2015)
- add linkset context menu "Extract path costs"

##### version 1.3.17 (29/01/2015)
- bug with New project menu entry

##### version 1.3.16 (17/12/2014)
- --rempatch and --remlink commands can be used with mpi

##### version 1.3.15 (08/12/2014)
- CLI : add --rempatch and --remlink commands, remove iteratively the patches (or links) which minimize the given global metric

##### version 1.3.14 (28/11/2014)
- add PCCirc global metric
- add OD matrix circuit menu item in graph context menu

##### version 1.3.13 (26/06/2014)
- addpatch : erroneous results with euclidean linkset without real paths or with thresholded euclidean linkset in multi mode

##### version 1.3.12 (24/06/2014)
- addpatch : create polygonal patch with polygonal shapefile
- addpatch : save the landuse.tif for creating a new project

##### version 1.3.11 (11/06/2014)
- CLI : add slope option in --linkset command
- manage slope for circuit calculation

##### version 1.3.10 (03/06/2014)
- hide Java Preferences Warning from logging

##### version 1.3.9 (22/05/2014)
- add menu item "Set DEM" in Data menu
- slope calculation for linkset : newcost = oldcost * (1 + coefSlope * |slope|)
- metapatch : add option weighted distance for capacity
- correct bug in shapefile cache loading (null exception while creating several pointsets from the same shapefile)

##### version 1.3.8 (18/04/2014) merge from 1.2.1
- correct bug for component metrics calculation
- New graph dialog : intra patch was disabled with external cost

##### version 1.3.7 (14/04/2014)
- CLI --linkset circuit=optim crash when landscape map contains nodata

##### version 1.3.6 (04/04/2014)
- CLI : add option circuit in --linkset cmd
- CLI : add --corridor command (circuit and leastcost)
- CLI : add --circuit command (temporary)

##### version 1.3.3 (12/03/2014)
- add metrics as plugin : 
    - E#eBC : BC entropy on edges
    - D#BC : BC division on nodes
    - D#eBC : BC division on edges

##### version 1.3.2 (10/03/2014) merge from 1.2rc2 (1.2 final)
- adding cost planar linkset does not throw an error if a patch is surrounded by nodata
- CLI --graph : add option "nointra"

##### version 1.3.1 (10/03/2014)
- meta patch : correct bug "This method does not support GeometryCollection"

##### version 1.3 (20/02/2014)
- add meta patch project creation in menu Graph

##### version 1.2-rc1 (19/02/2014)
WARNING : first path metric calculation from older project can be slow, due to real intra patch distance calculation

- intra patch distance is now accurate
- least cost paths are more accurate (float -> double precision)
- re include deltaPC metric for graph with intrapatch distance
- addpatch cannot be used on graph with intrapatch distance

##### version 1.2-beta (14/02/2014)
- exclude deltaPC metric for graph with intrapatch distance
- use MainFrame ProgressBar for extrapolation
- use common 1.2.x version -> move to openmpi 1.7
- exclude netlib native lib

##### version 1.2-alpha6 (10/12/2013)
- keep CRS from landscape map in project -> shapefile and raster exports contain correct projection
- CLI addPatch : execute on each graph and gridres can have multiple values
- Properties of linkset and graph did not work since alpha3

##### version 1.2-alpha5 (09/12/2013)
- upgrading GeoTools 2.6.3 -> 2.7.5
- avoid loading of gt-epsg-hsql package, causes crash in some cases with MPI
- addpatch : add intermediate results in subdir "detail"

##### version 1.2-alpha4 (03/12/2013)
- previous version cannot load any raster (problem with maven shade plugin and ImageIO)
- CLI addPatch can be executed on shapefile of point (works also with MPI)
- hide splash screen in CLI mode

##### version 1.2-alpha3 (28/11/2013)
- project moved to maven
- move resources
- bug when creating external cost linkset at project creation.

##### version 1.2-alpha2 (21/11/2013)
- plugin management for metrics : create your own metric in Java and copy jar file in plugins folder
- remove method isTesting in Indice
- rename all packages, classes and methods from indice to metric, exo to pointset, costdistance to linkset
- CLI :
	- rename commands indice to metric
	- add option -nosave

##### version 1.2-alpha (08/11/2013)
- Métrique F, S#F et dPC ne tenait pas compte des chemins à distance nulle. Peut arriver dans le cas d'un jeu de lien euclidien avec un projet 4-connex
- Optimisation des métriques de chemin pour des graphes déconnectés (ie. à plusieurs composantes) : F, FPC, Ec, CCe, BC, BCcirc, PC, GD, DeltaPC
- Nouveau projet : gestion des images sur 1, 2, 4 bits
- Nouveau projet : erreur Raster is not WritableRaster
- Après le calcul d'une métrique locale, par composante ou delta, le graphe est affiché avec la métrique nouvellement calculée
- Mise en place du calcul distribué avec OpenMPI (version en test) :
	- pour les fonctions : addpatch et delta avec toutes les métriques globales
	- pour les fonctions : lindice, cindice et gindice seulement avec les métriques : S#F, PC, dPC, E#BC, IIC, GD, H, BC, BCCirc, CBC, CF, PCF
- Les métriques de circuit ne sont pas calculables sur un graphe MST (aucun intérêt)
- CLI : commande --gremove : permet de calculer une métrique globale en enlevant un ensemble de noeuds et de liens
- Suppression de la variabilité dans les calculs de chemin sur un graphe en cas d'égalité (ordre total)
- Suppression de la variabilité dans la création des graphes MST
- Distance matrix sur données exo : ajout de la distance sur le graphe en circuit
- Distance matrix was wrong for MST graph
- surface de cout externe gérée pour le calcul de la capacité, l'ajout de jeu de point et SDM
- nom des métriques en mode delta contiennent les paramètres comme pour les métriques locales

##### version 1.1 (06/05/2013)
- AddPatch : bug enregistrement du shapefile des liens quand des métriques ont déjà été calculées
- AddPatch : save topo links
- CLI : add global and component metric (--gindice, --cindice) as --lindice
- Création du projet : optimisation du calcul de voronoi

##### version 1.1rc1 (15/05/2013)
- Restructuration des packages
- Alias des classes pour la sérialisation XML
- Graphe par lot et métrique par lot n'enregistre rien par défaut.
- Ajout interpolation de métrique dans Analyse
- Ajout métrique globale Entropie pour BC (E#BC) en ligne de commande
- Optimisation des métriques de chemin avec des graphes contenant beaucoup de petites composantes
- Paramètre "a" renommé en "beta" en ligne de commande
- Vue topologique les cercles proportionnels fonctionnent avec des nombres négatifs 

##### version 1.1beta2 (09/04/2013)
- Suppression de l'indice Cut (correspond à NC en delta)
- Ajout de l'indice PCF (version très lente)
- Parallélisation de l'indice IIC et H
- Simplification de la hiérarchie objet des indices
- Définition des couleurs des jeux de liens et des graphes
- Carte de paysage affichée à l'envers...

##### version 1.1beta (03/04/2013)
- Ajout de tache seulement accessible en ligne de commande
- Distance matrix sur données exo : ajout de plusieurs distances (il faudra épurer)
- Ajout du delta PC décomposé, accessible qu'en delta
- Ajout de l'indice local FPC
- Ajout de l'indice IIC
- Ajout de l'indice CF (Current flow)
- Suppression de l'indice NL, FTopo, PCTopo, Fmax, LDF, BCl
- Suppression de tous les indices BC sommés en global (il ne reste plus que F)
- Nettoyage de l'interface pour version finale
	- Meilleure intégration de la barre de progression
	- Nettoyage et traduction des propriétés des jeux de liens et des graphes, ajout du nombre de liens dans la fenêtre
	- Suppression des métriques circuit de l'interface (toujours accessible en ligne de commande) : BCCirc, CBC, CF
	- On ne peut plus calculer plusieurs métriques en même temps
	- Le paramétrage de la métrique se fait directement au moment du calcul (il n'y plus l'entrée métrique->paramètre)
	- l'option maxCost pour le calcul des métriques n'est accessible qu'en ligne de commande
	- groupement des métriques par type (Pondéré, Surface, Topo)
	- l'affichage de la carte de paysage fonctionne à nouveau
	- ajout du journal dans le menu fichier
	- plus beaucoup d'autres détails d'interface..

##### version 1.1alpha (01/02/2013)
- Ajout de tache : teste l'ajout de plusieurs taches en même temps
- Ajout de tache : à partir d'un fichier de point au lieu d'une grille
- Exo : calcul des distances cout ou sur le graphe entre les tous les points exos
- Fenêtre addRandomPoint texte erroné (capacity raster au lieu de point de présence)
- Ajout du BCs et BCl en indice global (somme des valeurs des taches)
- Indice BCcircuit renommer en BCsCirc
- Ajout indice CBC : BC en théorie des circuits
- Ajout paramètre beta dans CBC
- Bug export taches et liens dans la fenêtre résultat d'ajout de taches
- Erreur allocation processeur pour delta et batch indice avec des indices locaux
- ajout en ligne de commande de l'opération addlocal (teste l'ajout de lien ou de tache pour un indice local) en batch

##### version 1.0beta2 (11/07/2012)
- Fonction ajout de tâches dans le menu Graphe (en test)
- Optimisation temps et mémoire du calcul des distances intra-patch (suppression de pathgraph remplacé par NodeWeighter)
- Ajout des indices PCTopo et FTopo
- Enregistrement automatique de l'extrapolation
- Ajout de l'indice BCcircuit (BCs avec calcul de chemins multiples par la théorie des circuits)

##### version 1.0beta1
- nettoyage des indices par composante
- passage de Area à Capacity pour les indices (SLC, MSC, CCP, ECS)
- bug affichage fenêtre de calcul de la capacité des tâches
- BC fonctionne aussi sur un graphe sans distance intra patch
- dans SDM possibilité de faire un différentiel de proba (pas sûr de garder cette fonctionnalité)
- suppression des multiplications de barres de progression
- Calcul indice : suppression de random impedance
- Calcul indice : ajout de max cost pour optimiser le calcul des métriques de chemins (PC, F, BCl, BCs, ...)

##### version 1.0alpha13 (15/12/2011)
- enregistrement de l'option simplify dans le fichier projet xml
- enregistrement des paramètres de calcul de la capacité dans le fichier projet xml
- dans la génération des points aléatoires, on charge les points de présence directement d'un shapefile
- dans la génération des points aléatoires, ajout d'une option pour ne garder qu'un point de présence par cellule
- renommage de certaines métriques

##### version 1.0alpha12 (23/11/2011)
- attention version alpha11 peut produire des résultats erronés sur le calcul de jeu de liens en complet et parallélisé
- optimisation du calcul des jeu de liens en euclidien complet

##### version 1.0alpha11 (21/11/2011)
- correction allocation mémoire jeu de lien complet
- création de jeu de lien complet seuillé possible en ligne de commande

##### version 1.0alpha10 (16/11/2011)
- ajout des indices locaux Fmax (FM) et Long Distance Flux (LDF)

##### version 1.0alpha9 (15/11/2011)
- calcul de la matrice des distances sur données exo avec un flux comme impédance
- les impédance peuvent être en partie aléatoire (champs Random impedance (%)) dans les calculs de chemins pour les indices en global, composante, local et delta

##### version 1.0alpha8 (18/10/2011)
Attention les projets ne sont plus compatibles !!
Il faut supprimer toutes les données exo avant de passer à cette version !
- modification de la procédure de calcul de rattachement des attributs des données exo aux taches, lors de l'import de données exo

##### version 1.0alpha7 (10/10/2011)
- ajout du calcul des capacités sur le voisinage (menu Données)

##### version 1.0alpha6 (27/09/2011)
- correction de voronoi pour qu'il soit stable entre 2 même exécutions
- ajout de l'option de simplication dans la création du projet pour stabiliser voronoi

##### version 1.0alpha5 (16/09/2011)
- Ajout (provisoire ?) de l'IFPC
- Problème de précision sur le seuil des tailles minimales des taches -> variabilité du nb de taches dont la taille = seuil

##### version 1.0alpha4 (28/06/2011)
- Bug estimation en multi-attach après une première estimation
- Extrapolation possible en euclidien

##### version 1.0alpha3 (22/06/2011)
- Cercle proportionnel toujours identique quelque soit le niveau de zoom
- Taille du cercle paramétrable dans le style
- Ajout des indices locaux :
  - Closeness centrality
  - Eccentricity
  - Connectivity correlation
  - Cut elements

##### version 1.0alpha2 (26/05/2011)
- Bug sur l'enregistrement des liens quand on annule le calcul de l'indice de traversabilité

##### version 1.0alpha1 (24/05/2011)
- Indice CC corrigé dans le cas où le degré du noeud est inférieur à 2 -> CC = 0
- A la création d'un graphe, distance intra patch n'est utilisable seulement si les chemins ont été enregistrés
- A la création des liens, quand les chemins ne sont pas enregistrés la longueur des chemins n'était pas non plus enregistré

##### version 1.0alpha (19/05/2011)
ATTENTION cette version n'est pas compatible avec les projets créés par une version précédente !!!!
La mise à niveau d'un projet existant est toujours possible, mais il faut prendre un rdv avec le service de maintenance (51 36)...

- nettoyage de l'interface et harmonisation des termes
- version française
- à la création d'un graphe choix d'inclure les distances intra patch ou non pour les calculs d'indices
- dans création d'un linkset dist max n'est plus en mètre mais en cout

##### version 0.6 (10/05/2011)
ATTENTION cette version n'est pas compatible avec les projets créés par une version précédente !!!!
La mise à niveau d'un projet existant est toujours possible, mais il faut prendre un rdv avec le service de maintenance (51 36)...

- ajout indice local Degree (Dg), Clustering Coefficient (CC) et l'indice global ECS
- les indices locaux (F, T, Ti) sont calculables en global (somme des indices sur les patch)
- le choix du type de distance (cout cumulé ou longueur) se fait au moment de la création des couts
  le type de distance est pris en compte pour les indices, les données exogènes, modèle et extrapolation
- import capacity : on peut importer des données de capacité qui remplace area
- l'extrapolation se fait dans une fenêtre à part
- distance euclidienne est gérée dans l'extrapolation

##### version 0.5.3 (14/04/2011)
- bug (encore!) sur l'extraction des patchs (dans de rares cas)
- indice wilks supprimé pour l'instant
- calcul du PC à nouveau possible sur des graphes seuillés
- export svg
- add layer

##### version 0.5.2 (08/04/2011)
- coefficient standardisé dans model enfin bon !

##### version 0.5.1 (28/03/2011)
- ajout de l'indice local de Traversibilité "inverse" (Ti)

##### version 0.5 (22/03/2011)
- ajout du paramètre a correspondant à l'exposant de la surface (dans F, T, PC)
- ce qui permet la fusion des indices F et Fa ainsi que T et Ta
- multi attachement dans Model
- les noms de variables créées à partir d'indice locaux contiennent les paramètres
- bug dans la génération des points aléatoires le champs presence n'était pas créé

##### version 0.4.7 (17/03/2011)
- bug calcul distance intra-patch 
- add delta indice en ligne de commande (CLI)
- change Traversability indice (T et Ta)

##### version 0.4.6 (14/02/2011)
- bug batch param indice global en version parallèle

##### version 0.4.5 (11/02/2011)
- ajout d'un batch en ligne de commande (exécuter : java -jar GraphAB.jar --help)
- ajout Batch param indice local et global
- bug suppression graphe
- ajout propriété pour graph et exodata
- dans model affiche que les graphes ayant le même links que celui de exodata sélectionné
- dans model : teste si la variable à estimer est bien binaire

##### version 0.4.4 (02/02/2011)
- ajout de la génération aléatoire stratifiée de points de pseudo absence

##### version 0.4.3 (28/01/2010)
- bug chargement raster dans Model désolé Céline...
- les cercles ont la même superficie que les taches (pour JC)
- amélioration de la fenêtre de symbologie (chaque classe peut être modifiée manuellement)

##### version 0.4.2 (28/01/2010)
- bug enregistrement exo data : enregistre les points hors zone aussi ce qui fait planter l'estimation du modèle après fermeture et réouverture du projet
- bug à la création du projet avec le rhinolophe : l'extraction des patchs contenait dans un cas particulier une boucle infinie
- permet de changer la formule pour l'extrapolation
- diverse correction et test pour l'extrapolation
- affiche le nom du projet !

##### version 0.4.1 (20/01/2010)
0.4.1-1 : - restructuration de la fenêtre Model
	- bug extrapolation avec raster externe corrigé

##### version 0.4 (07/01/2010)
- dans model ajout  de variable explicative externe provenant de raster
- dans model export en csv des variables explicatives, variable estimée, résidus...

##### version 0.4-beta3 (17/12/2010)
ATTENTION les projets créés à partir de la version 0.4-beta sont erronés au niveau des liens voronoi.

- erreur à la création du projet des liens planaires (voronoi)

##### version 0.4-beta2 (15/12/2010)
ATTENTION le calcul de l'indice local F (anciennement D) était faux !!!

- ajout fonction extrapolation dans model
- erreur de calcul sur local indice D dispersal flux
- l'indice local D est renommé en F
- ajout du calcul de la distance intra patch (à vol d'oiseau) pour les graphes planaires.
- PC et PC_comp fusionne
- ajout de dist et p dans le paramétrage du PC et de F

##### version 0.4-beta (08/12/2010)
ATTENTION cette version n'est pas compatible avec les projets créés par une version précédente !!!!
La mise à niveau d'un projet existant est toujours possible, mais il faut prendre un rdv avec le service de maintenance (51 36)...

- modification de l'édition des couts dans add cost distance, corrige le bug de non prise en compte de la dernière valeur éditée
- ajout d'une fenêtre de propriété pour les liens permettant de retrouver les couts associés
- enregistrement en shp des composantes du graphe (voronoi) du coup les indices calculés par composantes sont aussi enregistrés
- réorganise la création des graphes avec l'option complet (pas de threshold)
- stocke tous les paramètres des distances couts pour les retrouver plus tard dasn propriété
- passe tous les HashMap en TreeMap pour les couts et les graphes
- stocke les paramètres des données exo pour les utiliser dans le modèle
- supprime les shapefile associés au graphe et au exodata quand ceux-ci sont supprimés
- delta indice choix du type d'éléments à tester noeuds et/ou liens
- parallélise le calcul des distances euclidiennes
- ajout de l'indice PC_comp qui calcule le PC sur un graphe complet (pas de calcul de chemin)
- ajout d'une entrée modèle dans data pour estimer la probabilité de présence à partir de données exogènes 

##### version 0.3 (02/12/2010)
- Test khi2 wilks corrigé (degré de liberté était faux) et la proba était inversée
- Calcul du PC en Delta indice corrigé pour que l'aire d'étude reste stable (validé avec conefor)

##### version 0.2.9 (01/12/2010)
- bug pondération du nb d'individu dans Wilks
- ajout pondération en surface de tache dans Wilks
- parallélisation du calcul d'indice local

##### version 0.2.8 (26/11/2010) version corrective de le version 0.2.7
- le calcul des couts en euclidien ne se faisait plus (Error null)
- le tracé du chemin en vectoriel n'incluait pas le premier point de la tâche de départ

##### version 0.2.7 (24/11/2010) version buggée cf. 0.2.8
- bug fenêtre préférence mémoire bloquée à 4Go
- calcul du graphe complet (attention très lourd !)
- les pas dans batch indice peuvent être en nombre de liens

##### version 0.2.6 (12/11/2010)
- bug calcul distance cout quand des couts ont des valeurs à virgule (problème de précision entre double et float)
- bug au chargement de links.csv quand des liens n'existent pas car traverse une tache

##### version 0.2.5 (10/11/2010)
- dans batch ajout des point intermédiaires non calculés
- ajout des distances métriques aux liens + paramètre dans création de graphe et dans batch
- ajout histogram et scatter plot dans menu contextuel des layers
- option suppression des liens qui traversent des taches dans addcostdistance
- quand on supprime une distance cout -> suppression des graphes qui l'utilise

##### version 0.2.4 (22/10/2010)
 - dans add exo data -> test si les points sont en dehors de la zone
 - correction Wilks pour ne pas prendre en compte les patchs sans donnée attributaire

##### version 0.2.3 (21/10/2010)
 - dans batchdialog suppression du bouton estim interval et calcul auto du min et du max à partir de l'ensemble des liens et non plus à partir du MST
 - modif de la structure des indices globaux pour gérer plusieurs résultats
 - gestion des paramètres des indices à part dans Indice -> Param indice...
 - ajout de l'indice de Wilks
 - bug dans calcul des chemins pour indice GD, PC, H, D - l'influence de l'erreur est à priori minime

##### version 0.2.2
 - optimisation du calcul des distances couts : utilise moins de mémoire et se parallélise bien

##### version 0.2.1 (29/09/2010)
 - suppression des points hors zone dans add exo data
 - suppression des chemins traversant des taches dans add cost distance
 - affichage d'un précédent jeu de cout s'il y en a un dans addcostdistance

##### version 0.2 (24/09/2010)
 - indice locaux et indice components
 - graphe en cercle prop
 - export all : exo data avec data patch et data components 

##### Version 0.1 (12/05/2010)
 - version initiale
 - création, chargement et enregistrement du projet
 - format d'import : TIFF ou RST
