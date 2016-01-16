package TileMap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import Entity.MapObject;

/*
 * Layer0 - images to lay on top of the map
 * Layer1 - images that sort and overlap with map objects
 * 
 * BB:
 * Include "ignore" in the tag to have the BB be ignored for collision purposes
 */
public class TileMapWithBB extends TileMap {

	String bbFile;
	
	int lastX;
	int lastY;
	
	boolean placingBB = false;
	boolean dontUpdateMouse = false;
	
	private ArrayList<BoundingBox> boundingBoxes = new ArrayList<BoundingBox>();
	
	public TileMapWithBB(int tileSize_) {
		super(tileSize_);
	}
	
	public ArrayList<BoundingBox> getBoundingBoxes() {
		return boundingBoxes;
	}
	
	public BoundingBox getBoundingBoxByTag(String tag) {
		for (int i = 0; i < boundingBoxes.size(); i++) {
			if (boundingBoxes.get(i).getTag().equals(tag)) {
				return boundingBoxes.get(i);
			}
		}
		return null;
	}
	
	public ArrayList<BoundingBox> getBoundingBoxesWhoseTagContains(String st) {
		ArrayList<BoundingBox> toReturn = new ArrayList<BoundingBox>();
		
		for (BoundingBox bb : boundingBoxes) {
			if (bb.getTag().contains(st)) {
				toReturn.add(bb);
			}
		}
		
		return toReturn;
	}
	
	public ArrayList<BoundingBox> getBoundingBoxesWhoseTagContains(ArrayList<String> sts) {
		ArrayList<BoundingBox> toReturn = new ArrayList<BoundingBox>();
		
		for (BoundingBox bb : boundingBoxes) {
			boolean success = true;
			for (String st : sts) {
				if (!bb.getTag().contains(st)) {
					success = false;
				}
			}
			if (success) {
				toReturn.add(bb);
			}
		}
		
		return toReturn;
	}
	
	public int getBoundingBoxIndex(String tag) {
		for (int i = 0; i < boundingBoxes.size(); i++) {
			if (boundingBoxes.get(i).getTag().equals(tag)) {
				return i;
			}
		}
		return -1;
	}
	
	/*
	 * Load map and decor files given they follow a naming convention.
	 */
	public void createNewRoomFilesIfNeeded(String sceneName) {
		File folderLoc = new File("Resources/Maps/" + sceneName + "/");

		// if the directory does not exist, create it
		if (!folderLoc.exists()) {
			folderLoc.mkdir();
			String map2 = "Map Yay\n2\n2\n1 1\n1 1";
			writeFile(map2, "Resources/Maps/" + sceneName + "/room.txt");
			String decor = "Decor Yay (xpos, ypos, tileID, layer)\n0\n0";
			writeFile(decor, "Resources/Maps/" + sceneName + "/decor.txt");
			String bb = "Bounding Boxes Yay (xpos ypos xlen ylen tag)\n0\n0";
			writeFile(bb, "Resources/Maps/" + sceneName + "/bounding boxes.txt");
		}
	}
	
	@Override
	public void loadSceneLazy(String sceneName, int numMaps) {
		if (numMaps <= 0) {
			throw new Error();
		}
		ArrayList<String> mapFiles_ = new ArrayList<String>();
		
		if (numMaps == 1) {
			mapFiles_.add("/Maps/" + sceneName + "/room.txt");
		} else {
			for (int i = 0; i < numMaps; i++) {
				mapFiles_.add("/Maps/" + sceneName + "/room_" + (i+1) + ".txt");
			}
		}
		
		loadMaps(mapFiles_);
		mapFiles = mapFiles_;
		loadDecor("/Maps/" + sceneName + "/decor.txt");
		loadBB("/Maps/" + sceneName + "/bounding boxes.txt");
	}
	
	protected void loadBB(String bbFile_) {
		bbFile = bbFile_;
		boundingBoxes = new ArrayList<BoundingBox>();
		
		String[][] tempData = parseFileAsStrings(bbFile_, 1, true);
		
		for (int row = 0; row < tempData.length; row++) {
			int[] tempData2 = new int[4];
			String tag = "null";
			for (int col = 0; col < tempData[0].length; col++) {
				try {
					tempData2[col] = Integer.parseInt(tempData[row][col]);
				} catch (NumberFormatException e) {
					if (!tempData[row][col].equals(tempData[row][col-1])) {
						tag = tempData[row][col];
					}
					break;
				}
			}
			boundingBoxes.add(new BoundingBox(new Rectangle(tempData2[0], tempData2[1], tempData2[2], tempData2[3]), tag));
		}
	}
	
