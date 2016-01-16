package TileMap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import Entity.MapObject;
import Main.GamePanel;

/*
 * Layer0 - images to lay on top of the map
 * Layer1 - images that sort and overlap with map objects
 */
public class TileMap {
	
	//For record purposes.
	protected ArrayList<String> mapFiles;
	protected ArrayList<Integer> mapLengths;//How long is each map file?
	protected String decorFile;
	protected ArrayList<String> tilesetInfoFiles = new ArrayList<String>();//Where are the tilemap files?
	
	// pos and dimensions
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	
	// map
	protected int[][] mapCopy;
	protected int[][] map;
	protected ArrayList<Decoration> decorations = new ArrayList<Decoration>();//Lowest layers first.
	protected int tileSize;
	protected int numRow;
	protected int numCol;
	
	// tiles
	protected BufferedImage tileset;// Temp var
	protected Tile[] tiles;
	
	// Bounds. Set in loadMap()
	protected int xmin;
	protected int ymin;
	protected int xmax;
	protected int ymax;
	
	// drawing
	protected int rowOffset;
	protected int colOffset;
	protected int numRowsToDraw;
	protected int numColsToDraw;
	
	// editing
	protected boolean editing;
	protected int editingMode;
	protected boolean removingDecor;
	
	protected int mouseX;
	protected int mouseY;
	protected boolean mouseDown;
	protected int lastKeyPress = 0;
	
	protected int tileID;
	
	//For navigating tiles
	protected int tileNavDirection;
	protected int frameWait;
	protected boolean delaying;
	protected boolean stratifyX;
	protected boolean stratifyY;
	
	public TileMap(int tileSize_) {
		tileSize = tileSize_;
	}
	
	/*
	 * This method takes the location of the tileset and extra tile information as parameters.
	 * This method clears all tilesets previously used. loadAdditionalTiles does not.
	 */
	public int loadTiles(String tilesetImage, String extraTileInfoFile) {
		tilesetInfoFiles = new ArrayList<String>();
		tiles = new Tile[]{};
		return loadAdditionalTiles(tilesetImage, extraTileInfoFile);
	}
	
	public int loadAdditionalTiles(String tilesetImage, String extraTileInfoFile) {
		try {
			// Import information and images.
			tilesetInfoFiles.add(extraTileInfoFile);
			tileset = ImageIO.read(getClass().getResourceAsStream(tilesetImage));
		    
			int[][] extraTileInfo = parseFile(extraTileInfoFile, 1);

		    
			// Put all information gathered into easily accessibly tiles array.
			int previousTileCount = tiles.length;
		    int tileCount = extraTileInfo.length + tiles.length;
		    if (extraTileInfo[0].length < 7) {
		    	throw new Error("Inadequate tile info.");
		    }
		    
		    Tile[] tempTiles = new Tile[tileCount];
		    for (int i = 0; i < tileCount; i++) {
		    	if (i < tiles.length) {
		    		tempTiles[i] = tiles[i];
		    	} else {
		    		int[] tileInfo = extraTileInfo[i-tiles.length];
		    		tempTiles[i] = new Tile(getTileImage(tileInfo[0], tileInfo[1], tileInfo[2], tileInfo[3]), tileInfo[4], tileInfo[5], tileInfo[6]);
		    	}
		    }
		    tiles = tempTiles;
		    return previousTileCount;
		} catch (IOException e) {
			System.out.println("Err, cannot load images. The rebel attack succedded. (TileMap)");
			return -1;
		}
	}
	
	public BufferedImage getTileImage(int row, int col, int width_in_tiles, int height_in_tiles) {
		try {
			return tileset.getSubimage(col*tileSize, row*tileSize, width_in_tiles*tileSize, height_in_tiles*tileSize);
		} catch (RasterFormatException e) {
			throw new Error();
		}
	}
	
	public void loadTilesetLazy(String name) {
		loadTiles("/Tilesets/" + name + ".png", "/Tilesets/" + name + "_extrainfo.txt");
	}
	
