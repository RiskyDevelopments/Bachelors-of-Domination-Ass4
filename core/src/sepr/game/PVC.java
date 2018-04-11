package sepr.game;

import java.util.Random;


/**
 * base class for storing Neutral and Human player data
 */
public class PVC {

    private float spawnChance; //likelihood that the PVC will spawn , between 0 and 1
    // 0 = 0% ; 1 = 100%
    private boolean PVCSpawned = false;
    private GameScreen gameScreen;

    /**
     * creates a PVC object with the specified properties
     *
     * @param spawnChance player's unique identifier
     * @param gameScreen  used to start the mini game
     */


    public PVC(float spawnChance, GameScreen gameScreen) {
        this.spawnChance = spawnChance;
        this.gameScreen = gameScreen;
    }


    /**
     * @return true or false depending if the random float value is less than the spawn chance
     */
    public boolean PVCSpawn() {
        Random rand = new Random();
        Float randomValue = rand.nextFloat();
        if (randomValue <= spawnChance && (!PVCSpawned)) {

            return true;
        }
        return false;
    }


    /**
     * starts the mini game
     */
    public void startMiniGame() {
        gameScreen.openMiniGame();
    }


    /**
     * @return if the PVC has been spawned
     */

    public boolean isPVCSpawned() {
        return PVCSpawned;
    }

    public void setPVCSpawned(boolean value) {
        this.PVCSpawned = value;
    }

    public float getSpawnChance() {
        return spawnChance;
    }

    public void setSpawnChance(float spawnChance) {
        this.spawnChance = spawnChance;
    }
}
