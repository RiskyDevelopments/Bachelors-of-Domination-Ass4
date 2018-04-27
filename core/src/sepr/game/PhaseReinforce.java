package sepr.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import sepr.game.utils.TurnPhaseType;

/**
 * handles input, updating and rendering for the reinforcement phase
 */
public class PhaseReinforce extends Phase {

    /**
     * initialises a reinforcement phase
     *
     * @param gameScreen gamescreen that this phase is setup in
     */
    public PhaseReinforce(GameScreen gameScreen) {
        super(gameScreen, TurnPhaseType.REINFORCEMENT);
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

    @Override
    public void visualisePhase(SpriteBatch batch) {

    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (super.touchUp(screenX, screenY, pointer, button)) {
            return true;
        }

        if (currentPlayer.getTroopsToAllocate() == 0) { // stop allocation as player does not have any more troops to allocate
            AudioPlayer.playAllocationErrorAudio();
            DialogFactory.basicDialogBox(gameScreen, "Allocation Problem", "You have no more troops to allocate", this);
            return true;
        }

        Vector2 worldCoord = gameScreen.screenToWorldCoords(screenX, screenY);
        int sectorId = gameScreen.getMap().detectSectorContainsPoint((int) worldCoord.x, (int) worldCoord.y);

        if (sectorId == -1) { // stop allocation as no sector clicked
            return true;
        }

        Sector selected = gameScreen.getMap().getSectorById(sectorId);
        if (selected.getOwnerId() != currentPlayer.getId()) { // stop allocation as selected sector is not owned by current player
            AudioPlayer.playInvalidMoveAudio();
            DialogFactory.basicDialogBox(gameScreen, "Allocation Problem", "Cannot allocate units to a sector you do not own", this);
            return true;
        }

        DialogFactory.allocateUnitsDialog(gameScreen, currentPlayer.getTroopsToAllocate(), sectorId, this);
        return true;
    }
}