	/*
	 * Load map and decor files given they follow a naming convention.
	 */
	public void loadSceneLazy(String sceneName, int numMaps) {
		if (numMaps <= 0) {
			throw new Error();
		}
		ArrayList<String> mapFiles_ = new ArrayList<String>();

		if (numMaps == 1) {
			mapFiles_.add("/Maps/" + sceneName + ".txt");
		} else {
			for (int i = 0; i < numMaps; i++) {
				mapFiles_.add("/Maps/" + sceneName + "_" + (i+1) + ".txt");
			}
		}
		
		loadMaps(mapFiles_);
		mapFiles = mapFiles_;
		loadDecor("/Maps/" + sceneName + " decor.txt");
	}
	
	/*
	 * This method loads a map file (secretly a txt file).
	 */
	public void loadMaps(ArrayList<String> mapFiles_) {
		//Setting up some variables.
		mapFiles = mapFiles_;
		mapLengths = new ArrayList<Integer>();
		map = null;
		int[][][] tempMaps = new int[mapFiles_.size()][][];
		int length = 0;
		int breadth;
		
		//Reading in each map one by one
		for (int i = 0; i < mapFiles_.size(); i++) {
			tempMaps[i] = parseFile(mapFiles_.get(i), 1);
			length += tempMaps[i][0].length;
			mapLengths.add(tempMaps[i][0].length);
		}
		
		//Concating the maps horizontally
		breadth = tempMaps[0].length;
		map = new int[breadth][length];
		int tempLength = 0;
		int[][] tempMap;
		
		for (int mapNum = 0; mapNum < tempMaps.length; mapNum++) {
			tempMap = tempMaps[mapNum];
			for (int col = 0; col < tempMap[0].length; col++) {
				for (int row = 0; row < breadth; row++) {
					map[row][tempLength] = tempMap[row][col];
				}
				tempLength++;
			}
		}
			
		generateMapData();
	}
	
	protected void generateMapData() {
		numRow = map.length;
		numCol = map[0].length;
		width = tileSize * numCol;
		height = tileSize * numRow;
		
			
		xmin = 0;
		xmax = width - GamePanel.WIDTH;
		ymin = 0;
		ymax = height - GamePanel.HEIGHT;
		
		mapCopy = cloneArray(map);
	}
	
	public void loadDecor(String decorFile_) {
		decorFile = decorFile_;
		int[][] tempData;
		decorations = new ArrayList<Decoration>();
		try {
			tempData = parseFile(decorFile_, 1);
		} catch (NullPointerException e) {
			tempData = parseFileUnboundedColumn(decorFile_, 1);
		}
		for (int decorNum = 0; decorNum < tempData.length; decorNum++) {
			int[] decorData = tempData[decorNum];
			decorations.add(new Decoration(decorData[0], decorData[1], decorData[2], decorData[3]));
		}
		
		
		sortDecor();
	}
	
	protected void sortDecor() {
		//Sort decor by layer. Lowest -> Highest layer.
		for (int i = 0; i < decorations.size(); i++) {
			int lowestDecor = i;
			for (int j = i + 1; j < decorations.size(); j++) {
				if (decorations.get(j).getLayer() < decorations.get(lowestDecor).getLayer()) {
					lowestDecor = j;
				}
			}
			Decoration temp = decorations.get(lowestDecor);
			Decoration temp2 = decorations.get(i);
			decorations.set(i, temp);
			decorations.set(lowestDecor, temp2);
		}
	}
	
	public int getTileSize() { return tileSize; }
	public double getx() { return x; }
	public double gety() { return y; }
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	
	public Integer getType(int row, int col) {
		if (row >= numRow || col >= numCol || row < 0 || col < 0) {
			return null;
		} else {
			int tileID = map[row][col];
			if (tileID == 0){
				return 0;
			} else {
				return tiles[tileID-1].getType();
			}
		}
	}
	
	public void setMapTile(int row, int col, int newType) {
		try {
			map[row][col] = newType;
		} catch (ArrayIndexOutOfBoundsException e) {
			
		}
	}
	
	public ArrayList<Decoration> getDecor() {
		return decorations;
	}
	
	public ArrayList<Decoration> getDecorLayer(int index) {
		ArrayList<Decoration> toReturn = new ArrayList<Decoration>();
		for (Decoration d : decorations) {
			if (d.getLayer() == index) {
				toReturn.add(d);
			}
		}
		
		return toReturn;
	}
	
	public void setDecor(ArrayList<Decoration> d) {
		decorations = d;
		
		sortDecor();
	}
	
