package sepr.game;

import com.badlogic.gdx.math.Vector2;
import sepr.game.utils.TurnPhaseType;


/**
 * handles input, updating and rendering for the movement phase
 * not implemented
 */
public class PhaseMovement extends PhaseAttackMove {

    public PhaseMovement(GameScreen gameScreen) {
        super(gameScreen, TurnPhaseType.MOVEMENT);
    }

    /**
     * creates a dialog asking the player how many units they want to move
     *
     * @throws RuntimeException if the source sector or target sector are set to null
     */
    private void getNumberOfUnitsToMove(final GameScreen gameScreen) throws RuntimeException {
        if (!sourceSector.canChangeUnits()) {
            DialogFactory.basicDialogBox(gameScreen, "Cannot Move", "Sorry, you cannot move units from this sector currently", this);
            sourceSector = null;
            targetSector = null;
            return;
        }

        if (!targetSector.canChangeUnits()) {
            DialogFactory.basicDialogBox(gameScreen, "Cannot Move", "Sorry, you cannot move units to this sector currently", this);
            sourceSector = null;
            targetSector = null;
            return;
        }

        if (sourceSector == null || targetSector == null) {
            throw new RuntimeException("Cannot execute attack unless both an attacking and defending sector have been selected");
        }
        numOfUnits = new int[1];
        numOfUnits[0] = -1;
        DialogFactory.moveDialog(gameScreen, sourceSector.getUnderGradsInSector(), numOfUnits, this);
    }

    /**
     * carries out movement once number of troops has been set using the dialog
     */
    private void executeMoveTroops() {
        gameScreen.getMap().moveUnits(sourceSector.getId(), targetSector.getId(), numOfUnits[0]);
    }

    /**
     * process a movement if one is being carried out
     */
    @Override
    public void phaseAct() {
        if (sourceSector != null && targetSector != null && numOfUnits[0] != -1) {

            if (numOfUnits[0] != 0 && sourceSector.canChangeUnits() && targetSector.canChangeUnits()) {
                executeMoveTroops();
            }

            // reset attack
            sourceSector = null;
            targetSector = null;
            numOfUnits = null;
        }
    }

    /**
     * @param screenX mouse x position on screen when clicked
     * @param screenY mouse y position on screen when clicked
     * @param pointer pointer to the event
     * @param button  which button was pressed
     * @return if the event has been handled
     */
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (super.touchUp(screenX, screenY, pointer, button)) {
            return true;
        }

        Vector2 worldCoord = gameScreen.screenToWorldCoords(screenX, screenY);

        int sectorId = gameScreen.getMap().detectSectorContainsPoint((int) worldCoord.x, (int) worldCoord.y);
        if (sectorId != -1) { // If selected a sector

            Sector selected = gameScreen.getMap().getSectorById(sectorId); // Current sector


            if (this.sourceSector != null && this.targetSector == null) { // If its the second selection in the movement phase

                if (this.sourceSector.isAdjacentTo(selected) && selected.getOwnerId() == this.currentPlayer.getId()) { // check the player does owns the defending sector and that it is adjacent
                    this.arrowHeadPosition.set(worldCoord.x, worldCoord.y); // Finalise the end position of the arrow
                    this.targetSector = selected;

                    getNumberOfUnitsToMove(gameScreen); // attacking and defending sector selected so find out how many units the player wants to move with
                } else { // cancel the movement as selected defending sector cannot be moved to: may not be adjacent or may be owned by the attacker
                    this.sourceSector = null;
                }

            } else if (selected.getOwnerId() == this.currentPlayer.getId() && selected.getUnderGradsInSector() > 1) { // First selection, is owned by the player and has enough troops
                this.sourceSector = selected;
                this.arrowTailPosition.set(worldCoord.x, worldCoord.y); // set arrow tail position
            } else {
                this.sourceSector = null;
                this.targetSector = null;
            }
        } else { // mouse pressed and not hovered over a sector to attack therefore cancel any movement in progress
            this.sourceSector = null;
            this.targetSector = null;
        }

        return true;
    }
}

