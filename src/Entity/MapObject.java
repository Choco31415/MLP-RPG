package Entity;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import GameState.GameState;
import GameState.MapState;
import Main.GamePanel;
import TileMap.BoundingBox;
import TileMap.TileMap;
import TileMap.TileMapWithBB;

public class MapObject {
	
	protected TileMapWithBB tileMap;
	protected MapState level;
	protected int tileSize;
	protected double xmap;
	protected double ymap;
	
	// position
	protected double x;//upper left corner
	protected double y;//upper left corner

	// velocities
	protected double dx = 0;
	protected double dy = 0;
	protected Double futuredy = null;
	
	// temp positions variables
	protected double xtemp;
	protected double ytemp;
	
	//image width and height
	protected int width;
	protected int height;
	
	// bounding box width and height
	protected int cwidth;
	protected int cheight;
	
	// are you going left, right, up, down, jumping, or falling?
	protected boolean left;
	protected boolean right;
	protected boolean up;
	protected boolean down;
	protected boolean facingRight = true;
	
	// for collisions
	protected int topLeft;
	protected int topRight;
	protected int bottomLeft;
	protected int bottomRight;
	private int currCol;
	private int currRow;
	protected boolean collided;
	
	private double surfaceDifference;
	
	// for drawing
	protected BufferedImage image;
	protected Rectangle rect;
	protected int xOffset;
	protected int yOffset;
	
	protected ArrayList<int[]> tilesToDestroy = new ArrayList<int[]>();
	
	// constructor
	public MapObject(TileMapWithBB tm, String image_, int cwidth_, int cheight_, int xOffset_, int yOffset_, GameState level_) {
		level = (MapState) level_;
		
		tileMap = tm;
		tileSize = tm.getTileSize(); 

		image = level.getRM().getImage(image_);
		width = image.getWidth();
		height = image.getHeight();
		
		cwidth = cwidth_;
		cheight = cheight_;
		xOffset = xOffset_;
		yOffset = yOffset_;
	}
	
	// constructor
	public MapObject(TileMapWithBB tm, BufferedImage image_, int cwidth_, int cheight_, int xOffset_, int yOffset_, GameState level_) {
		level = (MapState) level_;
		
		tileMap = tm;
		tileSize = tm.getTileSize(); 
		image = image_;
		width = image.getWidth();
		height = image.getHeight();
		
		cwidth = cwidth_;
		cheight = cheight_;
		xOffset = xOffset_;
		yOffset = yOffset_;
	}


	public boolean intersects(MapObject o) {
		Rectangle rectangle1 = getRectangle();
		Rectangle rectangle2 = o.getRectangle();
		return rectangle1.intersects(rectangle2);
	}
	
	public Rectangle getRectangle() {
		return new Rectangle(
				(int)x - cwidth/2,
				(int)y - cheight/2,
				cwidth,
				cheight
				);
	}
	
	public void calculateCorners(double x_, double y_) {
		
		rect = getRectangle();
		
		double xleft = x_ - cwidth / 2;
		double xright = x_ + cwidth / 2 - 1;
		double ytop = y_ - cheight / 2;
		double ybot = y_ + cheight / 2 - 1;

		int leftPos = (int)((xleft) / tileSize);
		int rightPos = (int)((xright) / tileSize);
		int topPos = (int)((ytop) / tileSize);
		int bottomPos = (int)((ybot) / tileSize);
		
		surfaceDifference = 0;
		topLeft = checkCorner(topPos, leftPos, xleft, ytop, false, true);
		topRight = checkCorner(topPos, rightPos, xright, ytop, false, false);
		bottomLeft = checkCorner(bottomPos, leftPos, xleft, ybot, true, true);
		bottomRight = checkCorner(bottomPos, rightPos, xright, ybot, true, false);
		if (topLeft != 0 || topRight != 0 || bottomLeft != 0 || bottomRight != 0) {
			collided = true;
		}
		
	}
	