	public void setDecor(int index, Decoration d) {
		decorations.set(index, d);
		
		sortDecor();
	}
	
	public ArrayList<Integer> getDecorLayers() {
		ArrayList<Integer> layers = new ArrayList<Integer>();
		
		for (Decoration d : decorations) {
			if (!layers.contains(d.getLayer())) {
				layers.add(d.getLayer());
			}
		}
		
		return layers;
	}
	
	public void changePosition (double dx, double dy) {
		setPosition(x + dx, y + dy);
	}
	
	public void setPosition(double oldX, double oldY) {
		//System.out.println("1:" + x);
		x = (int) oldX;
		y = (int) oldY;
		//System.out.println("2:" + x);
		
		fixBounds();
		//System.out.println("3:" + x);
		
		colOffset = x/tileSize;
		rowOffset = y/tileSize;
	}
	
	protected void fixBounds() {
		if (x < xmin) { x = xmin;}
		if (y < ymin) { y = ymin;}
		if (x > xmax) { x = xmax;}
		if (y > ymax) { y = ymax;}
	}
	
	//Only neccessary to call while editing.
	public void update() {
		if (tileNavDirection != 0) {
			frameWait--;
			if (frameWait <= 0) {
				tileID += tileNavDirection;
				if (tileID < 0) {
					tileID = tiles.length;
				}
				if (tileID > tiles.length) {
					tileID = 0;
				}
				
				if (delaying) {
					frameWait = (int)(GamePanel.FPS*0.6);
				} else {
					frameWait = (int)(GamePanel.FPS*0.1);
				}
				
				delaying = false;
			}
		}
	}
	
	public void draw(Graphics2D g) {
		draw(g, GamePanel.WIDTH, GamePanel.HEIGHT, 0, 0, 0, 0, null);
	}
	
	public void draw(Graphics2D g, int gWidth, int gHeight) {
		draw(g, gWidth, gHeight, 0, 0, 0, 0, null);
	}
	
	public void draw(Graphics2D g, int gWidth, int gHeight, ArrayList<MapObject> objects) {
		draw(g, gWidth, gHeight, 0, 0, 0, 0, objects);
	}
	
