package sepr.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import javafx.util.Pair;
import sepr.game.utils.SectorStatusEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * class for specifying properties of a sector that is part of a map
 */
public class Sector implements ApplicationListener {
    private int id;
    private int ownerId;
    private String displayName;
    private int unitsInSector;
    private int reinforcementsProvided;
    private String college; // name of the college this sector belongs to
    private boolean neutral; // is this sector a default neutral sector
    private int[] adjacentSectorIds; // ids of sectors adjacent to this one
    private Texture sectorTexture;
    private String texturePath;
    private Pixmap sectorPixmap; // the pixel data of this sectors texture
    private int sectorCentreX; // the centre x coordinate of this sector, relative to the sectorTexture
    private int sectorCentreY; //the centre y coordinate of this sector, relative to the sectorTexture
    private boolean decor; // is this sector for visual purposes only, i.e. lakes are decor
    private String fileName;
    private boolean allocated; // becomes true once the sector has been allocated
    private boolean isPVCTile;
    private Map map;

    private static Texture troopCountOverlay = new Texture("uiComponents/troopCountOverlay.png");
    private static Texture pooStatus = new Texture("pooStatus.png");
    private static Texture asbestosStatus = new Texture("asbestosStatus.png");
    private static BitmapFont font = WidgetFactory.getFontSmall(); // font for rendering sector unit data
    private static GlyphLayout layout = new GlyphLayout();

    private int asbestosCount; // turn the asbestos effect is active on this tile, 0 = not active
    private int poopCount; // turn the poop effect is active on this tile, 0 = not active

    /**
     * @param id sector id
     * @param ownerId id of player who owns sector
     * @param displayName sector display name
     * @param unitsInSector number of units in sector
     * @param reinforcementsProvided number of reinforcements the sector provides
     * @param college unique id of the college this sector belongs to
     * @param adjacentSectorIds ids of adjacent sectors
     * @param sectorTexture sector texture from assets
     * @param sectorPixmap pixmap of sector texture
     * @param fileName sector filename
     * @param sectorCentreX xcoord of sector centre
     * @param sectorCentreY ycoord of sector centre
     * @param decor false if a sector is accessible to a player and true if sector is decorative
     */
    public Sector(int id, int ownerId, String fileName, Texture sectorTexture, String texturePath, Pixmap sectorPixmap, String displayName, int unitsInSector, int reinforcementsProvided, String college, boolean neutral, int[] adjacentSectorIds, int sectorCentreX, int sectorCentreY, boolean decor, int asbestosCount, int poopCount, Map map) {
        this.id = id;
        this.ownerId = ownerId;
        this.displayName = displayName;
        this.unitsInSector = unitsInSector;
        this.reinforcementsProvided = reinforcementsProvided;
        this.college = college;
        this.neutral = neutral;
        this.adjacentSectorIds = adjacentSectorIds;
        this.sectorTexture = new Texture(texturePath);
        this.texturePath = texturePath;
        this.sectorPixmap = sectorPixmap;
        this.sectorCentreX = sectorCentreX;
        this.sectorCentreY = 1080 - sectorCentreY;
        this.decor = decor;
        this.fileName = fileName;
        this.allocated = false;
        this.asbestosCount = asbestosCount;
        this.poopCount = poopCount;
        this.map = map;


        //DELETLE
        Random r = new Random();
        if (r.nextInt(3) == 0) this.asbestosCount = r.nextInt(4);
        if (r.nextInt(3) == 0) this.poopCount = r.nextInt(4);
    }

    public Sector(int id, int ownerId, String fileName, String texturePath, Pixmap sectorPixmap, String displayName, int unitsInSector, int reinforcementsProvided, String college, boolean neutral, int[] adjacentSectorIds, int sectorCentreX, int sectorCentreY, boolean decor, boolean allocated, Color color, int asbestosCount, int poopCount, Map map) {
        this(id, ownerId, fileName, new Texture(texturePath), texturePath, sectorPixmap, displayName, unitsInSector, reinforcementsProvided, college, neutral, adjacentSectorIds, sectorCentreX, sectorCentreY, decor, asbestosCount, poopCount, map);
        
        this.allocated = allocated;
        this.sectorCentreY = sectorCentreY;

        if(!isDecor()){this.changeSectorColor(color);
            this.changeSectorColor(color);
        }
    }

