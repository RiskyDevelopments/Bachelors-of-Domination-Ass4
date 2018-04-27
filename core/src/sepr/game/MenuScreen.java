package sepr.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * controls the UI for the main menu screen
 */
public class MenuScreen extends UiScreen {

    /**
     * sets up the menu screen
     *
     * @param main for changing which screen is currently being displayed
     */
    public MenuScreen(final Main main) {
        super(main);
        this.stage.addListener(new InputListener(){
            @Override
            public boolean keyUp(InputEvent event, int keyCode) {
                if (keyCode == Input.Keys.ESCAPE) { // ask player if they would like to exit the game if they press escape
                    DialogFactory.exitProgramDialogBox(stage);
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * generates a table containing the start game, load game and options buttons
     * pressing;
     *      Start Game  --> Takes player to setup game screen
     *      Load Game   --> Not yet implemented
     *      Options     --> Takes player to options screen
     * @return a table of buttons
     */
    private Table setupMenuTable() {
        final TextButton startGameBtn = WidgetFactory.genBasicButton("START NEW GAME");
        final TextButton loadGameBtn = WidgetFactory.genBasicButton("LOAD GAME");
        final TextButton optionsBtn = WidgetFactory.genBasicButton("OPTIONS");

        /* Create sub-table for all the menu buttons */
        Table btnTable = new Table();
        btnTable.setDebug(false);
        btnTable.left();
        btnTable.add(startGameBtn).height(72).width(439).pad(30);

        btnTable.row();
        btnTable.left();
        btnTable.add(loadGameBtn).height(72).width(439).pad(30);

        btnTable.row();
        btnTable.left();
        btnTable.add(optionsBtn).height(72).width(439).pad(30);

        startGameBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                main.setGameSetupScreen();
            }
        });

        loadGameBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(main.hasLoadedSaves()) {
                    main.loadGame();
                } else {
                    DialogFactory.basicDialogBox(null, "No saved game found", "No save game was found. Please start a new game", stage);
                }
            }
        });

        optionsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                main.setOptionsScreen();
            }
        });

        /* Sub-table complete */
        return btnTable;
    }

    /**
     * sets up the UI tables for the menu screen
     */
    protected Table setupUi() {
        Table uiComponentsTable =  new Table();
        uiComponentsTable.setDebug(false);

        uiComponentsTable.background(new TextureRegionDrawable(new TextureRegion(new Texture("uiComponents/Scanline.png"))));
        uiComponentsTable.pad(80).padLeft(85).padRight(95);

        uiComponentsTable.center();
        uiComponentsTable.add(WidgetFactory.genMenusTopBar("MAIN MENU")).colspan(2);

        uiComponentsTable.row();
        uiComponentsTable.left();
        uiComponentsTable.add(setupMenuTable()).expand();

        uiComponentsTable.right();

        uiComponentsTable.add(WidgetFactory.genMapGraphic()).height(657).width(811).pad(30);

        uiComponentsTable.row();
        uiComponentsTable.add(WidgetFactory.genBottomBar("QUIT", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                DialogFactory.exitProgramDialogBox(stage);}

        })).colspan(2).fillX();
        return uiComponentsTable;
    }
}
