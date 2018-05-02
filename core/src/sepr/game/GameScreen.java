package sepr.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import sepr.game.utils.PlayerType;
import sepr.game.utils.TurnPhaseType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/*
Modified in assessment 4
 - refactored to inherit from new UiScreen class to reduce code duplication for setting up screen
 - changed to using AudioPlayer for playing audio instead of using instance of AudioManager
 - created constants for storing: the chance of PVC minigame triggering and the max turn time for when the timer is enabled
 - removed PVC class references as no longer required due to changes to the minigame triggering mechanic
 - added method which may be called to give a chance of triggering the minigame - PVCSpawn()
 - refactored the turn timer system so that it may be saved and loaded and simplified pausing the timer
 - removed the ability to move and zoom in/out of the map as did not add anything to the game
 - added automatic scaling of the game map for when the game is resized so it is always easy to see the game
 */

/**
 * main class for controlling the game
 * implements screen for swapping what is being displayed with other screens, i.e. menu screens
 * input processor implemented to parse user input
 */
public class GameScreen extends UiScreen implements InputProcessor{
    public static final int NEUTRAL_PLAYER_ID = 4;
    private static final float PVC_SPAWN_CHANCE = 0.1f; // chance that the PVC minigame will start after each successful attack
    private static final int MAX_TURN_TIME = 60; // seconds per turn each player has if turn timer is enabled

    private TurnPhaseType currentPhase = TurnPhaseType.REINFORCEMENT; // set initial phase to the reinforcement phase
    private HashMap<TurnPhaseType, Phase> phases; // hashmap for storing the three phases of the game

    private SpriteBatch gameplayBatch; // sprite batch for rendering the game to
    private OrthographicCamera gameplayCamera; // camera for controlling what aspects of the game can be seen
    private Viewport gameplayViewport; // viewport for handling rendering the game at different resolutions

    private Map map; // stores state of the game: who owns which sectors
    private HashMap<Integer, Player> players; // player id mapping to the relevant player

    // timer settings
    private boolean turnTimerEnabled;
    private boolean paused = false;
    private float turnTimeElapsed = 0; // seconds since start of current players turn

    private List<Integer> turnOrder; // array of player ids in order of players' turns;
    private int currentPlayerPointer; // index of current player in turnOrder list

    private Texture mapBackground; // texture for drawing as a background behind the game

    private boolean gameSetup = false; // true once setupGame has been called

    /**
     * sets up rendering objects and key input handling
     * setupGame then start game must be called before a game is ready to be played
     *
     * @param main used to change screen
     */
    public GameScreen(Main main) {
        super(main);

        this.gameplayBatch = new SpriteBatch();
        this.gameplayCamera = new OrthographicCamera();
        this.gameplayViewport = new ScreenViewport(gameplayCamera);

        this.mapBackground = new Texture("uiComponents/mapBackgroundBox.png");
    }

    /**
     *
     * @param main used to change screen
     * @param currentPhase phase the game should be in when it starts
     * @param map the map that the game should be played on
     * @param players the players that should be part of this game
     * @param turnTimerEnabled is the turn timer enabled
     * @param turnTimeElapsed how long has passed since the begining of the current player's turn
     * @param turnOrder list of player ids still in the game in the order that the players turn's should occur
     * @param currentPlayerPointer pointer to index of turnOrder of the current player
     */
    public GameScreen(Main main, TurnPhaseType currentPhase, Map map, HashMap<Integer, Player> players, boolean turnTimerEnabled, float turnTimeElapsed, List<Integer> turnOrder, int currentPlayerPointer){
        this(main);

        setUpPhases();

        this.currentPhase = currentPhase;

        this.map = map;
        this.players = players;
        this.turnTimerEnabled = turnTimerEnabled;
        this.turnTimeElapsed = turnTimeElapsed;
        this.turnOrder = turnOrder;
        this.currentPlayerPointer = currentPlayerPointer;
        this.gameSetup = true;
    }

    /**
     * sets up a new game
     * start game must be called before the game is ready to be played
     *
     * @param players HashMap of the players in this game
     * @param turnTimerEnabled should players turns be limited
     */
    public void setupGame(HashMap<Integer, Player> players, boolean turnTimerEnabled, boolean allocateNeutralPlayer) {
        this.players = players;
        this.turnOrder = new ArrayList<Integer>();
        for (Integer i : players.keySet()) {
            if ((players.get(i).getPlayerType() != PlayerType.NEUTRAL_AI)) { // don't add the neutral player or unassigned to the turn order
                this.turnOrder.add(i);
            }
        }

        this.currentPlayerPointer = 0; // set the current player to the player in the first position of the turnOrder list

        this.turnTimerEnabled = turnTimerEnabled;
        this.map = new Map(this.players, allocateNeutralPlayer); // setup the game map and allocate the sectors

        setUpPhases();

        gameSetup = true; // game is now setup
    }

