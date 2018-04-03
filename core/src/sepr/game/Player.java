package sepr.game;

import com.badlogic.gdx.graphics.Color;
import sepr.game.utils.PlayerType;

/**
 * base class for storing Neutral and Human player data
 */
public class Player {
    private int id; // player's unique id
    private GameSetupScreen.CollegeName collegeName; // college this player chose
    private String playerName;
    private int troopsToAllocate; // how many troops the player has to allocate at the start of their next reinforcement phase
    private Color sectorColour; // what colour to shade sectors owned by the player
    private PlayerType playerType; // Human or Neutral player
    private Boolean OwnsPVC;

    private int collusionCards = 0;  // num of collusion cards this player has
    private int poopyPathCards = 0;  // num of poopy path cards this player has
    private int asbestosCards = 0;   // num of asbestos cards this player has

    /**
     * creates a player object with the specified properties
     *
     * @param id player's unique identifier
     * @param collegeName display name for this player
     * @param sectorColour colour that the sectors owned by this player are coloured
     * @param playerType is this player a Human, AI or Neutral AI
     * @param playerName player's name to be displayed
     */
    public Player(int id, GameSetupScreen.CollegeName collegeName, Color sectorColour, PlayerType playerType, String playerName, int collusionCards, int poopyPathCards, int asbestosCards) {
        this.id = id;
        this.collegeName = collegeName;
        this.troopsToAllocate = 0;
        this.sectorColour = sectorColour;
        this.playerType = playerType;
        this.playerName = playerName;
        this.OwnsPVC = false;
        this.collusionCards = collusionCards;
        this.poopyPathCards = poopyPathCards;
        this.asbestosCards = asbestosCards;
    }

    public Player(int id, GameSetupScreen.CollegeName collegeName, Color sectorColour, PlayerType playerType, String playerName, int collusionCards, int poopyPathCards, int asbestosCards, int troopsToAllocate, boolean ownsPVC){
        this(id, collegeName, sectorColour, playerType, playerName, collusionCards, poopyPathCards, asbestosCards);

        this.troopsToAllocate = troopsToAllocate;
        this.setOwnsPVC(ownsPVC);
    }

    /**
     * @param id player's unique identifier
     * @param collegeName display name for this player
     * @param sectorColour colour that the sectors owned by this player are coloured
     * @param playerName player's name to be displayed
     */
    public static Player createHumanPlayer(int id, GameSetupScreen.CollegeName collegeName, Color sectorColour, String playerName) {
        return new Player(id, collegeName, sectorColour, PlayerType.HUMAN, playerName, 0, 0, 0);
    }

    /**
     * @param id player's unique identifier
     */
    public static Player createNeutralPlayer(int id) {
        return new Player(id, GameSetupScreen.CollegeName.UNI_OF_YORK, Color.GRAY, PlayerType.NEUTRAL_AI, "THE NEUTRAL PLAYER", 0, 0, 0);
    }


    /**
     * @return  if the player owns the PVC tile
     */

    public Boolean getOwnsPVC() { return OwnsPVC; }

    /**
     * @param  ownsPVC boolean if the player owns the PVC
     */

    public void setOwnsPVC(Boolean ownsPVC) { OwnsPVC = ownsPVC; }

    /**
     *
     * @return the player's id
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @return the name of the player's college
     */
    public GameSetupScreen.CollegeName getCollegeName() {
        return collegeName;
    }

    /**
     *
     * @return the colour associated with this player
     */
    public Color getSectorColour() {
        return sectorColour;
    }

    /**
     *
     * @return the name of the player
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     *
     * @return the player's type
     */
    public PlayerType getPlayerType() {
        return playerType;
    }

    /**
     * fetches number of troops this player can allocate in their next turn
     * @return amount troops to allocate
     */
    public int getTroopsToAllocate() {
        return troopsToAllocate;
    }

    /**
     * sets the number of troops this player has to allocate to this value
     *
     * @param troopsToAllocate number of troops to allocate
     */
    public void setTroopsToAllocate(int troopsToAllocate) {
        this.troopsToAllocate = troopsToAllocate;
    }

    /**
     * increases the number of troops to allocate by the the given amount
     * @param troopsToAllocate amount to increase allocation by
     */
    public void addTroopsToAllocate(int troopsToAllocate) {
        this.troopsToAllocate += troopsToAllocate;
    }

    public void addCollusionCards(int num) {
        this.collusionCards += num;
    }

    public void addPoopyPathCards(int num) {
        this.poopyPathCards += num;
    }

    public void addAsbestosCards(int num) {
        this.asbestosCards += num;
    }
}
