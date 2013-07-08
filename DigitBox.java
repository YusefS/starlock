package starlock;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * DigitBox is a visual area on the screen which displays a single numeral (albeit not an Arabic one!). It acts very similarly to one of the digit wheels on a bicycle lock.
 * @author Yusef Shari'ati
 */
public class DigitBox extends Rectangle {

	private static final long serialVersionUID = 1L;

	private BufferedImage background; // a pointer to the background image. It's loaded elsewhere.
	private Point offset; // A pixel offset into the background image, allowing some variation (so not each DigitBox looks the same, even with the same background image).

	private int value; // The value stored in this digit.
	
	private int mouseState; // Tells us if the mouse is hovering (1), clicking (2), or not (0).
	
	public DigitBox(int x, int y, int width, int height, BufferedImage b){
		super(x, y, width, height);
		value = 0;
		background = b;
		offset = new Point();
		offset.x = (int) (Math.random()*(background.getWidth() - width));
		offset.x = Math.max(offset.x, 0);
		offset.y = (int) (Math.random()*(background.getHeight() - height));
		offset.y = Math.max(offset.y, 0);
	}
	
	public void draw(Graphics G){
		
		if (background != null) {
			G.drawImage(background, x, y, x + width, y + height, offset.x, offset.y, offset.x + width, offset.y + height, null); 
		}
		
		// Draw a lovely outline for our box
		if (mouseState == 1) {G.setColor(Color.green);}
		else {G.setColor(Color.gray);}
		G.drawRect(x, y, width, height);	
	}
	
	// Increments the value in the digit box, wrapping 0-6.
	public void click(){
		value = (value + 1) % 7;
	}
	
	// Getters and setters
	public int getValue() {return value;}
	public void setState(int s) {mouseState = s;}
	
	public void setValue(int v) {value = v;}
}
