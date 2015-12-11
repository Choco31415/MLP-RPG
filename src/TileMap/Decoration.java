package TileMap;

public class Decoration {
	
	private int x;
	private int y;
	private int tileID;
	private int layer;
	
	public Decoration(int x_, int y_, int tileID_, int layer_) {
		x = x_;
		y = y_;
		tileID = tileID_;
		layer = layer_;
	}
	
	public void setx(int x_) { x = x_; }
	public void sety(int y_) { y = y_; }
	public void setTileID(int tileID_) { tileID = tileID_; }
	public void setLayer(int layer_) { layer = layer_; }
	
	public int getx() { return x; }
	public int gety() { return y; }
	public int getTileID() { return tileID; }
	public int getLayer() { return layer; }
	
	@Override
	public String toString() {
		String decor = "";
		
		decor += "x: " + x;
		decor += "y: " + y;
		decor += "tileID: " + tileID;
		decor += "layer: " + layer;
		
		return decor;
	}
}
