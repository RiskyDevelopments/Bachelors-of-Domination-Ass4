package sepr.game.utils;

/*
Added in assessment 4
 - added so that the different types of punishment cards could be referenced
 */

/**
 * Created by Dom's Surface Mark 2 on 17/03/2018.
 */
public enum PunishmentCardType {
    COLLUSION_CARD("Collusion Card"), FAUX_COLLUSION_CARD("Faux Collusion Card"),
    POOPY_PATH_CARD("Poopy Path Card"), FAUX_POOPY_PATH_CARD("Faux Poopy Path Card"),
    ASBESTOS_CARD("Asbestos Card"), FAUX_ASBESTOS_CARD("Faux Asbestos Card"),
    HIDDEN_CARD("Hidden Card"),
    NO_CARD("No Card Selected");

    private final String name;

    PunishmentCardType(String s){
        name = s;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
