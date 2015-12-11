package GameState;

import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import Entity.Player;
import TileMap.TileMap;
import TileMap.TileMapWithBB;

public class RPGState extends MapState {

	public RPGState(GameStateManager gsm_) {
		super(gsm_);
		
		//Initialize variables
		deathSceneLength = 1;
		endingTilesFromEnd = 3;

		//Other stuff
		tileMap = new TileMapWithBB(20);
		tileMap.loadTilesetLazy("galatileset");
		tileMap.loadSceneLazy("North Room", 1);
		tileMap.setPosition(100, 0);
		
		try {
			background = ImageIO.read(getClass().getResourceAsStream("/Backgrounds/sky.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		readFromKeyboard = true;
		
		createLevel();
	}
	
	public void createLevel() {

		player = new Player(tileMap, this);
		player.setPosition(50, 310);
	}
}
