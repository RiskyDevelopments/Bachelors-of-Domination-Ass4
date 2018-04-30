package sepr.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import sepr.game.utils.CollegeName;
import sepr.game.utils.PunishmentCardType;
import sepr.game.utils.TurnPhaseType;

/*
Modified in assessment 4
 - changed using instance of AudioManager to play sound FX to the new AudioPlayer
 - added ability for player to have a punishment card selected and apply it to a sector - see touchUp(int screenX, int screenY, int pointer, int button) method
 - changed to using AudioPlayer for playing audio instead of using instance of AudioManager
 */

/**
 * base class for handling phase specific input
 */
public abstract class Phase extends Stage {
    GameScreen gameScreen;
    Player currentPlayer;

    private PunishmentCardType punishmentCardSelected = PunishmentCardType.NO_CARD;

    private Table table;
    private Label bottomBarRightPart;
    private Label topBarTextLabel;
    private TurnPhaseType turnPhase;

    private Label.LabelStyle playerNameStyle; // store style for updating player name colour with player's colour

    private Label playerNameLabel; // displays the name of the current player in their college's colour colour
    private Label reinforcementLabel; // label showing how many troops the player has to allocate in their next reinforcement phase
    private Label turnTimerLabel; // displays how much time the player has left
    private Image collegeLogo; // ui component for displaying the logo of the current players college

    private static Texture gameHUDBottomBarLeftPartTexture;

    /**
     *
     * @param gameScreen for accessing the map and additional game properties
     * @param turnPhase type of phase this is
     */
    public Phase(GameScreen gameScreen, TurnPhaseType turnPhase) {
        this.setViewport(new ScreenViewport());

        this.gameScreen = gameScreen;

        this.turnPhase = turnPhase;

        this.table = new Table();
        this.table.setFillParent(true); // make ui table fill the entire screen
        this.addActor(table);
        this.table.setDebug(false); // enable table drawing for ui debug

        gameHUDBottomBarLeftPartTexture = new Texture("uiComponents/HUD-Bottom-Bar-Left-Part.png");

        this.setupUi();
    }

