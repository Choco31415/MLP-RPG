package GameState;

import java.awt.Point;
import java.awt.event.MouseEvent;

public abstract class GameState {
	
	protected GameStateManager gsm;
	
	public abstract void reset();
	public abstract void end();
	public abstract boolean update();
	public abstract void draw(java.awt.Graphics2D g);
	public abstract void keyPressed(int k);
	public abstract void keyReleased(int k);
	public abstract void setMousePos(Point p);
	public abstract void mousePressed(MouseEvent e);
	public abstract void mouseReleased(MouseEvent e);
}
