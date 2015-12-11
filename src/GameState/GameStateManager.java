package GameState;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import Main.GamePanel;

public class GameStateManager {

	private GameState currentState;
	private int currentStateNumber;
	
	public static final int NUMGAMESTATES = 2;
	public static final int MENUSTATE = 0; //NOT USED
	public static final int RPGSTATE = 1;

	GamePanel gp;
	
	private boolean audioAllowed = false;
	
	private boolean stateSetThisFrame;
	
	public GameStateManager(GamePanel gp_) {
		gp = gp_;
		setState(MENUSTATE);
	}
	public void setState(int state) {
		setState(state, null);
	}
	
	public void setState(int state, Object preferences) {
		if (state == MENUSTATE) {
			currentState = new MenuState(this);
		} else if (state == RPGSTATE) {
			currentState = new RPGState(this);
		}
		
		if (state >= 0 && state < NUMGAMESTATES) {
			currentStateNumber = state;
		} else {
			throw new Error();
		}
	}
	
	public void update() {
		stateSetThisFrame = false;
		try {
			boolean refresh = currentState.update();
			
			if (refresh) {
				resetLevel();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void resetLevel() {
		currentState.reset();
		stateSetThisFrame = true;
	}
	
	public void draw(java.awt.Graphics2D g) {
		if (!stateSetThisFrame) {
			try {
				currentState.draw(g);
			} catch(Exception e) {}
		}
	}
	
	public void keyPressed(int k) {
		currentState.keyPressed(k);
		
		if (k == KeyEvent.VK_E) {
			//Something
		}
	}
	
	
	public void keyReleased(int k) {
		currentState.keyReleased(k);
	}
	
	public void setMousePos(Point p) {
		currentState.setMousePos(p);
	}
	
	public void mousePressed(MouseEvent e) {
		currentState.mousePressed(e);
	}
	
	public void mouseReleased(MouseEvent e) {
		currentState.mouseReleased(e);
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
	
	public int getCurrentStateNumber() { return currentStateNumber; }
	public boolean getAudioAllowed() { return audioAllowed; }
}
