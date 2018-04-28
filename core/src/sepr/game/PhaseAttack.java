package sepr.game;

import sepr.game.utils.TurnPhaseType;

/*
Modified in assessment 4
 - refactored attacking system so more behaviour is moved to PhaseAttackMove to reduce code duplication
 - implemented PhaseAttackMove methods for
   - checking if a sector is a valid source for an attack - isValidSource(Sector sourceSector)
   - checking if a sector is a valid target for an attack - isValidTarget(Sector targetSector)
   - executing an attack when a valid source and target have been found
 - refactored passing attack data through arrays as difficult to read and follow
 - changed to using AudioPlayer for playing audio instead of using instance of AudioManager
 */

/**
 * handles input, updating and rendering for the attack phase
 */
public class PhaseAttack extends PhaseAttackMove{

    /**
     * initialises an attack phase
     *
     * @param gameScreen gamescreen that this phase is setup in
     */
    public PhaseAttack(GameScreen gameScreen) {
        super(gameScreen, TurnPhaseType.ATTACK);
    }

    @Override
    protected void onArrowCreated(Sector sourceSector, Sector targetSector) {
        DialogFactory.attackDialog(gameScreen, sourceSector.getUnderGradsInSector(), targetSector.getUnderGradsInSector(), sourceSector, targetSector, this);
    }

    @Override
    protected boolean isValidSource(Sector sourceSector) {
        if (currentPlayer.getId() == sourceSector.getOwnerId() && sourceSector.canAttack()) {
            return true;
        } else {
            DialogFactory.basicDialogBox(gameScreen, "Cannot attack", "Sorry, you cannot attack from this sector.", this);
            return false;
        }
    }

    @Override
    protected boolean isValidTarget(Sector sourceSector, Sector targetSector) {
        if (currentPlayer.getId() != targetSector.getOwnerId() && targetSector.canBeAttacked() && sourceSector.isAdjacentTo(targetSector)) {
            return true;
        } else {
            DialogFactory.basicDialogBox(gameScreen, "Cannot attack", "Sorry, you cannot attack this sector.", this);
            return false;
        }
    }
}
