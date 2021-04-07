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

import java.awt.*;
import java.util.Random;

@ScriptManifest(author = "lolitsozzie", category = Category.WOODCUTTING, name = "Draynor willow slayer", version = 1.0)
public class Main  extends AbstractScript{
    private static final Tile CHOPPING_TILES = new Tile(3087, 3234, 0);
    private static final Tile BANK_TILES = new Tile(3092, 3242, 0);
    Random rng = new Random();
    private int state = 0;
    private int beginningXP;
    private int currentXP;
    private int xpGained;

    @Override
    public void onStart() {
        getSkillTracker().start(Skill.WOODCUTTING);
    }


    @Override
    public void onPaint(Graphics graphics) {
        graphics.drawString("WoodCutting XP/Hr: " + getSkillTracker().getGainedExperiencePerHour(Skill.WOODCUTTING) / 1000 + "k", 10, 335);
    }

    @Override
    public int onLoop(){
        sleep(rng.nextInt(500) + 250 );
        if (state == 0) {
            if (CHOPPING_TILES.distance() > 7){
                getWalking().walk(CHOPPING_TILES);
            }
            chop(); //chopping anim: 867
        } else if (state == 1) {
            bank();
        }
        return 1000;
    }


    public void chop() {
        if(getInventory().isFull()){
            state = 1;
        } else {
            GameObject tree = getGameObjects().closest("Willow");
            if(getLocalPlayer().getAnimation() != 867) {
                log("Chopping new tree.");
                tree.interact("Chop down");
            }


            sleepWhile(tree::exists, Calculations.random(5000, 10497));
        }
    }

    public void bank() {
        if(getInventory().isFull()){
            getInventory().dropAll();
            state = 0;
        }
        else state = 1;
    }

}
