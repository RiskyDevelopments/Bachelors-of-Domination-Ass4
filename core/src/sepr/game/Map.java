package sepr.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import sepr.game.utils.PunishmentCardType;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/*
Modified in assessment 4
 - removed loading number of troops from the mapData.csv file as this is always overwritten - change in sectorDataToSector(String[] sectorData) method
 - removed PVC sector spawning from this class due to changes to how the minigame is triggered
 - rewritten the conflict resolution algorithm to take into account postgrad units
 - added support for applying a punishment card effect to a sector - the addSectorPunishmentEffect(int sectorId, PunishmentCardType punishmentCardType) method
 - added support for applying punishment card effects that last multiple turns - updateSectorStatusEffects(int currentPlayerId) method
 - refactored unit movement such that data is no longer passed around in mutable arrays, which was very difficult to follow. Now just uses parameters of methods to pass data
 - moved sector drawing to be part of the sector class
 */

/**
 * stores the game map and the sectors within it
 */
public class Map {
    private HashMap<Integer, Sector> sectors; // mapping of sector ID to the sector object
    private List<UnitChangeParticle> particles; // list of active particle effects displaying the changes to the amount of units on a sector

    private Random random;
    private Player neutralPlayer;

    private boolean successfulAttackOccurred = false;

    /**
     * sets up the map for the players that are part of this game
     *
     * @param players the players that are playing the game on this map
     */
    private Map(HashMap<Integer, Player> players) {
        random = new Random();

        this.neutralPlayer = players.get(GameScreen.NEUTRAL_PLAYER_ID);
        particles = new ArrayList<UnitChangeParticle>();
    }

    /**
     * Performs the maps initial setup
     * loads the sector data from the sectorProperties.csv file
     * allocates each sector to the players in the passed players hashmap
     *
     * @param players               hashmap of players who are in the game
     * @param allocateNeutralPlayer if true then the neutral player should be allocated the default neutral sectors else they should be allocated no sectors
     */
    public Map(HashMap<Integer, Player> players, boolean allocateNeutralPlayer) {
        this(players);

        this.loadSectors();
        this.allocateSectors(players, allocateNeutralPlayer);
    }

    /**
     *
     * @param players mapping of player ids to the respective players, playing the game on this map
     * @param sectors
     */
    public Map(HashMap<Integer, Player> players, HashMap<Integer, Sector> sectors) {
        this(players);
        this.sectors = sectors;
    }

    /**
     * converts a space seperated string of integers to an integer array
     *
     * @param stringData space separated integers e.g. '1 2 3 4 5'
     * @return the integers in the data in an array
     */
    private static int[] strToIntArray(String stringData) {
        String[] strArray = stringData.split(" ");
        int[] intArray = new int[strArray.length];
        for (int i = 0; i < intArray.length; i++) {
            if (strArray[i].equals("")) {
                continue; // skip if string is empty
            }
            intArray[i] = Integer.parseInt(strArray[i]);
        }
        return intArray;
    }