    public Sector(int id, int ownerId, String fileName, String texturePath, Pixmap sectorPixmap, String displayName, int unitsInSector, int reinforcementsProvided, String college, boolean neutral, int[] adjacentSectorIds, int sectorCentreX, int sectorCentreY, boolean decor, boolean allocated, Color color, boolean test, int asbestosCount, int poopCount, Map map) {
        HeadlessApplicationConfiguration conf = new HeadlessApplicationConfiguration();

        new HeadlessApplication(this, conf);

        this.id = id;
        this.ownerId = ownerId;
        this.displayName = displayName;
        this.unitsInSector = unitsInSector;
        this.reinforcementsProvided = reinforcementsProvided;
        this.college = college;
        this.neutral = neutral;
        this.adjacentSectorIds = adjacentSectorIds;
        this.sectorTexture = new Texture(texturePath);
        this.texturePath = texturePath;
        this.sectorPixmap = sectorPixmap;
        this.sectorCentreX = sectorCentreX;
        this.sectorCentreY = sectorCentreY;
        this.decor = decor;
        this.fileName = fileName;
        this.allocated = allocated;
        this.asbestosCount = asbestosCount;
        this.poopCount = poopCount;
        this.map = map;
    }

    /**
     *
     * @return this sectors unique id
     */
    public int getId() { return id; }

    /**
     *
     * @return the id of the player that owns this sector
     */
    public int getOwnerId() {
        return ownerId;
    }

    /**
     * sets the owner id and colour of this sector
     * @param player the player object that owns this sector
     */
    public void setOwner(Player player) {
        this.ownerId = player.getId();
        if(!this.isPVCTile){
            this.changeSectorColor(player.getSectorColour());
        }
        this.allocated = true;
    }

    public boolean canAttack() {
        return poopCount == 0 && unitsInSector > 1;
    }

    public boolean canBeAttacked() {
        return poopCount == 0;
    }

    public boolean canChangeUnits() {
        return poopCount == 0;
    }

    /**
     *
     * @return if sector is PVC sector
     */
    public boolean getIsPVCTile() { return isPVCTile; }

    /**
     *
     * @return if sector is PVC sector
     */
    public void setIsPVCTile(boolean value) { this.isPVCTile = value; }


    /**
     *
     * @return the name of the sector that is to be shown in the GUI
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     *
     * @return number of troops rewarded for conquering this territory
     */
    public int getReinforcementsProvided() {
        return reinforcementsProvided;
    }

    /**
     *
     * @return number of units present in this sector
     */
    public int getUnitsInSector() {
        return unitsInSector;
    }

    /**
     *
     * @return the texture used for drawing the sectorNeutral
     */
    public Texture getSectorTexture() {
        return sectorTexture;
    }

    /**
     * Sets the new texture for a sector
     * @param newPixmap the memory representation of the textures pixels
     */
    private void setNewSectorTexture(Pixmap newPixmap) {
        this.sectorTexture.dispose();
        Texture temp = new Texture(newPixmap);
        this.sectorTexture = temp;
    }

    /**
     *
     * @return the pixel data of this sectors texture
     */
    public Pixmap getSectorPixmap() {
        return sectorPixmap;
    }

    /**
     *
     * @return centre x coordinate of this sector
     */
    public int getSectorCentreX() {
        return sectorCentreX;
    }

    /**
     *
     * @return centre y coordinate of this sector
     */
    public int getSectorCentreY() {
        return sectorCentreY;
    }

    /**
     * @return boolean value to check whether sector is decorative
     */
    public boolean isDecor() {
        return decor;
    }

    /**
     *
     * @return true if this sector is a default neutral sector, else false
     */
    public boolean isNeutral() { return neutral; }

    /**
     *
     * @return true if this sector has been allocated to a player, else false
     */
    public boolean isAllocated() {
        return allocated;
    }

    /**
     *
     * @return the name of the college this sector belongs to
     */
    public String getCollege() { return college; }

