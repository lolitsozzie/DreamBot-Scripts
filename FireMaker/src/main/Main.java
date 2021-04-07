package main;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import sun.font.TrueTypeFont;

import java.awt.*;
import java.util.Arrays;
import java.util.Random;

@ScriptManifest(author = "lolitsozzie", category = Category.FIREMAKING, name = "firemaker v1", version = 1.0)
public class Main extends AbstractScript {
    private Area burningArea = new Area(3211, 3429, 3170, 3428);
    private Tile burnStart1 = new Tile(3212, 3429, 0);
    private Tile burnStart2 = new Tile(3212, 3428, 0);
    private Area burnStartArea = new Area(3212, 3429, 3215, 3428);
    private Area burnStartArea2 = new Area(3212, 3428, 3214, 3428);
    private static final Tile BANK_TILE = new Tile(3254, 3420, 0);
    private Tile starterTile;
    private static final int TINDERBOX_ID = 590;
    private int log_id = 0;
    private Random rng = new Random();
    long x1 = 0;
    long x2 = 0;
    boolean toBurn = true;
    boolean hitStart = true;
    private Timer timer = new Timer();
    private final Color color1 = new Color(102, 102, 102);
    private final Color color2 = new Color(102, 102, 102);
    Point[] lastPositions = new Point[150];
    private final BasicStroke stroke1 = new BasicStroke(1);
    private int topBottopn  = 0;

    private enum State {
        BANK, BURN, FIND_TILES, ERROR
    }

    public enum Logs {
        NORMAL, OAK, WILLOW, TEAK, ARCTIC_PINE, MAPLE, MAHOGANY, YEW, MAGIC, REDWOOD
    }

    private State getState() {
        if (getInventory().isEmpty() || (!getInventory().contains(log_id) || !getInventory().contains(TINDERBOX_ID))) {
            return State.BANK;
        } else if (hitStart && getInventory().contains(log_id)){
            return State.BURN;
        }else if (!hitStart && getInventory().contains(log_id) && getInventory().contains(TINDERBOX_ID) && !getBank().isOpen()) {
            return State.FIND_TILES;
        } else if (getInventory().contains(log_id) && getInventory().contains(TINDERBOX_ID) && hitStart) {
            return State.BURN;
        } else {
            return State.ERROR;
        }
    }

    private Logs getLogs() {
        int skillLevel = getSkills().getRealLevel(Skill.FIREMAKING);
        if (skillLevel < 16) {
            return Logs.NORMAL;
        } else if (skillLevel < 30) {
            return Logs.OAK;
        } else if (skillLevel < 35) {
            return Logs.WILLOW;
        } else if (skillLevel < 42) {
            return Logs.TEAK;
        } else if (skillLevel < 45) {
            return Logs.ARCTIC_PINE;
        } else if (skillLevel < 50) {
            return Logs.MAPLE;
        } else if (skillLevel < 60) {
            return Logs.MAHOGANY;
        } else if (skillLevel < 75) {
            return Logs.YEW;
        } else if (skillLevel < 90) {
            return Logs.MAGIC;
        } else if (skillLevel > 90) {
            return Logs.MAGIC;
        }
        return Logs.MAGIC;
    }

    @Override
    public void onStart() {
        getSkillTracker().start(Skill.FIREMAKING);
        log("Starting script");
    }


    @Override
    public void onPaint(Graphics graphics) {
        graphics.setColor(Color.GREEN);
        graphics.setFont(new Font("Times New Roman", Font.BOLD, 20));
        Polygon tile = getMap().getPolygon(starterTile);
        if (tile != null) {
            graphics.setColor(new Color(255, 255, 0));
            graphics.drawPolygon(tile);
            graphics.setColor(new Color(0, 255, 0));
            graphics.drawPolygon(getMap().getPolygon(getLocalPlayer().getTile()));
        }
        Graphics2D g = (Graphics2D) graphics;

        g.setColor(color1);
        g.fillRoundRect(1, 338, 515, 139, 16, 16);
        g.setColor(color2);
        g.setStroke(stroke1);
        g.drawRoundRect(1, 338, 515, 139, 16, 16);
        g.setColor(Color.WHITE);
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Arial", Font.BOLD, 20));
        graphics.drawString("fm xp/hr: " + getSkillTracker().getGainedExperiencePerHour(Skill.FIREMAKING) / 1000 + "k", 10, 470);
        graphics.drawString("state: " + getState(), 10, 450);
        graphics.drawString("time ran: " + timer.formatTime(), 10, 430);
        graphics.drawString("xp gained: " + getSkillTracker().getGainedExperience(Skill.FIREMAKING), 10, 410);
        graphics.setColor(Color.BLACK);
        graphics.drawString("levels gained: " + getSkillTracker().getGainedLevels(Skill.FIREMAKING), 210, 470);
        graphics.drawString("time to level: " + Timer.formatTime(getSkillTracker().getTimeToLevel(Skill.FIREMAKING)), 210, 450);
        graphics.drawString("level: " + getSkills().getRealLevel(Skill.FIREMAKING), 210, 430);
        Point currentPosition = getMouse().getPosition();

// Shift all elements down and insert the new element
        for (int i = 0; i < lastPositions.length - 1; i++) {
            lastPositions[i] = lastPositions[i + 1];
        }
        lastPositions[lastPositions.length - 1] = new Point(currentPosition.x, currentPosition.y);

// This is the point before the new point to draw to
        Point lastpoint = null;

        Color mColor = Color.WHITE;
//Go in reverse
        for (int i = lastPositions.length - 1; i >= 0; i--) {
            Point p = lastPositions[i];
            if (p != null) {
                if (lastpoint == null)
                    lastpoint = p;

                g.setColor(mColor);
                g.drawLine(lastpoint.x, lastpoint.y, p.x, p.y);
            }
            lastpoint = p;

            //Every 2 steps - mouse fade out
            if (i % 2 == 0)
                mColor = mColor.darker();
        }

        g.setColor(Color.BLACK);
        g.drawRect(currentPosition.x - 3, currentPosition.y - 3, 7, 7);
        g.setColor(Color.WHITE);
        g.drawRect(currentPosition.x, currentPosition.y, 1, 1);
        for (NPC neartile : getNpcs().all()) {
            if (neartile.getID() == 1526) {
                tile = getMap().getPolygon(neartile.getTile());
                if (tile != null) {
                    graphics.setColor(new Color(255, 255, 0));
                    graphics.drawPolygon(tile);
                    graphics.setColor(new Color(0, 255, 0));
                    graphics.drawPolygon(getMap().getPolygon(getLocalPlayer().getTile()));
                }
            }
        }
        if (tile != null) {
            graphics.setColor(new Color(255, 255, 0));
            graphics.drawPolygon(tile);
            graphics.setColor(new Color(0, 255, 0));
            graphics.drawPolygon(getMap().getPolygon(getLocalPlayer().getTile()));
        }

    }