	private Integer getType(int row, int col) {
		Integer type = tileMap.getType(row, col);
		if (type == null) {
			return 0;
		}
		return type;
	}
	
	public int checkCorner(int row, int col, double xcorner, double ycorner, boolean bottom, boolean left) {
		Integer type = getType(row, col);
		
		//Handle some triangle edge cases		
		if (bottom && !left && type == 2) {
			xcorner += 1;
			ycorner += 1;
		} else if ( bottom && left && type == 3) {
			ycorner += 1;
		} else if ( !bottom && !left && type == 5) {
			ycorner -= 1;
		}
		
		//Move on!
		switch (type) {
			case 0:
				//Open space
				return 0;
			case 1:
				//Solid block
				return 1;
			case 2:
				// slope: /.
				if (((row+1)*tileSize) - (x - col*tileSize) - 1 >= y) {
					//You are up the tile.	
					double tileSlopeY = ((row+1)*tileSize) - (xcorner - col*tileSize);
					double tileSlopeX = ((col+1)*tileSize) - (ycorner - row*tileSize);
					if ((!bottom && tileSlopeY < ycorner+cheight) || (left && tileSlopeX < xcorner+cwidth)) {
						return 1;
					} else {
						if (tileSlopeY <= ycorner) {
							surfaceDifference = tileSlopeY - ycorner;
							return 2;
						} else {
							return 0;
						}
					}
				}
				return 1;
			case 3:
				// slope: .\
				if (((row)*tileSize) + (x - col*tileSize) - 1 >= y) {
					//You are up the tile.	
					double tileSlopeY = ((row)*tileSize) + (xcorner - col*tileSize);
					double tileSlopeX = ((col)*tileSize) + (ycorner - row*tileSize);
					if ((!bottom && tileSlopeY < ycorner+cheight) || (!left && tileSlopeX > xcorner-cwidth)) {
						return 1;
					} else {
						if (tileSlopeY <= ycorner) {
							surfaceDifference = tileSlopeY - ycorner;
							return 3;
						} else {
							return 0;
						}
					}
				}
				return 1;
			case 4:
				// slope: */
				if (((row+1)*tileSize) - (x - col*tileSize) <= y) {
					//You are up the tile.	
					double tileSlopeY = ((row+1)*tileSize) - (xcorner - col*tileSize);
					double tileSlopeX = ((col+1)*tileSize) - (ycorner - row*tileSize);
					if ((bottom && tileSlopeY > ycorner-cheight) || (!left && tileSlopeX > xcorner-cwidth)) {
						return 1;
					} else {
						if (tileSlopeY >= ycorner) {
							surfaceDifference = tileSlopeY - ycorner;
							return 2;
						} else {
							return 0;
						}
					}
				}
				return 1;
			case 5:
				// slope: \*
				if (((row)*tileSize) + (x - col*tileSize) <= y) {
					//You are up the tile.	
					double tileSlopeY = ((row)*tileSize) + (xcorner - col*tileSize);
					double tileSlopeX = ((col)*tileSize) + (ycorner - row*tileSize);
					if ((bottom && tileSlopeY > ycorner-cheight) || (left && tileSlopeX < xcorner+cwidth)) {
						return 1;
					} else {
						if (tileSlopeY >= ycorner) {
							surfaceDifference = tileSlopeY - ycorner;
							return 3;
						} else {
							return 0;
						}
					}
				}
				return 1;
			default:
				throw new Error();
		}
	}
	
