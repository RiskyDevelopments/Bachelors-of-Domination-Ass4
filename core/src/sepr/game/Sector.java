package sepr.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import sepr.game.utils.PunishmentCardType;

/*
Modified in assessment 4
 - removed fileName variable as duplicate of texturePath
 - removed isPVCTile variable as no longer applicable due to changes to the PVC minigame
 - added textures and text drawing for rendering the status effect icons
 - added variables for storing how long this sector is under status effects for - asbestosCount and poopCountVariable
 - added methods for checking if this sector canAttack(), canBeAttacked() and canChangeUnits() for easier checking if this sector can attack; be attacked; be reinforced and move units to/from it
 - added support for two unit types, undergrads and postgrads
 - separated sector texture drawing and drawing sector HUD element, (e.g. unit count), so that UI elements cannot be rendered below sector textures
 - added method for applying a status effect punishment card to this sector - incrementStatusEffect(PunishmentCardType puinishmentCardType)
 */

/**
 * class for specifying properties of a sector that is part of a map
 */
public class Sector {
    private int id; // unique id of this sector
    private int ownerId; // id of the player that owns this sector
    private String displayName; // name of this sector to be displayed in the UI
    private int underGradsInSector; // num of undergrad units in this sector
    private int postGradsInSector; // num of postgrad units in this sector
    private int reinforcementsProvided; // num of allocation points given to a player that conquers this texture
    private String college; // name of the college this sector belongs to
    private boolean neutral; // is this sector a default neutral sector
    private int[] adjacentSectorIds; // ids of sectors adjacent to this one
    private Texture sectorTexture; // texture for rendering this sector
    private String texturePath; // file path to this sector's texture
    private Pixmap sectorPixmap; // the pixel data of this sectors texture
    private int sectorCentreX; // the centre x coordinate of this sector, relative to the sectorTexture
    private int sectorCentreY; //the centre y coordinate of this sector, relative to the sectorTexture
    private boolean decor; // is this sector for visual purposes only, i.e. lakes are decor
    private boolean allocated; // becomes true once the sector has been allocated

    private static Texture troopCountOverlay = new Texture("icons/troopCountOverlay.png");
    private static Texture pooStatus = new Texture("icons/poopStatus.png");
    private static Texture asbestosStatus = new Texture("icons/asbestosStatus.png");
    private static Texture postgradIcon = new Texture("icons/postgradCountOverlay.png");
    private static BitmapFont font = WidgetFactory.getFontSmall(); // font for rendering sector unit data
    private static GlyphLayout layout = new GlyphLayout();

    private int asbestosCount; // turn the asbestos effect is active on this tile, 0 = not active
    private int poopCount; // turn the poop effect is active on this tile, 0 = not active

    /**
     * @param id sector id
     * @param ownerId id of player who owns sector
     * @param texturePath file path of this sector's texture
     * @param displayName sector display name
     * @param underGradsInSector number of units in sector
     * @param postGradsInSector number of postgrads in sector
     * @param reinforcementsProvided number of reinforcements the sector provides
     * @param college unique id of the college this sector belongs to
     * @param neutral is this sector a default neutral sector
     * @param adjacentSectorIds ids of adjacent sectors
     * @param sectorPixmap pixmap of sector texture
     * @param sectorCentreX xcoord of sector centre
     * @param sectorCentreY ycoord of sector centre
     * @param decor false if a sector is accessible to a player and true if sector is decorative
     * @param asbestosCount number of turns this sector has the asbestos effect for
     * @param poopCount number of turns this sector has the poopy path effect for
     */
    public Sector(int id, int ownerId, String texturePath, Pixmap sectorPixmap, String displayName, int underGradsInSector, int postGradsInSector, int reinforcementsProvided, String college, boolean neutral, int[] adjacentSectorIds, int sectorCentreX, int sectorCentreY, boolean decor, int asbestosCount, int poopCount) {
        this.id = id;
        this.ownerId = ownerId;
        this.displayName = displayName;
        this.underGradsInSector = underGradsInSector;
        this.postGradsInSector = postGradsInSector;
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
        this.allocated = false;
        this.asbestosCount = asbestosCount;
        this.poopCount = poopCount;
    }

