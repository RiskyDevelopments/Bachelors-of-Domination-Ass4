package sepr.game.utils;

import com.badlogic.gdx.graphics.Color;

/*
Added in assessment 4
 - moved from the GameSetupScreen class as enum was not just used in that class
 */

/**
 * the colleges available to play as
 */
public enum CollegeName {

    ALCUIN("ALCUIN", new Color(1, 0.294f, 0.294f, 0.4f)),
    DERWENT("DERWENT", new Color(0.212f, 0.380f, 1, 0.4f)),
    HALIFAX("HALIFAX", new Color(0, 0.824f, 1, 0.4f)),
    HES_EAST("HESLINGTON EAST", new Color(0.392f, 0.855f, 0.271f, 0.4f)),
    JAMES("JAMES", new Color(0.101f, 0.101f, 0.101f, 0.4f)),
    UNI_OF_YORK("UNIVERSITY OF YORK", new Color(1,1,1,0.4f)),
    VANBRUGH("VANBRUGH", new Color(0.886f, 0.373f, 0.992f, 0.4f)),
    WENTWORTH("WENTWORTH", new Color(0.996f, 0.674f, 0.251f, 0.4f));

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