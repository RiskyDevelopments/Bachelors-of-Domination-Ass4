package sepr.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import sepr.game.utils.TurnPhaseType;

/**
 * handles input, updating and rendering for the attack phase
 */
public class PhaseAttack extends PhaseAttackMove{

    private AudioManager Audio = AudioManager.getInstance();
    private int[] unitsToMove;


    public PhaseAttack(GameScreen gameScreen) {
        super(gameScreen, TurnPhaseType.ATTACK);
    }

    /**
     * creates a dialog asking the player how many units they want to attack with
     *
     * @throws RuntimeException if the attacking sector or defending sector are set to null
     */
    private void getNumberOfAttackers() throws RuntimeException {
        if (sourceSector == null || targetSector == null) {
            throw new RuntimeException("Cannot execute attack unless both an attacking and defending sector have been selected");
        }
        numOfUnits = new int[1];
        numOfUnits[0] = -1;
        DialogFactory.attackDialog(sourceSector.getUnitsInSector(), targetSector.getUnitsInSector(), numOfUnits, this);
    }

    /**
     * rebalanced attack algorithm (Dom 17/03/2018)
     *
     * carries out attack once number of attackers has been set using the dialog
     */
    private void executeAttack() {
        System.out.println("attacking");
        // record owners to keep track of changes after the attack
        int sourceSectorOwner = sourceSector.getOwnerId();
        int targetSectorOwner = targetSector.getOwnerId();

        gameScreen.getMap().completeAttack(gameScreen.getPlayerById(sourceSectorOwner), gameScreen.getPlayerById(GameScreen.NEUTRAL_PLAYER_ID), sourceSector, targetSector, numOfUnits[0]);

        if (targetSector.getOwnerId() == sourceSectorOwner) { // attacker took over the target sector
            numOfUnits = new int[] {-1, sourceSector.getId(), targetSector.getId()};

            DialogFactory.attackSuccessDialogBox(targetSector.getReinforcementsProvided(),
                    sourceSector.getUnitsInSector(),
                    unitsToMove,
                    gameScreen.getPlayerById(targetSectorOwner).getPlayerName(),
                    gameScreen.getPlayerById(sourceSectorOwner).getPlayerName(),
                    targetSector.getDisplayName(),
                    this);

        } else { // defender wiped out attacking units and attacker sector is now neutral
            DialogFactory.sectorOwnerChangeDialog(gameScreen.getPlayerById(sourceSectorOwner).getPlayerName(), gameScreen.getPlayerById(GameScreen.NEUTRAL_PLAYER_ID).getPlayerName(), sourceSector.getDisplayName(), this);
        }
    }

    /**
     * once unitsToMove has had the amount of units to move and the ids of the source and target sector set, perform the move
     */
    private void detectUnitsMove() {
        if (unitsToMove != null) {
            if (unitsToMove[0] != -1) {
                gameScreen.getMap().moveUnits(unitsToMove[1], unitsToMove[2], unitsToMove[0]);
                unitsToMove = null;
            }
        }
    }

    /**
     * process an attack if one is being carried out
     */
    @Override
    public void phaseAct() {
        if (sourceSector != null && targetSector != null && numOfUnits[0] != -1) {

            if (numOfUnits[0] == 0 || !sourceSector.canAttack() || !targetSector.canBeAttacked()) {
                // cancel attack
                int voice = random.nextInt(3);

                switch (voice){
                    case 0:
                        Audio.get("sound/Invalid Move/Colin_Your_request_does_not_pass_easily_through_my_mind.wav", Sound.class).play(AudioManager.GlobalFXvolume);
                        break;
                    case 1:
                        Audio.get("sound/Invalid Move/Colin_You_would_find_more_success_trying_to_invert_a_singular_matrix.wav", Sound.class).play(AudioManager.GlobalFXvolume);
                        break;
                    case 2:
                        Audio.get("sound/Invalid Move/Colin_Your_actions_are_questionable.wav", Sound.class).play(AudioManager.GlobalFXvolume);
                        break;
                    case 3:
                        // play no sound
                        break;
                }
            } else {
                executeAttack();
            }
            // reset attack
            sourceSector = null;
            targetSector = null;
            numOfUnits = null;
        }
        detectUnitsMove();
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
        if (sectorId != -1) { // If selected a sector

            Sector selected = gameScreen.getMap().getSectorById(sectorId); // Current sector
            boolean notAlreadySelected = this.sourceSector == null && this.targetSector == null; // T/F if the attack sequence is complete

            if (this.sourceSector != null && this.targetSector == null) { // If its the second selection in the attack phase

                if (this.sourceSector.isAdjacentTo(selected) && selected.getOwnerId() != this.currentPlayer.getId()) { // check the player does not own the defending sector and that it is adjacent
                    this.arrowHeadPosition.set(worldCoord.x, worldCoord.y); // Finalise the end position of the arrow
                    this.targetSector = selected;

                    getNumberOfAttackers(); // attacking and defending sector selected so find out how many units the player wants to attack with
                } else { // cancel attack as selected defending sector cannot be attack: may not be adjacent or may be owned by the attacker
                    this.sourceSector = null;
                }

            } else if (selected.getOwnerId() == this.currentPlayer.getId() && selected.getUnitsInSector() > 1 && notAlreadySelected) { // First selection, is owned by the player and has enough troops
                this.sourceSector = selected;
                this.arrowTailPosition.set(worldCoord.x, worldCoord.y); // set arrow tail position
            } else {
                this.sourceSector = null;
                this.targetSector = null;
            }
        } else { // mouse pressed and not hovered over a sector to attack therefore cancel any attack in progress
            this.sourceSector = null;
            this.targetSector = null;
        }

        return true;
    }
}
