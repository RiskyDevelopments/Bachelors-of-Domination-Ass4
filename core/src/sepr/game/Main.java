package sepr.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import sepr.game.saveandload.SaveLoadManager;

import java.util.HashMap;

/**
 * executable http://www.riskydevelopments.co.uk/XXXXXXXXXXXXXXXXXX.zip
 *
 * main game class used for controlling what screen is currently being displayed
 */
public class Main extends Game implements ApplicationListener {
	private MenuScreen menuScreen;
	private GameSetupScreen gameSetupScreen;
	private OptionsScreen optionsScreen;
	private GameScreen gameScreen;
	private MiniGameScreen miniGameScreen;

	private SaveLoadManager saveLoadManager;
	private AudioManager audio = AudioManager.getInstance();

	/**
	 * Setup the screens and set the first screen as the menu
	 */
	@Override
	public void create () {
		new WidgetFactory(); // setup widget factory for generating UI components
		new DialogFactory(); // setup dialog factory for generating dialogs
		new AudioPlayer();

		this.menuScreen = new MenuScreen(this);
		this.gameSetupScreen = new GameSetupScreen(this);
		this.optionsScreen = new OptionsScreen(this);
		this.gameScreen = new GameScreen(this);
		this.miniGameScreen = new MiniGameScreen( this, gameScreen);
		this.saveLoadManager = new SaveLoadManager(this, gameScreen);

		applyPreferences();

		this.setMenuScreen();
	}

	public GameScreen getGameScreen() {
		return gameScreen;
	}

	/**
	 * sets the current screen to the minigame
	 * reset the game for the current player to play
	 * starts the game
	 */
	public void setMiniGameScreen() {
		System.out.println("Setting MG1");
		miniGameScreen = new MiniGameScreen(this, gameScreen);
		System.out.println("Setting MG2");
		miniGameScreen.setupGame(gameScreen.getPlayerById(gameScreen.getCurrentPlayerPointer()));
		System.out.println("Setting MG3");
		this.setScreen(miniGameScreen);
		System.out.println("Setting MG4");
		miniGameScreen.startGame();
		System.out.println("Setting MG4");
	}

	/**
	 * sets the current screen to the menu screen
	 */
	public void setMenuScreen() {
		this.setScreen(menuScreen);
	}

	/**
	 * displays the game screen and starts a game with the passed properties
	 *
	 * @param players hashmap of players who should be present in the game
	 * @param turnTimerEnabled whether or not this game should have a turn timer on
	 * @param allocateNeutralPlayer should the neutral player be given sectors to start with
	 */
	public void setGameScreen(HashMap<Integer, Player> players, boolean turnTimerEnabled, boolean allocateNeutralPlayer) {
		gameScreen.setupGame(players, turnTimerEnabled, allocateNeutralPlayer);
		this.setScreen(gameScreen);
		gameScreen.startGame();
	}

	/**
	 * sets the gamescreen to the passed one
	 * to be used when loading a save game
	 *
	 * @param screen instance of gamescreen to be used for the game
	 */
	public void setGameScreenFromLoad(GameScreen screen){
	    this.gameScreen = screen;
	    this.setScreen(this.gameScreen);
	    this.gameScreen.startGame();
    }

	/**
	 * change the screen currently being displayed to the options screen
	 */
	public void setOptionsScreen() {
		this.setScreen(optionsScreen);
	}

	/**
	 * change the screen currently being displayed to the game setup screen
	 */
	public void setGameSetupScreen() {
		this.setScreen(gameSetupScreen);
	}

	/**
	 * saves the game currently in play
	 */
	public void saveGame(){
        this.saveLoadManager.saveCurrentGame();
    }

	/**
	 * checks to see if there is a save available to load
	 *
	 * @return true if there is a save available to load, else false
	 */
	public boolean hasLoadedSaves(){
		this.saveLoadManager = new SaveLoadManager(this, gameScreen);
		return this.saveLoadManager.savesToLoad;
	}

	/**
	 * loads the save game from file
	 */
    public void loadGame(){
	    this.saveLoadManager.LoadFromFile();
		this.saveLoadManager.loadSaveGame();
	}

	/**
	 * Applies the players options preferences
	 * Sets the
	 *      Music Volume
	 *      FX Volume
	 *      Screen Resolution
	 *      Fullscreen
	 * A default setting should be applied for any missing preferences
	 */
	public void applyPreferences() {
		Preferences prefs = Gdx.app.getPreferences(OptionsScreen.PREFERENCES_NAME);

		AudioManager.GLOBAL_FX_VOLUME = prefs.getFloat(OptionsScreen.FX_VOL_PREF, 0.5f);
		AudioManager.GLOBAL_MUSIC_VOLUME = prefs.getFloat(OptionsScreen.MUSIC_VOL_PREF, 0.5f);
		audio.setMusicVolume();

		int screenWidth = prefs.getInteger(OptionsScreen.RESOLUTION_WIDTH_PREF, 1920);
		int screenHeight = prefs.getInteger(OptionsScreen.RESOLUTION_HEIGHT_PREF, 1080);
		Gdx.graphics.setWindowedMode(screenWidth, screenHeight);

		if (prefs.getBoolean(OptionsScreen.FULLSCREEN_PREF)) {
			// change game to fullscreen
			Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		}
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		super.render();
	}

	@Override
	public void dispose() {
		super.dispose();
		menuScreen.dispose();
		optionsScreen.dispose();
		gameSetupScreen.dispose();
		gameScreen.dispose();
	}
}

