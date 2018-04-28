package sepr.game.saveandload;

import sepr.game.Map;
import sepr.game.Player;
import sepr.game.Sector;
import sepr.game.utils.CollegeName;
import sepr.game.utils.PlayerType;
import sepr.game.utils.TurnPhaseType;

import java.util.HashMap;
import java.util.List;

/*
Modified in assessment 4
 - fixed how turn time remaining is stored so it can be reloaded successfully
 - added punishment cards to the player state so they may be saved/loaded
 - removed sector color from player state as no longer stored in player class
 - added postgradUnitsInSector to sector state so they may be saved/loaded
 - removed storing sector filePath variable as no longer stored in sector
 - added asbestosCount and poopCount to sector state so sector status effects may be saved/loaded
 */

/**
 * Class to store data relating to the current state of the game in preparation for saving
 */
public class GameState {
    public TurnPhaseType currentPhase; // Current phase of the game
    public Map map; // The map of the game
    public MapState mapState; // The stripped-down map state
    public HashMap<Integer, Player> players; // HashMap of players
    public PlayerState[] playerStates; // Stripped-down player states
    public boolean turnTimerEnabled; // Whether the turn timer is enabled
    public int turnTimeElapsed; // Seconds since player's turn began
    public List<Integer> turnOrder; // The order in which players take their turn
    public int currentPlayerPointer; // The player currently taking their turn

    /**
     * Class to store the map in a way that can be written to JSON later
     */
    public class MapState {
        public HashMap<Integer, Sector> sectors; // HashMap of Sectors making up the map
        public SectorState[] sectorStates; // Array of sector states
    }

    /**
     * Class to store data for each player in the game
     */
    public class PlayerState {
        public int hashMapPosition; // Position of this player in the players HashMap
        public int id; // Player's ID number
        public CollegeName collegeName; // The college the player repreents
        public String playerName; // The name of the player
        public int troopsToAllocate; // Number of troops to allocate
        public PlayerType playerType; // The player's type
        public int collusionCards;
        public int poopyPathCards;
        public int asbestosCards;
    }

    /**
     * Class to store data for each sector of the Map
     */
    public class SectorState {
        public int hashMapPosition; // Position of this sector in the sectors hashmap
        public int id; // Sector's ID number
        public int ownerId; // Player ID of the owning player
        public String displayName; // Name of the sector
        public int undergradsInSector; // Number of units in the sector
        public int postgradsInSector; // Number of units in the sector
        public int reinforcementsProvided; // Number of reinforcements added to the sector
        public String college; // Name of the college this sector belongs to
        public String texturePath; // Path to the sector's texture
        public boolean neutral; // Is this sector a default neutral sector
        public int[] adjacentSectorIds; // Ids of sectors adjacent to this one
        public int sectorCentreX; // The centre x coordinate of this sector, relative to the sectorTexture
        public int sectorCentreY; // The centre y coordinate of this sector, relative to the sectorTexture
        public boolean decor; // Is this sector for visual purposes only, i.e. lakes are decor
        public boolean allocated; // Becomes true once the sector has been allocated
        public int asbestosCount; // Num of turns this sector has the asbestos status effect for
        public int poopCount; // Num of turns this sector has the poopy path status effect for
    }
}
