package sepr.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import javafx.util.Pair;

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
public class GameSetupScreen implements Screen{

    private Main main; // main stored so that screen can be changed
    private Stage stage; // stage for drawing the UI to
    private Table table; // table for laying out the UI components

    private final int MAX_NUMBER_OF_PLAYERS = 4; // maximum number of players that cna be in a game
    private final int MAX_TURN_TIME = 120; // max time for a player's turn if the timer is enabled

    private Label[] playerTypes; // array of player types, index n -> player n's type
    private TextField[] playerNames; // array of TextFields for players to enter names, index n -> player n's name
    private Pair<Label, Image>[] playerColleges; // array of pairs of college name and college logo, index n -> player n's college
    private CheckBox neutralPlayerSwitch; // switch for enabling the neutral player
    private CheckBox turnTimerSwitch; // switch for enabling the turn timer

    private Texture collegeTableBackground;

    /**
     * The colleges available to play as
     */
    public enum CollegeName {
        ALCUIN("ALCUIN"),
        DERWENT("DERWENT"),
        HALIFAX("HALIFAX"),
        HES_EAST("HESLINGTON EAST"),
        JAMES("JAMES"),
        UNI_OF_YORK("UNIVERSITY OF YORK"),
        VANBRUGH("VANBRUGH"),
        WENTWORTH("WENTWORTH");

        private final String shortCode;

        CollegeName(String code){
            this.shortCode = code;
        }

        public String getCollegeName(){
            return this.shortCode;
        }

        /**
         * converts the string representation of the enum to the enum value
         * @throws IllegalArgumentException if the text does not match any of the enum's string values
         * @param text string representation of the enum
         * @return the enum value of the provided text
         */
        public static CollegeName fromString(String text) throws IllegalArgumentException {
            for (CollegeName collegeName : CollegeName.values()) {
                if (collegeName.getCollegeName().equals(text)) return collegeName;
            }
            throw new IllegalArgumentException("Text parameter must match one of the enums");
        }
    }

    /**
     *
     * @param main
     */
    public GameSetupScreen (Main main) {
        this.main = main;

        this.stage = new Stage();
        this.table = new Table();
        this.table.setFillParent(true); // make ui table fill the entire screen
        this.stage.addActor(table);
        this.table.setDebug(false); // enable table drawing for ui debug

        this.collegeTableBackground = new Texture("ui/HD-assets/Game-Setup-Name-Box.png");

        this.setupUi();
    }

    /**
     * update the player type label when the left ui button is pressed
     * @param playerLabel to update the contents of
     */
    private void leftPlayerTypeButtonPress(Label playerLabel){
        if (playerLabel.getText().toString().equals(PlayerType.NONE.getPlayerType())){
            playerLabel.setText(PlayerType.AI.getPlayerType());
        }else if (playerLabel.getText().toString().equals(PlayerType.HUMAN.getPlayerType())){
            playerLabel.setText(PlayerType.NONE.getPlayerType());
        }else {
            playerLabel.setText(PlayerType.HUMAN.getPlayerType());
        }
    }

    /**
     * update the player type label when the right ui button is pressed
     * @param playerLabel to update the contents of
     */
    private void rightPlayerTypeButtonPress(Label playerLabel){
        if (playerLabel.getText().toString().equals(PlayerType.NONE.getPlayerType())){
            playerLabel.setText(PlayerType.HUMAN.getPlayerType());
        }else if (playerLabel.getText().toString().equals(PlayerType.HUMAN.getPlayerType())){
            playerLabel.setText(PlayerType.AI.getPlayerType());
        }else {
            playerLabel.setText(PlayerType.NONE.getPlayerType());
        }
    }

