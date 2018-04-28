package sepr.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Timer;
import javafx.util.Pair;
import sepr.game.utils.PunishmentCardType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/*
Modified in assessment 4
 - refactored to inherit from new UiScreen class to reduce code duplication for setting up screen
 - redesigned minigame to be played as matching punishment cards instead of numbers
 - modified reward system to give players punishment cards as a reward not troops
 */

public class MiniGameScreen extends UiScreen {

    private static final int ROWS = 2;
    private static final int COLS = 4;
    private static final float DELAY_TIME = 3; // time in seconds before cards are hidden

    private GameScreen gameScreen;
    private Player player; // player to allocate gang members to at the end of the minigame

    private PunishmentCardType[][] locations; // array containing card type locations
    private ImageButton[][] cardButtons; // array to contain all buttons

    private List<PunishmentCardType> rewards; // lists which cards the player has won so far

    private Pair<Integer, Integer> pairSelected; // location of pair currently selected, (-1,-1) if no pair currently selected

    /**
     * sets up the minigame screen
     *
     * @param main
     * @param gameScreen the gamescreen for the game that this minigame will be launched from
     */
    public MiniGameScreen(final Main main, final GameScreen gameScreen) {
        super(main);
        this.stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE){
                    endGame(true);
                    return true;
                }
                return false;
            }
        });

        this.gameScreen = gameScreen;
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
        resetGameTable();
    }

    @Override
    protected Table setupBackground(){
        Table backgroundTable = new Table();
        backgroundTable.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture("uiComponents/miniGameBackground.png"))));
        backgroundTable.pad(0);
        backgroundTable.add(setupUi());
        return backgroundTable;
    }

    /**
     * Sets up the user interface
     */
    @Override
    protected Table setupUi() {
        Table uiComponentsTable =  new Table();
        uiComponentsTable.background(new TextureRegionDrawable(new TextureRegion(new Texture("uiComponents/Scanline-Purple.png"))));
        uiComponentsTable.pad(80).padLeft(85).padRight(95);

        uiComponentsTable.center();
        uiComponentsTable.add(WidgetFactory.genMenusTopBar("MINIGAME - MATCH THE PAIRS")).colspan(2);

        uiComponentsTable.row();
        uiComponentsTable.left();
        uiComponentsTable.add(setupGameTable()).expand();

        uiComponentsTable.row();
        uiComponentsTable.center();
        uiComponentsTable.add(WidgetFactory.genBottomBar("END MINIGAME", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                endGame(true); // player choosing to quit the game early counts as win and keeps cards collected so far
            }

        })).colspan(2).fillX();

        return uiComponentsTable;
    }

    /**
     * sets up a the minigame UI containing 2 rows and 4 columns of punishment cards
     *
     * @return table containing the UI widgets for the minigame
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
        setupCardLocations();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                cardButtons[i][j] = WidgetFactory.genPunishmentCardButton(locations[i][j]);
                cardButtons[i][j].addListener(listener);
            }
        }

        Table gameTable = new Table();
        gameTable.setDebug(false);
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                gameTable.add(cardButtons[i][j]).height(350).width(250).pad(40);
                gameTable.right();
            }
            gameTable.row();
        }

        return gameTable;
    }

    /**
     * Added by Dom (18/03/2018)
     *
     * sets up the locations array as a 2x4 2D array containing 2 of each real card type and 2 fake cards spread randomly
     */
    private void setupCardLocations() {
        locations = new PunishmentCardType[ROWS][COLS];
        List<PunishmentCardType> cardList = new ArrayList<PunishmentCardType>();
        cardList.add(PunishmentCardType.COLLUSION_CARD);
        cardList.add(PunishmentCardType.COLLUSION_CARD);
        cardList.add(PunishmentCardType.POOPY_PATH_CARD);
        cardList.add(PunishmentCardType.POOPY_PATH_CARD);
        cardList.add(PunishmentCardType.ASBESTOS_CARD);
        cardList.add(PunishmentCardType.ASBESTOS_CARD);
        // select 2 fake cards to add
        Random random = new Random();
        switch (random.nextInt(3)) {
            case 0 :
                cardList.add(PunishmentCardType.FAUX_COLLUSION_CARD);
                cardList.add(PunishmentCardType.FAUX_ASBESTOS_CARD);
                break;
            case 1 :
                cardList.add(PunishmentCardType.FAUX_COLLUSION_CARD);
                cardList.add(PunishmentCardType.FAUX_POOPY_PATH_CARD);
                break;
            case 2 :
                cardList.add(PunishmentCardType.FAUX_POOPY_PATH_CARD);
                cardList.add(PunishmentCardType.FAUX_ASBESTOS_CARD);
                break;
        }

        Collections.shuffle(cardList);

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                locations[i][j] = cardList.remove(0);
            }
        }
    }

    /**
     * sets up the table containing the game i.e. a 3x3 grid of cards
     */
    private void resetGameTable() {
        // generate each of the image buttons and set their name and listener
        setupCardLocations();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                Drawable btnImage = WidgetFactory.genPunishmentCardDrawable(locations[i][j]);
                cardButtons[i][j].setStyle(new ImageButton.ImageButtonStyle(btnImage, btnImage, btnImage, btnImage, btnImage, btnImage));
                cardButtons[i][j].setName("-1");
            }
        }
    }

    /**
     * revelas the type of card at the specified location
     *
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
     *
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
     *
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
     *
     * @param win true if the player should be allocated the rewards else false
     */
    private void endGame(boolean win) {
        if (!win) rewards.clear(); // clear rewards if player did not win the minigame

        if (rewards.contains(PunishmentCardType.COLLUSION_CARD)) player.addCollusionCards(1);
        if (rewards.contains(PunishmentCardType.POOPY_PATH_CARD)) player.addPoopyPathCards(1);
        if (rewards.contains(PunishmentCardType.ASBESTOS_CARD)) player.addAsbestosCards(1);

        DialogFactory.miniGameOverDialog(main, stage, gameScreen, rewards);
    }
}