	public void draw(Graphics2D g, int gWidth, int gHeight, int leftBias, int rightBias, int topBias, int bottomBias, ArrayList<MapObject> objects) {
		numRowsToDraw = (int) Math.ceil((double)gHeight/tileSize);
		numColsToDraw = (int) Math.ceil((double)gWidth/tileSize);
		ArrayList<Decoration> layer0 = getDecorLayer(0);
		ArrayList<Double> layer0Y = new ArrayList<Double>();
		ArrayList<Decoration> layer1 = getDecorLayer(1);
		
		//Sort layer 1 and objects by y
		ArrayList<Object> obs = new ArrayList<Object>();
		ArrayList<Double> obsY = new ArrayList<Double>();
		
		for (Decoration d : layer0) {
			double boty = (double) (d.gety() + tiles[d.getTileID()-1].getHeight());
			layer0Y.add(boty);
		}
		for (Decoration d : layer1) {
			obs.add(d);
			double boty = (double) (d.gety() + tiles[d.getTileID()-1].getHeight());
			obsY.add(boty);
		}
		for (MapObject mo : objects) {
			obs.add(mo);
			obsY.add(mo.getVisualBottomy());
		}
		
		for (int i = 0; i < obs.size(); i++) {
			int lowest = i;
			for (int j = i + 1; j < obs.size(); j++) {
				if (obsY.get(j) < obsY.get(lowest)) {
					lowest = j;
				}
			}
			
			Object temp = obs.get(i);
			Object temp2 = obs.get(lowest);
			obs.set(i, temp2);
			obs.set(lowest, temp);
			
			temp = obsY.get(i);
			temp2 = obsY.get(lowest);
			obsY.set(i, (Double) temp2);
			obsY.set(lowest, (Double) temp);
		}
		
		//Draw the top most objects and such.
		ArrayList<Integer> toRemove;
		for (int i = 0; i < layer0.size(); i++) {
			double boty = layer0Y.get(i);
			double topy = boty - tiles[layer0.get(i).getTileID()-1].getHeight();
			if (boty < tileSize || (boty >= tileSize && (topy < tileSize))) {
				drawDecor(g, decorations.get(i), new Rectangle(0, -y, gWidth, tileSize));
			}
		}
		
		toRemove = new ArrayList<Integer>();
		for (int i = 0; i < obs.size(); i++) {
			double y = obsY.get(i);
			Object object = obs.get(i);
			if (y <= tileSize) {
				toRemove.add(i);
				if (Decoration.class.isAssignableFrom(object.getClass())) {
					drawDecor(g, (Decoration) object);
				} else {
					((MapObject)object).draw(g);
				}
			} else {
				break;
			}
		}
		for (int i = toRemove.size()-1; i >= 0; i--) {
			obs.remove((int)toRemove.get(i));
			obsY.remove((int)toRemove.get(i));
		}
		
		//Draw
		for(
				int row = rowOffset + topBias;
				row <= rowOffset + numRowsToDraw + bottomBias;
				row++) {
			
			for(
					int col = colOffset + leftBias;
					col <= colOffset + numColsToDraw + rightBias;
					col++) {
				if (col >= numCol) break;
				if (col < 0) continue;
				if (row >= numRow) break;
				if (row < 0) break;
				
				int tileID = map[row][col];
				
				if(tileID == 0) continue;
				
				Tile tile = tiles[tileID-1];
				BufferedImage image = tile.getImage();
				int xOffset = tile.getXOffset();
				int yOffset = tile.getYOffset();
				
				g.drawImage(
						image,
						(int)-x + col * tileSize + xOffset,
						(int)-y + row * tileSize + yOffset - image.getHeight() + tileSize,
						null
					);
			}
			
			//Draw layer 0
			for (int i = 0; i < layer0.size(); i++) {
				double boty = layer0Y.get(i);
				double topy = boty - tiles[layer0.get(i).getTileID()-1].getHeight();
				if (boty > (row)*tileSize && (boty <= (row+1)*tileSize || topy <= (row+1)*tileSize)) {
					drawDecor(g, decorations.get(i), new Rectangle(0, (row)*tileSize - y, gWidth, tileSize));
				}
			}
			
			//Draw in map objects & layer 1;
			toRemove = new ArrayList<Integer>();
			for (int i = 0; i < obs.size(); i++) {
				double boty = obsY.get(i);
				Object object = obs.get(i);
				if (Decoration.class.isAssignableFrom(object.getClass())) {
					//Draw decor
					Decoration decor = ((Decoration)object);
					Tile tile = tiles[decor.getTileID()-1];
					int topy = (int)(boty - tile.getHeight());
					if (boty > (row)*tileSize && boty <= (row+1)*tileSize) {
						drawDecor(g, decor);
					}
				} else {
					//Draw map object
					MapObject mo = (MapObject)object;
					if (boty > (row)*tileSize && boty <= (row+1)*tileSize) {


						mo.draw(g);
					}
				}

			}
			for (int i = toRemove.size()-1; i >= 0; i--) {
				obs.remove((int)toRemove.get(i));
				obsY.remove((int)toRemove.get(i));
			}
		}
		
		if (editing) {
			if (tileID != 0) {
				Tile tile = tiles[tileID-1];
				BufferedImage image = tile.getImage();
				int xOffset = tile.getXOffset();
				int yOffset = tile.getYOffset();
				
				if (editingMode == 1 && !removingDecor) {
					g.drawImage(
							image,
							(int)( mouseX + xOffset),
							(int)( mouseY + yOffset + tileSize - image.getHeight() - tileSize),
							null
						);
				} else if (editingMode == 0) {
					g.drawImage(
							image,
							(int) mouseX - ((mouseX + (x)%tileSize)%tileSize) + xOffset,
							(int) mouseY - ((mouseY + (y)%tileSize)%tileSize) + yOffset - image.getHeight() + tileSize,
							null
						);
				}
			}
		}
	}
	
	/**
	 * @param g The Graphics2D on which to draw the decor.
	 * @param layerFrom Draw decor from this layer up. Inclusive.
	 * @param layerTo Draw decor below this layer. Exclusive.
	 */
	public void drawDecor(Graphics2D g, int layerFrom, int layerTo) {
		for (int i = 0; i < decorations.size(); i++) {
			Decoration decor = decorations.get(i);
			if (decor.getLayer() >= layerFrom && decor.getLayer() < layerTo) {
				drawDecor(g, decor);
			}
		}
	}
	
