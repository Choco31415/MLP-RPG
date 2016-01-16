package GameState;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import Main.GamePanel;

public class MenuState extends GameState {

	protected BufferedImage background;
	protected int level = 0;
	protected boolean isGAactive;
	protected Object preferences = null;
	protected int selection = 0;
	protected String[] extraText;
	protected String[] options;
	protected String menuName;
	
	public MenuState(GameStateManager gsm_) {
		gsm = gsm_;
		
		try {
			background = ImageIO.read(getClass().getResourceAsStream("/Backgrounds/sky.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		goToMenu();
	}
	
	private void goToMenu() {
		extraText = new String[]{};
		menuName = "Menu";
		options = new String[]{"Play", "Instructions", "Developer Info", "Quit"};
	}
	
	public void reset() {
		return;
	}

	public void end() {
		return;
	}

	public boolean update() {
		return false;
	}

	public void draw(Graphics2D g) {
		g.drawImage(background, 0, 0, null);
		drawCenteredString(g, "Platformer Tales", GamePanel.WIDTH/2, 30, new Font("Times New Roman", 0, 20), Color.BLACK);
		
		int y = 60;
		for (String line : extraText) {
			drawCenteredString(g, line, GamePanel.WIDTH/2, y, new Font("Arial", 0, 12), Color.black);
			y += 18;
		}
		for (int i = 0; i < options.length; i++) {
			Color buttonColor;
			if (i == selection && options.length > 1) {
				buttonColor = Color.red;
			} else {
				buttonColor = Color.black;
			}
			
			drawCenteredString(g, options[i], GamePanel.WIDTH/2, y, new Font("Arial", 0, 12), buttonColor);
			y += 18;
		}
	}
	
	private void drawCenteredString(Graphics2D g, String text, int x, int y, Font font, Color color) {
		Font oldFont = g.getFont();
		
        //Set font
		g.setFont(font);
		
		drawCenteredString(g, text, x, y, color);
        
        g.setFont(oldFont);
	}
	
	
	private void drawCenteredString(Graphics2D g, String text, int x, int y, Color color) {
		
		//Center text
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D r = fm.getStringBounds(text, g);
        int newX = (GamePanel.WIDTH - (int) r.getWidth()) / 2;
        
		//Set color
		g.setColor(color);
		
		//Draw
        g.drawString(text, newX, y);
	}

	public void keyPressed(int k) {
		if (k == KeyEvent.VK_UP) {
			selection--;
			if (selection < 0) {
				selection = options.length-1;
			}
		} else if (k == KeyEvent.VK_DOWN) {
			selection++;
			if (selection == options.length) {
				selection = 0;
			}
		} else if (k == KeyEvent.VK_ENTER) {
			selectionMade();
		}
	}
	
	private void selectionMade() {
		boolean setSelection = true;
		switch (menuName) {
			case "Menu":
				if (selection == 0) {
					gsm.setState(1);
				} else if (selection == 1) {
					menuName = "Instructions";
					extraText = new String[]{"WASD - Move", "Arrow Keys - Move", "Z - Interact", ""};
					options = new String[]{"Hit enter to go back"};
				} else if (selection == 2) {
					String help = "Developer Controls:";
					String[] lines = new String[]{ "P O - Print out player position", ", - Print mouse position", ".  - Click", "1/2 - Stratify on the X/Y axis", "R - Remove decor", "Y - Shift Tile ID's", "U - Enter bounding box edit mode", "P - Print the number of tiles in the tileset", "N - Generate New Map", "P Q - Enter and exit edit mode", "Z/X - Decrease/Increase tile ID", "E - Export", "T - Toggle decoration editing"};
					Arrays.sort(lines);
					for (String line : lines) {
						help += "\n" + line;
					}
					
					System.out.println(help);
					writeFile(help, "Developer Tools.txt");
					setSelection = false;
				} else if (selection == 3) {
					System.exit(0);
				}
				break;
			case "Instructions":
				goToMenu();
				break;
			default:
				throw new Error();
		}
		
		if (setSelection) {
			selection = 0;
		}
	}
	
	public void playerMenu() {
		menuName = "Player";
		options = new String[]{"Player", "Computer"};
	}

	public void keyReleased(int k) {
		
	}
	
	public void setMousePos(Point p) {
		
	}
	
	public void mousePressed(MouseEvent e) {
		
	}
	
	public void mouseReleased(MouseEvent e) {

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