	public void checkTileMapCollision() {
		if (futuredy != null) {
			dy = futuredy;
			futuredy = null;
		}
		
		collided = false;
		
		currCol = (int)x/tileSize;
		currRow = (int)y/tileSize;
		
		double xdest = x + dx;
		double ydest = y + dy;
		if (xdest < 0) {
			xdest = 0;
		} else if (xdest > tileMap.getWidth()) {
			xdest = tileMap.getWidth();
		}
		if (ydest < 0) {
			ydest = 0;
		} else if (ydest > tileMap.getHeight()) {
			ydest = tileMap.getHeight();
		}
		
		xtemp = x;
		ytemp = y;
		
		calculateCorners(x, ydest);
		boolean checkedSlope = false;
		if (topLeft == 2 || bottomRight == 2) {
			//Hit /
			xtemp -= dy;
			ytemp += dy;
			ytemp += surfaceDifference;
			checkedSlope = true;
		} else if (topRight == 3 || bottomLeft == 3) {
			//Hit \
			xtemp += dy;
			ytemp += dy;
			ytemp += surfaceDifference;
			checkedSlope = true;
		} else if ((bottomLeft==1 || bottomRight==1) && dy > 0 || (topLeft==1 || topRight==1) && dy <= 0) {
			//Regular wall
			if(dy < 0) {
				dy = 0;
				ytemp = currRow * tileSize + (cheight%40) / 2.0;
			} else {
				ytemp = (currRow + 1) * tileSize - (cheight%40) / 2.0;
			}
			dy = 0;
		} else if (bottomRight != 3 && topLeft != 3 && topRight != 2 && bottomLeft != 2) {
			ytemp += dy;
		}
		
		calculateCorners(xdest, y);
		if (checkedSlope && (topLeft == 2 || bottomRight == 2 || topRight == 3 || bottomLeft == 3)) {
			//If pressing left and up while hitting a corner both times, don't move.
			xtemp = xdest;
			ytemp = y;
			ytemp += surfaceDifference;
		} else if (topLeft == 2 || bottomRight == 2) {
			//Hit /
			ytemp -= dx;
			xtemp += dx;
			xtemp += surfaceDifference;
		} else if (topRight == 3 || bottomLeft == 3) {
			//Hit \
			ytemp += dx;
			xtemp += dx;
			xtemp -= surfaceDifference;
		} else if((topLeft==1 || bottomLeft==1) && dx < 0 || (topRight==1 || bottomRight==1) && dx >= 0) {
			//Regular wall
			if(dx < 0) {
				dx = 0;
				xtemp = currCol * tileSize + (cwidth%40) / 2.0;
			} else {
				dx = 0;
				xtemp = (currCol + 1) * tileSize - (cwidth%40) / 2.0;
			}
		} else if (bottomRight != 3 && topLeft != 3 && topRight != 2 && bottomLeft != 2) {
			if (dx != 0 && checkedSlope && bottomRight == 0 && bottomLeft == 0 && topRight == 0 && topLeft == 0) {
				xtemp = x + dx;
			} else {
				xtemp += dx;
			}
		}
	}
	
	private BoundingBox collidesWithBB(Point p1, Point p2) {
		ArrayList<BoundingBox> bbs = tileMap.getBoundingBoxes();
		
		boolean horizontal = false;
		boolean vertical = false;
		if (p1.getX() == p2.getX()) {
			vertical = true;
		}
		
		if (p1.getY() == p2.getY()) {
			horizontal = true;
		}
		
		for (int i = 0; i < bbs.size(); i++) {
			Rectangle rect = bbs.get(i).getRectangle();
			if (rect.contains(p1) || rect.contains(p2)) {
				return bbs.get(i);
			}
			
			//Check that our MO is not "speared" by bb
			if (horizontal) {
				if (rect.contains(new Point((int)rect.getX()+1, (int)p1.getY()))) {
					//x pos checks out
					if ((p1.getX() > rect.getX()) != (p2.getX() > rect.getX())) {
						return bbs.get(i);
					}
				}
			} else if (vertical) {
				if (rect.contains(new Point((int)p1.getX(), (int)rect.getY()+1))) {
					//y pos checks out
					if ((p1.getY() > rect.getY()) != (p2.getY() > rect.getY())) {
						return bbs.get(i);
					}
				}
			}
		}
		
		return null;
	}
	
