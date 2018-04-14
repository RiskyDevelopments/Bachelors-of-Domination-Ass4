package sepr.game.saveandload;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import org.junit.*;
import org.junit.runners.model.InitializationError;
import sepr.game.*;
import sepr.game.utils.PlayerType;
import sepr.game.utils.TurnPhaseType;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;

public class SaveLoadManagerTest implements ApplicationListener {
    public SaveLoadManager saveLoadManager;
    public GameState gameState;

    private static HeadlessApplicationConfiguration application;

    public SaveLoadManagerTest() throws InitializationError {
        HeadlessApplicationConfiguration conf = new HeadlessApplicationConfiguration();

        new HeadlessApplication(this, conf);
    }

    @BeforeClass
    public static void init() {

    }

    @Before
    public void setUp() throws Exception {
        this.saveLoadManager = new SaveLoadManager();

        this.gameState = new GameState();
        gameState.mapState = gameState.new MapState();


        gameState.mapState.sectorStates = new GameState.SectorState[4];

        for (int i = 0; i < 4; i++){
            GameState.SectorState sectorState = gameState.new SectorState();

            sectorState.hashMapPosition = i;
            sectorState.id = i;
            sectorState.ownerId = i;
            sectorState.displayName = Integer.toString(i);
            sectorState.unitsInSector = i;
            sectorState.reinforcementsProvided = i;
            sectorState.college = "DERWENT";
            sectorState.texturePath = "assets/uiComponents/menuBackground.png";
            sectorState.neutral = true;
            sectorState.adjacentSectorIds = new int[3];
            sectorState.adjacentSectorIds[0] = 1;
            sectorState.adjacentSectorIds[1] = 2;
            sectorState.adjacentSectorIds[2] = 3;
            sectorState.sectorCentreX = i;
            sectorState.sectorCentreY = i;
            sectorState.decor = false;
            sectorState.fileName = "assets/uiComponents/menuBackground.png";
            sectorState.allocated = false;

            gameState.mapState.sectorStates[i] = sectorState;
        }

        gameState.playerStates = new GameState.PlayerState[4];

        for (int i = 0; i < 4; i++){
            GameState.PlayerState playerState = gameState.new PlayerState();

            playerState.hashMapPosition = i;
            playerState.id = i;
            playerState.collegeName = GameSetupScreen.CollegeName.DERWENT;
            playerState.playerName = Integer.toString(i);
            playerState.troopsToAllocate = i;
            playerState.sectorColour = new Color(0, 0, 0, 0);
            playerState.playerType = PlayerType.HUMAN;
            playerState.ownsPVC = false;

            gameState.playerStates[i] = playerState;
        }

        gameState.currentPhase = TurnPhaseType.MOVEMENT;
        gameState.turnTimerEnabled = true;
        gameState.maxTurnTime = 10;
        gameState.turnTimeStart = 1;

        gameState.turnOrder = new ArrayList<Integer>();
        gameState.turnOrder.add(0);
        gameState.turnOrder.add(1);
        gameState.turnOrder.add(2);
        gameState.turnOrder.add(3);

        gameState.currentPlayerPointer = 2;
    }

    @After
    public void tearDown() throws Exception {
        saveLoadManager = null;
        gameState = null;
    }

    @AfterClass
    public static void cleanUp() {
        application = null;
    }

    @Test
    public void mapFromMapState() {
    }

    @Test
    public void playersFromPlayerState() {
        HashMap<Integer, Player> playerHashMap = saveLoadManager.PlayersFromPlayerState(gameState.playerStates);

        assertTrue("Player HashMap Size", playerHashMap.size() == 4);

        int index = 0;

        for (java.util.Map.Entry<Integer, Player> playerEntry : playerHashMap.entrySet()){
            Integer key = playerEntry.getKey();
            Player value = playerEntry.getValue();

            assertTrue("Player ID", value.getId() == index);
            assertTrue("Player College", value.getCollegeName() == GameSetupScreen.CollegeName.DERWENT);
            assertTrue("Player Name", value.getPlayerName().equalsIgnoreCase(Integer.toString(index)));
            assertTrue("Player Troops To Allocate", value.getTroopsToAllocate() == index);
            assertTrue("Player Sector Colour", value.getSectorColour().equals(new Color(0, 0, 0, 0)));
            assertTrue("Player Type",value.getPlayerType() == PlayerType.HUMAN);
            assertTrue("Player Owns PVC",value.getOwnsPVC() == false);

            index++;
        }
    }

    @Test
    public void sectorsFromSectorState() {
        HashMap<Integer, Player> playerHashMap = new HashMap<Integer, Player>();
        HashMap<Integer, Sector> sectorHashMap = this.saveLoadManager.SectorsFromSectorState(this.gameState.mapState.sectorStates, playerHashMap, true);
        
        int index = 0;

        for (java.util.Map.Entry<Integer, Sector> sectorEntry : sectorHashMap.entrySet()){
            Integer key = sectorEntry.getKey();
            Sector value = sectorEntry.getValue();
            
            assertTrue("Sector ID", value.getId() == index);
            assertTrue("Sector Owner ID", value.getOwnerId() == index);
            assertTrue("Sector Display Name", value.getDisplayName().equalsIgnoreCase(Integer.toString(index)));
            assertTrue("Units In Sector", value.getUnderGradsInSector() == index);
            assertTrue("Reinforcements Provided", value.getReinforcementsProvided() == index);
            assertTrue("Sector College", value.getCollege() == "DERWENT");
            assertTrue("Sector Texture Path", value.getTexturePath().equalsIgnoreCase("assets/uiComponents/menuBackground.png"));
            assertTrue("Sector Is Neutral", value.isNeutral() == true);

            for (int i = 0; i < value.getAdjacentSectorIds().length; i++){
                assertTrue(value.getAdjacentSectorIds()[i] == i + 1);
            }

            assertTrue("Sector Centre X", value.getSectorCentreX() == index);
            assertTrue("Sector Centre Y", value.getSectorCentreY() == index);

            assertTrue("Sector Is Decor", value.isDecor() == false);
            assertTrue("Sector FileName", value.getFileName() == "assets/uiComponents/menuBackground.png");
            assertTrue("Sector Is Allocated", value.isAllocated() == false);

            index++;
        }

    }

    @Test
    public void getCurrentSaveID() {
        assertTrue("Current Save ID", saveLoadManager.GetCurrentSaveID() == 0);
    }

    @Test
    public void getNextSaveID() {
        assertTrue("Next Save ID", saveLoadManager.GetNextSaveID() == 1);
    }

    @Override
    public void create() {

    }

    @Override
    public void resize(int i, int i1) {

    }

    @Override
    public void render() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }
}