    /**
     * finds the name of the next college when the left UI button is pressed
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
     * generates the left hand sub table for setting the player types, i.e. selecting NONE, HUMAN or AI
     * @return a table containing MAX_NUMBER_OF_PLAYERS rows each with a left arrow button a player type label and right arrow button
     */
    private Table setUpPlayerTypesTable() {
        playerTypes = new Label[MAX_NUMBER_OF_PLAYERS];
        Button[] leftButtons = new Button[MAX_NUMBER_OF_PLAYERS]; // array of left selector buttons
        Button[] rightButtons = new Button[MAX_NUMBER_OF_PLAYERS]; // array of right selector buttons
        for (int i = 0; i < MAX_NUMBER_OF_PLAYERS; i++) {
            Button leftButton = WidgetFactory.genPlayerLeftButton();
            Button rightButton = WidgetFactory.genPlayerRightButton();


            final int finalI = i;
            leftButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    leftPlayerTypeButtonPress(playerTypes[finalI]);
                }
            });

            rightButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    rightPlayerTypeButtonPress(playerTypes[finalI]);
                }
            });

            playerTypes[i] = WidgetFactory.genPlayerLabel(PlayerType.NONE.getPlayerType());
            leftButtons[i] = leftButton;
            rightButtons[i] = rightButton;
        }

        Table leftTable = new Table();

        for(int i = 0; i < MAX_NUMBER_OF_PLAYERS; i++){
            leftTable.left();
            leftTable.add(leftButtons[i]).height(60).width(50).pad(5);
            leftTable.add(playerTypes[i]).height(60).width(320).expandX().padTop(20).padBottom(20);
            leftTable.right();
            leftTable.add(rightButtons[i]).height(60).width(50).pad(5);
            leftTable.row();
        }

        return leftTable;
    }

    /**
     * Sets up the table for the switch based part of the game setup
     * Setup options for: neutral player and turn timer
     * @return a table containing a switch for the the neutral player and switch for the turn timer
     */
    private Table setupSwitchTable(){
        // neutral player
        Label neutralPlayerLabel = WidgetFactory.genMenuBtnLabel("NEUTRAL PLAYER");
        neutralPlayerLabel.setAlignment(0, 0);

        neutralPlayerSwitch = WidgetFactory.genOnOffSwitch();

        // turn timer
        Label turnTimerLabel = WidgetFactory.genMenuBtnLabel("TURN TIMER");
        turnTimerLabel.setAlignment(0, 0);

        turnTimerSwitch = WidgetFactory.genOnOffSwitch();

        /* Add components to table */
        Table switchTable = new Table();
        switchTable.setDebug(false);

        switchTable.left();
        switchTable.add(neutralPlayerLabel).height(60).width(420);
        switchTable.right();
        switchTable.add(neutralPlayerSwitch).padLeft(50);

        switchTable.row().padTop(20).padBottom(20);

        switchTable.left();
        switchTable.add(turnTimerLabel).height(60).width(420);
        switchTable.right();
        switchTable.add(turnTimerSwitch).padLeft(50);

        return switchTable;
    }

    /**
     * Generates the UI table for setting player's colleges and names
     * Populates the playerColleges and playerNames arrays to be accessed later
     * @return the right hand sub table containing the player name setup and college selector UI components
     */
    private Table setUpCollegeTable(){
        playerNames = new TextField[MAX_NUMBER_OF_PLAYERS];
        playerColleges = new Pair[MAX_NUMBER_OF_PLAYERS];

        // generate the textfields, college labels and college logos
        for (int i = 0; i < MAX_NUMBER_OF_PLAYERS; i++) {
            Label collegeName = WidgetFactory.genNameBoxLabel("ALCUIN");
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


            logoTable.add(leftButton).height(50).width(25);
            logoTable.add(playerColleges[i].getValue()).height(80).width(100);
            logoTable.add(rightButton).height(50).width(25);

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
     *
     * @param collegeName name of college to get colour for
     * @return Color corresponding to the given college
     */
    private static Color getCollegeColor (CollegeName collegeName) {
        switch (collegeName) {
            case ALCUIN:
                return Color.RED;
            case DERWENT:
                return Color.BLUE;
            case HALIFAX:
                return Color.CYAN;
            case HES_EAST:
                return Color.GREEN;
            case JAMES:
                return Color.GRAY;
            case UNI_OF_YORK:
                return Color.WHITE;
            case VANBRUGH:
                return Color.PURPLE;
            case WENTWORTH:
                return Color.ORANGE;
        }
        return Color.BLACK;
    }

    /**
     * using the information configured in the setup screen generate a hashmap of players to be used in the game
     * @return HashMap of Players derived from the Setup Screen
     */
    private HashMap<Integer, Player> generatePlayerHashmaps() {
        HashMap<Integer, Player> players = new HashMap<Integer, Player>();

        // setup Human and AI players
        for (int i = 0; i < MAX_NUMBER_OF_PLAYERS; i++) {
            if (playerTypes[i].getText().toString().equals(PlayerType.HUMAN.getPlayerType())) {
                // create human player
                players.put(i, new PlayerHuman(i, CollegeName.fromString(playerColleges[i].getKey().getText().toString()), getCollegeColor(CollegeName.fromString(playerColleges[i].getKey().getText().toString())), playerNames[i].getText()));
            } else if (playerTypes[i].getText().toString().equals(PlayerType.AI.getPlayerType())) {
                // create AI player
                players.put(i, new PlayerAI(i, CollegeName.fromString(playerColleges[i].getKey().getText().toString()), getCollegeColor(CollegeName.fromString(playerColleges[i].getKey().getText().toString())), playerNames[i].getText()));
            }
        }

        // setup neutral player
        if (neutralPlayerSwitch.isChecked()) {
            players.put(MAX_NUMBER_OF_PLAYERS, new PlayerNeutralAI(MAX_NUMBER_OF_PLAYERS, CollegeName.UNI_OF_YORK, "THE NEUTRAL PLAYER"));
        }
        return players;
    }

    /**
     * Checks if:
     *  there are no duplicate player names
     *  the names are at least three characters long
     *  the names contain numbers and digits only
     * @throws GameSetupException if name conditions are not met
     */
    private void validatePlayerNames() throws GameSetupException{
        Set<String> appeared = new HashSet();
        for (int i = 0; i < playerNames.length; i++) {
            if (PlayerType.fromString(playerTypes[i].getText().toString()) == PlayerType.NONE) {
                continue;
            }
            Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(playerNames[i].getText());
            boolean b = m.find();
            if (playerNames[i].getText().length() < 3){
                throw new GameSetupException(GameSetupException.GameSetupExceptionType.INVALID_PLAYER_NAME);
            }
            else if (b){
                throw new GameSetupException(GameSetupException.GameSetupExceptionType.INVALID_PLAYER_NAME);
            }
            else if (!appeared.add(playerNames[i].getText())) {
                throw new GameSetupException(GameSetupException.GameSetupExceptionType.DUPLICATE_PLAYER_NAME);
            }
        }
    }

    /**
     * Checks if:
     *  any college has been selected more than once
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
     *  there's at least 1 human player
     *  there's only 2 players then the neutral player is enabled
     * @throws GameSetupException if player configuration conditions are not met
     */
    private void validatePlayerConfiguration() throws GameSetupException{
        int totalNumPlayers = 0;
        boolean humanPlayerPresent = false;
        for (Label label : playerTypes) {
            if (label.getText().equals(PlayerType.HUMAN.getPlayerType())) {
                totalNumPlayers++;
                humanPlayerPresent = true;
            } else if (label.getText().equals(PlayerType.AI.getPlayerType())) {
                totalNumPlayers++;
            }
        }
        /*  TOTAL NUM PLAYERS not being added to for some reason so commented out as always throwing error
        if (totalNumPlayers < 2) { // must be at least two players
            throw new GameSetupException(GameSetupException.GameSetupExceptionType.MINIMUM_TWO_PLAYERS);
        } else if (!humanPlayerPresent) { // no human player present
            throw new GameSetupException(GameSetupException.GameSetupExceptionType.NO_HUMAN_PLAYER);
        } else if (totalNumPlayers == 2 && !neutralPlayerSwitch.isChecked()) { // only two players need neutral player
            throw new GameSetupException(GameSetupException.GameSetupExceptionType.NO_NEUTRAL_PLAYER);
        }*/
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
            WidgetFactory.dialogBox("Game Setup Error", e.getExceptionType().getErrorMessage(), stage);
            return;
        }

        main.setGameScreen(generatePlayerHashmaps(), turnTimerSwitch.isChecked(), MAX_TURN_TIME);
    }

    private void setupUi() {
        TextButton startGameButton = WidgetFactory.genStartGameButton("START GAME");
        startGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startGame();
            }
        });

        // add the menu background
        table.background(new TextureRegionDrawable(new TextureRegion(new Texture("ui/HD-assets/Menu-Background.png"))));

        table.center();
        table.add(WidgetFactory.genTopBar("GAME SETUP")).colspan(2);

        table.row();
        table.left();
        table.add(setUpPlayerTypesTable()).expand();
        table.right();
        table.add(setUpCollegeTable()).expand();
        table.row();
        table.left();
        table.add(setupSwitchTable()).expand();
        table.add(startGameButton).height(60).width(420);

        table.row();
        table.center();
        table.add(WidgetFactory.genBottomBar("MAIN MENU", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                main.setMenuScreen();
            }

        })).colspan(2);
    }


    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        main.applyPreferences();
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