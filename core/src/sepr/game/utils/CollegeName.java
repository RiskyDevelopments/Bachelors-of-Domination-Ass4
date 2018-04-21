package sepr.game.utils;

import com.badlogic.gdx.graphics.Color;

/**
 * the colleges available to play as
 */
public enum CollegeName {

    ALCUIN("ALCUIN", new Color(0.8f, 0.2f, 0.2f, 0.7f)),
    DERWENT("DERWENT", Color.BLUE),
    HALIFAX("HALIFAX", Color.CYAN),
    HES_EAST("HESLINGTON EAST", Color.GREEN),
    JAMES("JAMES", Color.GRAY),
    UNI_OF_YORK("UNIVERSITY OF YORK", Color.WHITE),
    VANBRUGH("VANBRUGH", Color.PURPLE),
    WENTWORTH("WENTWORTH", Color.ORANGE);

    private final String shortCode;
    private final Color collegeColor;

    CollegeName(String code, Color collegeColor){
        this.shortCode = code;
        this.collegeColor = collegeColor;
    }

    public String getCollegeName(){
        return this.shortCode;
    }

    public Color getCollegeColor() {
        return collegeColor;
    }

    /**
     * converts the string representation of the enum to the enum value
     *
     * @param text string representation of the enum
     * @return the enum value of the provided text
     * @throws IllegalArgumentException if the text does not match any of the enum's string values
     */
    public static CollegeName fromString(String text) throws IllegalArgumentException {
        for (CollegeName collegeName : CollegeName.values()) {
            if (collegeName.getCollegeName().equals(text)) return collegeName;
        }

        throw new IllegalArgumentException("Text parameter must match one of the enums");
    }
}