    /**
     * setup UI that is consistent across all game phases
     */
    private void setupUi() {
        TextButton endPhaseButton = WidgetFactory.genEndPhaseButton();
        endPhaseButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                gameScreen.nextPhase();
                AudioPlayer.playButtonClick();
            }
        });
        bottomBarRightPart = WidgetFactory.genGameHUDBottomBarRightPart("INIT");
        Table bottomBarLeftPart = genGameHUDBottomBarLeftPart();
        table.setDebug(false);
        table.top().center();

        topBarTextLabel = new Label("TOP BAR TEXT", DialogFactory.skin);
        table.add(WidgetFactory.genGameHUDTopBar(this, topBarTextLabel, gameScreen)).colspan(2).expandX().height(72).width(910).padTop(80);

        table.row();
        table.add(new Table()).expand();

        Table subTable = new Table();

        subTable.bottom();
        subTable.add(bottomBarLeftPart).height(220).width(285);
        subTable.add(bottomBarRightPart).bottom().expandX().fillX().height(72);

        table.row();
        table.add(subTable).expandX().fill().padBottom(80).padLeft(90).padRight(30);
        table.bottom().right();
        table.add(endPhaseButton).fill().height(60).width(170).padRight(95).padTop(80);

        setBottomBarText(null);
        updateTopBarText();
    }

    /**
     * updates the text on the top bar of the HUD to show the current phase or which punishment card is being used currently
     */
    private void updateTopBarText() {
        String text = "";

        if (getPunishmentCardSelected() == PunishmentCardType.NO_CARD) {
            switch (turnPhase) {
                case REINFORCEMENT:
                    text = "REINFORCEMENT  -  Attack  -  Movement";
                    break;
                case ATTACK:
                    text = "Reinforcement  -  ATTACK  -  Movement";
                    break;
                case MOVEMENT:
                    text = "Reinforcement  -  Attack  -  MOVEMENT";
                    break;
            }
        } else {
            text = "Select a sector to apply the " + getPunishmentCardSelected().toString() + " to";
        }
        topBarTextLabel.setText(text);
    }

    /**
     * generates the UI widget to be displayed at the bottom left of the HUD
     *
     * @return table containing the information to display in the HUD
     */
    private Table genGameHUDBottomBarLeftPart(){
        Label.LabelStyle style = new Label.LabelStyle();
        playerNameStyle = new Label.LabelStyle();

        // load fonts
        style.font = WidgetFactory.getFontSmall();

        playerNameStyle.font = WidgetFactory.getFontSmall();
        playerNameStyle.background = new TextureRegionDrawable(new TextureRegion(new Texture("uiComponents/Name-Box.png")));

        playerNameLabel = new Label("", playerNameStyle);
        reinforcementLabel = new Label("", style);
        turnTimerLabel = new Label("Timer: DISABLED", style);
        collegeLogo = new Image(WidgetFactory.genCollegeLogoDrawable(CollegeName.UNI_OF_YORK));

        Table table = new Table();
        table.background(new TextureRegionDrawable(new TextureRegion(gameHUDBottomBarLeftPartTexture)));

        Table subTable = new Table();
        subTable.setDebug(false);
        subTable.left().add(collegeLogo).height(80).width(100).pad(0);
        subTable.right().add(playerNameLabel).pad(0).height(40).expandX();
        subTable.row();
        subTable.add(reinforcementLabel).colspan(2);
        subTable.row();
        subTable.add(turnTimerLabel).colspan(2);

        table.add(subTable);

        return table;
    }

    /**
     * sets the bar at the bottom of the HUD to the details of the sector currently hovered over
     * if no sector is being hovered then displays "Mouse over a sector to see further details"
     *
     * @param sector the sector of details to be displayed
     */
    public void setBottomBarText(Sector sector) {
        if (sector == null) {
            this.bottomBarRightPart.setText("Mouse over a sector to see further details");
        } else {
            this.bottomBarRightPart.setText("College: " + sector.getCollege() + " - " + sector.getDisplayName() + " - " + "Owned By: " + gameScreen.getPlayerById(sector.getOwnerId()).getPlayerName() + " - " + "Grants +" + sector.getReinforcementsProvided() + " Troops");
        }
    }

    /**
     * sets up phase when a new player enters it
     *
     * @param player the new player that is entering the phase
     */
    void enterPhase(Player player) {
        this.currentPlayer = player;

        Color fontColor = new Color(currentPlayer.getCollegeName().getCollegeColor());
        fontColor.a = 1;
        playerNameStyle.fontColor =  fontColor; // update colour of player name

        playerNameLabel.setText(" " + new StringBuilder((CharSequence) currentPlayer.getPlayerName()) + " "); // change the bottom bar label to the players name
        collegeLogo.setDrawable(WidgetFactory.genCollegeLogoDrawable(player.getCollegeName()));
        updateTroopReinforcementLabel();
    }

    /**
     * updates the text of the turn timer label
     *
     * @param timeRemaining time remaining of turn in seconds
     */
    void setTimerValue(int timeRemaining) {
        turnTimerLabel.setText(new StringBuilder("Turn Timer: " + timeRemaining));
    }

    /**
     *
     * @return the currently selected type of punishment card
     */
    private PunishmentCardType getPunishmentCardSelected() {
        return punishmentCardSelected;
    }

    /**
     *
     * @throws IllegalArgumentException if punishmentCardSelected is not Collusion/Asbestos/PoopyPath/NoCard card
     * @param punishmentCardSelected punishment card that has been selected
     */
    public void setPunishmentCardSelected(PunishmentCardType punishmentCardSelected) {
        switch (punishmentCardSelected) {
            case COLLUSION_CARD:
            case ASBESTOS_CARD:
            case POOPY_PATH_CARD:
            case NO_CARD:
                break;
            default:
                throw new IllegalArgumentException("May only set selected punishment card to the Collusion/Asbestos/PoopyPath/NoCard Card");
        }
        this.punishmentCardSelected = punishmentCardSelected;
        updateTopBarText();
    }

    /**
     * updates the display of the number of troops the current player will have in their next reinforcement phase
     */
    void updateTroopReinforcementLabel() {
        this.reinforcementLabel.setText("Troop Allocation: " + currentPlayer.getTroopsToAllocate());
    }

    /**
     * method for tidying up phase for next player to use
     */
    public void endPhase () {
        this.currentPlayer = null;
    }

    /**
     * abstract method for writing phase specific rendering
     *
     * @param batch
     */
    protected abstract void visualisePhase(SpriteBatch batch);

    @Override
    public void act() {
        super.act();
    }

    @Override
    public void draw() {
        gameScreen.getGameplayBatch().begin();
        visualisePhase(gameScreen.getGameplayBatch());
        gameScreen.getGameplayBatch().end();

        super.draw();
    }

    @Override
    public String toString() {
        switch(this.turnPhase){
            case ATTACK:
                return "PHASE_ATTACK";
            case MOVEMENT:
                return "PHASE_MOVEMENT";
            case REINFORCEMENT:
                return "PHASE_REINFORCEMENT";
            default:
                return "PHASE_BLANK";
        }
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (super.touchUp(screenX, screenY, pointer, button)) {
            return true;
        }

        if (getPunishmentCardSelected() == PunishmentCardType.NO_CARD) {
            return false;
        }
        Vector2 worldCoords = gameScreen.screenToWorldCoords(Gdx.input.getX(), Gdx.input.getY());
        int sectorClicked = gameScreen.getMap().detectSectorContainsPoint((int)worldCoords.x, (int)worldCoords.y);
        if (sectorClicked == -1) {
            // no sector pressed
            return true;
        } else {
            gameScreen.getMap().addSectorPunishmentEffect(sectorClicked, punishmentCardSelected);
            switch (punishmentCardSelected) {
                case COLLUSION_CARD:
                    currentPlayer.addCollusionCards(-1);
                    break;
                case POOPY_PATH_CARD:
                    currentPlayer.addPoopyPathCards(-1);
                    break;
                case ASBESTOS_CARD:
                    currentPlayer.addAsbestosCards(-1);
                    break;
            }
            punishmentCardSelected = PunishmentCardType.NO_CARD;
            updateTopBarText();
            return true;
        }
    }
}
