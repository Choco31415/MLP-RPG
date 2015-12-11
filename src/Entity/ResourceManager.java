package Entity;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class ResourceManager {

	ArrayList<String> imageNames = new ArrayList<String>();
	ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
	
	public ResourceManager() {
		//loadImage("/Sprites/Background/Clouds.png", "Clouds");
		loadImage("/Sprites/Pony/Fluttershy.png", "Fluttershy");
		loadImage("/Sprites/Player/player.png", "Player");
	}
	
	public void loadImage(String path, String name) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(getClass().getResourceAsStream(path));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Err");
			e.printStackTrace();
		}
		
		imageNames.add(name);
		images.add(image);
	}
	
	public boolean doesImageExist(String name) {
		return imageNames.contains(name);
	}
	
	public BufferedImage getImage(String name) {
		int index = imageNames.indexOf(name);
		if (index == -1) {
			throw new Error();
		}
		return images.get(index);
	}
	
	public BufferedImage getImage(int index) {
		if (index < 0 || index >= images.size()) {
			throw new Error();
		}
		return images.get(index);
	}
}
