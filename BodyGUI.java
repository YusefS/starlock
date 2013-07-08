package starlock;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/** BodyGUI is a view that Has-A Body and adds x,y positional information for screen coordinates. */
public class BodyGUI {

	private int x, y; // x and y will be relative to the central star, not the top left of the screen.
	private int sx, sy; // As above, except these are the coordinates for the solution circle.
	private int minutes; // Why do we duplicate minutes? Because it really helps to keep track of where the view is, and whether we need to update it.
	private Body base; // Has-A relationship with a specific Body.
	
	private BufferedImage img;
	private Color color;
	
	private final double MINUTES_TO_RADIANS = 2.00 * Math.PI / 21600;
	
	/** Set up this object with a pointer to a Body object. */
	public BodyGUI(Body base) {
		this.base = base;
		// Calculate sx and sy -- they will never change.
		// First take into account that 0 degrees is DUE NORTH, NOT DUE EAST! So subtract 5400 arcminutes. Then, convert to radians (multiply by 0.000290...). Finally, scale by distance.
		sx = (int) (Math.cos((base.getSolution()-5400)*MINUTES_TO_RADIANS)*base.getDistance());
		sy = (int) (Math.sin((base.getSolution()-5400)*MINUTES_TO_RADIANS)*base.getDistance()); 
		color = Color.green; // A default color if everything else fails
		// Should load up the appearance data in Body base.
		if (base.getLook().charAt(0) == '#') {
			// If this is a hex RGB string...
			// This function doesn't validate the string! If the length is wrong or something like that we'll have big problems.
			int red = Integer.parseInt(base.getLook().substring(1, 3), 16); // Specify hex string, with 16 as radix
			int green = Integer.parseInt(base.getLook().substring(3, 5), 16);
			int blue = Integer.parseInt(base.getLook().substring(5, 7), 16);
			color = new Color(red, green, blue);
		} else {
			img = StarlockGUI.loadImage(base.getLook());
		}
	}

	// Some getters and setters
	public int getMinutes() {return minutes;}
	public int getDistance() {return base.getDistance();}
	public int getX() {return x;}
	public int getY() {return y;}
	public void setX(int x) {this.x = x;}
	public void setY(int y) {this.y = y;}
	
	/** Calculates and sets the x,y position of the body based on its distance and minutes. (Zero, zero is origin) */
	public void calcLoc() {
		// First take into account that 0 degrees is DUE NORTH, NOT DUE EAST! So subtract 5400 arcminutes. Then, convert to radians (multiply by 0.000290...). Finally, scale by distance.
		x = (int) (Math.cos((minutes-5400)*MINUTES_TO_RADIANS)*base.getDistance());
		y = (int) (Math.sin((minutes-5400)*MINUTES_TO_RADIANS)*base.getDistance());
	}

	/**
	 * Prints the x and y coordinates.
	 */
	public String toString(){
		String ret = "";
		ret += "X = " + x + ", Y = " + y;
		return ret;
	}
	
	/** Call this function to update the visuals to match the model. Delta is how many arcminutes to rotate on this cycle, and thus dictates how fast the visuals are going to change to match the model. */
	public int update(int delta) {
		int diff = base.getMinutes() - minutes; // How far has the model deviated from this view?
		
		// The next two lines determine the shortest path to rotate. I don't want a planet rotating 315 degrees around the star when 45 degrees in the opposite direction will do it.
		if (diff <= -Starlock.MINUTES/2) {diff += Starlock.MINUTES;}
		if (diff > Starlock.MINUTES/2) {diff -= Starlock.MINUTES;}
		
		// If we need to rotate clockwise, then let's do so, but no more than we have to -- we don't want to overshoot. Always remember to wrap minutes to between 0 and 21599 afterwards.
		if (diff > 0) {
			minutes = Starlock.pmod(minutes + Math.min(delta, diff), Starlock.MINUTES);
			calcLoc();
		}
		// Rotating counter-clockwise.
		else if (diff < 0) {
			minutes = Starlock.pmod(minutes - Math.min(delta, -diff), Starlock.MINUTES);
			calcLoc();
		}
		// If diff == 0 we don't need to do anything, because we are N'sync.
		return diff;
	}
	
	
	/**
	 * Draw this graphical body
	 * @param g -- the graphics context
	 * @param cx -- x and y are the center of the screen, necessary because planets are oriented relative to this.
	 * @param cy
	 */
	public void paint(Graphics g, int cx, int cy) {
		// Draw the orbit
		g.setColor(Color.GRAY);
		int d = base.getDistance();
		g.drawOval(cx - d, cy - d, 2*d, 2*d);
		
		// Draw the solution circle
		if (base.isSolved()) {g.setColor(Color.GREEN);}
		else {g.setColor(Color.lightGray);}
		g.drawOval(cx + sx - 12, cy + sy - 12, 23, 23);
		
		if (img != null) {
			// If there's an image, let's draw it
			g.drawImage(img, x, y, x + img.getWidth(), y + img.getHeight(), 0, 0, img.getWidth(), img.getHeight(), null);
		}
		else {
			// Draw the body with primitives
			g.setColor(color);
			g.fillOval(cx + x - 10, cy + y - 10, 20, 20);
		}		
	}
	
	
	// End of Class
}
