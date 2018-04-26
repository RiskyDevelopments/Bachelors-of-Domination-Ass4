package sepr.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public abstract class UiScreen implements Screen {
    protected Main main;
    protected Stage stage;
    private Table backgroundTable;// table for inserting ui widgets into

    public UiScreen(Main main) {
        this.main = main;
        this.stage = new Stage();

        this.backgroundTable = setupBackground();
        this.stage.addActor(backgroundTable);
        this.backgroundTable.setFillParent(true);
        this.backgroundTable.setDebug(false);
        this.stage.setViewport(new ScreenViewport());
    }

    protected abstract Table setupUi();

    protected Table setupBackground() {
        Table backgroundTable = new Table();
        backgroundTable.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture("uiComponents/menuBackground.png"))));
        backgroundTable.pad(0);
        backgroundTable.add(setupUi());
        return backgroundTable;
    }

    public Stage getStage() {
        return stage;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        this.stage.act(Gdx.graphics.getDeltaTime());
        this.stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        this.stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
