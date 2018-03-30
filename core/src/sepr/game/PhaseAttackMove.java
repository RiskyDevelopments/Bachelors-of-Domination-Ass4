package sepr.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import sepr.game.utils.TurnPhaseType;

import java.util.Random;

public abstract class PhaseAttackMove extends Phase {


    protected TextureRegion arrow; // TextureRegion for rendering attack visualisation
    protected Sector sourceSector; // Stores the sector being used to attack in the attack phase (could store as ID and lookup object each time to save memory)
    protected Sector targetSector; // Stores the sector being attacked in the attack phase (could store as ID and lookup object each time to save memory)

    protected Vector2 arrowTailPosition; // Vector x,y for the base of the arrow
    protected Vector2 arrowHeadPosition; // Vector x,y for the point of the arrow
    protected int[] numOfUnits; // number of units being used to attack/move


    protected Random random; // random object for adding some unpredictability to the outcome of attacks

    public PhaseAttackMove(GameScreen gameScreen, TurnPhaseType turnPhaseType){

        super(gameScreen, turnPhaseType);
        this.arrow = new TextureRegion(new Texture(Gdx.files.internal("uiComponents/arrow.png")));
        this.sourceSector = null;
        this.targetSector = null;
        this.arrowHeadPosition = new Vector2();
        this.arrowTailPosition = new Vector2();

        this.random = new Random();
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
        targetSector = null;
    }

    /**
     * Creates an arrow between coordinates
     *
     * @param gameplayBatch The main sprite batch
     * @param startX        Base of the arrow x
     * @param startY        Base of the arrow y
     * @param endX          Tip of the arrow x
     * @param endY          Tip of the arrow y
     */
    public void generateArrow(SpriteBatch gameplayBatch, float startX, float startY, float endX, float endY) {
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
            Vector2 screenCoords = gameScreen.screenToWorldCoords(Gdx.input.getX(), Gdx.input.getY());
            if (this.targetSector == null) { // In mid attack
                generateArrow(batch, this.arrowTailPosition.x, this.arrowTailPosition.y, screenCoords.x, screenCoords.y);
            } else if (this.targetSector != null) { // Attack confirmed
                generateArrow(batch, this.arrowTailPosition.x, this.arrowTailPosition.y, this.arrowHeadPosition.x, this.arrowHeadPosition.y);
            }
        }
    }
}