	@Override
	public void draw(Graphics2D g, int gWidth, int gHeight, int leftBias, int rightBias, int topBias, int bottomBias, ArrayList<MapObject> objects) {
		super.draw(g, gWidth, gHeight, leftBias, rightBias, topBias, bottomBias, objects);
		
		if (editing) {
			if (editingMode == 2) {
				Color prevColor = g.getColor();
				g.setColor(Color.RED);
				g.setStroke(new BasicStroke(1));
				
				for (BoundingBox box : boundingBoxes) {
					Rectangle rec = box.getRectangle();
					Rectangle tempDrawRec = new Rectangle((int)(rec.getX() - x), (int)(rec.getY() - y), (int)rec.getWidth(), (int)rec.getHeight());
					g.draw(tempDrawRec);
				}
				
				if (mouseDown && placingBB) {
					int width = mouseX + x - lastX;
					int tempx;
					if (width < 0) {
						width *= -1;
						tempx = mouseX + x;
					} else {
						tempx = lastX;
					}
					
					int height = mouseY + y - lastY;
					int tempy;
					if (height < 0) {
						height *= -1;
						tempy = mouseY + y;
					} else {
						tempy = lastY;
					}

					g.draw(new Rectangle(tempx-x, tempy-y, width, height));
				}
				g.setColor(prevColor);
			}
		}
	}
	
	@Override
	public void toggleEditing() {
		super.toggleEditing();
	}
	