    @Override
    public int onLoop() {
        switch (getLogs()) {
            case NORMAL:
                log_id = 1511;
                break;
            case OAK:
                log_id = 1521;
                break;
            case WILLOW:
                log_id = 1519;
                break;
            case TEAK:
                log_id = 6333;
                break;
            case ARCTIC_PINE:
                log_id = 10810;
                break;
            case MAPLE:
                log_id = 1517;
                break;
            case MAHOGANY:
                log_id = 6332;
                break;
            case YEW:
                log_id = 1515;
                break;
            case MAGIC:
                log_id = 1513;
            case REDWOOD:
                log_id = 19669;
        }
        log_id = 1513;
        switch (getState()) {
            case BANK:
                hitStart = false;

                int toSleep = rng.nextInt(1348) + 300;
                log("Sleeping " + String.valueOf(toSleep));
                sleep(toSleep);
                if (!BankLocation.VARROCK_WEST.getArea(4).contains(getLocalPlayer())) {
                    if (getWalking().shouldWalk(rng.nextInt(4) + 4)) {

                        getWalking().walk(BankLocation.VARROCK_WEST.getArea(3).getRandomTile());
                    }
                } else {
                    log("standing on  bank tile");
                    getBank().open(BankLocation.VARROCK_WEST);
                    if (getBank().isOpen()) {
                        if (getInventory().contains(TINDERBOX_ID)) {
                            sleep(rng.nextInt(500) + rng.nextInt(100) + 300);
                            getBank().withdrawAll(log_id);
                            sleep(rng.nextInt(500) + rng.nextInt(100) + 300);
                        } else {
                            sleep(rng.nextInt(500) + rng.nextInt(100) + 300);
                            getBank().withdraw(TINDERBOX_ID);
                            sleep(rng.nextInt(500) + rng.nextInt(100) + 300);
                            getBank().withdrawAll(log_id);
                            sleep(rng.nextInt(500) + rng.nextInt(100) + 300);
                        }
                        getBank().close();
                    }
                }
                break;
            case FIND_TILES:
                starterTile = burnStartArea.getRandomTile();
                if (starterTile != null) {
                    if (!hitStart) {
                        getWalking().walk(starterTile);
                        sleep(rng.nextInt(500) + 1932);
                    }
                }
                if (burnStartArea.contains(getLocalPlayer())){
                    hitStart = true;
                }


                break;
            case BURN:


                log("case burn");
                if (getDialogues().canContinue()) {
                    for (GroundItem gi : getGroundItems().getItemsOnTile(getLocalPlayer().getTile())) {
                        if (gi.getID() == log_id) {
                            getInventory().get(TINDERBOX_ID).useOn(gi);
                        }
                    }
                }

                if ((getLocalPlayer().isMoving() || ((burnStartArea.contains(getLocalPlayer()) || burningArea.contains(getLocalPlayer())) && (getInventory().count(log_id) == 27)) || getInventory().count(log_id) <= 27)) {
                    Item logs = getInventory().get(log_id);
                    sleep(rng.nextInt(9) + 845);
                    logs.useOn(TINDERBOX_ID);
                } else {
                    for (GameObject go : getGameObjects().getObjectsOnTile(getLocalPlayer().getTile())) {
                        if (go.getID() == 26185) {
                            log("On top of fire or otherwise broken");
                        }
                    }
                }
        }
        return 1000;
    }

}