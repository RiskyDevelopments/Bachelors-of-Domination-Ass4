package sepr.game;

import com.badlogic.gdx.graphics.Color;
import sepr.game.utils.CollegeName;
import sepr.game.utils.PlayerType;

/**
 * base class for storing Neutral and Human player data
 */
public class Player {
    private int id; // player's unique id
    private CollegeName collegeName; // college this player chose
    private String playerName;
    private int troopsToAllocate; // how many troops the player has to allocate at the start of their next reinforcement phase
    private PlayerType playerType; // Human or Neutral player

    private int collusionCards;  // num of collusion cards this player has
    private int poopyPathCards;  // num of poopy path cards this player has
    private int asbestosCards;   // num of asbestos cards this player has

    /**
     * creates a player object with the specified properties
     *
     * @param id player's unique identifier
     * @param collegeName display name for this player
     * @param playerType is this player a Human, AI or Neutral AI
     * @param playerName player's name to be displayed
     */
    public Player(int id, CollegeName collegeName, int troopsToAllocate, PlayerType playerType, String playerName, int collusionCards, int poopyPathCards, int asbestosCards) {
        this.id = id;
        this.collegeName = collegeName;
        this.troopsToAllocate = troopsToAllocate;
        this.playerType = playerType;
        this.playerName = playerName;
        this.collusionCards = collusionCards;
        this.poopyPathCards = poopyPathCards;
        this.asbestosCards = asbestosCards;
    }

    /**
     * creates
     *
     * @param id player's unique identifier
     * @param collegeName display name for this player
     * @param playerName player's name to be displayed
     */
    public static Player createHumanPlayer(int id, CollegeName collegeName, int troopsToAllocate, String playerName) {
        return new Player(id, collegeName, troopsToAllocate, PlayerType.HUMAN, playerName, 0, 0, 0);
    }

    /**
     * @param id player's unique identifier
     */
    public static Player createNeutralPlayer(int id) {
        return new Player(id, CollegeName.UNI_OF_YORK, 0, PlayerType.NEUTRAL_AI, "NEUTRAL",0, 0, 0);
    }

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
    public CollegeName getCollegeName() {
        return collegeName;
    }

    /**
     *
     * @return the colour associated with this player
     */
    public Color getSectorColour() {
        return collegeName.getCollegeColor();
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

    public int getCollusionCards() {
        return collusionCards;
    }

    public int getPoopyPathCards() {
        return poopyPathCards;
    }

    public int getAsbestosCards() {
        return asbestosCards;
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
