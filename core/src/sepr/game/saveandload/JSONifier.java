package sepr.game.saveandload;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import sepr.game.utils.CollegeName;
import sepr.game.utils.PlayerType;
import sepr.game.utils.TurnPhaseType;

import java.util.ArrayList;

/**
 * Class to convert the game state to and from a JSON representation
 */
public class JSONifier {

    public GameState state; // The state of the game
    public JSONObject saveState; // The JSON state of the game

    /**
     * Set the state to represent as JSON
     * @param state The game state
     */
    public void SetState(GameState state){
        this.state = state;
    }

    /**
     * Set the JSON Object to read from
     * @param json JSON Representation of game state to load
     */
    public void SetStateJSON(JSONObject json){
        this.saveState = json;
    }

    /**
     * Get a game state from its JSON representation
     * @return GameState to load
     */
    public GameState getStateFromJSON() {
        GameState gameState = new GameState(); // GameState to return
        gameState.currentPhase = this.StringToPhase(this.saveState.get("CurrentPhase").toString()); // Get the current stage

        JSONArray sectors = (JSONArray) this.saveState.get("MapState"); // Get a JSONArray of the map Sectors
        gameState.mapState = gameState.new MapState(); // MapState to return
        gameState.mapState.sectorStates = new GameState.SectorState[sectors.size()]; // Array of SectorStates to load to the map

        int i = 0; // Index of current sector state to load

        for (Object obj : sectors){ // Iterate through JSON sectors
            JSONObject sector = (JSONObject)obj; // Cast to JSONObject

            gameState.mapState.sectorStates[i] = gameState.new SectorState(); // SectorState to load

            gameState.mapState.sectorStates[i].hashMapPosition = (int)(long)(Long)sector.get("HashMapPosition"); // Get Sector's HashMap position
            gameState.mapState.sectorStates[i].id = (int)(long)(Long)sector.get("ID"); // Get Sector's ID
            gameState.mapState.sectorStates[i].ownerId = (int)(long)(Long)sector.get("OwnerID"); // Get Sector's Owner's ID
            gameState.mapState.sectorStates[i].displayName = (String)sector.get("DisplayName"); // Get Sector's display name
            gameState.mapState.sectorStates[i].undergradsInSector = (int)(long)(Long)sector.get("UndergradsInSector"); // Get the number of undergrads in the Sector
            gameState.mapState.sectorStates[i].postgradsInSector = (int)(long)(Long)sector.get("PostgradsInSector"); // Get the number of postgrads in the Sector
            gameState.mapState.sectorStates[i].reinforcementsProvided = (int)(long)(Long)sector.get("ReinforcementsProvided"); // Get the number of reinforcements provided
            gameState.mapState.sectorStates[i].college = (String)sector.get("College"); // Get the Sector's college
            gameState.mapState.sectorStates[i].texturePath = (String)sector.get("TexturePath"); // Get the Sector's texture filepath
            gameState.mapState.sectorStates[i].neutral = (Boolean)sector.get("Neutral"); // Get whether the Sector is neutral
            gameState.mapState.sectorStates[i].poopCount = (int)(long)(Long)sector.get("PoopCount"); // Get time left of poop effect
            gameState.mapState.sectorStates[i].asbestosCount = (int)(long)(Long)sector.get("AsbestosCount"); // Get time left of asbestos effect


            JSONArray adjacentSectors = (JSONArray)sector.get("AdjacentSectorIDs"); // Get the JSONArray of adjacent Sectors
            gameState.mapState.sectorStates[i].adjacentSectorIds = new int[adjacentSectors.size()]; // Create a new array of adjacent Sectors

            int j = 0; // Index of current adjacent sector

            for (Object adj : adjacentSectors){ // Iterate through adjacent sectors and add each to the adjacent sectors
                gameState.mapState.sectorStates[i].adjacentSectorIds[j] = (int)(long)(Long)adj;

                j++;
            }

            gameState.mapState.sectorStates[i].sectorCentreX = (int)(long)(Long)sector.get("SectorCenterX"); // Get the Sector's X coordinate
            gameState.mapState.sectorStates[i].sectorCentreY = (int)(long)(Long)sector.get("SectorCenterY"); // Get the Sector's Y coordinate
            gameState.mapState.sectorStates[i].decor = (Boolean)sector.get("Decor"); // Get whether the Sector is decor
            gameState.mapState.sectorStates[i].allocated = (Boolean)sector.get("Allocated"); // Get whether the sector has been allocated

            i++;
        }

        JSONArray players = (JSONArray)this.saveState.get("PlayerState"); // Get the JSONArray of player states
        gameState.playerStates = new GameState.PlayerState[players.size()]; // Create an array of Player States

        int k = 0; // Current index of player states

        for (Object pl : players){ // Iterate through players
            JSONObject player = (JSONObject)pl;

            gameState.playerStates[k] = gameState.new PlayerState(); // Create new PlayerState

            gameState.playerStates[k].hashMapPosition = (int)(long)(Long)player.get("HashMapPosition"); // Get Player's HashMap position
            gameState.playerStates[k].id = (int)(long)(Long)player.get("ID"); // Get Player's ID
            gameState.playerStates[k].collegeName = CollegeName.fromString((String)player.get("CollegeName")); // Get Player's college name
            gameState.playerStates[k].troopsToAllocate = (int)(long)(Long)player.get("TroopsToAllocate"); // Get the troops that the Player has left to allocate
            gameState.playerStates[k].playerName = (String)player.get("PlayerName"); // Get Player's name
            gameState.playerStates[k].playerType = PlayerType.fromString((String)player.get("PlayerType")); // Get the Player's type
            gameState.playerStates[k].collusionCards = (int)(long)(Long)player.get("CollusionCards"); // Get num of collusion cards player has
            gameState.playerStates[k].poopyPathCards = (int)(long)(Long)player.get("PoopyPathCards"); // Get num of poopy path cards player has
            gameState.playerStates[k].asbestosCards = (int)(long)(Long)player.get("AsbestosCards"); // Get num of asbestos cards the player has

            k++;
        }

        gameState.turnTimerEnabled = (Boolean)this.saveState.get("TurnTimerEnabled"); // Get whether the turn timer is enabled
        gameState.turnTimeElapsed = (int)(long)(Long)this.saveState.get("TurnTimeElapsed"); // Get the maximum turn time

        gameState.turnOrder = new ArrayList<Integer>(); // Turn order
        JSONArray turnOrderJSON = (JSONArray)this.saveState.get("TurnOrder"); // Get the turn order JSONArray

        for (Object obj : turnOrderJSON){ // Iterate through the turn order array and add the order to the game state
            gameState.turnOrder.add((int)(long)(Long)obj);
        }

        gameState.currentPlayerPointer = (int)(long)(Long)this.saveState.get("CurrentPlayerPointer"); // Get the pointer to the current Player

        return gameState;
    }

