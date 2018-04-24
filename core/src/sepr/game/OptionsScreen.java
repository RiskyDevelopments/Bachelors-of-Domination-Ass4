package sepr.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Options managed by this class:
 *      Music Volume
 *      FX Volume
 *      Resolution Selector
 *      Fullscreen On/Off
 */
public class OptionsScreen extends View {
    // names for accessing different preferences in the preferences file
    public static final String PREFERENCES_NAME = "Options";
    public static final String MUSIC_VOL_PREF = "musicVol";
    public static final String FX_VOL_PREF = "fxVol";
    public static final String RESOLUTION_WIDTH_PREF = "screenWidth";
    public static final String RESOLUTION_HEIGHT_PREF = "screenHeight";
    public static final String FULLSCREEN_PREF = "fullscreen";

    // screen UI widgets
    private Slider musicSlider;
    private Slider fxSlider;
    private SelectBox<String> resolutionSelector;
    private CheckBox fullscreenSwitch;

    /**
     * sets up the screen
     *
     * @param main for changing back to the menu screen
     */
    public OptionsScreen(final Main main) {
        super(main);

        this.stage = new Stage(){
            @Override
            public boolean keyUp(int keyCode) {
                if (keyCode == Input.Keys.ESCAPE) { // change back to the menu screen if the player presses esc
                    main.setMenuScreen();
                }
                return super.keyUp(keyCode);
            }
        };

        this.stage.setViewport(new ScreenViewport());
        this.backgroundTable = setupBackground();

        this.stage.addActor(backgroundTable);
        this.backgroundTable.setFillParent(true);
        this.backgroundTable.setDebug(false);
    }

    /**
     * Method generates a string array of possible resolutions the game could be displayed at on this monitor
     * Used to get selectable elements in the Resolution selector widget
     * Resolutions must be a minimum of 1000 x 1000 pixels
     *
     * @return possible display resolutions in format ScreenWidth x ScreenHeight
     */
    private String[] getPossibleResolutions() {
        DisplayMode[] displayModes = Gdx.graphics.getDisplayModes();
        Set<String> resolutions = new HashSet<String>();

        for (DisplayMode displayMode : displayModes) {
            if (displayMode.width > 1300 && displayMode.height > 700) { // window must be more than 1000 x 1000 resolution
                resolutions.add(displayMode.width + " x " + displayMode.height);
            }
        }
        String[] resStrings = new String[resolutions.size()];
        resolutions.toArray(resStrings);
        Arrays.sort(resStrings);
        return resStrings;
    }

