package TileMap;

import java.awt.Rectangle;

public class BoundingBox {

	Rectangle box;
	String tag;
	
	public BoundingBox(Rectangle box_, String tag_) {
		box = box_;
		tag = tag_;
	}
	
	public Rectangle getRectangle() {
		return box;
	}
	
	public String getTag() {
		return tag;
	}
	
	public void setRectangle(Rectangle box_) {
		box = box_;
	}
	
	public void setTag(String tag_) {
		tag = tag_;
	}
}
