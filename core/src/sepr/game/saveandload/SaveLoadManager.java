package sepr.game.saveandload;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.lwjgl.Sys;
import sepr.game.GameScreen;
import sepr.game.Main;
import sepr.game.Map;
import sepr.game.Player;
import sepr.game.Sector;

import java.io.*;
import java.util.HashMap;
import java.util.*;

/**
 * Class to manage saving and loading from files
 */
public class SaveLoadManager {
    public boolean savesToLoad = false; // Whether there are saves available to load from

    private Main main; // The main class
    private GameScreen gameScreen; // Game screen to read data from

    private static String SAVE_FILE_PATH = ""; // Path to the saves file
    private static int currentSaveID = -1; // ID of the current save
    private static int numberOfSaves = 0; // Current number of saves
    private static GameState loadedState; // The state that has just been loaded

    private static Boolean loadedSave = false; // Whether a save has been loaded

    public SaveLoadManager(){ }

    /**
     * Initializes the SaveLoadManager
     * @param main Main class
     * @param gameScreen GameScreen to save data from
     */
    public SaveLoadManager(final Main main, GameScreen gameScreen) {
        this.main = main;
        this.gameScreen = gameScreen;

        loadedSave = false;

        String home = System.getProperty("user.home"); // Get the user's home directory

        String path = home + File.separator + "Bachelors-of-Domination" + File.separator + "saves" + File.separator + "saves.json"; // Generate the path to the saves.json file
        boolean directoryExists = new File(path).exists();

        this.SAVE_FILE_PATH = path;

        if(directoryExists) { // Check that the directory exists
            LoadFromFile(); // Load the file if it exists
        } else { // Create a blank saves file
            File file = new File(path);
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();

                JSONObject savesTemplate = new JSONObject();
                savesTemplate.put("Saves", this.numberOfSaves);
                savesTemplate.put("CurrentSaveID", this.currentSaveID);

                try {
                    FileWriter fileWriter = new FileWriter(this.SAVE_FILE_PATH);
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
     * @return true if loading is successful
     */
    public boolean LoadFromFile(){

        JSONParser parser = new JSONParser(); // Create JSON parser

        try {
            Object obj = parser.parse(new FileReader(SAVE_FILE_PATH)); // Read file
            JSONObject loadProperties = (JSONObject)obj;

            this.numberOfSaves = Integer.parseInt(loadProperties.get("Saves").toString()); // Get number of saves

            if(this.numberOfSaves > 0){ // If saves exist, read the first save into the loaded state
                JSONObject gameStateJSON = (JSONObject)loadProperties.get("GameState"); // TODO Allow for more than one save


                JSONifier jifier = new JSONifier();
                jifier.SetStateJSON(gameStateJSON);
                GameState gameState = jifier.getStateFromJSON();

                this.loadedState = gameState;
                this.savesToLoad = true;
            }else{
                this.savesToLoad = false;
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } catch (ParseException e){
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Creates a Map from a given MapState
     * @param mapState
     * @param players
     * @param sectors
     * @return A Map object
     */
    public Map MapFromMapState(GameState.MapState mapState, HashMap<Integer, Player> players, HashMap<Integer, Sector> sectors){
        Map map = new Map(players, false, sectors);

        return map;
    }

    /**
     * Creates a Player from a given PlayerState
     * @param playerStates
     * @return A Player object
     */
    public HashMap<Integer, Player> PlayersFromPlayerState(GameState.PlayerState[] playerStates){
        HashMap<Integer, Player> players = new HashMap<Integer, Player>();

        for (GameState.PlayerState player : playerStates){
            players.put(player.hashMapPosition, new Player(player.id, player.collegeName, new Color(player.sectorColour.r, player.sectorColour.g, player.sectorColour.b, player.sectorColour.a), player.playerType, player.playerName, player.troopsToAllocate, player.ownsPVC));
        }

        return players;
    }

    /**
     * Creates a HashMap of Sectors from a list of SectorStates
     * @param sectorStates
     * @param players
     * @return A HashMap of Sectors
     */
    public HashMap<Integer, Sector> SectorsFromSectorState(GameState.SectorState[] sectorStates, HashMap<Integer, Player> players, boolean test){
        HashMap<Integer, Sector> sectors = new HashMap<Integer, Sector>();

        for (GameState.SectorState sector : sectorStates) {
            Pixmap map = new Pixmap(Gdx.files.internal(sector.texturePath));

            Color color = new Color(0, 0, 0, 1);

            for (java.util.Map.Entry<Integer, Player> player : players.entrySet()) {
                if (player.getValue().getId() == sector.ownerId) {
                    color = player.getValue().getSectorColour();
                }
            }

            if (test) {
                sectors.put(sector.hashMapPosition, new Sector(sector.id, sector.ownerId, sector.fileName, sector.texturePath, map, sector.displayName, sector.unitsInSector, sector.reinforcementsProvided, sector.college, sector.neutral, sector.adjacentSectorIds, sector.sectorCentreX, sector.sectorCentreY, sector.decor, sector.allocated, color, test, sector.asbestosCount, sector.poopCount, sector.map));
            }else{
                sectors.put(sector.hashMapPosition, new Sector(sector.id, sector.ownerId, sector.fileName, sector.texturePath, map, sector.displayName, sector.unitsInSector, sector.reinforcementsProvided, sector.college, sector.neutral, sector.adjacentSectorIds, sector.sectorCentreX, sector.sectorCentreY, sector.decor, sector.allocated, color, sector.asbestosCount, sector.poopCount, sector.map));
            }
        }

        return sectors;
    }

    /**
     * Loads a save file with a given ID
     * @param id
     * @return true if loading is successful
     */
    public boolean LoadSaveByID(int id){
        HashMap<Integer, Player> players = PlayersFromPlayerState(loadedState.playerStates);
        HashMap<Integer, Sector> sectors = SectorsFromSectorState(loadedState.mapState.sectorStates, players, false);

        Map loadedMap = MapFromMapState(loadedState.mapState, players, sectors);

        this.gameScreen = new GameScreen(this.main, loadedState.currentPhase, loadedMap, players, loadedState.turnTimerEnabled, loadedState.maxTurnTime, loadedState.turnTimeStart, loadedState.turnOrder, loadedState.currentPlayerPointer);

        this.main.setGameScreenFromLoad(this.gameScreen);

        return true;
    }

    /**
     * Saves to the saves.json file
     * @param newSave
     * @return true if loading is successful
     */
    public boolean SaveToFile(JSONObject newSave){
        try {
            FileWriter fileWriter = new FileWriter(this.SAVE_FILE_PATH);
            fileWriter.write(newSave.toJSONString());
            fileWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Save to a save with a given ID
     * @param id
     * @return true if saving is successful
     */
    public boolean SaveByID(int id){
        GameState gameState = new GameState(); // GameState to store data in
        gameState.currentPhase = this.gameScreen.getCurrentPhase(); // Store current phase
        gameState.map = this.gameScreen.getMap(); // Store map
        gameState.players = this.gameScreen.getPlayers(); // Store players
        gameState.turnTimerEnabled = this.gameScreen.isTurnTimerEnabled(); // Store whether the turn timer is enabled
        gameState.maxTurnTime = this.gameScreen.getMaxTurnTime(); // Store the maximum turn time
        gameState.turnTimeStart = this.gameScreen.getTurnTimeStart(); // Store the start time of the current turn
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
            sectorState.unitsInSector = value.getUnitsInSector(); // Store the number of units in the Sector
            sectorState.reinforcementsProvided = value.getReinforcementsProvided(); // Store the number of reinforcements provided to the Sector
            sectorState.college = value.getCollege(); // Store the college of the Sector
            sectorState.texturePath = value.getTexturePath(); // Store the path to the Sector's texture
            sectorState.neutral = value.isNeutral(); // Store whether the Sector is neutral
            sectorState.adjacentSectorIds = value.getAdjacentSectorIds(); // Store the adjacent Sector IDs
            sectorState.sectorCentreX = value.getSectorCentreX(); // Store the Sector's location
            sectorState.sectorCentreY = value.getSectorCentreY();
            sectorState.decor = value.isDecor(); // Store whether the Sector is for decoration
            sectorState.fileName = value.getFileName(); // Store the filename of the Sector file
            sectorState.allocated = value.isAllocated(); // Store whether the Sector has been allocated

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
            playerState.sectorColour = value.getSectorColour(); // Store the Player's Sector's colour
            playerState.playerType = value.getPlayerType(); // Store the Player's type
            playerState.ownsPVC = value.getOwnsPVC(); // Store whether the Player owns the PVC

            gameState.playerStates[i] = playerState;
            i++;
        }

        gameState.players = null;

        JSONObject newSave = new JSONObject(); // Create the save object
        newSave.put("Saves", this.numberOfSaves);
        newSave.put("CurrentSaveID", this.currentSaveID);

        JSONifier jifier = new JSONifier(); // Create a JSON representation of the state
        jifier.SetState(gameState);
        newSave.put("GameState", jifier.getJSONGameState());

        SaveToFile(newSave); // Save the JSON representation to a file

        return true;
    }

    /**
     * Returns the ID of the currently loaded save, generates a new ID if no file is loaded
     * @return The current save ID
     */
    public int GetCurrentSaveID(){
        if (!this.loadedSave){
            return GetNextSaveID();
        }else{
            return this.currentSaveID;
        }
    }

    /**
     * Returns the next available save ID
     * @return The nexxt available save ID
     */
    public int GetNextSaveID(){
        this.currentSaveID++;
        this.numberOfSaves++;

        return this.currentSaveID;
    }

}
