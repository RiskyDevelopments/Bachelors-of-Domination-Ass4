package sepr.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import sepr.game.utils.TurnPhaseType;

import java.util.Random;

/**
 * handles input, updating and rendering for the reinforcement phase
 */
public class PhaseReinforce extends Phase {
    private int[] allocateUnits; // 3 index array storing : [0] number of undergraduate; [1] number of postgraduates; [2] id of sector to allocate to

    private Random random;

    public PhaseReinforce(GameScreen gameScreen) {
        super(gameScreen, TurnPhaseType.REINFORCEMENT);
        random = new Random();
    }

    @Override
    void enterPhase(Player player) {
        super.enterPhase(player);

        updateTroopReinforcementLabel();
        DialogFactory.nextTurnDialogBox(gameScreen, currentPlayer.getPlayerName(), currentPlayer.getTroopsToAllocate(), this);
    }

    @Override
    public void endPhase() {
        currentPlayer.setTroopsToAllocate(5); // any unallocated units are removed and 5 are set for next turn
        super.endPhase();
    }

    /**
     * checks if the user has completed the unit allocation dialog
     */
    private void detectUnitAllocation() {
        if (allocateUnits != null) { // check that an allocation has been initiated
            if (allocateUnits[2] == -1 || (allocateUnits[0] == 0 && allocateUnits[1] == 0)) { // cancel allocation if sector id set to -1 or 0 units are allocated
                allocateUnits = null;
            } else if (allocateUnits[0] != -1 || allocateUnits[1] != -1) {
                gameScreen.getMap().addUnitsToSectorAnimated(allocateUnits[2], allocateUnits[0], allocateUnits[1]);
                currentPlayer.addTroopsToAllocate(-(allocateUnits[0] + (allocateUnits[1]*2)));
                allocateUnits = null;
                updateTroopReinforcementLabel();
            }
        }
    }

    @Override
    public void phaseAct() {
        detectUnitAllocation();
    }

    @Override
    public void visualisePhase(SpriteBatch batch) {

    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (super.touchUp(screenX, screenY, pointer, button)) {
            return true;
        }

        Vector2 worldCoord = gameScreen.screenToWorldCoords(screenX, screenY);

        int sectorId = gameScreen.getMap().detectSectorContainsPoint((int)worldCoord.x, (int)worldCoord.y);
        if (sectorId != -1) { // If selected a sector
            if (gameScreen.getMap().getSectorById(sectorId).canChangeUnits()) { // check if sector can have units allocated to it
                if (currentPlayer.getTroopsToAllocate() <= 0) { // check the player still has units to allocate
                    int voice = random.nextInt(2);

                    if (voice == 0) {
                        AudioPlayer.playInsufficientGangMembersAudio();
                    } else {
                        invalidMove();
                    }

                    DialogFactory.basicDialogBox(gameScreen,"Allocation Problem", "You have no more troops to allocate", this);
                } else if (gameScreen.getMap().getSectorById(sectorId).getOwnerId() != currentPlayer.getId()) { // check the player has chosen to add units to their own sector
                    invalidMove();
                    DialogFactory.basicDialogBox(gameScreen, "Allocation Problem", "Cannot allocate units to a sector you do not own", this);
                } else {
                        // setup allocation form
                        allocateUnits = new int[3];
                        allocateUnits[0] = -1;
                        allocateUnits[1] = -1;
                        allocateUnits[2] = sectorId;
                        DialogFactory.allocateUnitsDialog(gameScreen, currentPlayer.getTroopsToAllocate(), allocateUnits, this);
                }
            } else {
                invalidMove();
                DialogFactory.basicDialogBox(gameScreen, "Cannot Reinforce", "Sorry, this sector currently cannot accept reinforcements", this);
            }
        }
        return false;
    }

    private void invalidMove(){
        AudioPlayer.playAllocationErrorAudio();
    }
}
