package sepr.game.saveandload;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sepr.game.*;

import java.io.*;
import java.util.HashMap;

/*
Modified in assessment 4
 - removed references to save ids and multiple saves as not implemented by previous team
 - fixed not being able to load a game without restarting program if no prior save existed when a game is saved
 - changed method names to follow naming conventions of the rest of the program
 */

/**
 * Class to manage saving and loading from files
 */
public class SaveLoadManager {
    private Main main; // The main class
    private GameScreen gameScreen; // Game screen to read data from

    private static String SAVE_FILE_PATH = ""; // Path to the saves file
    private static GameState loadedState; // The state that has just been loaded

    public SaveLoadManager() {

    }

    /**
     * Initializes the SaveLoadManager
     *
     * @param main Main class
     * @param gameScreen GameScreen to save data from
     */
    public SaveLoadManager(final Main main, GameScreen gameScreen) {
        this.main = main;
        this.gameScreen = gameScreen;

        String home = System.getProperty("user.home"); // Get the user's home directory

        String path = home + File.separator + "Bachelors-of-Domination" + File.separator + "saves" + File.separator + "saves.json"; // Generate the path to the saves.json file
        boolean directoryExists = new File(path).exists();

        SAVE_FILE_PATH = path;

        if(!directoryExists) { // Create a blank saves file
            File file = new File(path);
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();

                JSONObject savesTemplate = new JSONObject();
                try {
                    FileWriter fileWriter = new FileWriter(SAVE_FILE_PATH);
                    fileWriter.write(savesTemplate.toJSONString());
                    fileWriter.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Load GameState JSON from file
     *
     * @return true if game loaded, else false
     */
    private boolean loadFromFile(){
        JSONParser parser = new JSONParser(); // Create JSON parser

        try {
            Object obj = parser.parse(new FileReader(SAVE_FILE_PATH)); // Read file
            JSONObject loadProperties = (JSONObject)obj;

            JSONObject gameStateJSON;
            gameStateJSON = (JSONObject) loadProperties.get("GameState");

            JSONifier jifier = new JSONifier();
            jifier.SetStateJSON(gameStateJSON);

            try {
                loadedState = jifier.getStateFromJSON();
            }catch (NullPointerException e) {
                return false;
            }
            return true;
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } catch (ParseException e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * creates a Map for a game from a collection of players and sectors
     *
     * @param players playing the game on this map
     * @param sectors that are part of this map
     * @return A Map object
     */
    private Map mapFromMapState(HashMap<Integer, Player> players, HashMap<Integer, Sector> sectors){
        return new Map(players, sectors);
    }

    /**
     * creates a Player from a given PlayerState
     *
     * @param playerStates player state data that has been loaded from a save file
     * @return player object, dervied from the passed player state
     */
    public HashMap<Integer, Player> playersFromPlayerState(GameState.PlayerState[] playerStates){
        HashMap<Integer, Player> players = new HashMap<Integer, Player>();

        for (GameState.PlayerState player : playerStates){
            players.put(player.hashMapPosition, new Player(player.id, player.collegeName, player.troopsToAllocate, player.playerType, player.playerName, player.collusionCards, player.poopyPathCards, player.asbestosCards));
        }

        return players;
    }

    /**
     * converts sectorState data into a hashmap of sectors
     *
     * @param sectorStates sectorState data that has been loaded from a saved file
     * @param players hashmap of player ids to their respective players
     * @return mapping of sector id to their respective sector
     */
    public HashMap<Integer, Sector> sectorsFromSectorState(GameState.SectorState[] sectorStates, HashMap<Integer, Player> players){
        HashMap<Integer, Sector> sectors = new HashMap<Integer, Sector>();

        for (GameState.SectorState sector : sectorStates) {
            Pixmap map = new Pixmap(Gdx.files.internal(sector.texturePath));
            Color color = new Color(0, 0, 0, 1);

            for (java.util.Map.Entry<Integer, Player> player : players.entrySet()) {
                if (player.getValue().getId() == sector.ownerId) {
                    color = player.getValue().getSectorColour();
                }
            }
            sectors.put(sector.hashMapPosition, new Sector(sector.id, sector.ownerId, sector.texturePath, map, sector.displayName, sector.undergradsInSector, sector.postgradsInSector, sector.reinforcementsProvided, sector.college, sector.neutral, sector.adjacentSectorIds, sector.sectorCentreX, sector.sectorCentreY, sector.decor, sector.allocated, color, sector.asbestosCount, sector.poopCount));

        }
        return sectors;
    }

    /**
     * Loads and plays the game stored in loadedState
     *
     * @throws NullPointerException if loadedSate is null
     */
    public boolean loadSaveGame(){
        if (!loadFromFile()) return false;
        if (loadedState == null) throw new NullPointerException("Cannot load game if loadedState is null");

        HashMap<Integer, Player> players = playersFromPlayerState(loadedState.playerStates);
        HashMap<Integer, Sector> sectors = sectorsFromSectorState(loadedState.mapState.sectorStates, players);

        Map loadedMap = mapFromMapState(players, sectors);

        this.gameScreen = new GameScreen(this.main, loadedState.currentPhase, loadedMap, players, loadedState.turnTimerEnabled, loadedState.turnTimeElapsed, loadedState.turnOrder, loadedState.currentPlayerPointer);

        this.main.setGameScreenFromLoad(this.gameScreen);
        return true;
    }

    /**
     * saves to the saves.json file
     *
     * @param newSave save object to be written to file
     */
    private void saveToFile(JSONObject newSave){
        try {
            FileWriter fileWriter = new FileWriter(SAVE_FILE_PATH);
            fileWriter.write(newSave.toJSONString());
            fileWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * saves the current instance of the game in play to a file
     */
    public void saveCurrentGame(){
        GameState gameState = new GameState(); // GameState to store data in
        gameState.currentPhase = this.gameScreen.getCurrentPhaseType(); // Store current phase
        gameState.map = this.gameScreen.getMap(); // Store map
        gameState.players = this.gameScreen.getPlayers(); // Store players
        gameState.turnTimerEnabled = this.gameScreen.isTurnTimerEnabled(); // Store whether the turn timer is enabled
        gameState.turnTimeElapsed = (int)this.gameScreen.getTurnTimeElapsed(); // Seconds since player's turn began
        gameState.turnOrder = this.gameScreen.getTurnOrder(); // Store the turn order
        gameState.currentPlayerPointer = this.gameScreen.getCurrentPlayerPointer(); // Store the pointer to the current player

        GameState.MapState mapState = gameState.new MapState(); // Create a new MapState

        mapState.sectors = gameState.map.getSectors(); // Store the Map sectors in the Mapstate
        mapState.sectorStates = new GameState.SectorState[mapState.sectors.size()]; // Create an array of Sector States

        int i = 0;

        for (java.util.Map.Entry<Integer, Sector> sector : mapState.sectors.entrySet()){ // Iterate through each Sector and store it as a SectorState
            Integer key = sector.getKey();
            Sector value = sector.getValue();

            GameState.SectorState sectorState = gameState.new SectorState(); // Create SectorState
            sectorState.hashMapPosition = key; // Store the Sector position in the HashMap
            sectorState.id = value.getId(); // Store the Sector's ID
            sectorState.ownerId = value.getOwnerId(); // Store the Sector's owner's ID
            sectorState.displayName = value.getDisplayName(); // Store the Sector's display name
            sectorState.undergradsInSector = value.getUnderGradsInSector(); // Store the number of units in the Sector
            sectorState.postgradsInSector = value.getPostGradsInSector();
            sectorState.reinforcementsProvided = value.getReinforcementsProvided(); // Store the number of reinforcements provided to the Sector
            sectorState.college = value.getCollege(); // Store the college of the Sector
            sectorState.texturePath = value.getTexturePath(); // Store the path to the Sector's texture
            sectorState.neutral = value.isNeutral(); // Store whether the Sector is neutral
            sectorState.adjacentSectorIds = value.getAdjacentSectorIds(); // Store the adjacent Sector IDs
            sectorState.sectorCentreX = value.getSectorCentreX(); // Store the Sector's location
            sectorState.sectorCentreY = value.getSectorCentreY();
            sectorState.decor = value.isDecor(); // Store whether the Sector is for decoration
            sectorState.allocated = value.isAllocated(); // Store whether the Sector has been allocated
            sectorState.poopCount = value.getPoopCount();
            sectorState.asbestosCount = value.getAsbestosCount();

            mapState.sectorStates[i] = sectorState;

            i++;
        }

        mapState.sectors = null;
        gameState.map = null;
        gameState.mapState = mapState;

        gameState.playerStates = new GameState.PlayerState[gameState.players.size()]; // Create an array of PlayerStates

        i = 0;

        for (java.util.Map.Entry<Integer, Player> player : gameState.players.entrySet()) { // Iterate through all of the Players and store them as PlayerStates
            Integer key = player.getKey();
            Player value = player.getValue();

            GameState.PlayerState playerState = gameState.new PlayerState();
            playerState.hashMapPosition = key; // Store the Player's position in the HashMap
            playerState.id = value.getId(); // Store the Player's ID
            playerState.collegeName = value.getCollegeName(); // Store the Player's college
            playerState.playerName = value.getPlayerName(); // Store the Player's name
            playerState.troopsToAllocate = value.getTroopsToAllocate(); // Store the number of troops left to allocate
            playerState.playerType = value.getPlayerType(); // Store the Player's type
            playerState.collusionCards = value.getCollusionCards(); // Store num of collusion cards player has
            playerState.poopyPathCards = value.getPoopyPathCards(); // Store num of poopy path cards the player has
            playerState.asbestosCards = value.getAsbestosCards(); // Store num of asbestos cards the player has

            gameState.playerStates[i] = playerState;
            i++;
        }

        gameState.players = null;

        JSONObject newSave = new JSONObject(); // Create the save object

        JSONifier jifier = new JSONifier(); // Create a JSON representation of the state
        jifier.SetState(gameState);
        newSave.put("GameState", jifier.getJSONGameState());

        saveToFile(newSave); // Save the JSON representation to a file
    }
}
