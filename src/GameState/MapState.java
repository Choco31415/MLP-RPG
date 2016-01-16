package GameState;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;

import Audio.AudioPlayer;
import Entity.Enemy;
import Entity.MapObject;
import Entity.Projectile;
import Entity.Player;
import Entity.ResourceManager;
import Main.GamePanel;
import TileMap.TileMap;
import TileMap.TileMapWithBB;

@SuppressWarnings("unused")
/*
 * Q - Edit map, resume play.
 * Arrow keys - Move around the map while editing it.
 */
public abstract class MapState extends GameState {
	
	protected TileMapWithBB tileMap;
	protected BufferedImage background;
	
	protected ResourceManager rm;
	
	protected Player player;
	protected boolean readFromKeyboard;
	
	protected ArrayList<Projectile> projectiles = new ArrayList<Projectile>();
	
	protected AudioPlayer bgMusic;
	
	protected int endingTilesFromEnd;
	
	public int winState = 0; // 0 = playing, -1 = dead, 1 = winnerz
	public int frame = 0;
	public double deathSceneLength;
	
	//For level editing.
	private int lastKeyPress = 0;
	private boolean editingTileMap;
	private double oldTileMapX;
	private double oldTileMapY;
	
	//For navigating the world during editing
	boolean left;
	boolean right;
	boolean up;
	boolean down;
	
	//For transitions
	boolean transitioning = false;
	double transitionLength = 0;
	int transitionFrame = 0;
	
	public MapState(GameStateManager gsm_) {
		gsm = gsm_;
		
		if (gsm.getAudioAllowed()) {
			bgMusic = new AudioPlayer("/Music/Jaunty Gumption.wav");
			bgMusic.play();
		}
		
		rm = new ResourceManager();
	}

	@Override
	public boolean update() {
		if (!transitioning) {
			if (!editingTileMap) {
				frame++;
				
				if (winState != -1) {
					player.update();
				}
				
				if (player.getDead() && winState == 0) {
					winState = -1;
					frame = 0;
				}
				if (winState == -1 && frame >= deathSceneLength*GamePanel.IDEAL_FPS) {
					return true;
				}
				
				boolean collided;
				ArrayList<Projectile> toRemove = new ArrayList<Projectile>();
				for (Projectile fb : projectiles) {
					collided = fb.update();
					if (collided) {
						toRemove.add(fb);
					}
				}
				for (Projectile p : toRemove) {
					projectiles.remove(p);
				}
				
				if (winState == 1) {
					if (player.getdy() == 0) {
						player.setRight(true);
					}
					if (player.getx() > tileMap.getWidth() - tileMap.getTileSize()) {
						return true;
					}
				}
			} else {
				int vel = 6;
				if (up) {
					tileMap.changePosition(0, -vel);
				} else if (down) {
					tileMap.changePosition(0, vel);
				}
				if (left) {
					tileMap.changePosition(-vel, 0);
				} else if (right) {
					tileMap.changePosition(vel, 0);
				}
				
				setMapPositions();
			}
		} else {
			player.scroll();
			setMapPositions();
		}
		
		return false;
	}
	
	@Override
	public void draw(Graphics2D g) {
		draw(g, false);
	}

	public void draw(Graphics2D g, boolean doingScreenshot) {
		double oldX = tileMap.getx();
		double oldY = tileMap.gety();
		

		if (!doingScreenshot) {
			//Background
			g.drawImage(background, 0, 0, null);
		} else {
			//Doing a screenshot, set everything up.
			tileMap.setPosition(0, 0);
		}
		
		//Set up layers.
		ArrayList<Integer> layers = tileMap.getDecorLayers();
		int previousLayer = Integer.MIN_VALUE;
		previousLayer = drawTilemapLayers(g, doingScreenshot, layers, Integer.MIN_VALUE, 0, previousLayer);
		
		if (doingScreenshot) {
			player.setMapPosition();
		}
		
		for (Projectile fb : projectiles) {
			if (doingScreenshot) {
				fb.setMapPosition();
			}
		}
		
		drawLayers(g, doingScreenshot, 0, 2);
		previousLayer = 2;
		if (!doingScreenshot) {
			tileMap.draw(g, GamePanel.WIDTH, GamePanel.HEIGHT, -2, 2, -1, 8, getMapObjects());
		} else {
			tileMap.draw(g, tileMap.getWidth(), tileMap.getHeight(), 0, 0, 0, 0, getMapObjects());
		}
		
		previousLayer = drawTilemapLayers(g, doingScreenshot, layers, 2, Integer.MAX_VALUE, previousLayer);
		
		//If done with a screenshot, undo changes made to obtain the screenshot.
		if (doingScreenshot) {
			tileMap.setPosition(oldX, oldY);
		}
		
		if (transitioning) {
			transitionFrame++;
			int frameLength = (int)(GamePanel.FPS*transitionLength);
			int halfFrameLength = frameLength / 2;
			Color color;
			if (transitionFrame > frameLength / 2) {
				int frameResidual = frameLength - transitionFrame;
				color = new Color(0, 0, 0, (frameResidual*(255/halfFrameLength)));
				if (!(transitionFrame - 1 > frameLength / 2)) {
					player.transition();
					color = Color.black;
				}
			} else {
				color = new Color(0, 0, 0, (transitionFrame*(255/halfFrameLength)));
			}
			g.setColor(color);
			g.fill(new Rectangle(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT));
			if (transitionFrame == frameLength) {
				transitioning = false;
				transitionFrame = 0;
			}
		}
	}
	