    /**
     *
     * @param id sector id
     * @param ownerId id of player who owns sector
     * @param texturePath file path of this sector's texture
     * @param displayName sector display name
     * @param underGradsInSector number of units in sector
     * @param postGradsInSector number of postgrads in sector
     * @param reinforcementsProvided number of reinforcements the sector provides
     * @param college unique id of the college this sector belongs to
     * @param neutral is this sector a default neutral sector
     * @param adjacentSectorIds ids of adjacent sectors
     * @param sectorPixmap pixmap of sector texture
     * @param sectorCentreX xcoord of sector centre
     * @param sectorCentreY ycoord of sector centre
     * @param decor false if a sector is accessible to a player and true if sector is decorative
     * @param allocated has this sector been allocated
     * @param color color this sector should be
     * @param asbestosCount number of turns this sector has the asbestos effect for
     * @param poopCount number of turns this sector has the poopy path effect for
     */
    public Sector(int id, int ownerId, String texturePath, Pixmap sectorPixmap, String displayName, int underGradsInSector, int postGradsInSector, int reinforcementsProvided, String college, boolean neutral, int[] adjacentSectorIds, int sectorCentreX, int sectorCentreY, boolean decor, boolean allocated, Color color, int asbestosCount, int poopCount) {
        this(id, ownerId, texturePath, sectorPixmap, displayName, underGradsInSector, postGradsInSector, reinforcementsProvided, college, neutral, adjacentSectorIds, sectorCentreX, sectorCentreY, decor, asbestosCount, poopCount);
        
        this.allocated = allocated;
        this.sectorCentreY = sectorCentreY;

        if(!isDecor()){this.changeSectorColor(color);
            this.changeSectorColor(color);
        }
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
        this.changeSectorColor(player.getSectorColour());
        this.allocated = true;
    }

    /**
     *
     * @return true if not under poopy path status effect and there's more than one undergrad on the sector, else false
     */
    public boolean canAttack() {
        return poopCount == 0 && underGradsInSector > 1;
    }

    /**
     *
     * @return true if not under poopy path status effect
     */
    public boolean canBeAttacked() {
        return poopCount == 0;
    }

    /**
     *
     * @return true if not under poopy path status effect
     */
    public boolean canChangeUnits() {
        return poopCount == 0;
    }

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
    public int getUnderGradsInSector() {
        return underGradsInSector;
    }

