package starlock;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class StarlockGUI extends JPanel implements MouseListener, MouseMotionListener, ActionListener {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		new StarlockGUI();
	}
	
	private final int width = 700; // Width of the window
	private final int height = 800; // Height of the window
	private final int centerX = 350; // This is the center of orbit
	private final int centerY = 350; // 
	private JFrame jWnd;
	
	private ArrayList<Starlock> slLevels;
	private int iLevel; // The currently displayed level.
	
	private BodyGUI[] planets;

	private final int NUM_DIGITS = 5; // The number of digits in the controls.
	
	// Buttons with which to interact
	private DigitBox[] db;
	private DigitBox submit; // Submit button
	
	Timer agT; // Our timer object
	
	// Keep track of the status of the game
	private final int GAME_LOADING = 0; // The application has just started and is still loading resources.
	private final int GAME_PLAYING = 1; // The game is playing.
	private final int GAME_SUCCESS = 2; // The user has solved a level: display a success message until they click or some time has passed.
	private int iGameState = GAME_LOADING;
	
	// Image RESOURCES
	
	private BufferedImage background;
	private BufferedImage cback; // An image for each control button
	private BufferedImage[] numerals; // Each numeral 0-6 is an image
	private BufferedImage success; // When you've won!
	
	// Level data file
	private final String levels_path = "resources/Levels.txt";
	
	// Font
	private Font fStarlock;
	
	public StarlockGUI(){
		jWnd = new JFrame("Starlock");
		jWnd.setResizable(false);
		jWnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jWnd.setLocationRelativeTo(null);
		this.setPreferredSize(new Dimension(width, height));
		this.setBackground(Color.black);
		this.setDoubleBuffered(true);
		this.setFocusable(true);
		this.setFocusTraversalKeysEnabled(false);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		jWnd.add(this); // This StarlockGUI class is a panel that sits inside the frame
		jWnd.pack();
		jWnd.setVisible(true);
		
		fStarlock = new Font("Impact", Font.PLAIN, 20);
		
		loadAllImages(); // Stuff like the background and numerals and things
		setupDigitBoxes(); // Now that numerals are loaded...Create and place the digit box buttons.

		slLevels = new ArrayList<Starlock>();
		// Read in the file and create the levels.
		if (!loadLevels()) {setupDefaultLevel();}
		
		iLevel = 0; // Start on the first level
		beginLevel(); // Go into the current level
		
		agT = new Timer(25, this); // Create and start the timer
		agT.start();
		
		iGameState = GAME_PLAYING; // Start the game now
	}
	
	//========================================================/ Painting Methods /===================================================/
	
	public void paint(Graphics g){
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		
		// Rendering hints make things look pretty!
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHints(rh);
		
		if (iGameState == GAME_LOADING) {return;} //Don't draw anything if we're still initializing
		
		if (background != null) {
			g2d.drawImage(background, 0, 0, null);
		}
		
		// Paint radial lines
		g2d.setColor(Color.gray);
		// + shape
		g2d.drawLine(width/2, 0, width/2, centerY*2);
		g2d.drawLine(0, centerY, width, centerY);
		// x shape
		g2d.drawLine(0, 0, width, centerY*2);
		g2d.drawLine(width, 0, 0, centerY*2);

		paintBodies(g2d);
		paintControls(g2d);
		
		// Paint some text to show what level we're on
		g2d.setFont(fStarlock);
		g2d.setColor(Color.black);
		g2d.drawString("Level: " + slLevels.get(iLevel).getTitle(), 9, 19);
		g2d.setColor(Color.cyan);
		g2d.drawString("Level: " + slLevels.get(iLevel).getTitle(), 10, 20);
		
		if (iGameState == GAME_SUCCESS) {
			g2d.drawImage(success, centerX - 144, centerY, null);
		}
	}

	
	/**
	 * Draws the planets and the central star.
	 * @param g
	 */
	public void paintBodies(Graphics2D g2d){
		
		for (int i = 0; i < planets.length; i++){
			planets[i].paint(g2d, centerX, centerY);
		}
		
		// Draw the sun
		g2d.setColor(Color.yellow);
		g2d.drawOval(centerX - 10, centerY - 10, 20, 20);	
	}
	
	/**
	 * Draws the digit boxes.
	 * @param g
	 */
	public void paintControls(Graphics2D g2d){
		// Paint the digital display
		for (int i = 0; i < NUM_DIGITS; i++) {
			db[i].draw(g2d);
			// Paint numbers on top
			g2d.drawImage(numerals[db[i].getValue()], db[i].x, db[i].y, db[i].x + db[i].width, db[i].y + db[i].height, 0, 0, numerals[i].getWidth(), numerals[i].getHeight(), null);
		}
		// Now paint the "submit" button
		submit.draw(g2d);
		g2d.setColor(Color.pink);
		g2d.drawOval(submit.x + 10, submit.y + 10, submit.width - 20, submit.height - 20);
	}
	

	// Swing Timer will call this every time a cycle is induced!
	@Override
	public void actionPerformed(ActionEvent ae) {
		
		switch (iGameState) {
		case GAME_LOADING:
			// Do nothing.
			break;
		case GAME_PLAYING:
			// Playing the puzzle
			// If the current level is solved then go to the next level.
			Boolean bLevelSolved = true; // Assume we solved the level
			
			for (int i = 0; i < planets.length; i++) {
				// Update the planets and check if they deviate from the model
				if (Math.abs(planets[i].update(50)) > 0) {
					bLevelSolved = false;
				}
				if (!slLevels.get(iLevel).isSolved()) { bLevelSolved = false;}
				if (bLevelSolved) {iGameState = GAME_SUCCESS;} // Signal that we need to move to the next level, but don't do so yet. We do that when the user clicks their assent.
			}		
		case GAME_SUCCESS:
			break;
		default:
			// Do nothing here
		}
				
		repaint(); // This triggers the paint() function above. We need to do this to force a redrawing every cycle.
	}
	
	/** What happens when you click the "submit" button. */
	public void submit(){
		System.out.println("You clicked on the submit button!");
		// Calculate the year; convert from heptal to decimal
		int year = 0; 
		int multiplier = 1;
		// each digit has a multiplier
		// 0th: 1
		// 1st: 7
		// 2nd: 49
		// 3rd: 343
		// etc.
		for (int i = 0; i < NUM_DIGITS; i++){
			year += multiplier * db[i].getValue();
			multiplier *= 7;
		}
		// Boom we're done
		slLevels.get(iLevel).changeYear(year);
	}
	
	//==============================================/ Setup and Loading Functions /==============================/
	
	/**
	 * Loads an image given a file name. It's made static so that other modules can use it; it's nice 'n generic.
	 * @param ref
	 * @return
	 */
	public static BufferedImage loadImage(String ref){
		BufferedImage source = null;
		try {
			source = ImageIO.read(new File(ref));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not load file: " + ref);
			return null;
		}
		return source;
	}

	/**
	 * Loads all image resources needed.
	 */
	public void loadAllImages(){
		background = loadImage("resources/background.png");
		cback = loadImage("resources/control_back.png"); // 340 x 340 px	
		
		numerals = new BufferedImage[7]; // 0-6
		for (int i = 0; i < 7; i++) {
			numerals[i] = loadImage("resources/" + i + ".png");
		}
		
		success = loadImage("resources/success.png");
	}
	
	/**
	 * Loads level data for the game, initializing appropriate structures with that data. Returns false on a failure.
	 */
	public Boolean loadLevels(){
		Path fp = Paths.get(levels_path);
		Charset charset = Charset.forName("UTF-8");
		try (BufferedReader reader = Files.newBufferedReader(fp, charset)) {
			String line = null;
			Starlock SL = new Starlock(); // The current level that we're reading/creating. We create one just in case.
			
			// Now read the whole file, line by line, until we get a null
			while ((line = reader.readLine()) != null) {
				// TODO This whole thing will give serious errors if the file isn't formatted correctly.
				System.out.println(line);
				if (line.startsWith("LEVEL")) {
					// Now...
					// Make a new level. We'll be modifying and adding content to it on further lines in the file
					SL = new Starlock();
					SL.setTitle(line.substring(6));
					slLevels.add(SL);
				} else if (line.startsWith("CONJUNCTION")) {
					// Set conjunction year
					String c = line.substring(12); // Get rid of "CONJUNCTION "
					SL.setConjunctionYear(Integer.parseInt(c)); // Turn that into an int and we're good.
				} else if (line.startsWith("BODY")){
					// Gotta create a new body
					String sp[] = line.substring(5).split(","); // Substring past "BODY " and then split on commas
					int distance = Integer.parseInt(sp[0].trim()); // Gotta trim to get rid of whitespace
					int period = Integer.parseInt(sp[1].trim());
					int solution = Integer.parseInt(sp[2].trim());
					String look = sp[3].trim();
					SL.addBody(distance, period, solution, look);
				} else {
					// Do nothing
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not load Level data.");
			return false;
		}
		
		if (slLevels.isEmpty()) {return false;}
		return true;
	}
	
	/**
	 * If the Levels file cannot be found or loaded, then set up a default puzzle.
	 */
	public void setupDefaultLevel(){
		Starlock SL = new Starlock();
		// When you add a new body, it is automatically placed at 0 degrees
		SL.addBody(30, 2);
		SL.addBody(60, 3);
		SL.addBody(100, 9);
		SL.addBody(150, 11);
		SL.addBody(200, 21);
		SL.addBody(250, 23);
		SL.addBody(300, 49);
		SL.setConjunctionYear(5035); // First conjunction in the year 343
		// Set year to 0, and calculate positions (positions NOT calculated unless you do this). This 'unsolves' the puzzle.
		SL.changeYear(0); // Set year to 0 and calculate positions.
		// Add it to the array list
		slLevels.add(SL);
	}
	
	/**
	 * Takes a level in the array list of levels and builds a visual display of BodyGUIs for it. Also resets digit boxes.
	 */
	public void beginLevel() {
		slLevels.get(iLevel).changeYear(0);
		// Now let's get all these guys and make use of them in our GUI
		Body temp[] = slLevels.get(iLevel).getBodies();
		
		planets = new BodyGUI[slLevels.get(iLevel).getNumBodies()]; // Make our overlay structure the same size
		
		for (int i = 0; i < planets.length; i++) {
			planets[i] = new BodyGUI(temp[i]);
			planets[i].calcLoc();
		}	
		// Clear the digit boxes
		for (int i = 0; i < NUM_DIGITS; i++) {
			db[i].setValue(0);
		}
		submit.setValue(0);
	}
	
	/**
	 * Creates NUM_DIGITS amount of digit boxes in the right location on screen.
	 * @param background
	 */
	public void setupDigitBoxes(){
		db = new DigitBox[NUM_DIGITS];
		int j = 0;
		for (; j < NUM_DIGITS; j++){
			db[j] = new DigitBox(j*70+143, 700, 64, 64, cback);
		}
		submit = new DigitBox(j*70+143, 700, 64, 64, cback);	
	}
	
	//==================================================/ MouseListener Events /==============================================/
	
	@Override
	public void mouseDragged(MouseEvent arg0) {}

	@Override
	public void mouseMoved(MouseEvent e) {
		
		Point p = e.getPoint();
		
		if (iGameState == GAME_PLAYING) {
			
			// Check and see if the mouse is hovering over a control
			for (int i = 0; i < NUM_DIGITS; i++){
				if (db[i].contains(p)){db[i].setState(1);}
				else {db[i].setState(0);}
			}
		
			if (submit.contains(p)) {submit.setState(1);}
			else {submit.setState(0);}
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent e) {

		Point p = e.getPoint();
		
		switch (iGameState) {
		case GAME_LOADING:
			break;
		case GAME_PLAYING:
			// Check and see if we clicked on a number control
			for (int i = 0; i < NUM_DIGITS; i++){
				if (db[i].contains(p)){db[i].click();}
			}			
			// Check and see if we clicked on submit button
			if (submit.contains(p)) {submit();}
			break;
		case GAME_SUCCESS:
			// You clicked! Go on to the next level, but don't go past the end of the array list
			iLevel++;
			if (iLevel >= slLevels.size()) {
				// Gotta quit
				jWnd.dispose();
				System.exit(0);
			}
			beginLevel();
			iGameState = GAME_PLAYING;
			break;
		default:	
		}		
	}

	@Override
	public void mouseReleased(MouseEvent e) {}

// End of CODE
}
