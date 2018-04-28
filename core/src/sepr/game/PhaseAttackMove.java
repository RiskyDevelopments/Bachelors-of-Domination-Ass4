package sepr.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import sepr.game.utils.TurnPhaseType;

public abstract class PhaseAttackMove extends Phase {
    private TextureRegion arrow; // TextureRegion for rendering attack visualisation
    private Sector sourceSector; // Stores the sector being used to attack in the attack phase (could store as ID and lookup object each time to save memory)

    private Vector2 arrowTailPosition; // Vector x,y for the base of the arrow

    /**
     *
     * @param gameScreen gamescreen that this phase is part of
     * @param turnPhaseType type of phase this is, must be ATTACK or MOVEMENT
     *
     * @throws IllegalArgumentException if turnPhaseType is not ATTACK or MOVEMENT
     */
    public PhaseAttackMove(GameScreen gameScreen, TurnPhaseType turnPhaseType){
        super(gameScreen, turnPhaseType);
        if (turnPhaseType == TurnPhaseType.REINFORCEMENT) throw new IllegalArgumentException("PhaseAttackMove must have turnPhaseType ATTACK or MOVE");
        this.arrow = new TextureRegion(new Texture(Gdx.files.internal("uiComponents/arrow.png")));
        this.sourceSector = null;

        this.arrowTailPosition = new Vector2();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (super.touchDown(screenX, screenY, pointer, button)) {
            return true;
        }
        return false;
    }

    @Override
    public void endPhase() {
        super.endPhase();
        sourceSector = null;

        this.arrowTailPosition = new Vector2();
    }

    /**
     * sets the source sector of an attack/movement to null
     */
    public void resetSourceSectors() {
        sourceSector = null;
    }

    /**
     * called when an arrow has successfully been created between a source and target sector so an attack/movement can be carried out
     *
     * @param sourceSector source sector of the attack/movement
     * @param targetSector target sector of the attack/movement
     */
    protected abstract void onArrowCreated(Sector sourceSector, Sector targetSector);

    /**
     * returns if this is a valid sector for the source of an attack/movement
     * notifies the player using a dialog if invalid
     *
     * @param sourceSector sector that is to be used as a source
     * @return true if valid source else false
     */
    protected abstract boolean isValidSource(Sector sourceSector);

    /**
     * returns if this is a valid target sector for an attack/movement from the given source sector
     * notifies the player using a dialog if invalid
     *
     * @param sourceSector source sector of attack/movement
     * @param targetSector target sector of attack/movement
     * @return true if valid target from the given source for an attack/movement else false
     */
    protected abstract boolean isValidTarget(Sector sourceSector, Sector targetSector);

    /**
     * Creates an arrow between coordinates
     *
     * @param gameplayBatch The main sprite batch
     * @param startX        Base of the arrow x
     * @param startY        Base of the arrow y
     * @param endX          Tip of the arrow x
     * @param endY          Tip of the arrow y
     */
    private void generateArrow(SpriteBatch gameplayBatch, float startX, float startY, float endX, float endY) {
        int thickness = 30;
        double angle = Math.toDegrees(Math.atan((endY - startY) / (endX - startX)));
        double height = (endY - startY) / Math.sin(Math.toRadians(angle));
        gameplayBatch.draw(arrow, startX, (startY - thickness / 2), 0, thickness / 2, (float) height, thickness, 1, 1, (float) angle);
    }

    /**
     * render graphics specific to the attack phase
     *
     * @param batch the sprite batch to render to
     */
    @Override
    public void visualisePhase(SpriteBatch batch) {
        if (this.sourceSector != null) { // If attacking
            Vector2 worldCoords = gameScreen.screenToWorldCoords(Gdx.input.getX(), Gdx.input.getY());
            generateArrow(batch, this.arrowTailPosition.x, this.arrowTailPosition.y, worldCoords.x, worldCoords.y);
        }
    }

    /**
     *
     * @param screenX mouse x position on screen when clicked
     * @param screenY mouse y position on screen when clicked
     * @param pointer pointer to the event
     * @param button which button was pressed
     * @return if the event has been handled
     */
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (super.touchUp(screenX, screenY, pointer, button)) {
            return true;
        }

        Vector2 worldCoord = gameScreen.screenToWorldCoords(screenX, screenY);

        int sectorId = gameScreen.getMap().detectSectorContainsPoint((int)worldCoord.x, (int)worldCoord.y);

        if (sectorId != -1) {
            // sector has been pressed
            Sector selected = gameScreen.getMap().getSectorById(sectorId);

            if (sourceSector == null) {
                // select source sector
                if (isValidSource(selected)) {
                    sourceSector = selected;
                    arrowTailPosition = new Vector2(worldCoord.x, worldCoord.y);
                }
            } else {
                // select target sector, (source already selected)
                if (isValidTarget(sourceSector, selected)) {
                    Sector temp = sourceSector;
                    sourceSector = null;
                    onArrowCreated(temp, selected);
                } else {
                    sourceSector = null;
                }
            }
        } else {
            sourceSector = null;
        }
        return true;
    }
}