	protected void drawDecor(Graphics2D g, Decoration decor) {
		Tile tile = tiles[decor.getTileID()-1];
		BufferedImage image = tile.getImage();
		int xOffset = tile.getXOffset();
		int yOffset = tile.getYOffset();
		g.drawImage(
				image,
				(int)-x + decor.getx() + xOffset,
				(int)-y + decor.gety() + yOffset,
				null
			);
	}
	
	protected void drawDecor(Graphics2D g, Decoration decor, Rectangle clip) {
		g.setClip(clip);

		Tile tile = tiles[decor.getTileID()-1];
		BufferedImage image = tile.getImage();
		int xOffset = tile.getXOffset();
		int yOffset = tile.getYOffset();
		g.drawImage(
				image,
				(int)-x + decor.getx() + xOffset,
				(int)-y + decor.gety() + yOffset,
				null
			);
		g.setClip(null);
	}
	
	/*
	 * This method takes a file formatted as a 2D array with format:
	 * First Line: Width
	 * Second Line: Height
	 * Other Lines: Array Items
	 * 
	 * Array Items are to be separated by spaces.
	 */
	protected int[][] parseFile(String s, int commentBufferLineCount) {
		try {
			// Read in the file!
			InputStream in = getClass().getResourceAsStream(s);
			BufferedReader br = new BufferedReader(
						new InputStreamReader(in)
					);
			
			// Ignore the comment
			for (int i = 0; i < commentBufferLineCount; i++) {
				br.readLine();
			}
			
			// Gather array size
			int tempNumCol = Integer.parseInt(br.readLine());
			int tempNumRow = Integer.parseInt(br.readLine());
			int[][] returnArray = new int[tempNumRow][tempNumCol];
			
			// Parse file array into java int array
			String split_regex = "\\s+";
			for (int row = 0; row < tempNumRow; row++) {
				String line = br.readLine();
				String[] parsedLine = line.split(split_regex);
				for (int col = 0; col < tempNumCol; col++) {
					try {
						returnArray[row][col] = Integer.parseInt(parsedLine[col]);
					} catch (ArrayIndexOutOfBoundsException e) {
						returnArray[row][col] = returnArray[row][col-1];
					}
				}
			}
			
			in.close();
			br.close();
			
			return returnArray;
		} catch (IOException e) {
			System.out.println("Read N Parse Fries 2 Fried 4 Me (TileMap)");
		}
		return null;
	}
	
	/*
	 * This method takes a file formatted as a 2D array with format:
	 * First Line: Width
	 * Second Line: Height
	 * Other Lines: Array Items
	 * 
	 * Array Items are to be separated by spaces. This method ignores the second line and reads in lines until it can no longer do so.
	 */
	protected int[][] parseFileUnboundedColumn(String s, int commentBufferLineCount) {
		try {
			// Read in the file!
			InputStream in = getClass().getResourceAsStream(s);
			BufferedReader br = new BufferedReader(
						new InputStreamReader(in)
					);
			
			// Ignore the comment
			for (int i = 0; i < commentBufferLineCount; i++) {
				br.readLine();
			}
			
			// Gather array size
			int tempNumCol = Integer.parseInt(br.readLine());
			br.readLine();
			ArrayList<ArrayList<Integer>> returnArray = new ArrayList<ArrayList<Integer>>();
			
			// Parse file array into java int array
			String split_regex = "\\s+";
			try {
				do {
					String line = br.readLine();
					String[] parsedLine = line.split(split_regex);
					returnArray.add(new ArrayList<Integer>());
					for (int col = 0; col < tempNumCol; col++) {

						try {
							returnArray.get(returnArray.size()-1).add(Integer.parseInt(parsedLine[col]));
						} catch (ArrayIndexOutOfBoundsException e) {
							ArrayList<Integer> row = returnArray.get(returnArray.size()-1);
							row.add(row.get(row.size()-1));
						}
					}
				} while (true);
			} catch (ArrayIndexOutOfBoundsException|NullPointerException e) {
				//Done.
			}
			
			in.close();
			br.close();
			
			int[][] toReturn2 = new int[returnArray.size()][returnArray.get(0).size()];
			
			for (int i = 0; i < returnArray.size(); i++) {
				for (int j = 0; j < returnArray.get(0).size(); j++) {
					toReturn2[i][j] = (int)(returnArray.get(i).get(j));
				}
			}
			
			return toReturn2;
		} catch (IOException e) {
			System.out.println("Read N Parse Fries 2 Fried 4 Me (TileMap)");
		}
		return null;
	}
	
