package Entity;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.util.ArrayList;

import GameState.GameState;
import GameState.MapState;
import Main.GamePanel;
import TileMap.BoundingBox;
import TileMap.TileMap;
import TileMap.TileMapWithBB;

public class MapObjectAnimatable extends MapObject{
	
	protected BufferedImage costume;
	
	protected int costumeSize;
	
	protected int costumeRow;
	protected int costumeCol;

	public MapObjectAnimatable(TileMapWithBB tm, String image_, int cwidth_, int cheight_, int xOffset_, int yOffset_, GameState level_, int costumeSize_) {
		super(tm, image_, cwidth_, cheight_, xOffset_, yOffset_, level_);
		costumeSize = costumeSize_;
		width = costumeSize;
		height = costumeSize;
		setCostume(0,0);
	}
	
	public MapObjectAnimatable(TileMapWithBB tm, BufferedImage image_, int cwidth_, int cheight_, int xOffset_, int yOffset_, GameState level_, int costumeSize_) {
		super(tm, image_, cwidth_, cheight_, xOffset_, yOffset_, level_);
		costumeSize = costumeSize_;
		width = costumeSize;
		height = costumeSize;
		setCostume(0,0);
	}
	
	public int getCostumeRow() { return costumeRow; }
	public int getCostumeCol() { return costumeCol; }
	
	public void setCostume(int row, int col) {
		costumeRow = row;
		costumeCol = col;
		
		costume = getTileImage(row, col);
	}
	
	
	public BufferedImage getTileImage(int row, int col) {
		try {
			return image.getSubimage(col*costumeSize, row*costumeSize, costumeSize, costumeSize);
		} catch (RasterFormatException e) {
			throw new Error();
		}
	}
	
	@Override
	public void draw(java.awt.Graphics2D g) {
		if (facingRight) {
			g.drawImage(
					costume,
					(int)(x - xmap - width / 2 + xOffset),
					(int)(y - ymap - height / 2 + yOffset),
					null
				);
		} else {
			g.drawImage(
					costume,
					(int)(x - xmap + width / 2 + xOffset),
					(int)(y - ymap - height / 2 + yOffset),
					-width,
					height,
					null
				);
		}
	}
	
}