	protected void checkBBCollision() {
		
		//Check up/down
		double xleft = x - cwidth / 2;
		double xright = x + cwidth / 2 - 1;
		double ytop = ytemp - cheight / 2;
		double ybot = ytemp + cheight / 2 - 1;
		
		if (dy > 0) {
			BoundingBox bb = collidesWithBB(new Point((int)xleft, (int)ybot), new Point((int)xright, (int)ybot));
			
			if (bb != null) {
				int bbTopy = (int)(bb.getRectangle().getY());
				ytemp = bbTopy - cheight/2;
				dy = 0;
			}
		} else if (dy < 0) {
			BoundingBox bb = collidesWithBB(new Point((int)xleft, (int)ytop), new Point((int)xright, (int)ytop));
			
			if (bb != null) {
				int bbBoty = (int)(bb.getRectangle().getY() + bb.getRectangle().getHeight());
				ytemp = bbBoty + cheight/2;
				dy = 0;
			}
		}
		
		//Check left/right
		xleft = xtemp - cwidth / 2;
		xright = xtemp + cwidth / 2 - 1;
		ytop = y - cheight / 2;
		ybot = y + cheight / 2 - 1;
		
		if (dx < 0) {
			BoundingBox bb = collidesWithBB(new Point((int)xleft, (int)ytop), new Point((int)xleft, (int)ybot));
			
			if (bb != null) {
				int bbRight = (int)(bb.getRectangle().getX() + bb.getRectangle().getWidth());
				xtemp = bbRight + cwidth/2;
				dx = 0;
			}
		} else if (dx > 0) {
			BoundingBox bb = collidesWithBB(new Point((int)xright, (int)ytop), new Point((int)xright, (int)ybot));
			
			if (bb != null) {
				int bbLeft = (int)(bb.getRectangle().getX());
				xtemp = bbLeft - cwidth/2;
				dx = 0;
			}
		}
	}
	
	public void setMapPosition() {
		xmap = tileMap.getx();
		ymap = tileMap.gety();
	}
	
	public void setLeft(boolean b) { left = b;}
	public void setRight(boolean b) { right = b; }
	public void setUp(boolean b) { up = b; }
	public void setDown(boolean b) { down = b; }
	
	public double getx() { return x; }
	public double gety() { return y; }
	public double getBottomy() { return y + cheight/2; }
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	public int getCWidth() { return cwidth; }
	public int getCHeight() { return cheight; }
	
	public void setPosition(double x_, double y_) {
		x = x_;
		y = y_;
		if (x < cwidth/2) {
			x = cwidth/2;
		} else if (x > tileMap.getWidth() - cwidth/2) {
			x = tileMap.getWidth() - cwidth/2;
		}
		
		if (y < cheight/2) {
			y = cheight/2;
		} else if (y >= tileMap.getHeight() - cheight/2) {
			y = tileMap.getHeight() - cheight/2;
		}
		
	}
	
	public void setVector(double dx_, double dy_) {
		dx = dx_;
		dy = dy_;
	}
	
	public void setx(double x_) {
		x = x_;
	}
	
	public void setYvelFuture(Double ddy_) {
		futuredy = ddy_;
	}
	
	public boolean notOnScreen() {
		return x - xmap + width < 0 ||
				x - xmap - width > GamePanel.WIDTH ||
				y - ymap + height < 0 ||
				y - ymap - height > GamePanel.HEIGHT;
	}
	
	public void draw(java.awt.Graphics2D g) {
		if (facingRight) {
			g.drawImage(
					image,
					(int)(x - xmap - width / 2 + xOffset),
					(int)(y - ymap - height / 2 + yOffset),
					null
				);
		} else {
			g.drawImage(
					image,
					(int)(x - xmap - width / 2 + width + xOffset),
					(int)(y - ymap - height / 2 + yOffset),
					-width,
					height,
					null
				);
		}
	}
	
}