    /**
     * Creates JSON representation of GameState
     * @return JSON representation of GameState
     */
    public JSONObject getJSONGameState(){
        JSONObject gameStateObject = new JSONObject(); // Create JSON Object to store state
        gameStateObject.put("CurrentPhase", this.state.currentPhase.toString()); // Store the current phase

        JSONObject mapState = new JSONObject(); // Create JSON Object to store map state

        JSONArray sectorStates = new JSONArray(); // JSONArray of sector states

        for (int i = 0; i < this.state.mapState.sectorStates.length; i++){ // Iterate through sectors in the map
            JSONObject sectorState = new JSONObject(); // Create a JSON object for each state
            GameState.SectorState sector = this.state.mapState.sectorStates[i];

            sectorState.put("HashMapPosition", sector.hashMapPosition); // Store the Sector's position in the HashMap
            sectorState.put("ID", sector.id); // Store the Sector's ID
            sectorState.put("OwnerID", sector.ownerId); // Store the Sector's Owner's ID
            sectorState.put("DisplayName", sector.displayName); // Store the Sector's display name
            sectorState.put("UndergradsInSector", sector.undergradsInSector); // Store the number of undergrads in the Sector
            sectorState.put("PostgradsInSector", sector.postgradsInSector); // Store the number of postgrads in the Sector
            sectorState.put("ReinforcementsProvided", sector.reinforcementsProvided); // Store the number of reinforcements provided to the sector
            sectorState.put("College", sector.college); // Store the college that the Sector belongs to
            sectorState.put("TexturePath", sector.texturePath); // Store the path to the Sector's texture
            sectorState.put("Neutral", sector.neutral); // Store whether the Sector is neutral
            sectorState.put("PoopCount", sector.poopCount);
            sectorState.put("AsbestosCount", sector.asbestosCount);

            JSONArray adjSectors = new JSONArray(); // JSONArray of adjacent sectors

            for (int j = 0; j < sector.adjacentSectorIds.length; j++){ // Store adjacent sectors
                adjSectors.add(sector.adjacentSectorIds[j]);
            }

            sectorState.put("AdjacentSectorIDs", adjSectors);

            sectorState.put("SectorCenterX", sector.sectorCentreX); // Store sector center
            sectorState.put("SectorCenterY", sector.sectorCentreY);
            sectorState.put("Decor", sector.decor); // Store whether the sector is decor
            sectorState.put("Allocated", sector.allocated); // Store whether the sector has been allocated

            sectorStates.add(sectorState);
        }

        gameStateObject.put("MapState", sectorStates); // Store the map state

        JSONArray playerStates = new JSONArray();

        for (int k = 0; k < this.state.playerStates.length; k++){
            JSONObject playerState = new JSONObject();
            GameState.PlayerState player = this.state.playerStates[k];

            playerState.put("HashMapPosition", player.hashMapPosition); // Store Player HashMap position
            playerState.put("ID", player.id); // Store Player ID
            playerState.put("CollegeName", player.collegeName.getCollegeName()); // Store Player college name
            playerState.put("TroopsToAllocate", player.troopsToAllocate); // Store the number of troops left to allocate
            playerState.put("PlayerName", player.playerName); // Store Player name
            playerState.put("CollusionCards", player.collusionCards); // Store num of collusion cards player has
            playerState.put("PoopyPathCards", player.poopyPathCards); // Store num of poopy path cards player has
            playerState.put("AsbestosCards", player.asbestosCards); // Store num of asbestos cards player has

            playerState.put("PlayerType", player.playerType.toString()); // Store the Player's type

            playerStates.add(playerState);
        }

        gameStateObject.put("PlayerState", playerStates); // Store the Player's state

        gameStateObject.put("TurnTimerEnabled", this.state.turnTimerEnabled); // Store whether the turn timer is enabled
        gameStateObject.put("TurnTimeElapsed", this.state.turnTimeElapsed); // Store the max turn time

        JSONArray turnOrder = new JSONArray(); // Store the order of player turns
        for (int i = 0; i < this.state.turnOrder.size(); i++){
            turnOrder.add(this.state.turnOrder.get(i));
        }

        gameStateObject.put("TurnOrder", turnOrder);

        gameStateObject.put("CurrentPlayerPointer", this.state.currentPlayerPointer); // Store the pointer to the current player

        return gameStateObject;
    }

    /**
     * Converts a string to the corresponding phase
     * @param phase string
     * @return Phase object
     */
    public TurnPhaseType StringToPhase(String phase) {
        for (TurnPhaseType type : TurnPhaseType.values()){
            if (type.equalsName(phase)){
                return type;
            }
        }

        return TurnPhaseType.INVALID;
    }

}