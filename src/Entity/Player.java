package Entity;

import java.awt.Graphics2D;

import GameState.GameState;
import Main.GamePanel;
import TileMap.TileMap;
import TileMap.TileMapWithBB;

public class Player extends MapObjectAnimatable {

	private int health;
	private int maxHealth;
	private boolean dead = false;
	
	private boolean flinching = false;
	private long flinchTimer;
	private double flinchLength = 1.0;
	
	// player properties
	private double moveSpeed = 2.5;

	private double prev_dx;
	private double prev_dy;
	
	private boolean animationUp = true;
	
	int frame = 0;
	
	public Player(TileMapWithBB tm_, GameState level_) {
		super(tm_, "Fluttershy", 20, 20, 0, -6, level_, 32);
		health = 1;
	}
	
	public int getHealth() { return health; }
	public int getMaxHealth() { return maxHealth; }
	public boolean getDead() { return dead; }
	public double getdy() { return dy; }
	
	public void hit(int damage) {
		if (flinching) {
			return;
		}
		health -= damage;
		if (health <= 0) {
			dead = true;
		}
		flinching = true;
		flinchTimer = System.nanoTime();
	}
	
	private void getNextPosition() {
		prev_dx = dx;
		prev_dy = dy;
		if (right) {
			dx = moveSpeed;
		} else if (left) {
			dx = -moveSpeed;
		} else {
			dx = 0;
		}
		if (up) {
			dy = -moveSpeed;
		} else if (down) {
			dy = moveSpeed;
		} else {
			dy = 0;
		}
	}
	
	public void setCostume() {
		int frame_limit = 6;
		if (prev_dx != dx) {
			frame = frame_limit;
		} else if (prev_dy != dy) {
			frame = frame_limit;
		}
		if (frame >= frame_limit) {
			frame = 0;
			if ((dx == 0) && (dy == 0)) {
				//Test standing still
				if (prev_dy > 0) {
					setCostume(0, 3);
				} else if (prev_dy < 0) {
					setCostume(0, 6);
				} else if (prev_dx != 0) {
					setCostume(0, 0);
					if (prev_dx < 0) {
						facingRight = true;
					} else if (prev_dx > 0) {
						facingRight = false;
					}
				}
			} else {
				//Test moving
				if (dy != 0) {
					facingRight = true;
					if (dy > 0) {
						if (costumeCol == 3) {
							if (animationUp) {
								setCostume(0, 4);
							} else {
								setCostume(0, 5);
							}
							animationUp = !animationUp;
						} else {
							setCostume(0, 3);
						}
					} else {
						if (costumeCol == 6) {
							if (animationUp) {
								setCostume(0, 7);
							} else {
								setCostume(0, 8);
							}
							animationUp = !animationUp;
						} else {
							setCostume(0, 6);
						}
					}
				} else if (dx != 0) {
					if (costumeCol == 0) {
						if (animationUp) {
							setCostume(0, 1);
						} else {
							setCostume(0, 2);
						}
						animationUp = !animationUp;
					} else {
						setCostume(0, 0);
					}
					facingRight = dx < 0;
 				}
			}
		}
	}
	
	/*	public void setCostume() {
		int frame_limit = 5;
		if (prev_dx != dx) {
			frame = frame_limit;
		} else if (prev_dy != dy) {
			frame = frame_limit;
		}
		if (frame >= frame_limit) {
			frame = 0;
			if ((dx == 0) && (dy == 0)) {
				//Test standing still
				if (prev_dy > 0) {
					setCostume(0, 4);
				} else if (prev_dy < 0) {
					setCostume(0, 7);
				} else if (prev_dx != 0) {
					setCostume(0, 0);
					if (prev_dx < 0) {
						facingRight = true;
					} else if (prev_dx > 0) {
						facingRight = false;
					}
				}
			} else {
				//Test moving
				if (dy != 0) {
					if (dy > 0) {
						if (costumeCol == 4) {
							if (animationUp) {
								setCostume(0, 5);
							} else {
								setCostume(0, 6);
							}
							animationUp = !animationUp;
						} else {
							setCostume(0, 4);
						}
					} else {
						if (costumeCol == 7) {
							if (animationUp) {
								setCostume(0, 8);
							} else {
								setCostume(0, 9);
							}
							animationUp = !animationUp;
						} else {
							setCostume(0, 7);
						}
					}
				} else if (dx != 0) {
					setCostume(0, (costumeCol + 1)%4);
					facingRight = dx < 0;
 				}
			}
		}
	}*/
	
	public void update() {
		frame++;
		getNextPosition();
		checkTileMapCollision();
		checkBBCollision();
		setCostume();

		setPosition(xtemp, ytemp);
		
		// check if done flinching
		if(flinching) {
			long elapsed =
				(System.nanoTime() - flinchTimer) / 1000000;
			if(elapsed > 1000*flinchLength) {
				// 1 sec
				flinching = false;
			}
		}
		
		int newX = (int)(x-GamePanel.WIDTH/2);
		int newY = (int)(y-GamePanel.HEIGHT/2 - 20);
		tileMap.setPosition(newX, newY);
		
		for (int[] tile : tilesToDestroy) {
			tileMap.setMapTile(tile[0], tile[1], 0);
			if (tileMap.getType(tile[0]-1, tile[1]) == 0) {
				tileMap.setMapTile(tile[0]-1, tile[1], 0);
			}
		}
		tilesToDestroy.clear();
		
		setMapPosition();
	}
	
	public void draw(Graphics2D g) {
		
		// draw player
		if(flinching) {
			long elapsed =
				(System.nanoTime() - flinchTimer) / 1000000;
			if(elapsed / 100 % 2 == 0) {
				return;
				// Causes player to not be drawn, resulting in "flashing"
			}
		}
		
		super.draw(g);
		
	}
	
	@Override
	public void setLeft(boolean b) {
		super.setLeft(b);
	}
	
	@Override
	public void setRight(boolean b) {
		super.setRight(b);
	}
	
	@Override
	public void setUp(boolean b) {
		super.setUp(b);
	}
	
	@Override
	public void setDown(boolean b) {
		super.setDown(b);
	}
}
