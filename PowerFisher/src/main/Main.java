package main;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.utilities.Timer;


import java.awt.*;
import java.util.Random;

@ScriptManifest(author = "lolitsozzie", category = Category.FISHING, name = "Barbarian fisher", version = 1.0)
public class Main extends AbstractScript {
    private static final Tile FISHING_TILES = new Tile(3105, 3430, 0);
    private static final Tile BANK_TILES = new Tile(3092, 3242, 0);
    Random rng = new Random();
    private int state = 0;
    boolean test;
    private Tile toPaint;
    private NPC fishSpot;
    private Timer timer = new Timer();
    private final Color color1 = new Color(102, 102, 102);
    private final Color color2 = new Color(102, 102, 102);
    Point[] lastPositions = new Point[150];

    private final BasicStroke stroke1 = new BasicStroke(1);


    @Override
    public void onStart() {
        getSkillTracker().start(Skill.FISHING);
        getClient().getInstance().setDrawMouse(false);
    }


    @Override
    public void onPaint(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setColor(color1);
        g.fillRoundRect(1, 338, 515, 139, 16, 16);
        g.setColor(color2);
        g.setStroke(stroke1);
        g.drawRoundRect(1, 338, 515, 139, 16, 16);
        g.setColor(Color.WHITE);
        graphics.setColor(Color.GREEN);
        graphics.setFont(new Font("Arial", Font.BOLD, 20));
        graphics.drawString("fishing xp/hr: " + getSkillTracker().getGainedExperiencePerHour(Skill.FISHING) / 1000 + "k", 10, 470);
        graphics.drawString("time ran: " + timer.formatTime(), 10, 450);
        graphics.drawString("xp gained: " + getSkillTracker().getGainedExperience(Skill.FISHING), 10, 430);
        graphics.setColor(Color.YELLOW);
        graphics.drawString("levels gained: " + getSkillTracker().getGainedLevels(Skill.FISHING), 210, 470);
        graphics.drawString("time to level: " + Timer.formatTime(getSkillTracker().getTimeToLevel(Skill.FISHING)), 210, 450);
        graphics.drawString("level: " + getSkills().getRealLevel(Skill.FISHING), 210, 430);
        Polygon tile = getMap().getPolygon(toPaint);
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
        sleep(rng.nextInt(245) + 175);
        if (state == 0) {
            if (FISHING_TILES.distance() > 10) {
                getWalking().walk(FISHING_TILES);
            }
            fish(); //chopping anim: 867
        } else if (state == 1) {
            bank();
        }
        return 1000;
    }

    @Override
    public void onExit() {
        getClient().getInstance().setDrawMouse(true);
    }

    public void fish() {
        if (getInventory().isFull()) {
            state = 1;
        } else {
            fishSpot = getNpcs().closest(1526);
            if (fishSpot.distance() < 25 && !getLocalPlayer().isAnimating()) {
                test = rng.nextBoolean();
                if (test) {
                    if (fishSpot.isOnScreen()) {
                        fishSpot.interactForceLeft("Lure");
                    }
                } else {
                    fishSpot.interactForceRight("Lure");
                }
            }
        }
    }

    public void bank() {
        if (getInventory().isFull()) {

            getInventory().dropAllExcept("Fly fishing rod", "Feather", "Heron");
            getInventory().dropAll("Raw trout");
            state = 0;
        } else state = 1;
    }

}
