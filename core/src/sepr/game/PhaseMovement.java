package sepr.game;

import sepr.game.utils.TurnPhaseType;


/**
 * handles input, updating and rendering for the movement phase
 * not implemented
 */
public class PhaseMovement extends PhaseAttackMove {

    /**
     * initialises a movement phase
     *
     * @param gameScreen gamescreen that this phase is setup in
     */
    public PhaseMovement(GameScreen gameScreen) {
        super(gameScreen, TurnPhaseType.MOVEMENT);
    }

    @Override
    protected void onArrowCreated(Sector sourceSector, Sector targetSector) {
        DialogFactory.moveDialog(gameScreen, sourceSector.getId(), targetSector.getId(), sourceSector.getUnderGradsInSector(), this);
    }

    @Override
    protected boolean isValidSource(Sector sourceSector) {
        if (currentPlayer.getId() == sourceSector.getOwnerId() && sourceSector.canChangeUnits()) {
            return true;
        } else {
            DialogFactory.basicDialogBox(gameScreen, "Cannot move!", "Sorry, you cannot move units from this sector.", this);
            return false;
        }
    }

    @Override
    protected boolean isValidTarget(Sector sourceSector, Sector targetSector) {
        if (currentPlayer.getId() == targetSector.getOwnerId() && targetSector.canChangeUnits() && sourceSector.isAdjacentTo(targetSector) && sourceSector.getId() != targetSector.getId()) {
            return true;
        } else {
            DialogFactory.basicDialogBox(gameScreen, "Cannot move!", "Sorry, you cannot move units to this sector.", this);
            return false;
        }
    }
}

