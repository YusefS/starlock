
package starlock;

/** 
 * A Body is a generic model representing any object orbiting in an astronomical system. It could be a planet, an asteroid 
 * whatever you like so long as it's a uniformly circular and regular orbit.
 * @author Yusef Shari'ati
 */
public class Body {

	private int distance; // The distance, in pixels, from the central star.
	private int period; // The number of years it takes this body to make one full rotation about the central star.
	private int minutes; // The current angle, in minutes (out of 21600 for a full circle).
	private int solution; // The angle, in minutes, at which the solution is located (the target location which solves this particular planet).
	private String look; // The appearance of this Body. Can be a hex color (e.g. #FF00FF) or a file reference (e.g. "venus.png").
	
	public Body(int d, int p){
		distance = d;
		period = p;
		minutes = 0;
		solution = 0;
		look = "#00FF00"; //Default to a green color
	}
	
	public Body(int d, int p, int m){
		distance = d;
		period = p;
		solution = 0;
		minutes = Math.max(Math.min(m, 21599), 0); // Positive number between 0 and 21599
		look = "#00FF00"; //Default to a green color
	}

	public Body(int d, int p, int m, String ref){
		this(d, p, m);
		look = ref; 
	}
	
	public int getDistance() {return distance;}
	public int getPeriod() {return period;}
	public int getMinutes() {return minutes;}
	public String getLook() {return look;}
	public void setLook(String look) {this.look = look;}
	public int getSolution() {return solution;}
	
	public void setMinutes(int m) {
		// This will even work with negative minutes -- will make positive.
		minutes = Starlock.pmod(m, 21600); // Should constrain the variable to between 0 and 21599, with wrapping.
	} 
	
	public void setSolution(int m) {
		solution = Starlock.pmod(m,  21600);
	}
	
	/** Returns true if the planet is at the correct location.
	 */
	public Boolean isSolved() {
		if (minutes == solution) {return true;}
		return false;
	}
}