    /**
     *
     * @return the filename of the sector image
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Function to check if a given sector is adjacent
     * @param toCheck The sector object to check
     * @return True/False
     */
    public boolean isAdjacentTo(Sector toCheck) {
        for (int adjacent : this.adjacentSectorIds) {
            if (adjacent == toCheck.getId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Changes the number of units in this sector
     * If there are 0 units in sector then ownerId should be -1 (neutral)
     * @param amount number of units to change by, (can be negative to subtract units
     * @throws IllegalArgumentException if units in sector is below 0
     */
    public void addUnits(int amount) throws IllegalArgumentException {
        this.unitsInSector += amount;
        if (this.unitsInSector < 0) {
            this.unitsInSector = 0;
            throw new IllegalArgumentException("Cannot have less than 0 units on a sector");
        }
    }

    /**
     * The method takes a sectorId and recolors it to the specified color
     * @param newColor what color the sector be changed to
     * @throws RuntimeException if attempt to recolor a decor sector
     */
    public void changeSectorColor(Color newColor){
        if (this.isDecor()) {
            throw new RuntimeException("Should not recolour decor sector");
        }

        Pixmap newPix = new Pixmap(Gdx.files.internal(this.getFileName())); // pixmap for drawing updated sector texture to
        for (int x = 0; x < this.getSectorPixmap().getWidth(); x++){
            for (int y = 0; y < this.getSectorPixmap().getHeight(); y++){
                if(newPix.getPixel(x, y) != -256){
                    Color tempColor = new Color(0,0,0,0);
                    Color.rgba8888ToColor(tempColor, newPix.getPixel(x, y)); // get the pixels current color
                    tempColor.sub(new Color(Color.WHITE).sub(newColor)); // calculate the new color of the pixel
                    newPix.drawPixel(x, y, Color.rgba8888(tempColor));  // draw the modified pixel value to the new pixmap
                }
            }
        }
        this.setNewSectorTexture(newPix); // draw the generated pixmap to the new texture
        newPix.dispose();
    }

    public int[] getAdjacentSectorIds() {
        return this.adjacentSectorIds;
    }

    public String getTexturePath() {
        return texturePath;
    }

    /**
     * called at the end of a turn and applies any active status effect and decrements the count that they apply for
     * @param currentPlayerId id of the player whos turn it currently is
     */
    public void updateStatusEffects(int currentPlayerId) {
        if (this.ownerId != currentPlayerId) return;

        if (poopCount > 0) poopCount--;
        if (asbestosCount > 0) {
            asbestosCount--;
            map.addUnitsToSectorAnimated(this.id, -(int)Math.ceil(unitsInSector * 0.1));
        }
    }

    private int getAsbestosCount() {
        return asbestosCount;
    }

    private int getPoopCount() {
        return poopCount;
    }

    public void draw (SpriteBatch batch) {
        String text = this.getUnitsInSector() + "";
        batch.draw(this.getSectorTexture(), 0, 0);
        if (!this.isDecor()) { // don't need to draw the amount of units on a decor sector
            layout.setText(font, text);

            float overlaySize = 40.0f;
            batch.draw(troopCountOverlay, this.getSectorCentreX() - overlaySize / 2, this.getSectorCentreY() - overlaySize / 2, overlaySize, overlaySize);
            font.draw(batch, layout, this.getSectorCentreX() - layout.width / 2, this.getSectorCentreY() + layout.height / 2);
        }

        if (this.getAsbestosCount() != 0) {
            batch.draw(asbestosStatus, this.getSectorCentreX(), this.getSectorCentreY() + 10);
            layout.setText(font, this.getAsbestosCount() + "");
            font.draw(batch, layout, this.getSectorCentreX() + layout.width + 2, this.getSectorCentreY() + layout.height + 18);
        }

        if (this.getPoopCount() != 0) {
            batch.draw(pooStatus, this.getSectorCentreX(), this.getSectorCentreY() - 50);
            layout.setText(font, this.getPoopCount() + "");
            font.draw(batch, layout, this.getSectorCentreX() + layout.width + 2, this.getSectorCentreY() + layout.height - 40);
        }
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
