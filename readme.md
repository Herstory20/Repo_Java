Bonjour,

Voici notre projet concernant un système de clavardage.
Pour lancer le projet, il vous faut :
- soit ouvrir les sources et compiler src/Clavardeur.java avec Eclipse. Dans ce cas, à priori, il suffit d'ouvrir le projet en tant que projet maven et tout sera installé automatiquement.
- soit se placer dans le dossier target/ et exécuter :
    - mvn compile
    - mvn exec:java
    dans ce cas, il faut installer maven sur la machine. Pour cela, il faut télécharger un Jar sur internet puis l'ajouter au PATH avec la commande suivante :
    - export path="$PATH:/[votre chemin vers le dossier décompressé]"

Il est à noter que si vous souhaitez compiler le programme sur plusieurs machines en même temps, c'est possible mais cela nécessite 2 choses :
    - utiliser ssh avec l'option -X pour permettre la récupération de l'affichage de la machine distante sur votre écran (ssh est à sens unique sinon, -X assure la transmission dans les deux sens)
    - faire attention à ne pas les exécuter dans le même dossier avec le ssh, car la base de données sera partagée dans ce cas, ce qui donnera une exécution peu représentative d'une exécution classique.
    
Nous vous invitons à nous contacter si vous avez un quelconque problème avec nos fichiers aux adresses suivantes :
    <lvendevi@insa-toulouse.fr>
    <bzhong@insa-toulouse.fr>


Bonne journée à vous, et bon clavardage !




Brandon Zhong
Léo Vendeville