	/*
	 * This method takes a file formatted as a 2D array with format:
	 * First Line: Width
	 * Second Line: Height
	 * Other Lines: Array Items
	 * 
	 * Array Items are to be separated by spaces. This method reads array items as strings.
	 */
	protected String[][] parseFileAsStrings(String s, int commentBufferLineCount, boolean includeResidual) {
		try {
			// Read in the file!
			InputStream in = getClass().getResourceAsStream(s);
			BufferedReader br = new BufferedReader(
						new InputStreamReader(in)
					);
			
			// Ignore the comment
			for (int i = 0; i < commentBufferLineCount; i++) {
				br.readLine();
			}
			
			// Gather array size
			int tempNumCol = Integer.parseInt(br.readLine());
			int tempNumRow = Integer.parseInt(br.readLine());
			String[][] returnArray = new String[tempNumRow][tempNumCol];
			
			// Parse file array into java int array
			String split_regex = "\\s+";
			for (int row = 0; row < tempNumRow; row++) {
				String line = br.readLine();
				String[] parsedLine = line.split(split_regex);
				for (int col = 0; col < tempNumCol; col++) {
					if (includeResidual && col == tempNumCol - 1) {
						String residual = "";
						for (int i = col; i < parsedLine.length; i++) {
							residual += parsedLine[i];
							if (i != parsedLine.length - 1) {
								residual += " ";
							}
						}
						returnArray[row][col] = residual;
					} else {
						try {
							returnArray[row][col] = parsedLine[col];
						} catch (ArrayIndexOutOfBoundsException e) {
							returnArray[row][col] = returnArray[row][col-1];
						}
					}
				}
			}
			
			in.close();
			br.close();
			
			return returnArray;
		} catch (IOException e) {
			System.out.println("Read N Parse Fries 2 Fried 4 Me (TileMap)");
		}
		return null;
	}
	
	public void reset() {
		map = cloneArray(mapCopy);
	}
	