    /**
     * load the sector properties from the sectorProperties.csv file into the sectors hashmap
     */
    private void loadSectors() {
        this.sectors = new HashMap<Integer, Sector>();

        String csvFile = "mapData/sectorProperties.csv";
        String line;
        try {
            BufferedReader br = Gdx.files.internal(csvFile).reader(1000);
            while ((line = br.readLine()) != null) {
                Sector temp = sectorDataToSector(line.split(","));
                this.sectors.put(temp.getId(), temp);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace(); // csv file no present
        } catch (IOException e) {
            e.printStackTrace(); // error occurred whilst reading the file
        }
    }

    /**
     * converts a String array of sector data to a sector object
     *
     * @param sectorData sector data taken from the sectorProperties csv file
     * @return a sector with the properties fo the supplied data
     */
    private Sector sectorDataToSector(String[] sectorData) {
        int sectorId = Integer.parseInt(sectorData[0]);
        int ownerId = -1;
        String texturePath = "mapData/" + sectorData[1];
        Pixmap sectorPixmap = new Pixmap(Gdx.files.internal("mapData/" + sectorData[1]));
        String displayName = sectorData[2];
        int unitsInSector = 8 + random.nextInt(8);
        int postgradsInSector = 0;
        int reinforcementsProvided = Integer.parseInt(sectorData[3]);
        String college = sectorData[4];
        boolean neutral = Boolean.parseBoolean(sectorData[5]);
        int[] adjacentSectors = strToIntArray(sectorData[6]);
        int sectorX = Integer.parseInt(sectorData[7]);
        int sectorY = Integer.parseInt(sectorData[8]);
        boolean decor = Boolean.parseBoolean(sectorData[9]);

        return new Sector(sectorId, ownerId, texturePath, sectorPixmap, displayName, unitsInSector, postgradsInSector, reinforcementsProvided, college, neutral, adjacentSectors, sectorX, sectorY, decor, 0, 0);
    }

    /**
     * allocates sectors in the map to the players in a semi-random fashion
     * if there is a neutral player then the default neutral sectors are allocated to them
     *
     * @param players               the players the sectors are to be allocated to
     * @param allocateNeutralPlayer should the neutral player be allocated sectors
     * @throws RuntimeException if the players hashmap is empty
     */
    private void allocateSectors(HashMap<Integer, Player> players, boolean allocateNeutralPlayer) {
        if (players.size() == 0) {
            throw new RuntimeException("Cannot allocate sectors to 0 players");
        }

        // set any default neutral sectors to the neutral player
        if (allocateNeutralPlayer) {
            allocateNeutralSectors(players);
        }

        HashMap<Integer, Integer> playerReinforcements = new HashMap<Integer, Integer>(); // mapping of player id to amount of reinforcements they will receive currently
        // set all players to currently be receiving 0 reinforcements, ignoring the neutral player
        for (Integer i : players.keySet()) {
            if (i != GameScreen.NEUTRAL_PLAYER_ID) playerReinforcements.put(i, 0);
        }

        int lowestReinforcementId = players.keySet().iterator().next(); // id of player currently receiving the least reinforcements, any player id is chosen to start as all have 0 reinforcements
        List<Integer> sectorIdsRandOrder = new ArrayList<Integer>(getSectorIds()); // list of sector ids
        Collections.shuffle(sectorIdsRandOrder); // randomise the order sectors ids are stored so allocation order is randomised

        for (Integer i : sectorIdsRandOrder) {
            if (!sectors.get(i).isAllocated()) { // check sector has not already been allocated, may have been allocated to the neutral player
                if (this.getSectorById(i).isDecor()) {
                    continue; // skip allocating sector if it is a decor sector
                }
                this.getSectorById(i).setOwner(players.get(lowestReinforcementId));
                playerReinforcements.put(lowestReinforcementId, playerReinforcements.get(lowestReinforcementId) + this.getSectorById(i).getReinforcementsProvided()); // updates player reinforcements hashmap

                // find the new player with lowest reinforcements
                int minReinforcements = Collections.min(playerReinforcements.values()); // get lowest reinforcement amount
                for (Integer j : playerReinforcements.keySet()) { // find id of player which has the lowest reinforcement amount
                    if (playerReinforcements.get(j) == minReinforcements) { // if this player has the reinforcements matching the min amount set them to the new lowest player
                        lowestReinforcementId = j;
                        break;
                    }
                }
            }
        }
    }

    /**
     * allocates the default neutral sectors to the neutral player
     *
     * @param players hashmap of players containing the Neutral Player at key value GameScreen.NEUTRAL_PLAYER_ID
     */
    private void allocateNeutralSectors(HashMap<Integer, Player> players) {
        for (Sector sector : sectors.values()) {
            if (sector.isNeutral() && !sector.isDecor()) {
                sector.setOwner(players.get(GameScreen.NEUTRAL_PLAYER_ID));
            }
        }
    }

    /**
     * gets the sector that has the corresponding sector id in the sectors hashmap
     *
     * @param sectorId id of the desired sector
     * @return Sector object with the corresponding id in hashmap sectors
     * @throws NullPointerException if the key sectorId does not exist in the sectors hashmap
     */
    public Sector getSectorById(int sectorId) {
        if (sectors.containsKey(sectorId)) {
            return sectors.get(sectorId);
        } else {
            throw new NullPointerException("Cannot get sector as sector id " + sectorId + " does not exist in the sectors hashmap");
        }
    }

    /**
     * @return Set of all SectorIds
     */
    public Set<Integer> getSectorIds() {
        return sectors.keySet();
    }

    /**
     *
     * @return hashmap of sector ids to their respective sector objects that are part of this map
     */
    public HashMap<Integer, Sector> getSectors() {
        return sectors;
    }

    /**
     * returns the id of the sector that contains the specified point
     * ignores decor sectors
     *
     * @param worldX world x coord
     * @param worldY world y coord
     * @return id of sector that contains point or -1 if no sector contains the point or sector is decor only
     */
    public int detectSectorContainsPoint(int worldX, int worldY) {
        int worldYInverted = 1080 - 1 - worldY; // invert y coordinate for pixmap coordinate system
        for (Sector sector : sectors.values()) {
            if (worldX < 0 || worldYInverted < 0 || worldX > sector.getSectorTexture().getWidth() || worldYInverted > sector.getSectorTexture().getHeight()) {
                return -1; // return no sector contains the point if it outside of the map bounds
            }
            int pixelValue = sector.getSectorPixmap().getPixel(worldX, worldYInverted); // get pixel value of the point in sector image the mouse is over
            if (pixelValue != -256) { // if pixel is not transparent then it is over the sector
                if (!sector.isDecor()) {
                    return sector.getId(); // return id of sector which is hovered over
                }
            }
        }
        return -1;
    }

    /**
     * Executes an attack
     *
     * @param gameScreen
     * @param source sector attack is coming from
     * @param target sector that is being attacked
     * @param attackers Number of troops attacking
     */
    public void completeAttack(GameScreen gameScreen, Sector source, Sector target, int attackers) {
        int originalSourceOwnerId = source.getOwnerId();
        int originalTargetOwnerId = target.getOwnerId();
        Player attacker = gameScreen.getPlayerById(originalSourceOwnerId);

        int attackersRemaining = attackers;
        int underGrads = target.getUnderGradsInSector();
        int postGrads = target.getPostGradsInSector();

        // ATTACK BALANCING SETTINGS
        float winChance = postGrads > 5 ? 0.4f : 0.55f - (postGrads * 0.03f); // Chance of a 1v1 being a win for the attacker - stored as float between 0.0 and 1.0

        while (attackersRemaining > 0 && (underGrads > 0 || postGrads > 0)) { // While there are troops to attack and defend
            if (random.nextFloat() < winChance) {
                if (underGrads > 0) {
                    underGrads--;
                } else if (postGrads > 0) {
                    postGrads--;
                }
            } else {
                attackersRemaining--;
            }
        }

        if(attackersRemaining == 0){
            // Poor Move
            AudioPlayer.playBadMoveAudio();
        } else {
            // Good move
            AudioPlayer.playGoodMoveAudio();
            attacker.addTroopsToAllocate(target.getReinforcementsProvided()); // give the player the appropriate amount of troops to allocate next turn for conquering the target sector
        }

        // apply the attack to the map
        addUnitsToSectorAnimated(source.getId(), -(attackers - attackersRemaining), 0);
        addUnitsToSectorAnimated(target.getId(), -(target.getUnderGradsInSector() - underGrads), -(target.getPostGradsInSector() - postGrads));

        if (source.getUnderGradsInSector() == 0) {
            if (source.getPostGradsInSector() == 0) source.setOwner(neutralPlayer);
            if (target.getUnderGradsInSector() == 0 && target.getPostGradsInSector() == 0)  target.setOwner(neutralPlayer);
        } if (source.getUnderGradsInSector()== 1 && target.getUnderGradsInSector() == 0 && target.getPostGradsInSector() == 0) {
            target.setOwner(neutralPlayer);
        } else if (target.getUnderGradsInSector() == 0 && target.getPostGradsInSector() == 0) {
            target.setOwner(attacker);
            successfulAttackOccurred = true;
        }

        if (originalSourceOwnerId == target.getOwnerId()) { // attacker took over the target sector
            DialogFactory.attackSuccessDialogBox(gameScreen,
                    target.getReinforcementsProvided(),
                    source.getUnderGradsInSector(),
                    source.getId(),
                    target.getId(),
                    gameScreen.getPlayerById(originalTargetOwnerId).getPlayerName(),
                    gameScreen.getPlayerById(originalSourceOwnerId).getPlayerName(),
                    target.getDisplayName(),
                    gameScreen.getCurrentPhase());
        } else if (source.getOwnerId() == originalSourceOwnerId) {
            // all attackers wiped out, but units remain on source sector
            DialogFactory.basicDialogBox(gameScreen,"Unsuccessful!", "You failed to conquer the target", gameScreen.getCurrentPhase());
        } else { // defender wiped out attacking units and attacker sector is now neutral
            DialogFactory.sectorOwnerChangeDialog(gameScreen, gameScreen.getPlayerById(source.getOwnerId()).getPlayerName(), gameScreen.getPlayerById(GameScreen.NEUTRAL_PLAYER_ID).getPlayerName(), source.getDisplayName(), gameScreen.getCurrentPhase());
        }
        gameScreen.getCurrentPhase().updateTroopReinforcementLabel();
    }

    /**
     * returns if a successful attack has occurred
     * if it has then return that it has and set successfulAttackOccurred to false
     *
     * @return true if a successful attack has occurred else return false
     */
    public boolean checkIfSuccessfulAttackOccurred() {
        if (successfulAttackOccurred) {
            successfulAttackOccurred = false;
            return true;
        }
        return false;
    }

    /**
     * applies the effect of a guven punishment card to the specified sector
     *
     * @param sectorId id of sector to apply effect to
     * @param punishmentCardType type of punishment card to apply
     */
    public void addSectorPunishmentEffect(int sectorId, PunishmentCardType punishmentCardType) {
        Sector sector = this.getSectorById(sectorId);
        switch (punishmentCardType) {
            case COLLUSION_CARD:
                this.addUnitsToSectorAnimated(sectorId, sector.getPostGradsInSector(), -sector.getPostGradsInSector());
                break;
            case POOPY_PATH_CARD:
            case ASBESTOS_CARD:
                sector.incrementStatusEffect(punishmentCardType);
                break;
        }
    }

    /**
     * apply sector status effects that occur periodically to the sectors owned by the given player
     *
     * @param currentPlayerId id of player to apply status effects to
     */
    public void updateSectorStatusEffects(int currentPlayerId) {
        for (Sector sector : sectors.values()) {
            if (sector.getOwnerId() != currentPlayerId) return;

            if (sector.getPoopCount() > 0) {
                addUnitsToSectorAnimated(sector.getId(), -(int)Math.ceil(sector.getUnderGradsInSector() * 0.1), 0);
            }
            sector.decrementStatusEffects();
        }
    }

    /**
     * carries out the unit movement specified by unitsToMove array
     * - unitsToMove[0] : number of units to move
     * - unitsToMove[1] : source sector id
     * - unitsToMove[2] : target sector id
     * changes in units on sectors are shown on scren using the UnitChangeParticle
     *
     * @param sourceSectorId id of sector to move units from
     * @param targetSecotId id of sector to move units to
     * @param amount number of units to move from the source to the target sector
     *
     * @throws IllegalArgumentException if the sector are not both owned by the same player
     * @throws IllegalArgumentException if the amount exceeds the (number of units - 1) on the source sector
     * @throws IllegalArgumentException if the sectors are not connected
     */
    public void moveUnits(int sourceSectorId, int targetSecotId, int amount) throws IllegalArgumentException {
        if (sectors.get(sourceSectorId).getOwnerId() != sectors.get(targetSecotId).getOwnerId()) {
            throw new IllegalArgumentException("Source and target sectors must have the same owners");
        }
        if (sectors.get(sourceSectorId).getUnderGradsInSector() <= amount) {
            throw new IllegalArgumentException("Must leave at least one unit on source sector and can't move more units than are on source sector");
        }
        if (!sectors.get(sourceSectorId).isAdjacentTo(sectors.get(targetSecotId))) {
            throw new IllegalArgumentException("Sectors must be adjacent in order to move units");
        }
        addUnitsToSectorAnimated(sourceSectorId, -amount, 0); // remove units from source
        addUnitsToSectorAnimated(targetSecotId, amount, 0); // add units to target
    }

    /**
     * adds the specified number of units to this sector and sets up drawing a particle effect showing the addition
     *
     * @param sectorId id of sector to add the units to
     * @param undergrad  number of undergrads to add
     * @param postgrad number of postgrads to add
     */
    public void addUnitsToSectorAnimated(int sectorId, int undergrad, int postgrad) {
        this.sectors.get(sectorId).addUnits(undergrad, postgrad, neutralPlayer);
        if (undergrad != 0){
            this.particles.add(new UnitChangeParticle(undergrad, new Vector2(sectors.get(sectorId).getSectorCentreX() - 45, sectors.get(sectorId).getSectorCentreY())));
        }
        if (postgrad != 0){
            this.particles.add(new UnitChangeParticle(postgrad, new Vector2(sectors.get(sectorId).getSectorCentreX() + 5, sectors.get(sectorId).getSectorCentreY())));
        }
    }

    /**
     * draws the map and the number of units in each sector and the units change particle effect
     *
     * @param batch
     */
    public void draw(SpriteBatch batch) {
        for (Sector sector : sectors.values()) {
            sector.drawSectorImage(batch);
        }

        for (Sector sector : sectors.values()) {
            sector.drawSectorUi(batch);
        }

        // render particles
        List<UnitChangeParticle> toDelete = new ArrayList<UnitChangeParticle>();
        for (UnitChangeParticle particle : particles) {
            particle.draw(batch);
            if (particle.toDelete()) {
                toDelete.add(particle);
            }
        }
        particles.removeAll(toDelete);
    }
}
