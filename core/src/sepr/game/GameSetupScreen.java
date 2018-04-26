package sepr.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import javafx.util.Pair;
import sepr.game.utils.CollegeName;
import sepr.game.utils.PlayerType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * screen for setting up the game
 * configurable properties
 *  - Number of players
 *  - Players' names
 *  - Players' college
 *  - Neutral player enabled
 *  - Turn timer enabled
 */
public class GameSetupScreen extends View{

    private AudioManager Audio = AudioManager.getInstance();
    private final int MAX_NUMBER_OF_PLAYERS = 4; // maximum number of players that cna be in a game

    private Label[] playerTypes; // array of player types, index n -> player n's type
    private TextField[] playerNames; // array of TextFields for players to enter names, index n -> player n's name
    private Pair<Label, Image>[] playerColleges; // array of pairs of college name and college logo, index n -> player n's college
    private CheckBox neutralPlayerSwitch; // switch for enabling the neutral player
    private CheckBox turnTimerSwitch; // switch for enabling the turn timer

    private Texture collegeTableBackground;

    /**
     *
     * @param main for changing to different screens
     */
    public GameSetupScreen (final Main main) {
        super(main);

        this.stage = new Stage(){
            @Override
            public boolean keyUp(int keyCode) {
                if (keyCode == Input.Keys.ESCAPE) { // change back to the menu screen if the player presses esc
                    main.setMenuScreen();
                }
                return super.keyUp(keyCode);
            }
        };
        this.collegeTableBackground = new Texture("uiComponents/Game-Setup-Name-Box.png");

        this.stage.setViewport(new ScreenViewport());

        this.backgroundTable = setupBackground();
        this.backgroundTable.setFillParent(true); // make ui table fill the entire screen
        this.stage.addActor(backgroundTable);
        this.backgroundTable.setDebug(false); // enable table drawing for ui debug


        this.Audio.loadMusic("sound/IntroMusic/Tron style music - Original track.mp3"); //load the introMusic
    }

    /**
     * updates the player type label when the left or right ui button is pressed
     *
     * @param playerLabel to update the contents of
     */
    private void togglePlayerType(Label playerLabel){
        if (playerLabel.getText().toString().equals(PlayerType.NONE.getPlayerType())){
            playerLabel.setText(PlayerType.HUMAN.getPlayerType());
        }else if (playerLabel.getText().toString().equals(PlayerType.HUMAN.getPlayerType())) {
            playerLabel.setText(PlayerType.NONE.getPlayerType());
        }
    }

    /**
     * finds the name of the next college when the left UI button is pressed
     *
     * @param collegeLabel the label to have its contents updated
     * @return the name of the newly selected college
     */
    private CollegeName leftCollegeTypeButtonPress(Label collegeLabel){
        if (collegeLabel.getText().toString().equals(CollegeName.ALCUIN.getCollegeName())){
            return CollegeName.WENTWORTH;
        }else if(collegeLabel.getText().toString().equals(CollegeName.DERWENT.getCollegeName())){
            return CollegeName.ALCUIN;
        }else if(collegeLabel.getText().toString().equals(CollegeName.HALIFAX.getCollegeName())){
            return CollegeName.DERWENT;
        }else if(collegeLabel.getText().toString().equals(CollegeName.HES_EAST.getCollegeName())){
            return CollegeName.HALIFAX;
        }else if(collegeLabel.getText().toString().equals(CollegeName.JAMES.getCollegeName())){
            return CollegeName.HES_EAST;
        }else if(collegeLabel.getText().toString().equals(CollegeName.VANBRUGH.getCollegeName())){
            return CollegeName.JAMES;
        }else {
            return CollegeName.VANBRUGH;
        }
    }

    /**
     * finds the name of the next college when the right UI button is pressed
     *
     * @param collegeLabel the label to have its contents updated
     * @return the name of the newly selected college
     */
    private CollegeName rightCollegeTypeButtonPress(Label collegeLabel){
        if (collegeLabel.getText().toString().equals(CollegeName.ALCUIN.getCollegeName())){
            return CollegeName.DERWENT;
        }else if(collegeLabel.getText().toString().equals(CollegeName.DERWENT.getCollegeName())){
            return CollegeName.HALIFAX;
        }else if(collegeLabel.getText().toString().equals(CollegeName.HALIFAX.getCollegeName())){
            return CollegeName.HES_EAST;
        }else if(collegeLabel.getText().toString().equals(CollegeName.HES_EAST.getCollegeName())){
            return CollegeName.JAMES;
        }else if(collegeLabel.getText().toString().equals(CollegeName.JAMES.getCollegeName())){
            return CollegeName.VANBRUGH;
        }else if(collegeLabel.getText().toString().equals(CollegeName.VANBRUGH.getCollegeName())){
            return CollegeName.WENTWORTH;
        }else {
            return CollegeName.ALCUIN;
        }
    }

