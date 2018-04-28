package sepr.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import sepr.game.Main;


/**
 * executable http://www.riskydevelopments.co.uk/BoDv2/BoDv2.zip
 */
public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        config.width = 1920;
        config.height = 1080;
        config.resizable = false;
        config.samples = 8; // Anti aliasing sampling

		new LwjglApplication(new Main(), config);
	}


}