	@Override
	public void keyPressed(int k) {
		if (k == KeyEvent.VK_E && editing) {
			//Export 
			String[] options = new String[] { "Cancel", "Tilemap", "Tilemap (compressed)", "Decor", "BB"};
		    int choice = JOptionPane.showOptionDialog(null, "What do you want to export?", null,
		            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
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
				case 4:
					exportBBFile();
				default:
					break;
			}
		} else {
			super.keyPressed(k);
		}
		
		if (k == KeyEvent.VK_U && editing) {
			if (editingMode == 2) {
				editingMode = 0;
			} else {
				editingMode = 2;
			}
		} else if (k == KeyEvent.VK_H && editing) {
			createNewRoomFilesIfNeeded("pie");
		}
	}
	@Override
	public void setMousePos(Point p) {
		if (!dontUpdateMouse) {
			super.setMousePos(p);
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		lastX = mouseX + x;
		lastY = mouseY + y;
		super.mousePressed(e);
		
		if (editingMode == 2) {
			boolean clickedBB = false;
			int bbID = -1;
			for (int i = 0; i < boundingBoxes.size(); i++) {
				BoundingBox bb = boundingBoxes.get(i);
				if (bb.getRectangle().contains(new Point(mouseX + x, mouseY + y))) {
					clickedBB = true;
					bbID = i;
					break;
				}
			}
			if (clickedBB) {
				placingBB = false;
				//Ask if you want to remove bb.
				String[] options = new String[] { "Cancel", "Re-tag", "Delete", "Change Rect"};
			    int choice = JOptionPane.showOptionDialog(null, "What do you want to do with this rectangle?", null,
			            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[2]);
				if (choice == 3) {
					options = new String[] { "xPos", "yPos", "Width", "Height"};
				    int choice2 = JOptionPane.showOptionDialog(null, "What value do oyu want to change?", null,
				            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[3]);
					options = new String[] { "Change", "Set"};
				    int choice3 = JOptionPane.showOptionDialog(null, "What do you want to do with this value?", null,
				            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
				    
				    
					String question = "";
					if (choice3 == 0) {
						question = "Change by what?";
					} else {
						question = "Set to what?";
					}
				    int amount = Integer.parseInt(JOptionPane.showInputDialog(question));
				    
				    //Get rectangle
				    Rectangle rect = boundingBoxes.get(bbID).getRectangle();
				    
				    switch (choice2) {
				    	case 0:
				    		if (choice3 == 0) {
				    			rect.setLocation((int)(rect.getX() + amount), (int)(rect.getY()));
				    		} else {
				    			rect.setLocation((int)(amount), (int)(rect.getY()));
				    		}
				    		break;
				    	case 1:
				    		if (choice3 == 0) {
				    			rect.setLocation((int)(rect.getX()), (int)(rect.getY() + amount));
				    		} else {
				    			rect.setLocation((int)(rect.getX()), (int)(amount));
				    		}
				    		break;
				    	case 2:
				    		if (choice3 == 0) {
				    			rect.setSize((int)(rect.getWidth() + amount), (int)(rect.getHeight()));
				    		} else {
				    			rect.setSize((int)(amount), (int)(rect.getHeight()));
				    		}
				    		break;
				    	case 3:
				    		if (choice3 == 0) {
				    			rect.setSize((int)(rect.getWidth()), (int)(rect.getHeight() + amount));
				    		} else {
				    			rect.setSize((int)(rect.getWidth()), (int)(amount));
				    		}
				    		break;
				    	default:
				    		break;
				    }
				    
				    boundingBoxes.get(bbID).setRectangle(rect);
				} else if (choice == 2) {
					boundingBoxes.remove(bbID);
				} else if (choice == 1) {
					String tag = JOptionPane.showInputDialog("Tag name?");
					if (tag.equals("")) {
						tag = "null";
					}
					boundingBoxes.get(bbID).setTag(tag);
				}
			} else {
				placingBB = true;
			}
		}

	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);
		
		if (editing) {
			if (editingMode == 2 && placingBB) {
				mouseDown = true;
				dontUpdateMouse = true;
				
				int width = mouseX + x - lastX;
				int tempx;
				if (width < 0) {
					width *= -1;
					tempx = mouseX + x;
				} else {
					tempx = lastX;
				}
				
				int height = mouseY + y - lastY;
				int tempy;
				if (height < 0) {
					height *= -1;
					tempy = mouseY + y;
				} else {
					tempy = lastY;
				}
				//mouseDown = false;
				
				
				int accept = JOptionPane.showConfirmDialog(null, "Place a rectangle?");
				if (accept != 0) {
					dontUpdateMouse = false;
					mouseDown = false;
					return;
				}
				
				String tag = JOptionPane.showInputDialog("Tag name?");
				if (tag.equals("")) {
					tag = "null";
				}
				
				boundingBoxes.add(new BoundingBox(new Rectangle(tempx, tempy, width, height), tag));
			}
		}
		dontUpdateMouse = false;
		mouseDown = false;
	}
	
	@Override
	public void update() {
		super.update();
	}
	
	protected void exportBBFile() {

		String file = "Bounding Boxes Yay (xpos ypos xlen ylen tag)\n";
		
		int numCol2 = 5;
		int numRow2 = boundingBoxes.size();
		file += numCol2 + "\n";
		file += numRow2 + "\n";
		
		//Find the longest number.
		int longestItem = 0;
		for (int bbNum = 0; bbNum < boundingBoxes.size(); bbNum++) {
			BoundingBox bb = boundingBoxes.get(bbNum);
			int num = (int)bb.getRectangle().getX();
			if (("" + num).length() > longestItem) {
				longestItem = ("" + num).length();
			}
			num = (int)bb.getRectangle().getY();
			if (("" + num).length() > longestItem) {
				longestItem = ("" + num).length();
			}
			num = (int)bb.getRectangle().getWidth();
			if (("" + num).length() > longestItem) {
				longestItem = ("" + num).length();
			}
			num = (int)bb.getRectangle().getHeight();
			if (("" + num).length() > longestItem) {
				longestItem = ("" + num).length();
			}
		}
		
		String line = "";
		for (int bbNum = 0; bbNum < boundingBoxes.size(); bbNum++) {
			BoundingBox bb = boundingBoxes.get(bbNum);
			
			int num = (int)bb.getRectangle().getX();
			int itemLength = ("" + num).length();
			String dataPoint = "" + num + new String(new char[longestItem + 1 - itemLength]).replace("\0", " ");
			file += dataPoint;
			
			num = (int)bb.getRectangle().getY();
			itemLength = ("" + num).length();
			dataPoint = "" + num + new String(new char[longestItem + 1 - itemLength]).replace("\0", " ");
			file += dataPoint;
			
			num = (int)bb.getRectangle().getWidth();
			itemLength = ("" + num).length();
			dataPoint = "" + num + new String(new char[longestItem + 1 - itemLength]).replace("\0", " ");
			file += dataPoint;
			
			num = (int)bb.getRectangle().getHeight();
			itemLength = ("" + num).length();
			dataPoint = "" + num + new String(new char[longestItem + 1 - itemLength]).replace("\0", " ");
			file += dataPoint;

			String tag = bb.getTag();
			itemLength = tag.length();
			dataPoint = "" + tag;
			file += dataPoint;
			
			file += line + "\n";
		}
		
		writeFile(file, "Resources" + bbFile);
	}
}