	protected int drawTilemapLayers(Graphics2D g, boolean doingScreenshot, ArrayList<Integer> layers, int fromLayer, int toLayer, int previousLayer) {
		for (Integer layer : layers) {
			if (fromLayer <= layer && layer < toLayer) {
				drawLayers(g, doingScreenshot, previousLayer, layer);
				tileMap.drawDecor(g, layer, layer+1);
				previousLayer = layer;
			}
		}
		drawLayers(g, doingScreenshot, previousLayer, toLayer);
		previousLayer = toLayer;
		
		return previousLayer;
	}
	
	//What map objects should be drawn on the map?
	protected ArrayList<MapObject> getMapObjects() {
		ArrayList<MapObject> toReturn = new ArrayList<MapObject>();
		toReturn.add(player);
		toReturn.addAll(projectiles);
		return toReturn;
	}
	
	protected void drawLayers(Graphics2D g, boolean doingScreenshot, int layerFrom, int layerTo) {
		//Override if need to draw objects between layers in class children
	}

	@Override
	public void keyPressed(int k) {
		checkKeyPresses(k, true);
		
		if ((k == KeyEvent.VK_Q && lastKeyPress == KeyEvent.VK_P) || (k == KeyEvent.VK_P && lastKeyPress == KeyEvent.VK_Q)) {
			toggleEditingMap();
		} else if ((k == KeyEvent.VK_O && lastKeyPress == KeyEvent.VK_P) || (k == KeyEvent.VK_P && lastKeyPress == KeyEvent.VK_O)) {
			System.out.println(player.getx() + ":" + player.gety());
		}
		
		tileMap.keyPressed(k);
		
		lastKeyPress = k;
	}

	@Override
	public void keyReleased(int k) {
		checkKeyPresses(k, false);
		
		tileMap.keyReleased(k);
	}
	
	private void toggleEditingMap() {
		editingTileMap = !editingTileMap;
		if (editingTileMap) {
			oldTileMapX = tileMap.getx();
			oldTileMapY = tileMap.gety();
			left = false;
			right = false;
			up = false;
			down = false;
		} else {
			tileMap.setPosition(oldTileMapX, oldTileMapY);
		}
		tileMap.toggleEditing();
	}
	
	private void checkKeyPresses(int k, Boolean bool) {
		if (player == null || winState != 0 || !readFromKeyboard) {
			return;
		}
		
		if (!editingTileMap) {
			if (k == KeyEvent.VK_LEFT || k == KeyEvent.VK_A) { player.setLeft(bool); }
			if (k == KeyEvent.VK_RIGHT || k == KeyEvent.VK_D) { player.setRight(bool); }
			if (k == KeyEvent.VK_UP || k == KeyEvent.VK_W) { player.setUp(bool); }
			if (k == KeyEvent.VK_DOWN || k == KeyEvent.VK_S) { player.setDown(bool); }
		} else {
			if (k == KeyEvent.VK_LEFT || k == KeyEvent.VK_A) { left = bool; }
			if (k == KeyEvent.VK_RIGHT || k == KeyEvent.VK_D) { right = bool; }
			if (k == KeyEvent.VK_UP || k == KeyEvent.VK_W) { up = bool; }
			if (k == KeyEvent.VK_DOWN || k == KeyEvent.VK_S) { down = bool; }
		}
	}
	
	public void setMousePos(Point p) {
		tileMap.setMousePos(p);
	}
	
	public void mousePressed(MouseEvent e) {
		tileMap.mousePressed(e);
	}
	
	public void mouseReleased(MouseEvent e) {
		tileMap.mouseReleased(e);
	}
	
	public void setReadFromKeyboard(boolean bool) {
		readFromKeyboard = bool;
	}
	
	public ArrayList<Projectile> getProjectiles() {
		return projectiles;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public TileMap getTM() {
		return tileMap;
	}
	
	public int randInt(int bot, int top) {
		Random rand = new Random();
		int randVar = rand.nextInt(top-bot);
		return randVar + bot;
	}
	
	protected int getColX(double col) {
		return (int) (col*tileMap.getTileSize() + 10);
	}
	
	protected int getRowY(double row) {
		return (int) (row*tileMap.getTileSize() + 10);
	}

	@Override
	public void end() {
		// TODO Auto-generated method stub
		//bgMusic.close();
	}
	
	public void reset() {
		projectiles = new ArrayList<Projectile>();
		
		tileMap.reset();
		
		createLevel();
		
		frame = 0;
		winState = 0;
		
		if (editingTileMap) {
			
		}
	}
	
	public int getWinState() {
		return winState;
	}
	
	public abstract void createLevel();
	
	public ResourceManager getRM() {
		return rm;
	}

	public TileMap getTileMap() {
		return tileMap;
	}
	
	public void screenshot() {
		BufferedImage imageRepresentation = new BufferedImage(tileMap.getWidth(), tileMap.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = imageRepresentation.createGraphics();
		draw(g, true);
		g.setColor(Color.black);
		g.drawString(tileMap.getWidth() + "x" + tileMap.getHeight(), 5, 25);
		saveImage(imageRepresentation, "Map Screenshot.png");
	}
	
	private void saveImage(BufferedImage img, String title) {
		try {
		    // retrieve image
		    BufferedImage bi = img;
		    File outputfile = new File(title);
		    ImageIO.write(bi, "png", outputfile);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	public void transition(double length) {
		transitioning = true;
		transitionLength = length;
	}
	
	public void setMapPositions() {
		player.setMapPosition();
		
		for (Projectile p: projectiles) {
			p.setMapPosition();
		}
		
		tileMap.update();
	}
}
