package starlock;

/**
 * Starlock is a project born out of an interest in predicting astronomical events in a fictional universe.
 * Now, it's not constrained to just that universe, and can be used as a fun puzzle.
 * @author Yusef Shari'ati
 *
 */
public class Starlock {

	private String title; // The name of this puzzle level
	private int year; // The current year.
	private int conjunction; // The first year at which all bodies are aligned at 0 minutes rotation.
							 // Note that 0 minutes rotation indicates straight up on the screen.
							 // 10800 minutes would be 180 degrees, or straight down towards the bottom of the screen
							 // 5400 minutes would be 90 degrees to the right, clockwise you see.
	
	private Body[] planets; // An array of all moving bodies in the system
	private int numBodies; // The number of initialized rotating objects in our puzzle, starts at 0.
	
	public static final int MINUTES = 21600; // There are 21,600 minutes in a full 360 degrees
	
	/**
	 * A quick test of the Starlock class. Creates a number of Bodies and then prints out their rotations.
	 */
	public static void main(String[] args) {
		Starlock SL = new Starlock();
		// Add some arbitrary planets here
		SL.addBody(20, 1);
		SL.addBody(40, 2);
		SL.addBody(60, 3);
		SL.addBody(80, 4);
		SL.addBody(100, 5);
		SL.addBody(120, 6);
		SL.addBody(140, 7);
		
		System.out.println("Starlock - TEXT VERSION\n");
		SL.changeYear(5);
		System.out.println(SL);	
	}

	public Starlock(String title, int conjunction){
		this.title = title;
		this.conjunction = conjunction;
		year = 0;
		planets = new Body[2];
	}

	public Starlock(){
		numBodies = 0; // We haven't made any planets yet.
		year = 0; // The system begins at year 0.
		conjunction = 0; // This is the year when all planets are aligned. Here, all planets start aligned (in conjunction).
		title = "Default"; // A default title.
		// Initialize space for some celestial bodies (space for at least two)
		planets = new Body[2];
	}
	
	/** Add a new planet to the system, with distance (pixels), and orbital period (in years). If the array isn't large
	 *  enough, more space is allocated. */
	public void addBody(int distance, int period){
		// Is there space to add this body?
		if (planets.length <= numBodies) {
			// We need to make more space
			Body[] temp = planets; // A pointer to the old array
			planets = new Body[numBodies*2]; // Double the size of the array
			// We also need to copy over everything into the new array
			System.arraycopy(temp, 0, planets, 0, temp.length);
			// Dispose of old array
			temp = null;
		}
		
		// We're good to go, let's really create the new body
		planets[numBodies] = new Body(distance, period);
		numBodies++;
	}
	
	/**
	 * Add a new planet to the system, initializing it with the given variables.
	 * @param distance
	 * @param period
	 * @param solution
	 * @param look
	 */
	public void addBody(int distance, int period, int solution, String look) {
		addBody(distance, period);
		planets[numBodies-1].setSolution(solution);
		planets[numBodies-1].setLook(look);
	}
	
	/** Returns the current number of initialized and active bodies. */
	public int getNumBodies() {
		return numBodies;
	}
	
	/** Removes all Bodies from the system. */
	public void clearBodies(){
		for (int i = 0; i < numBodies; i++){
			planets[i] = null;
		}
		numBodies = 0;
	}
	
	/** Sets the year of the system to any arbitrary number, and rotates all Bodies to their position at that time. */
	public void changeYear(int year){
		this.year = year;
		int delta = year - conjunction; // The number of years until the conjunction
		int m = 0;
		// Now, or every Body in the system...
		for (int i = 0; i < numBodies; i++){
			// What rotation (in minutes) will this Body be at?
			// Just see how many rotations we have made, given the period.
			m = MINUTES * pmod(delta, planets[i].getPeriod()) / planets[i].getPeriod();
			planets[i].setMinutes(m);
		}
	}
	
	/** Positive Modulus. Returns a % b, but always positive. This has the effect of constraining the result to a number between 0 and (b - 1), with wrapping. */
	public static int pmod(int a, int b){
		return (((a % b) + b) % b);
		// Ex. -60 % 25 = -10; -10 + 25 = 15; 15 % 25 = 15; Returns 15.
	}
	
	/** Prints the current state of all Bodies in the system. */
	public String toString(){
		String out = "";	
		for (int i = 0; i < numBodies; i++){
			out += "Planet " + i + "; Minutes = " + planets[i].getMinutes() + "\n";
		}
		return out;
	}
	
	public int getYear() {
		return year;
	}
	
	/** Returns the array of all Body objects in the system. */
	public Body[] getBodies() {
		return planets;
	}
	
	// A few more important getters/setters
	public int getConjunctionYear() {return conjunction;}
	public void setConjunctionYear(int c) {conjunction = c;}
	public String getTitle() {return title;}
	public void setTitle(String s) {title = s;}
	
	/**
	 * Checks to see if the current puzzle is solved -- i.e. all planets are in their circles.
	 * @return
	 */
	public Boolean isSolved(){
		for (int i = 0; i < numBodies; i++) {
			if (!planets[i].isSolved()) {return false;}
		}
		return true;
	}
}
