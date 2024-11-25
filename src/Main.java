import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.utilities.Timer;

import java.awt.*;
import java.util.function.Supplier;

import static org.dreambot.api.utilities.Timer.formatTime;

@ScriptManifest(
        category = Category.COMBAT,
        name = "Lumbridge Cow Killer",
        author = "ThePoff",
        version = 1.0
)
public class Main extends AbstractScript {

    Area cowArea = new Area(3265, 3255, 3246, 3297);
    private int cowsKilled = 0;
    private long startTime;
    private Timer timer;

    // Variable pour l'état du joueur
    private String playerState = "Ready"; // Possible valeurs : "Walking", "Attacking", "Ready"

    @Override
    public void onStart() {
        startTime = System.currentTimeMillis();
        timer = new Timer(0); // Crée un timer pour le temps d'exécution
    }

    @Override
    public int onLoop() {
        // Étape 1 : Vérifier et équiper les items requis
        if (!Equipment.containsAll("Bronze sword") || !Equipment.containsAll("Wooden shield")) {
            log("[INFO]: Not Equipped with required items.");
            equipItems();
            playerState = "Getting Items"; // Mise à jour de l'état
            return 250; // Diminuer le délai pour plus de réactivité
        }

        // Étape 2 : Marcher vers la zone des vaches si le joueur n'y est pas déjà
        if (!cowArea.contains(Players.getLocal())) {
            log("[INFO]: Walking to Cow Area.");
            Walking.walk(cowArea.getRandomTile());
            playerState = "Walking"; // Mise à jour de l'état
            sleep(250); // Réduire le délai pour rendre le bot plus réactif
            return 500;
        }

        // Étape 3 : Trouver et attaquer une vache
        playerState = "Attacking"; // Mise à jour de l'état
        log("[INFO]: Attacking Cow.");
        attackCow();
        return 500; // Réduire le délai pour rendre le bot plus réactif
    }

    @Override
    public void onPaint(Graphics g) {
        // Dessiner un fond noir
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 115, 90);  // Positionner et dessiner le fond noir

        // Dessiner le titre du script
        g.setColor(Color.WHITE);
        g.drawString("F2P - Cow Killer", 10, 20);

        // Calculer et afficher le temps d'exécution
        long elapsedTime = System.currentTimeMillis() - startTime;
        String formattedTime = formatTime(elapsedTime);
        g.drawString("Time: " + formattedTime, 10, 40);

        // Afficher le nombre de vaches tuées
        g.drawString("Cows Killed: " + cowsKilled, 10, 60);

        // Afficher l'état actuel du joueur
        g.drawString("Status: " + playerState, 10, 80);  // Affichage de l'état
    }

    private void equipItems() {
        if (Inventory.contains("Bronze sword") && Inventory.contains("Wooden shield")) {
            Inventory.get("Bronze sword").interact("Wield");
            sleep(250); // Réduit le délai pour plus de réactivité
            Inventory.get("Wooden shield").interact("Wield");
            sleep(250);
        } else {
            if (!Bank.isOpen()) {
                Bank.open();
                sleepUntil(Bank::isOpen, 3000); // Attendre que la banque s'ouvre
            } else {
                if (Bank.contains("Bronze sword")) {
                    Bank.withdraw("Bronze sword", 1);
                    sleep(250);
                }
                if (Bank.contains("Wooden shield")) {
                    Bank.withdraw("Wooden shield", 1);
                    sleep(250);
                }
                Bank.close();
            }
        }
    }

    private void sleepUntil(Supplier<Boolean> condition, int timeout) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeout) {
            if (condition.get()) {
                break;
            }
            sleep(50); // Petit délai entre chaque vérification
        }
    }

    private void attackCow() {
        NPC cow = NPCs.closest(npc -> npc != null && npc.getName().equals("Cow") && !npc.isInCombat());

        if (cow != null) {
            if (!Players.getLocal().isInCombat()) {
                cow.interact("Attack");
                sleepUntil(() -> !cow.exists() || Players.getLocal().isInCombat(), 3000); // Réduit le délai pour rendre le bot plus réactif
                cowsKilled++;
            }
        }
    }
}
