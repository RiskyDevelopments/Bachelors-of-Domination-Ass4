package sepr.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * stores the game map and the sectors within it
 */
public class Map {
    private AudioManager Audio = AudioManager.getInstance(); // Access to the AudioManager
    private HashMap<Integer, Sector> sectors; // mapping of sector ID to the sector object
    private List<UnitChangeParticle> particles; // list of active particle effects displaying the changes to the amount of units on a sector

    private Random random;
    private Player neutralPlayer;
    private GameScreen gameScreen;

    /**
     * Performs the maps initial setup
     * loads the sector data from the sectorProperties.csv file
     * allocates each sector to the players in the passed players hashmap
     *
     * @param players               hashmap of players who are in the game
     * @param allocateNeutralPlayer if true then the neutral player should be allocated the default neutral sectors else they should be allocated no sectors
     */
    public Map(HashMap<Integer, Player> players, boolean allocateNeutralPlayer, GameScreen gameScreen) {
        random = new Random();

        this.loadSectors();

        particles = new ArrayList<UnitChangeParticle>();
        this.allocateSectors(players, allocateNeutralPlayer);
        this.neutralPlayer = players.get(GameScreen.NEUTRAL_PLAYER_ID);
        this.gameScreen = gameScreen;
    }

    public Map(HashMap<Integer, Player> players, boolean allocateNeutralPlayer, HashMap<Integer, Sector> sectors, GameScreen gameScreen) {
        this(players, allocateNeutralPlayer, gameScreen);
        this.sectors = sectors;
    }

    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    /**
     * converts a space seperated string of integers to an integer array
     *
     * @param stringData space separated integers e.g. '1 2 3 4 5'
     * @return the integers in the data in an array
     */
    private int[] strToIntArray(String stringData) {
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
        String line = "";
        Integer ID = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFile));
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
        String filename = "mapData/" + sectorData[1];
        String texturePath = "mapData/" + sectorData[1];
        Texture sectorTexture = new Texture(texturePath);
        Pixmap sectorPixmap = new Pixmap(Gdx.files.internal("mapData/" + sectorData[1]));
        String displayName = sectorData[2];
        int unitsInSector = 10 + random.nextInt(15);
        int postgradsInSector = 0;
        int reinforcementsProvided = Integer.parseInt(sectorData[4]);
        String college = sectorData[5];
        boolean neutral = Boolean.parseBoolean(sectorData[6]);
        int[] adjacentSectors = strToIntArray(sectorData[7]);
        int sectorX = Integer.parseInt(sectorData[8]);
        int sectorY = Integer.parseInt(sectorData[9]);
        boolean decor = Boolean.parseBoolean(sectorData[10]);

        return new Sector(sectorId, ownerId, filename, sectorTexture, texturePath, sectorPixmap, displayName, unitsInSector, postgradsInSector, reinforcementsProvided, college, neutral, adjacentSectors, sectorX, sectorY, decor, 0, 0, this);
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
     * processes a movement from one sector to another
     * sets up drawing particle effects showing changes in amount of units in a sector
     * sets up movement of units after conquering a sector
     *
     * @param attackingSectorId id of the sector the troops are moving from
     * @param defendingSectorId id of the sector receiving troops
     * @param attackersLost     amount of units lost on the sector sending sector
     * @param defendersLost     amount of units gained on the sector receiving troops
     * @return true if movement successful else false
     **/
    public Boolean moveTroops(int attackingSectorId, int defendingSectorId, int attackersLost, int defendersLost) {

        addUnitsToSectorAnimated(attackingSectorId, -attackersLost, 0); // apply amount of attacking units lost
        addUnitsToSectorAnimated(defendingSectorId, defendersLost, 0); // apply amount of defending units lost
        return true;
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
            this.particles.add(new UnitChangeParticle(undergrad, new Vector2(sectors.get(sectorId).getSectorCentreX(), sectors.get(sectorId).getSectorCentreY())));
        }
        if (postgrad != 0){
            this.particles.add(new UnitChangeParticle(postgrad, new Vector2(sectors.get(sectorId).getSectorCentreX() + 40, sectors.get(sectorId).getSectorCentreY())));
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
     * carries out the unit movement specified by unitsToMove array
     * - unitsToMove[0] : number of units to move
     * - unitsToMove[1] : source sector id
     * - unitsToMove[2] : target sector id
     * changes in units on sectors are shown on scren using the UnitChangeParticle
     *
     * @throws IllegalArgumentException if the sector are not both owned by the same player
     * @throws IllegalArgumentException if the amount exceeds the (number of units - 1) on the source sector
     * @throws IllegalArgumentException if the sectors are not connected
     */
    public void moveUnits(int sourceSectorId, int targetSecotId, int amount) throws IllegalArgumentException {
        System.out.println(sourceSectorId + "  " + targetSecotId + "  " + amount);
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
     * Executes an attack
     *
     * @param attacker Player attacking
     * @param neutral Neutral player
     * @param source Source sector
     * @param target Target sector
     * @param attackers Number of troops attacking
     */
    public void completeAttack(Player attacker, Player neutral, Sector source, Sector target, int attackers) {
        int startAttackers = attackers;
        int underGrads = target.getUnderGradsInSector();
        int postGrads = target.getPostGradsInSector();

        // ATTACK BALANCING SETTINGS
        float winChance = postGrads > 5 ? 0.4f : 0.55f - postGrads * 0.3f; // Chance of a 1v1 being a win for the attacker - stored as float between 0.0 and 1.0
        int postGradStrength = 3; // Number of undergrads killed by a defending postgrad

        while (attackers > 0 && (underGrads > 0 || postGrads > 0)) { // While there are troops to attack and defend
            if (underGrads > 0) { // Attack undergraduates first
                if (random.nextFloat() > winChance) { // win
                    underGrads --;
                } else { // loss
                    attackers --;
                }
            } else if (postGrads > 0) { // All undergrads are dead but postgrads remain
                if (random.nextFloat() > winChance) { // win
                    postGrads --;
                } else { // loss
                    attackers =- attackers <= postGradStrength ? 0 : postGradStrength;
                }
            }
        }

        if(attackers <= 0){
            // Poor Move
            int voice = random.nextInt(3);

            switch (voice){
                case 0:
                    Audio.get("sound/Invalid Move/Colin_Your_actions_are_questionable.wav", Sound.class).play(AudioManager.GlobalFXvolume);
                    break;
                case 1:
                    Audio.get("sound/Battle Phrases/Colin_Seems_Risky_To_Me.wav", Sound.class).play(AudioManager.GlobalFXvolume);
                    break;
                case 2:
                    break;
            }
        } else {
            // Good move
            attacker.addTroopsToAllocate(target.getReinforcementsProvided()); // give the player the appropriate amount of troops to allocate next turn for conquering the target sector
            int voice = random.nextInt(5);

            switch (voice){
                case 0:
                    Audio.get("sound/Battle Phrases/Colin_An_Unlikely_Victory.wav", Sound.class).play(AudioManager.GlobalFXvolume);
                    break;
                case 1:
                    Audio.get("sound/Battle Phrases/Colin_Far_better_than_I_expected.wav", Sound.class).play(AudioManager.GlobalFXvolume);
                    break;
                case 2:
                    Audio.get("sound/Battle Phrases/Colin_I_couldnt_have_done_it_better_myself.wav", Sound.class).play(AudioManager.GlobalFXvolume);
                    break;
                case 3:
                    Audio.get("sound/Battle Phrases/Colin_Multiplying_by_the_identity_matrix_is_more_fasinating_than_your_last_move.wav", Sound.class).play(AudioManager.GlobalFXvolume);
                    break;
                case 4:
                    Audio.get("sound/Battle Phrases/Colin_Well_Done.wav", Sound.class).play(AudioManager.GlobalFXvolume);
                    break;
                case 5:
                    break;
            }
        }

        // apply the attack to the map
        addUnitsToSectorAnimated(source.getId(), -(startAttackers - attackers), 0);
        addUnitsToSectorAnimated(target.getId(), -(target.getUnderGradsInSector() - underGrads), 0);
        addUnitsToSectorAnimated(target.getId(), 0, -(target.getPostGradsInSector() - postGrads));

        if (source.getUnderGradsInSector() == 0 && source.getPostGradsInSector() == 0) { // defender won, and attacker has no postgrads so sector becomes neutral
            source.setOwner(neutral);
        } else if (source.getUnderGradsInSector() == 0 && source.getPostGradsInSector() != 0) {
            // all attackers lost but postgrads still hold attacking sector
        } else { // attacker won
            target.setOwner(attacker);
            if (gameScreen.PVCSpawn()) gameScreen.openMiniGame();
        }
    }

    /**
     * draws the map and the number of units in each sector and the units change particle effect
     *
     * @param batch
     */
    public void draw(SpriteBatch batch) {
        for (Sector sector : sectors.values()) {
            sector.draw(batch);
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

    public HashMap<Integer, Sector> getSectors() {
        return sectors;
    }

    public void updateSectorStatusEffects(int currentPlayerId) {
        for (Sector sector : sectors.values()) {
            sector.updateStatusEffects(currentPlayerId);
        }
    }
}