	public int[][] cloneArray(int[][] array) {
		int height = array.length;
		int length = array[0].length;
		int[][] temp = new int[height][length];
		
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < length; col++) {
				temp[row][col] = array[row][col];
			}
		}
		
		return temp;
	}
	
	public void toggleEditing() {
		editing = !editing;
		editingMode = 0;
		tileID = 1;
		frameWait = 0;
		delaying = true;
		tileNavDirection = 0;
		stratifyX = false;
		stratifyY = false;
		mouseDown = false;
		removingDecor = false;
	}
	
	public void keyPressed(int k) {
		if (k == KeyEvent.VK_X) {
			tileNavDirection = 1;
			delaying = true;
			frameWait = 0;
		} else if (k == KeyEvent.VK_Z) {
			tileNavDirection = -1;
			delaying = true;
			frameWait = 0;
		} else if (k == KeyEvent.VK_E && editing) {
			//Export 
			String[] options = new String[] { "Cancel", "Tilemap", "Tilemap (compressed)", "Decor"};
		    int choice = JOptionPane.showOptionDialog(null, "What do you want to export?", null,
		            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[3]);
			switch (choice) {
				case 0:
					break;
				case 1:
					exportMapFiles(false);
					break;
				case 2:
					exportMapFiles(true);
					break;
				case 3: 
					exportDecorFile();
					break;
				default:
					break;
			}
		} else if (k == KeyEvent.VK_R && editing) {
			removingDecor = !removingDecor;
		} else if (k == KeyEvent.VK_Y && editing) {
			//Shift tileIDs;
			int from = Integer.parseInt(JOptionPane.showInputDialog("Shift tilesIDs froom what and up?"));
			int magnitude = Integer.parseInt(JOptionPane.showInputDialog("Shift how much?"));
			for (int row = 0; row < map.length; row++) {
				for (int col = 0; col < map[0].length; col++) {
					if (map[row][col] >= from) {
						map[row][col] += magnitude;
					}
				}
			}
			for (Decoration d : decorations) {
				int tileID2 = d.getTileID();
				if (tileID2 >= from) {
					d.setTileID(tileID2 + magnitude);
				}
			}
		} else if (k == KeyEvent.VK_T && editing) {
			if (editingMode == 1) {
				editingMode = 0;
			} else {
				editingMode = 1;
			}
		} else if (k == KeyEvent.VK_N && editing) {
			//New map
			generateBlankMap();
		} else if (k == KeyEvent.VK_P && editing) {
			//Print out tilemap info file info.
			int[][] tilesetInfo = parseFileUnboundedColumn(tilesetInfoFiles.get(tilesetInfoFiles.size()-1), 1);
			System.out.println(tilesetInfo.length + ":" + tilesetInfo[0].length);
		} else if (k == KeyEvent.VK_1 && editing) {
			stratifyX = !stratifyX;
		} else if (k == KeyEvent.VK_2 && editing) {
			stratifyY = !stratifyY;
		} else if (k == KeyEvent.VK_PERIOD && editing) {
			mouseDown = true;
		} else if (k == KeyEvent.VK_COMMA && editing) {
			System.out.println((x + mouseX) + ":" + (y + mouseY));
		}
		
		lastKeyPress = k;
	}
	
	public void keyReleased(int k) {
		if (k == KeyEvent.VK_X) {
			tileNavDirection = 0;
		} else if (k == KeyEvent.VK_Z) {
			tileNavDirection = 0;
		} else if (k == KeyEvent.VK_PERIOD && editing) {
			mouseDown = false;
		}
	}
	
	protected void generateBlankMap() {
		try {
			int width = Integer.parseInt(JOptionPane.showInputDialog("New map width?"));
			int height = Integer.parseInt(JOptionPane.showInputDialog("New map height?"));
			int defaultFill = Integer.parseInt(JOptionPane.showInputDialog("Default tileID for filling?"));
			decorations = new ArrayList<Decoration>();
			map = new int[height][width];
			for (int row = 0; row < height; row++) {
				for (int col = 0; col < width; col++) {
					map[row][col] = defaultFill;
				}
			}
			
			ArrayList<Integer> temp = new ArrayList<Integer>();
			temp.add(width);
			mapLengths = temp;
			
			generateMapData();
		} catch (NumberFormatException e) {
			System.out.println("Try a new map number.");
		}
	}
	
	protected void exportMapFiles(boolean compress) {
		String fileHeader = "Map Yay\n";
		int columnStart = 0;
		for (int mapNum = 0; mapNum < mapFiles.size(); mapNum++) {
			String file = fileHeader;
			
			int numCol2 = mapLengths.get(mapNum);
			int numRow2 = numRow;
			file += numCol2 + "\n";
			file += numRow2 + "\n";
			
			//Find the longest number.
			int longestNum = 0;
			for (int row = 0; row < numRow2; row++) {
				for (int col = columnStart; col < columnStart + numCol2; col++) {
					if (("" + map[row][col]).length() > longestNum) {
						longestNum = ("" + map[row][col]).length();
					}
				}
			}
			
			for (int row = 0; row < numRow2; row++) {
				String line = "";
				int repetitions = 0;
				String oldTile = "";//For file compression
				for (int col = columnStart; col < columnStart + numCol2; col++) {
					int numLength = ("" + map[row][col]).length();
					String tile = "" + map[row][col] + new String(new char[longestNum + 1 - numLength]).replace("\0", " ");
					
					//Compression
					if (compress) {
						if (col != columnStart && map[row][col-1] == map[row][col]) {
							repetitions++;
						} else if (repetitions != 0) {
							for (int repe = 0; repe < repetitions; repe++) {
								line += oldTile;
							}
							repetitions = 0;
							line += tile;
						} else {
							repetitions = 0;
							line += tile;
						}
						oldTile = tile;
					} else {
						line += tile;
					}
				}
				file += line + "\n";
			}
				
			columnStart += numCol2;
			writeFile(file, "Resources" + mapFiles.get(mapNum));
		}
	}
	
	protected void exportDecorFile() {

		String file = "Decor Yay (xpos, ypos, tileID, layer)\n";
		
		int numCol2 = 4;
		int numRow2 = decorations.size();
		file += numCol2 + "\n";
		file += numRow2 + "\n";
		
		//Find the longest number.
		int longestNum = 0;
		for (int decorNum = 0; decorNum < decorations.size(); decorNum++) {
			Decoration decor = decorations.get(decorNum);
			int num = decor.getx();
			if (("" + num).length() > longestNum) {
				longestNum = ("" + num).length();
			}
			num = decor.gety();
			if (("" + num).length() > longestNum) {
				longestNum = ("" + num).length();
			}
			num = decor.getTileID();
			if (("" + num).length() > longestNum) {
				longestNum = ("" + num).length();
			}
			num = decor.getLayer();
			if (("" + num).length() > longestNum) {
				longestNum = ("" + num).length();
			}
		}
		
		String line = "";
		for (int decorNum = 0; decorNum < decorations.size(); decorNum++) {
			Decoration decor = decorations.get(decorNum);
			
			int num = decor.getx();
			int numLength = ("" + num).length();
			String dataPoint = "" + num + new String(new char[longestNum + 1 - numLength]).replace("\0", " ");
			file += dataPoint;
			
			num = decor.gety();
			numLength = ("" + num).length();
			dataPoint = "" + num + new String(new char[longestNum + 1 - numLength]).replace("\0", " ");
			file += dataPoint;
			
			num = decor.getTileID();
			numLength = ("" + num).length();
			dataPoint = "" + num + new String(new char[longestNum + 1 - numLength]).replace("\0", " ");
			file += dataPoint;
			
			num = decor.getLayer();
			numLength = ("" + num).length();
			dataPoint = "" + num + new String(new char[longestNum + 1 - numLength]).replace("\0", " ");num = decor.getx();
			file += dataPoint;
			
			file += line + "\n";
		}
		
		writeFile(file, "Resources" + decorFile);
	}
	
	public void setMousePos(Point p) {
		mouseX = (int) p.getX() - 1;
		if (stratifyX) {
			mouseX = mouseX - ((mouseX + (x)%(tileSize/2))%(tileSize/2));
		}

		mouseY = (int) p.getY() - 1;
		if (stratifyY) {
			mouseY = mouseY - ((mouseY + (y)%(tileSize/2))%(tileSize/2));
		}
		
    	if (editing && mouseDown) {
			Tile tile = tiles[tileID-1];
			int xOffset = tile.getXOffset();
			int yOffset = tile.getYOffset();
			
    		if (editingMode == 1) {

    			if (removingDecor) {
    				for (Decoration decor : decorations) {
    					Tile tile2 = tiles[decor.getTileID()-1];
    					if (new Rectangle(decor.getx(), decor.gety(), tile2.getWidth(), tile2.getHeight()).contains(new Point(mouseX + x, mouseY + y))) {
    						decorations.remove(decor);
    						break;
    					}
    				}
    				mouseDown = false;
    			} else {
	    			int tempx = (int) (x + mouseX );
	    			int tempy = (int) (y + mouseY - tile.getHeight());
	    			
	    			try {
	    				int layer = Integer.parseInt(JOptionPane.showInputDialog("What layer do you want this decoration?"));
	    				decorations.add(new Decoration(tempx, tempy, tileID, layer));
	    				sortDecor();
	    			} catch (NumberFormatException e) {
	    				
	    			}
					mouseDown = false;
    			}
    		} else if (editingMode == 0) {
    			setMapTile((int)((y + mouseY)/tileSize), (int)((x + mouseX)/tileSize), tileID);			
    		}
    	}
	}

	public void mousePressed(MouseEvent e) {
		mouseDown = true;
	}
	
	public void mouseReleased(MouseEvent e) {
		mouseDown = false;
	}
	
	public void writeFile(String text, String location) {
		PrintWriter writer = null;

		try {
			System.out.println(location);
			writer = new PrintWriter(location, "UTF-8");
		} catch (FileNotFoundException e) {
			System.out.println("Err1 File Not Found");
			return;
		} catch (UnsupportedEncodingException e) {
			System.out.println("Err2 Unsupported file format");
			return;
		}
		writer.write(text);
		writer.close();
	}
}
