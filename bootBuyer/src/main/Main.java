package main;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.methods.walking.pathfinding.impl.obstacle.impl.PassableObstacle;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.utilities.Timer;


import java.awt.*;
import java.util.Random;

@ScriptManifest(author = "lolitsozzie", category = Category.MONEYMAKING, name = "BOOT BUYER", version = 1.0)
public class Main extends AbstractScript {
    private Area BURTHROPE_LANDING = new Area(2894, 3555, 2903, 3549);
    private Area HOUSE_LOCATION = new Area(2817, 3557, 2826, 3552);
    private Area CASTLE_WARS = new Area(2446, 3080, 2435, 3098);
    private static final int GAMES_NECLACE_8 = 3853;
    private static final int GAMES_NECLACE_1 = 3867;
    private static final int RING_OF_DUELING_8 = 2552;
    private static final int RING_OF_DUELING_1 = 2566;
    private static final int COINS = 995;
    private static final int CLIMBING_BOOTS = 3105;
    private boolean checkedGates = false;
    private Random rng = new Random();
    private int bootsPurchase = 0;
    private int startingGold = 0;
    private int currentGold = 0;
    private Timer timer = new Timer();
    private enum State {
        BANK, BUY, WALKTO, WALKFROM, GATE, ERROR
    }

    private State getState() {
        if (CASTLE_WARS.contains(getLocalPlayer()) && getInventory().contains(COINS) && getInventory().emptySlotCount() != 27) {
        return State.BANK;
        }
        if (getInventory().count(CLIMBING_BOOTS) == 27){
            return State.WALKFROM;
        }
        if (!HOUSE_LOCATION.contains(getLocalPlayer().getTile()) && !getBank().isOpen() && getInventory().contains(COINS) && getInventory().getEmptySlots() == 27){
            return State.WALKTO;
        }
        if (HOUSE_LOCATION.contains(getLocalPlayer().getTile()) && !checkedGates) {
            return State.GATE;
        }
        if ( checkedGates && getInventory().count(CLIMBING_BOOTS) < 27) {
            return State.BUY;
        }

        return State.ERROR;
    }

    public void walk(Tile tile) {
        if (getWalking().shouldWalk(3)) {
            getWalking().walk(tile);
        }
    }

    @Override
    public void onStart() {
        startingGold = getInventory().count(COINS);
        log("Started bootbuyer");
    }


    @Override
    public void onPaint(Graphics graphics) {
        currentGold = getInventory().count(COINS);
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Arial", Font.BOLD, 20));
        graphics.drawString("time ran: " + timer.formatTime(), 10, 430);
        graphics.drawString("boots bought: " + (startingGold - currentGold) / 12 + "", 10, 470);

    }

    @Override
    public int onLoop() {
        sleep(15);
        switch (getState()) {
            case BANK:
                log("bank");
                checkedGates = false;
                if (!getBank().isOpen() && getInventory().contains(CLIMBING_BOOTS)) {
                    getBank().open(BankLocation.CASTLE_WARS);

                }
                if (getBank().isOpen()) {
                    if (getInventory().contains(CLIMBING_BOOTS)) {
                        getBank().depositAll(CLIMBING_BOOTS);
                    }
                    if ((getEquipment().contains(RING_OF_DUELING_1) || getEquipment().contains(GAMES_NECLACE_1)) || getEquipment().isEmpty()) {
                        getBank().depositAllEquipment();
                        getBank().withdraw(GAMES_NECLACE_8);
                        getBank().withdraw(RING_OF_DUELING_8);
                    }
                    getBank().close();
                }
                if (!getBank().isOpen() && !getInventory().contains(CLIMBING_BOOTS)) {
                    if (getInventory().contains(RING_OF_DUELING_8)) {
                        getInventory().interact(RING_OF_DUELING_8, "Wear");
                    }
                    if (getInventory().contains(GAMES_NECLACE_8)) {
                        getInventory().interact(GAMES_NECLACE_8, "Wear");
                    }
                }
                break;
            case WALKTO:
                log("walkto");
                if (CASTLE_WARS.contains(getLocalPlayer().getTile())) {
                    getEquipment().interact(EquipmentSlot.AMULET, "Burthorpe");
                    sleep(rng.nextInt(1500) + 2000);
                }
                if (!HOUSE_LOCATION.contains(getLocalPlayer().getTile())) {
                    walk(HOUSE_LOCATION.getRandomTile());
                }
                break;
            case GATE:
                log("gate3");
                GameObject door = getGameObjects().getTopObjectOnTile(new Tile(2822, 3555, 0));
                GameObject gate = getGameObjects().closest("Gate");
                if (gate.hasAction("Open")){
                    log("opening gate");
                    gate.interact();
                    sleep(rng.nextInt(2000) + 1);
                    checkedGates = true;
                }
                if (gate.hasAction("Close")) {
                    sleep(rng.nextInt(2000) + 3000);
                    door.interact();
                    checkedGates = true;
                }
                break;
            case BUY:
                sleep(rng.nextInt(rng.nextInt(25)) + rng.nextInt(25) + 60 );
                log("buy");
                NPC seller = getNpcs().closest(4094);
                if(!getMap().canReach(seller)){
                    GameObject door2 = getGameObjects().getTopObjectOnTile(new Tile(2822, 3555, 0));
                    door2.interact();
                }
                if(!getDialogues().inDialogue()){
                    seller.interact();
                }
                if (getDialogues().inDialogue()){
                    if (getDialogues().spaceToContinue()){
                        getDialogues().spaceToContinue();
                    }
                    if (getDialogues().chooseOption(1)){
                        getDialogues().spaceToContinue();
                    }
                }
                break;
            case WALKFROM:
                log("walkfrom");
                getEquipment().interact(EquipmentSlot.RING, "Castle Wars");
                sleep(rng.nextInt(500) + 1500);
                break;
        }
        return 1000;
    }
}
