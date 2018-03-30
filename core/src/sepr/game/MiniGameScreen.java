package sepr.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import javafx.util.Pair;
import sepr.game.utils.PunishmentCardType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MiniGameScreen implements Screen {

    private static final int COLS = 3;
    private static final int ROWS = 3;
    private static final float DELAY_TIME = 3; // time in seconds before cards are hidden

    private Main main;
    private Stage stage;
    private GameScreen gameScreen;
    private Table table; // table for inserting ui widgets into
    private Player player; // player to allocate gang members to at the end of the minigame
    private AudioManager Audio = AudioManager.getInstance();

    private PunishmentCardType[][] locations; // array containing card type locations
    private ImageButton[][] cardButtons; // array to contain all buttons

    private List<PunishmentCardType> rewards; // lists which cards the player has won so far

    private Pair<Integer, Integer> pairSelected; // location of pair currently selected, (-1,-1) if no pair currently selected

    public MiniGameScreen(final Main main, final GameScreen gameScreen) {
        this.main = main;
        this.gameScreen = gameScreen;
        this.stage = new Stage();
        this.stage.setViewport(new ScreenViewport());

        this.table = new Table();
        this.table.setFillParent(true); // make ui table fill the entire screen
        this.stage.addActor(table);
        this.table.setDebug(false); // enable table drawing for ui debug
    }

    /**
     * Starts the game by showing all the values for a set amount of time and then hiding them and enabling the buttons.
     */
    public void startGame() {
        /* Wait for time specified in DELAY_TIME to hide and enable buttons */
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                for (int i = 0; i < ROWS; i++) {
                    for (int j = 0; j < COLS; j++) {
                        cardButtons[i][j].setName(i + " " + j); // activates buttons
                        hideCardType(new Pair<Integer, Integer>(i, j));
                    }
                }
            }

        }, DELAY_TIME);

    }

    /**
     * Sets up the game by mapping values to locations and calling the function to set up the UI
     *
     * @param player player to be given additional troops at the end of the minigame
     */
    public void setupGame(Player player) {
        pairSelected = new Pair<Integer, Integer>(-1, -1);
        rewards = new ArrayList<PunishmentCardType>();
        this.player = player; // sets the player to the one that is playing this game
        setupCardLocations();
        setupUi();
    }

    /**
     * Added by Dom (18/03/2018)
     *
     * sets up the locations array as a 3x3 2D array containing 2 of each real card type and one of each fake card type spread randomly
     */
    private void setupCardLocations() {
        locations = new PunishmentCardType[3][3];
        List<PunishmentCardType> cardList = new ArrayList<PunishmentCardType>();
        cardList.add(PunishmentCardType.COLLUSION_CARD);
        cardList.add(PunishmentCardType.COLLUSION_CARD);
        cardList.add(PunishmentCardType.FAUX_COLLUSION_CARD);
        cardList.add(PunishmentCardType.POOPY_PATH_CARD);
        cardList.add(PunishmentCardType.POOPY_PATH_CARD);
        cardList.add(PunishmentCardType.FAUX_POOPY_PATH_CARD);
        cardList.add(PunishmentCardType.ASBESTOS_CARD);
        cardList.add(PunishmentCardType.ASBESTOS_CARD);
        cardList.add(PunishmentCardType.FAUX_ASBESTOS_CARD);

        Collections.shuffle(cardList);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                locations[i][j] = cardList.remove(0);
            }
        }
    }

    /**
     * revelas the type of card at the specified location
     * @param coord location of card to be shown
     */
    private void revealCardType(Pair<Integer, Integer> coord) {
        Drawable btnImage = WidgetFactory.genPunishmentCardDrawable(locations[coord.getKey()][coord.getValue()]);
        ImageButton.ImageButtonStyle revealedStyle = new ImageButton.ImageButtonStyle();
        revealedStyle.up = btnImage;
        revealedStyle.over = btnImage;
        revealedStyle.down = btnImage;
        cardButtons[coord.getKey()][coord.getValue()].setStyle(revealedStyle);
    }

    /**
     * sets the card image at the passed location to the hidden image
     * @param coord location of card to hide
     */
    private void hideCardType(Pair<Integer, Integer> coord) {
        Drawable btnImage = WidgetFactory.genPunishmentCardDrawable(PunishmentCardType.HIDDEN_CARD);
        ImageButton.ImageButtonStyle revealedStyle = new ImageButton.ImageButtonStyle();

        revealedStyle.up = btnImage;
        revealedStyle.over = btnImage;
        revealedStyle.down = btnImage;
        cardButtons[coord.getKey()][coord.getValue()].setStyle(revealedStyle);
    }

    /**
     * adds the card type at the second location found to the rewards list
     * ends game if all 3 pairs have been found
     * @param secondPairLocation location of the second pair found
     */
    private void pairFound(Pair<Integer, Integer> secondPairLocation) {
        rewards.add(locations[secondPairLocation.getKey()][secondPairLocation.getValue()]);
        pairSelected = new Pair<Integer, Integer>(-1, -1); // deselect the selected pair

        if (rewards.size() == 3) {
            // all pairs have been found so end game
            endGame(true);
        }
    }

    /**
     *
     * @param win true if the player should be allocated the rewards else false
     */
    public void endGame(boolean win) {
        if (!win) rewards.clear(); // clear rewards if player did not win the minigame
        player.addTroopsToAllocate(rewards.size()); // need to change to danger card allocation
        DialogFactory.miniGameOverDialog(main, stage, gameScreen, rewards);
    }

    /**
     *
     * @param location string describing the button's location in the grid in form "<digit> <digit>" where the digit is number 0 - 2
     */
    private void buttonClicked(String location) {
        if (location.equals("-1")) return; // button has already been clicked so return without doing anything

        // convert the string location to a coordinate
        Pair<Integer, Integer> btnPressedLocation = new Pair<Integer, Integer>(Integer.parseInt(location.split(" ")[0]), Integer.parseInt(location.split(" ")[1]));
        revealCardType(btnPressedLocation); // show the image of the card pressed
        cardButtons[btnPressedLocation.getKey()][btnPressedLocation.getValue()].setName("-1"); // make button unclickable

        if (pairSelected.equals(new Pair<Integer, Integer>(-1, -1))) {
            // no card currently selected
            pairSelected = btnPressedLocation;
        } else {
            // card is currently selected
            if (locations[pairSelected.getKey()][pairSelected.getValue()].equals(locations[btnPressedLocation.getKey()][btnPressedLocation.getValue()])) {
                // the selected pair are a match
                pairFound(btnPressedLocation);
            } else {
                // not a match so player looses game
                endGame(false);
            }
        }
    }

    /**
     * sets up the table containing the game i.e. a 3x3 grid of cards
     */
    private Table setupGameTable() {
        /* Listener for the buttons, passes the value of the clicked button to the buttonClicked method */
        InputListener listener = new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                ImageButton buttonUsed = (ImageButton) event.getListenerActor();
                String location = buttonUsed.getName(); // name of button equal to its location in cardButtons
                buttonClicked(location);
                return true;
            }
        };

        // generate each of the image buttons and set their name and listener
        cardButtons = new ImageButton[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                cardButtons[i][j] = WidgetFactory.genPunishmentCardButton(locations[i][j]);
                cardButtons[i][j].setName("-1");
                cardButtons[i][j].addListener(listener);
            }
        }

        Table gameTable = new Table();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                gameTable.add(cardButtons[i][j]).height(100).width(100).pad(30);
                gameTable.right();
            }
            gameTable.row();
        }

        return gameTable;
    }

    /**
     * sets up the UI for this screen when a new game is started
     */
    private void setupUi() {
        table.background(new TextureRegionDrawable(new TextureRegion(new Texture("uiComponents/menuBackground.png"))));

        table.center();
        table.add(WidgetFactory.genMenusTopBar("MINIGAME - MATCH THE PAIRS")).colspan(2);

        table.row();
        table.left();
        table.add(setupGameTable()).expand();

        table.row();
        table.center();
        table.add(WidgetFactory.genBottomBar("END MINI-GAME", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                endGame(true); // player choosing to quit the game early counts as win and keeps cards collected so far
            }

        })).colspan(2);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        this.stage.act(Gdx.graphics.getDeltaTime());
        this.stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        this.stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }
}