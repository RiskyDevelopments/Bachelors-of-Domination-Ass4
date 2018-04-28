package sepr.game.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import sepr.game.Main;


/**
 * executable http://www.riskydevelopments.co.uk/BoDv2/BoDv2.zip
 */
public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.title = "Bachelors of Domination";
		config.addIcon("gameIcon/icon800.png", Files.FileType.Internal);
		config.addIcon("gameIcon/icon256.png", Files.FileType.Internal);
		config.addIcon("gameIcon/icon128.png", Files.FileType.Internal);
		config.addIcon("gameIcon/icon32.png", Files.FileType.Internal);
		config.addIcon("gameIcon/icon16.png", Files.FileType.Internal);
        config.width = 1920;
        config.height = 1080;
        config.resizable = false;
        config.samples = 8; // Anti aliasing sampling

		new LwjglApplication(new Main(), config);
	}


}

