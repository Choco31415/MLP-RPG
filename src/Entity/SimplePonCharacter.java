package Entity;

import java.awt.image.BufferedImage;

import GameState.GameState;
import TileMap.TileMapWithBB;

public class SimplePonCharacter extends MapObjectAnimatable {
	
	private String sceneName;
	private BufferedImage icon;
	private String name;
	private String catchPhrase;
	
	public SimplePonCharacter(TileMapWithBB tm_, GameState level_, String iconFile_, String name_, String catchPhrase_, String sceneName_, int x_, int y_, boolean facingRight_, int row_, int col_) {
		super(tm_, "Pony Characters", 20, 8, 0, -10, level_, 32);
		x = x_;
		y = y_;
		facingRight = facingRight_;
		setCostume(row_, col_);
		
		sceneName = sceneName_;
		//icon = iconFile_;
		name = name_;
		catchPhrase = catchPhrase_;
	}
	
}
