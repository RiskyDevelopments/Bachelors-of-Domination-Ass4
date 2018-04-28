package sepr.game;

import sepr.game.utils.TurnPhaseType;

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
