package sepr.game.utils;

/**
 * Created by Dom's Surface Mark 2 on 17/03/2018.
 */
public enum PunishmentCardType {
    COLLUSION_CARD("Collusion Card"), FAUX_COLLUSION_CARD("Faux Collusion Card"),
    POOPY_PATH_CARD("Poopy Path Card"), FAUX_POOPY_PATH_CARD("Faux Poopy Path Card"),
    ASBESTOS_CARD("Asbestos Card"), FAUX_ASBESTOS_CARD("Faux Asbestos Card"),
    HIDDEN_CARD("Hidden Card");

    private final String name;

    PunishmentCardType(String s){
        name = s;
    }

    @Override
    public String toString() {
        return this.name;
    }
}