    /**
     * Instantiates the phases hashmap and puts an instance of each phase type, mapping to the respective phase object, in the map
     */
    private void setUpPhases() {
        this.currentPhase = TurnPhaseType.REINFORCEMENT;
        this.phases = new HashMap<TurnPhaseType, Phase>();
        this.phases.put(TurnPhaseType.REINFORCEMENT, new PhaseReinforce(this));
        this.phases.put(TurnPhaseType.ATTACK, new PhaseAttack(this));
        this.phases.put(TurnPhaseType.MOVEMENT, new PhaseMovement(this));
    }

    /**
     * configure input so that input into the current phase's UI takes priority then unhandled input is handled by this class
     */
    private void updateInputProcessor() {
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(phases.get(currentPhase));
        inputMultiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    /**
     * called once game is setup to enter the first phase of the game; centre the game camera and start the turn timer
     *
     * @throws RuntimeException if this is called before the game is setup, i.e. setupGame has not been called before this
     */
    public void startGame() {
        if (!gameSetup) {
            throw new RuntimeException("Cannot start game before it is setup");
        }
        this.phases.get(currentPhase).enterPhase(getCurrentPlayer());
        resetCameraPosition();
    }

    /**
     * pauses the timer, stops the turnTimeElapsed from being incremented
     */
    public void pauseTimer() {
        paused = true;
    }

    /**
     * unpauses the timer, turnTimeElapsed can now be incremented again
     */
    public void unpauseTimer() {
        paused = false;
    }

    /**
     * gets in seconds the amount of time remaining of the current player's turn
     *
     * @return time remaining in turn in seconds
     */
    private float getTurnTimeRemaining(){
        return MAX_TURN_TIME - turnTimeElapsed;
    }

    /**
     *
     * @return seconds since the current player's turn began
     */
    public float getTurnTimeElapsed() {
        return turnTimeElapsed;
    }

    /**
     *
     * @return true if the turn timer is enabled
     */
    public boolean isTurnTimerEnabled(){
        return this.turnTimerEnabled;
    }

    /**
     * returns the player object corresponding to the passed id from the players hashmap
     *
     * @param id of the player object that is wanted
     * @return the player whose id matches the given one in the players hashmap
     * @throws IllegalArgumentException if the supplied id is not a key value in the players hashmap
     */
    protected Player getPlayerById(int id) throws IllegalArgumentException {
        if (!players.containsKey(id)) throw new IllegalArgumentException("Cannot fetch player as id: " + id + " does not exist");
        return players.get(id);
    }

    /**
     *
     * @return gets the player object for the player who's turn it currently is
     */
    public Player getCurrentPlayer() {
        return players.get(turnOrder.get(currentPlayerPointer));
    }

    /**
     *
     * @return mapping of player ids to their respective player objects
     */
    public HashMap<Integer, Player> getPlayers() {
        return players;
    }

    /**
     *
     * @return list containing ids of players still in the game, where the order of ids is the order of player turns
     */
    public List<Integer> getTurnOrder(){
        return this.turnOrder;
    }

    /**
     *
     * @return index of current player id in turnOrder list
     */
    public int getCurrentPlayerPointer(){
        return this.currentPlayerPointer;
    }

    /**
     * called when the player ends the MOVEMENT phase of their turn to advance the game to the next Player's turn
     * increments the currentPlayerPointer and resets it to 0 if it now exceeds the number of players in the list
     */
    private void nextPlayer() {
        currentPlayerPointer++;
        if (currentPlayerPointer == turnOrder.size()) { // reached end of players, reset to 0 and increase turn number
            currentPlayerPointer = 0;
        }

        resetCameraPosition(); // re-centres the camera for the next player

        map.updateSectorStatusEffects(getCurrentPlayer().getId()); // apply status effects to tiles

        if (this.turnTimerEnabled) { // if the turn timer is on reset it for the next player
            this.turnTimeElapsed = 0;
        }
    }

    /**
     *
     * @return type of the phase currently in play
     */
    public TurnPhaseType getCurrentPhaseType(){
        return this.currentPhase;
    }

    /**
     *
     * @return phase object for the turn phase currently in play
     */
    public Phase getCurrentPhase() {
        return phases.get(getCurrentPhaseType());
    }

    /**
     * method is used for progression through the phases of a turn evaluating the currentPhase case label
     * if nextPhase is called during the movement phase then the game progresses to the next players turn
     */
    protected void nextPhase() {
        this.phases.get(currentPhase).endPhase();

        switch (currentPhase) {
            case REINFORCEMENT:
                currentPhase = TurnPhaseType.ATTACK;
                break;
            case ATTACK:
                currentPhase = TurnPhaseType.MOVEMENT;
                break;
            case MOVEMENT:
                currentPhase = TurnPhaseType.REINFORCEMENT;

                nextPlayer(); // nextPhase called during final phase of a player's turn so goto next player
                break;
        }

        this.updateInputProcessor(); // phase changed so update input handling
        this.phases.get(currentPhase).enterPhase(getCurrentPlayer()); // setup the new phase for the current player
        this.removeEliminatedPlayers(); // removes all players who have no remaining sectors from the turn order
    }

    /**
     * removes all players who have 0 sectors from the turn order
     */
    private void removeEliminatedPlayers() {
        List<Integer> playerIdsToRemove = new ArrayList<Integer>(); // list of players in the turn order who have 0 sectors
        for (Integer i : turnOrder) {
            boolean hasSector = false; // has a sector belonging to player i been found
            for (Integer j : map.getSectorIds()) {
                if (map.getSectorById(j).getOwnerId() == i) {
                    hasSector = true; // sector owned by player i found
                    break; // only need one sector to remain in turn order so can break once one found
                }
            }
            if (!hasSector) { // player has no sectors so remove them from the game
                playerIdsToRemove.add(i);
            }
        }

        if (playerIdsToRemove.size() > 0) { // if there are any players to remove
            turnOrder.removeAll(playerIdsToRemove);

            AudioPlayer.playPlayerEliminatedAudio();

            String[] playerNames = new String[playerIdsToRemove.size()]; // array of names of players who have been removed
            for (int i = 0; i < playerIdsToRemove.size(); i++) {
                playerNames[i] = players.get(playerIdsToRemove.get(i)).getPlayerName();
            }

            DialogFactory.playersOutDialog(playerNames, phases.get(currentPhase)); // display which players have been eliminated
        }

        if (isGameOver()) { // check if game is now over
            gameOver();
        }
    }

    /**
     * checks if game is over by checking how many players are in the turn order, if 1 then player has won, if 0 then the neutral player has won
     *
     * @return true if game is over else false
     */
    private boolean isGameOver() {
        return turnOrder.size() <= 1; // game is over if only one player is in the turn order
    }

    /**
     * method called when one player owns all the sectors in the map
     *
     * @throws RuntimeException if there is more than one player in the turn order when gameOver is called
     */
    private void gameOver() throws RuntimeException {
        if (turnOrder.size() == 0) { // neutral player has won
            DialogFactory.gameOverDialog(players.get(NEUTRAL_PLAYER_ID).getPlayerName(), players.get(NEUTRAL_PLAYER_ID).getCollegeName().getCollegeName(), main, phases.get(currentPhase));

        } else if (turnOrder.size() == 1){ // winner is player id at index 0 in turn order
            AudioPlayer.playGameOverAudio();

            int winnerId = turnOrder.get(0); // winner will be the only player in the turn order list
            DialogFactory.gameOverDialog(players.get(winnerId).getPlayerName(), players.get(winnerId).getCollegeName().getCollegeName(), main, phases.get(currentPhase));

        } else { // more than one player in turn order so no winner found therefore throw error
            throw new RuntimeException("Game Over called but more than one player in turn order");
        }
    }

    /**
     * when called there's a chance that the minigame is triggered
     */
    private void PVCSpawn() {
        Random rand = new Random();
        Float randomValue = rand.nextFloat();
        if (randomValue <= PVC_SPAWN_CHANCE) {
            openMiniGame();
        }
    }

    /**
     * re-centres the camera and sets the zoom level back to default
     */
    public void resetCameraPosition() {
        this.gameplayCamera.position.x = 1920/2;
        this.gameplayCamera.position.y = 1080/2;
    }

    /**
     * converts a point on the screen to a point in the world
     *
     * @param screenX x coordinate of point on screen
     * @param screenY y coordinate of point on screen
     * @return the corresponding world coordinates
     */
    public Vector2 screenToWorldCoords(int screenX, int screenY) {
        float x = gameplayCamera.unproject(new Vector3(screenX, screenY, 0)).x;
        float y = gameplayCamera.unproject(new Vector3(screenX, screenY, 0)).y;
        return new Vector2(x, y);
    }

    /**
     *
     * @return instance of main that his gamescreen is part of
     */
    public Main getMain(){
        return this.main;
    }

    /**
     *
     * @return the map object for this game
     */
    public Map getMap() {
        return map;
    }

    /**
     *
     * @return the sprite batch being used to render the game
     */
    protected SpriteBatch getGameplayBatch() {
        return this.gameplayBatch;
    }

    /**
     * changes the screen currently being displayed to the miniGame
     */
    private void openMiniGame() {
        main.setMiniGameScreen();
    }

    /**
     * changes the screen currently being displayed to the menu
     */
    public void openMenu() {
        AudioPlayer.playMenuMusic();
        main.setMenuScreen();
    }

    /**
     * draws a background image behind the map and UI covering the whole visible area of the render window
     */
    private void renderBackground() {
        Vector3 mapDrawPos = gameplayCamera.unproject(new Vector3(0, Gdx.graphics.getHeight(), 0));
        gameplayBatch.draw(mapBackground, mapDrawPos.x, mapDrawPos.y, gameplayCamera.viewportWidth * gameplayCamera.zoom, gameplayCamera.viewportHeight * gameplayCamera.zoom );
    }

    @Override
    protected Table setupUi() {
        return null;
    }

    /* Screen implementation */

    /**
     * when this screen is shown updates the input handling so it is from this screen
     */
    @Override
    public void show() {
        this.updateInputProcessor();
    }

    /**
     * updates the game and renders it to the screen
     *
     * @param delta time elapsed between this and the previous update in seconds
     * @throws RuntimeException when method called before the game is setup
     */
    @Override
    public void render(float delta) {
        if (!gameSetup) throw new RuntimeException("Game must be setup before attempting to play it"); // throw exception if attempt to run game before its setup

        if (turnTimerEnabled && !paused) turnTimeElapsed += delta; // update turn time elapsed
        if (map.checkIfSuccessfulAttackOccurred()) PVCSpawn(); // check if the current player has made a successful attack, if so chance for PVC to spawn

        gameplayCamera.update();
        gameplayBatch.setProjectionMatrix(gameplayCamera.combined);

        gameplayBatch.begin(); // begin rendering

        renderBackground(); // drawSectorImage the background of the game
        map.draw(gameplayBatch); // drawSectorImage the map

        gameplayBatch.end(); // stop rendering

        this.phases.get(currentPhase).act(delta); // update the stage of the current phase
        this.phases.get(currentPhase).draw(); // drawSectorImage the phase UI

        if (this.turnTimerEnabled) {
            this.phases.get(currentPhase).setTimerValue((int)getTurnTimeRemaining()); // update time remaining display
            if ((getTurnTimeRemaining() <= 0)) { // goto the next player's turn if the timer is enabled and they have run out of time
                this.currentPhase = TurnPhaseType.MOVEMENT;
                nextPhase();
            }
        }
    }

    /**
     * resizes the window contents to ensure fits the new size
     * scales the game map so that all of it is visible
     *
     * @param width window width
     * @param height window height
     */
    @Override
    public void resize(int width, int height) {
        for (Stage stage : phases.values()) { // update the rendering properties of each stage when the screen is resized
            stage.getViewport().update(width, height);
            stage.getCamera().viewportWidth = width;
            stage.getCamera().viewportHeight = height;
            stage.getCamera().position.x = width/2;
            stage.getCamera().position.y = height/2;
            stage.getCamera().update();
        }

        // update this classes rending properties for the new display size
        this.gameplayViewport.update(width, height);
        this.gameplayCamera.viewportWidth = width;
        this.gameplayCamera.viewportHeight = height;
        this.gameplayCamera.translate(1920/2, 1080/2, 0);

        float ar = width / height;

        if (ar > 16/9) {
            // height limited
            this.gameplayCamera.zoom = 1080f/((float) height - 400);
        } else {
            // width limited
            this.gameplayCamera.zoom = 1920f/((float) width - 400);
        }

        this.gameplayCamera.update();

        resetCameraPosition();
    }


    /* Input Processor implementation */
    @Override
    public boolean keyDown(int keycode) { return false; }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            DialogFactory.pauseGameDialogBox(this, phases.get(currentPhase)); // confirm if the player wants to leave if escape is pressed
        }

        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        Vector2 worldCoords = screenToWorldCoords(screenX, screenY);

        int hoveredSectorId = map.detectSectorContainsPoint((int)worldCoords.x, (int)worldCoords.y); // get id of sector mouse is currently hovered over
        if (hoveredSectorId == -1) {
            phases.get(currentPhase).setBottomBarText(null); // no sector hovered over: update bottom bar with null sector
        } else {
            phases.get(currentPhase).setBottomBarText(map.getSectorById(hoveredSectorId)); // update the bottom bar of the UI with the details of the sector currently hovered over by the mouse
        }
        return false;
    }

    @Override
    public boolean scrolled(int amount) { return false; }
}