    /**
     *
     * @return number of postgrads units present in this sector
     */
    public int getPostGradsInSector() {
        return postGradsInSector;
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
     *
     * @param undergrad number of undergrad units to add to this sector
     * @param postgrad number of postgrad units to add to this sector
     * @param neutralPlayer neutral player object to set the owner to if number of units on the sector is 0
     *
     * @throws IllegalArgumentException if units in sector is below 0
     */
    public void addUnits(int undergrad, int postgrad, Player neutralPlayer) throws IllegalArgumentException {
        this.underGradsInSector += undergrad;
        this.postGradsInSector += postgrad;

        if (this.underGradsInSector < 0) {
            this.underGradsInSector = 0;
            throw new IllegalArgumentException("Cannot have less than 0 units on a sector");
        }

        if (this.postGradsInSector < 0) {
            this.postGradsInSector = 0;
            throw new IllegalArgumentException("Cannot have less than 0 postgrad units on a sector");
        }

        if (this.underGradsInSector == 0 && this.postGradsInSector == 0) this.setOwner(neutralPlayer);
    }

    /**
     * The method takes a sectorId and recolors it to the specified color
     *
     * @param newColor what color the sector be changed to
     * @throws RuntimeException if attempt to recolor a decor sector
     */
    public void changeSectorColor(Color newColor){
        if (this.isDecor()) {
            throw new RuntimeException("Should not recolour decor sector");
        }

        Pixmap newPix = new Pixmap(Gdx.files.internal(this.getTexturePath())); // pixmap for drawing updated sector texture to
        Pixmap.setBlending(Pixmap.Blending.None);
        for (int x = 0; x < this.getSectorPixmap().getWidth(); x++){
            for (int y = 0; y < this.getSectorPixmap().getHeight(); y++){
                if(newPix.getPixel(x, y) != -256){
                    Color tempColor = new Color(0,0,0,0);
                    Color.rgba8888ToColor(tempColor, newPix.getPixel(x, y)); // get the pixels current color
                    tempColor.sub(new Color(Color.WHITE).sub(newColor)); // calculate the new color of the pixel
                    newPix.drawPixel(x, y, Color.rgba8888(tempColor));  // drawSectorImage the modified pixel value to the new pixmap
                }
            }
        }
        this.setNewSectorTexture(newPix); // drawSectorImage the generated pixmap to the new texture
        newPix.dispose();
    }

    /**
     *
     * @return array of sector ids adjacent to this sector
     */
    public int[] getAdjacentSectorIds() {
        return this.adjacentSectorIds;
    }

    /**
     *
     * @return file path to this sectors texture
     */
    public String getTexturePath() {
        return texturePath;
    }

    /**
     *
     * @return number of turns this sector is under the asbestos status effect
     */
    public int getAsbestosCount() {
        return asbestosCount;
    }

    /**
     *
     * @return number of turns this sector is under the poopy path status effect
     */
    public int getPoopCount() {
        return poopCount;
    }

    /**
     * render method for drawing this sectors image
     *
     * @param batch to draw the graphics to
     */
    public void drawSectorImage(SpriteBatch batch) {
        batch.draw(this.getSectorTexture(), 0, 0);
    }

    /**
     * render method for drawing status effect icons to
     *
     * @param batch to draw the graphics to
     */
    public void drawSectorUi(SpriteBatch batch) {
        if (!this.isDecor()) { // don't need to drawSectorImage the amount of units on a decor sector
            float overlaySize = 40.0f;
            batch.draw(troopCountOverlay, this.getSectorCentreX() - overlaySize - 5, this.getSectorCentreY() - overlaySize / 2, overlaySize, overlaySize);
            layout.setText(font, this.getUnderGradsInSector() + "");
            font.draw(batch, layout, this.getSectorCentreX() - overlaySize / 2 - layout.width / 2 - 5, this.getSectorCentreY() + layout.height / 2);

            batch.draw(postgradIcon, this.getSectorCentreX() + 5, this.getSectorCentreY() - overlaySize / 2, overlaySize, overlaySize);
            layout.setText(font, this.getPostGradsInSector() + "");
            font.draw(batch, layout, this.getSectorCentreX() + overlaySize / 2 - layout.width / 2 + 5, this.getSectorCentreY() + layout.height / 2);

            if (this.getAsbestosCount() != 0) {
                batch.draw(asbestosStatus, this.getSectorCentreX() - overlaySize / 2, this.getSectorCentreY() + 5, overlaySize, overlaySize);
                layout.setText(font, this.getAsbestosCount() + "");
                font.draw(batch, layout, this.getSectorCentreX() - layout.width / 2, this.getSectorCentreY() + layout.height + 18);
            }

            if (this.getPoopCount() != 0) {
                batch.draw(pooStatus, this.getSectorCentreX() - overlaySize / 2, this.getSectorCentreY() - 42, overlaySize, overlaySize);
                layout.setText(font, this.getPoopCount() + "");
                font.draw(batch, layout, this.getSectorCentreX() - layout.width / 2, this.getSectorCentreY() + layout.height - 34);
            }
        }
    }

    /**
     * applies the given punishment card to this sector
     *
     * @param punishmentCardType the type of effect to apply
     * @throws IllegalArgumentException can only increment status effects for Poopy path card and Asbestos card
     */
    public void incrementStatusEffect(PunishmentCardType punishmentCardType) {
        switch (punishmentCardType) {
            case POOPY_PATH_CARD:
                this.poopCount += 3;
                break;
            case ASBESTOS_CARD:
                this.asbestosCount += 3;
                break;
            default:
                throw new IllegalArgumentException("Can only increment status effects for Poopy path card and Asbestos card");

        }
    }

    /**
     * decrement the number of turns each status effect is on this sector for, if it is greater than 0
     */
    public void decrementStatusEffects() {
        if (this.poopCount > 0) this.poopCount--;
        if (this.asbestosCount > 0) this.asbestosCount--;
    }
}