    /**
     * generates the left hand sub table for setting the player types, i.e. selecting NONE or HUMAN
     *
     * @return a table containing MAX_NUMBER_OF_PLAYERS rows each with a left arrow button a player type label and right arrow button
     */
    private Table setUpPlayerTypesTable() {
        playerTypes = new Label[MAX_NUMBER_OF_PLAYERS]; // array of labels, not local as text value needs to be read later
        Button[] leftButtons = new Button[MAX_NUMBER_OF_PLAYERS]; // array of left selector buttons, local as buttons do not need to be read later
        Button[] rightButtons = new Button[MAX_NUMBER_OF_PLAYERS]; // array of right selector buttons, local as buttons do not need to be read later

        for (int i = 0; i < MAX_NUMBER_OF_PLAYERS; i++) { // generate the buttons and labels
            Button leftButton = WidgetFactory.genPlayerLeftButton();
            Button rightButton = WidgetFactory.genPlayerRightButton();


            final int finalI = i;
            leftButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    togglePlayerType(playerTypes[finalI]); // toggle player type when left arrow button pressed
                }
            });

            rightButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    togglePlayerType(playerTypes[finalI]); // toggle player type when right arrow button pressed
                }
            });

            playerTypes[i] = WidgetFactory.genPlayerLabel(PlayerType.NONE.getPlayerType()); // set to NONE player type by default
            leftButtons[i] = leftButton;
            rightButtons[i] = rightButton;
        }

        Table leftTable = new Table(); // table for storing the buttons and labels

        // add the buttons and labels to the table
        for(int i = 0; i < MAX_NUMBER_OF_PLAYERS; i++){
            leftTable.left();
            leftTable.add(leftButtons[i]).height(70).width(63).pad(5);
            leftTable.add(playerTypes[i]).height(70).width(312).expandX().padTop(20).padBottom(20);
            leftTable.right();
            leftTable.add(rightButtons[i]).height(70).width(63).pad(5);
            leftTable.row();
        }

        return leftTable;
    }

    /**
     * Sets up the table for the switch based part of the game setup
     * Setup options for: neutral player and turn timer
     *
     * @return a table containing a switch for the the neutral player and switch for the turn timer
     */
    private Table setupSwitchTable(){
        // neutral player
        Label neutralPlayerLabel = WidgetFactory.genMenuLabel("NEUTRAL PLAYER");
        neutralPlayerLabel.setAlignment(0, 0);

        neutralPlayerSwitch = WidgetFactory.genOnOffSwitch();

        // turn timer
        Label turnTimerLabel = WidgetFactory.genMenuLabel("TURN TIMER");
        turnTimerLabel.setAlignment(0, 0);

        turnTimerSwitch = WidgetFactory.genOnOffSwitch();

        // Add components to table
        Table switchTable = new Table();
        switchTable.setDebug(false);

        switchTable.left();
        switchTable.add(neutralPlayerLabel).height(72).width(439);
        switchTable.right();
        switchTable.add(neutralPlayerSwitch).padLeft(50);

        switchTable.row().padTop(20).padBottom(20);

        switchTable.left();
        switchTable.add(turnTimerLabel).height(72).width(439);
        switchTable.right();
        switchTable.add(turnTimerSwitch).padLeft(50);

        return switchTable;
    }

    /**
     * generates the UI table for setting player's colleges and names
     * populates the playerColleges and playerNames arrays to be accessed later
     *
     * @return the right hand sub table containing the player name setup and college selector UI components
     */
    private Table setUpCollegeTable(){
        playerNames = new TextField[MAX_NUMBER_OF_PLAYERS];
        playerColleges = new Pair[MAX_NUMBER_OF_PLAYERS];

        // generate the textfields, college labels and college logos
        for (int i = 0; i < MAX_NUMBER_OF_PLAYERS; i++) {
            Label collegeName = WidgetFactory.genTransparentLabel("ALCUIN");
            Image collegeImage = WidgetFactory.genCollegeLogoImage(CollegeName.ALCUIN);

            playerColleges[i] = new Pair<Label, Image>(collegeName, collegeImage);
            playerNames[i] = WidgetFactory.playerNameTextField("PLAYER" + (i + 1));
        }

        Table[] collegeTables = new Table[MAX_NUMBER_OF_PLAYERS]; // array of tables containing UI config widgets for college and player name setup
        for (int i = 0; i < MAX_NUMBER_OF_PLAYERS; i++) {
            Table textTable = new Table(); // table for storing the college name and player name textfield widgets
            Table logoTable = new Table(); // table for storing the college logo and selector buttons

            textTable.add(playerNames[i]).padBottom(10).width(220).left(); // add the textfields to the text table
            textTable.row();
            textTable.add(playerColleges[i].getKey()).height(20).width(220);

            Button leftButton = WidgetFactory.genCollegeLeftButton();
            Button rightButton = WidgetFactory.genCollegeRightButton();

            final int finalI = i;
            leftButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    CollegeName nextCollege = leftCollegeTypeButtonPress(playerColleges[finalI].getKey());

                    playerColleges[finalI].getKey().setText(nextCollege.getCollegeName());
                    playerColleges[finalI].getValue().setDrawable(WidgetFactory.genCollegeLogoDrawable(nextCollege));
                }
            });

            rightButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    CollegeName nextCollege = rightCollegeTypeButtonPress(playerColleges[finalI].getKey());

                    playerColleges[finalI].getKey().setText(nextCollege.getCollegeName());
                    playerColleges[finalI].getValue().setDrawable(WidgetFactory.genCollegeLogoDrawable(nextCollege));
                }
            });


            logoTable.add(leftButton).height(60).width(35);
            logoTable.add(playerColleges[i].getValue()).height(80).width(100);
            logoTable.add(rightButton).height(60).width(35);

            Table temp = new Table();
            temp.background(new TextureRegionDrawable(new TextureRegion(collegeTableBackground)));
            temp.setDebug(false);
            temp.add(textTable).expand().left().padLeft(20);
            temp.add(logoTable).padRight(60);

            collegeTables[i] = temp;
        }

        Table rightTable = new Table();
        for(int i = 0; i < MAX_NUMBER_OF_PLAYERS; i++){
            rightTable.add(collegeTables[i]).padBottom(10).padTop(10);
            rightTable.row();
        }

        return rightTable;
    }

    /**
     * using the information configured in the setup screen generate a hashmap of players to be used in the game
     *
     * @return HashMap of Players derived from the Setup Screen
     */
    private HashMap<Integer, Player> generatePlayerHashmaps() {
        HashMap<Integer, Player> players = new HashMap<Integer, Player>();

        // setup Human and AI players
        for (int i = 0; i < MAX_NUMBER_OF_PLAYERS; i++) {
            if (playerTypes[i].getText().toString().equals(PlayerType.HUMAN.getPlayerType())) {
                // create human player
                players.put(i, Player.createHumanPlayer(i, CollegeName.fromString(playerColleges[i].getKey().getText().toString()), 5, playerNames[i].getText()));
            }
        }

        // setup neutral player
        players.put(GameScreen.NEUTRAL_PLAYER_ID, Player.createNeutralPlayer(GameScreen.NEUTRAL_PLAYER_ID));

        return players;
    }

    /**
     * Checks if:
     *  there are no duplicate player names
     *  the names are at least three characters long
     *  the names contain numbers and digits only
     *
     * @throws GameSetupException if name conditions are not met
     */
    private void validatePlayerNames() throws GameSetupException{
        Set<String> appeared = new HashSet();
        for (int i = 0; i < playerNames.length; i++) {
            if (PlayerType.fromString(playerTypes[i].getText().toString()) == PlayerType.NONE) {
                continue;
            }

            Pattern p = Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(playerNames[i].getText());
            boolean containsInvalidChars = m.find(); // set to true if name contains none alphanumeric characters

            if (playerNames[i].getText().length() < 3){ // is player name more than three
                throw new GameSetupException(GameSetupException.GameSetupExceptionType.INVALID_PLAYER_NAME);
            }
            else if (containsInvalidChars){
                throw new GameSetupException(GameSetupException.GameSetupExceptionType.INVALID_PLAYER_NAME);
            }
            else if (!appeared.add(playerNames[i].getText())) { // checks name is not a duplicate
                throw new GameSetupException(GameSetupException.GameSetupExceptionType.DUPLICATE_PLAYER_NAME);
            }
        }
    }

    /**
     * Checks if:
     *  any college has been selected more than once
     *
     * @throws GameSetupException if college has been chosen more than once
     */
    private void validateCollegeSelection() throws GameSetupException{
        Set<String> appeared = new HashSet();
        for (int i = 0; i < playerNames.length; i++) {
            if (PlayerType.fromString(playerTypes[i].getText().toString()) != PlayerType.NONE && !appeared.add(playerColleges[i].getKey().getText().toString())) {
                throw new GameSetupException(GameSetupException.GameSetupExceptionType.DUPLICATE_COLLEGE_SELECTION);
            }
        }
    }

    /**
     * Checks if:
     *  there's at least 2 players
     *  there's only 2 players then the neutral player is enabled
     *
     * @throws GameSetupException if player configuration conditions are not met
     */
    private void validatePlayerConfiguration() throws GameSetupException{
        int totalNumPlayers = 0;
        for (int i = 0; i < playerNames.length; i++) {
            if (playerTypes[i].getText().toString().equals("HUMAN PLAYER")) {
                totalNumPlayers = totalNumPlayers + 1;
            }
        }
        if (totalNumPlayers < 2) { // must be at least two players
            throw new GameSetupException(GameSetupException.GameSetupExceptionType.MINIMUM_TWO_PLAYERS);
        } else if (totalNumPlayers == 2 && !neutralPlayerSwitch.isChecked()) { // only two players need neutral player
            throw new GameSetupException(GameSetupException.GameSetupExceptionType.NO_NEUTRAL_PLAYER);
        }
    }

    /**
     * Method attempts to start the game
     * Following conditions must be met for a game to be able to start
     *  At least 2 players
     *  At least 1 human player
     *  Neutral player must be enabled if there are only 2 players
     *  No duplicate player names
     *  No duplicate college selections
     *  Player names must be at least three characters long
     *  Player names contain numbers and digits only
     */
    private void startGame() {
        try {
            validatePlayerNames();
            validateCollegeSelection();
            validatePlayerConfiguration();
        } catch (GameSetupException e) {
            DialogFactory.basicDialogBox(null, "Game Setup Error", e.getExceptionType().getErrorMessage(), stage);
            return;
        }
        HashMap<Integer, Player> x = generatePlayerHashmaps();

        AudioPlayer.playMainGameMusic();

        main.setGameScreen(x, turnTimerSwitch.isChecked(), neutralPlayerSwitch.isChecked());
    }

    /**
     * sets up the UI for the game setup screen
     */
    @Override
    protected Table setupUi() {
        Table uiComponentsTable =  new Table();
        uiComponentsTable.setDebug(false);

        uiComponentsTable.background(new TextureRegionDrawable(new TextureRegion(new Texture("uiComponents/Scanline.png"))));
        uiComponentsTable.pad(80).padLeft(85).padRight(95);

        TextButton startGameButton = WidgetFactory.genStartGameButton();
        startGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startGame();
            }
        });

        uiComponentsTable.center();
        uiComponentsTable.add(WidgetFactory.genMenusTopBar("GAME SETUP")).colspan(2);

        uiComponentsTable.row();
        uiComponentsTable.left();
        uiComponentsTable.add(setUpPlayerTypesTable()).expand();
        uiComponentsTable.right();
        uiComponentsTable.add(setUpCollegeTable()).expand();

        uiComponentsTable.row();
        uiComponentsTable.left();
        uiComponentsTable.add(setupSwitchTable()).expand();
        uiComponentsTable.add(startGameButton).height(72).width(370);

        uiComponentsTable.row();
        uiComponentsTable.center();
        uiComponentsTable.add(WidgetFactory.genBottomBar("MAIN MENU", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                main.setMenuScreen();
            }

        })).colspan(2).fillX();

        return uiComponentsTable;
    }
}