    private Table setupOptionsTable() {
        // setup widgets for selecting the options
        musicSlider = WidgetFactory.genStyledSlider();
        fxSlider = WidgetFactory.genStyledSlider();
        resolutionSelector = WidgetFactory.genStyledSelectBox(getPossibleResolutions());
        fullscreenSwitch = WidgetFactory.genOnOffSwitch();

        // setup labels
        Label musicVolumeLabel = WidgetFactory.genMenuLabel("MUSIC VOLUME");
        musicVolumeLabel.setAlignment(Align.center);
        Label fxVolumeLabel = WidgetFactory.genMenuLabel("FX VOLUME");
        fxVolumeLabel.setAlignment(Align.center);
        Label resolutionSelectorLabel = WidgetFactory.genMenuLabel("RESOLUTION");
        resolutionSelectorLabel.setAlignment(Align.center);
        Label fullscreenSwitchLabel = WidgetFactory.genMenuLabel("FULLSCREEN");
        fullscreenSwitchLabel.setAlignment(Align.center);

        // add the setup widgets to a table
        Table table = new Table();
        table.setDebug(false);
        table.left();
        table.add(musicVolumeLabel).height(72).width(439).pad(20);
        table.right();
        table.add(musicSlider).width(260).height(18).padLeft(80);

        table.row();
        table.left();
        table.add(fxVolumeLabel).height(72).width(439).pad(20);
        table.right();
        table.add(fxSlider).width(260).height(18).padLeft(80);

        table.row();
        table.left();
        table.add(resolutionSelectorLabel).height(72).width(439).pad(20);
        table.right();
        table.add(resolutionSelector).padLeft(80);

        table.row();
        table.left();
        table.add(fullscreenSwitchLabel).height(72).width(439).pad(20);
        table.right();
        table.add(fullscreenSwitch).padLeft(80);

        TextButton acceptButton = WidgetFactory.genBasicButton("CONFIRM CHANGES");
        acceptButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                acceptChanges(); // save and apply changes when CONFIRM CHANGES button is pressed
                main.setMenuScreen(); // revert to the menu screen
            }
        });

        table.row();
        table.left();

        table.add(acceptButton).height(72).width(439).pad(20);

        return table;
    }

    @Override
    protected Table setupUi() {
        Table uiComponentsTable =  new Table();
        uiComponentsTable.setDebug(false);

        uiComponentsTable.background(new TextureRegionDrawable(new TextureRegion(new Texture("uiComponents/Scanline.png"))));
        uiComponentsTable.pad(80).padLeft(85).padRight(95);

        uiComponentsTable.center();
        uiComponentsTable.add(WidgetFactory.genMenusTopBar("OPTIONS")).colspan(2);

        uiComponentsTable.row();
        uiComponentsTable.add(setupOptionsTable()).expand();

        uiComponentsTable.add(WidgetFactory.genOptionsGraphic()).height(700).width(540).pad(30);

        uiComponentsTable.row();
        uiComponentsTable.add(WidgetFactory.genBottomBar("MAIN MENU", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                main.setMenuScreen();}

        })).colspan(2).fillX();

        return uiComponentsTable;
    }

    /**
     * Called when accept button clicked in options menu
     * applies the changes to the game settings made by the player
     * saves the updated preferences to file
     */
    private void acceptChanges() {
        Preferences prefs = Gdx.app.getPreferences(PREFERENCES_NAME);
        prefs.putFloat(MUSIC_VOL_PREF, musicSlider.getPercent());
        prefs.putFloat(FX_VOL_PREF, fxSlider.getPercent());

        // split the selected resolution into width and height values
        int screenWidth = Integer.parseInt(resolutionSelector.getSelected().split(" x ")[0]);
        int screenHeight = Integer.parseInt(resolutionSelector.getSelected().split(" x ")[1]);

        prefs.putInteger(RESOLUTION_WIDTH_PREF, screenWidth);
        prefs.putInteger(RESOLUTION_HEIGHT_PREF, screenHeight);

        prefs.putBoolean(FULLSCREEN_PREF, fullscreenSwitch.isChecked());

        prefs.flush(); // save the updated preferences to file
        main.applyPreferences(); // apply the changes to the game
    }

    /**
     * reads the preferences file so that the options screen may be set to the current settings
     */
    private void readPreferences() {
        Preferences prefs = Gdx.app.getPreferences(OptionsScreen.PREFERENCES_NAME);

        musicSlider.setValue(prefs.getFloat(MUSIC_VOL_PREF, 0.5f));
        fxSlider.setValue(prefs.getFloat(FX_VOL_PREF, 0.5f));

        if (prefs.getInteger(OptionsScreen.RESOLUTION_WIDTH_PREF, -1) == -1 || prefs.getInteger(OptionsScreen.RESOLUTION_HEIGHT_PREF, -1) == -1) {
            resolutionSelector.setSelected(Gdx.graphics.getWidth() + " x " + Gdx.graphics.getHeight());
        } else {
            resolutionSelector.setSelected(prefs.getInteger(RESOLUTION_WIDTH_PREF) + " x " + prefs.getInteger(RESOLUTION_HEIGHT_PREF));
        }

        fullscreenSwitch.setChecked(prefs.getBoolean(OptionsScreen.FULLSCREEN_PREF, Gdx.graphics.isFullscreen()));
    }

    @Override
    public void show() {
        super.show();
        readPreferences();
    }
}
