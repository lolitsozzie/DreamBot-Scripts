package main;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.methods.walking.pathfinding.impl.obstacle.impl.PassableObstacle;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.items.Item;


import java.awt.*;
import java.util.Random;

@ScriptManifest(author = "lolitsozzie", category = Category.MONEYMAKING, name = "Herb cleaner", version = 1.0)
public class Main extends AbstractScript {
    private Area BURTHROPE_LANDING = new Area(2894, 3555, 2903, 3549);
    private Area HOUSE_LOCATION = new Area(2817, 3557, 2826, 3552);
    private Area CASTLE_WARS = new Area(2446, 3080, 2435, 3098);
    private static final int FINISHED_POTION = 99;
    private static final int CLEANHERB = 2998;
    private static final int DIRTYHERB = 3049;

    private boolean hasClicked = false;
    private Random rng = new Random();
    private int potsMade = 0;
    private int startingGold = 0;
    private int currentGold = 0;
    private Timer timer = new Timer();
    private enum State {
        BANK, MAKE, ERROR
    }

    private State getState() {
        if (getInventory().count(DIRTYHERB) == 28 && !getBank().isOpen()){
            return State.MAKE;
        }
        if (getInventory().isEmpty() ||  getInventory().contains(CLEANHERB) && !getInventory().contains(DIRTYHERB) || getBank().isOpen()) {
            return State.BANK;
        } if (getInventory().count(DIRTYHERB) <= 28 && !getBank().isOpen()){
            return State.MAKE;
        }
        return State.ERROR;
    }

    public void walk(Tile tile) {
        if (getWalking().shouldWalk(5)) {
            getWalking().walk(tile);
        }
    }

    @Override
    public void onStart() {
        log("Started bootbuyer");
    }


    @Override
    public void onPaint(Graphics graphics) {
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Arial", Font.BOLD, 20));
        graphics.drawString("time ran: " + timer.formatTime(), 10, 430);
        graphics.drawString("State:" + getState(), 10, 410);
        graphics.drawString("Herbs :" + potsMade, 10, 290);
    }

    public void cleanHerbs(){


        for(Item i : getInventory().getCollection()) {
            if(i != null && i.getID() == DIRTYHERB) {
                i.interact("Clean");
                sleep(Calculations.random(50, 154));
                potsMade += 1;
            }
        }
    }

    @Override
    public int onLoop() {
        sleep(Calculations.random(150, 250));
        switch (getState()) {
            case BANK:
                hasClicked = false;
                if (!getBank().isOpen()){
                    getBank().open(BankLocation.GRAND_EXCHANGE);
                }
                if (getBank().isOpen()) {
                    if (getInventory().contains(CLEANHERB)){
                        getBank().depositAllItems();

                    }
                    if (getInventory().isEmpty()){
                        getBank().withdraw(DIRTYHERB, 28);
                    }
                    if (getInventory().contains(DIRTYHERB) && getBank().isOpen()){
                        sleep(Calculations.random(250, 500));
                        getBank().close();
                        sleep(Calculations.random(250, 432));
                    }
                }

                break;
            case MAKE:
                if(getInventory().contains(DIRTYHERB)){
                    cleanHerbs();

                }
                break;
        }
        return 1000;
    }
}
