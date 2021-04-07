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


import java.awt.*;
import java.util.Random;

@ScriptManifest(author = "lolitsozzie", category = Category.MONEYMAKING, name = "Pot maker", version = 1.0)
public class Main extends AbstractScript {
    private Area BURTHROPE_LANDING = new Area(2894, 3555, 2903, 3549);
    private Area HOUSE_LOCATION = new Area(2817, 3557, 2826, 3552);
    private Area CASTLE_WARS = new Area(2446, 3080, 2435, 3098);
    private static final int FINISHED_POTION = 99;
    private static final int WATER = 227;
    private static final int HERB = 257;

    private boolean hasClicked = false;
    private Random rng = new Random();
    private int potsMade = 0;
    private int startingGold = 0;
    private int currentGold = 0;
    private Timer timer = new Timer();
    private enum State {
        BANK, MAKE, ERROR, WAIT
    }

    private State getState() {
        if (getInventory().isEmpty() || getInventory().count(FINISHED_POTION) == 14 || getBank().isOpen()) {
            return State.BANK;
        } if (getInventory().count(WATER) == 14 && getInventory().contains(HERB) && !getBank().isOpen()){
            return State.MAKE;
        } if (getInventory().contains(HERB) || getLocalPlayer().getAnimation() == 363){
            return State.WAIT;
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
        graphics.drawString("Pots :" + potsMade, 10, 290);
    }

    @Override
    public int onLoop() {
        sleep(rng.nextInt(150) + 142);
        switch (getState()) {
            case BANK:
                hasClicked = false;
                if (!getBank().isOpen()){
                    if (getInventory().contains(FINISHED_POTION) || getInventory().isEmpty()){
                        getBank().open(BankLocation.GRAND_EXCHANGE);
                    }
                }
                if (getBank().isOpen()) {
                    if (getInventory().contains(FINISHED_POTION)){
                        getBank().depositAllItems();
                        potsMade += 14;
                    }
                    if (getInventory().isEmpty()){
                        getBank().withdraw(WATER, 14);
                    }
                    if (getInventory().contains(WATER) && !getInventory().contains(HERB)){
                        getBank().withdraw(HERB, 14);
                    }
                    if (getInventory().contains(WATER) && getInventory().contains(HERB) && getBank().isOpen()){
                        getBank().close();
                    }
                }

                break;
            case MAKE:
                    Widget makeWidget = getWidgets().getWidget(270);
                    if (getInventory().count(WATER) == 14 && !hasClicked) {
                        getInventory().interact(WATER, "Use");
                        sleep(rng.nextInt(174) + 500);
                        getInventory().interact(HERB, "Use");
                        hasClicked = true;
                    }
                    if (makeWidget.isVisible()){
                        if(getWidgets().getWidget(270).getChild(14) != null){
                            getWidgets().getWidget(270).getChild(14).interact();
                        }
                    }

                break;
            case WAIT:
                sleep(rng.nextInt(800) + 800);
                break;
        }
        return 1000;
    }
}
