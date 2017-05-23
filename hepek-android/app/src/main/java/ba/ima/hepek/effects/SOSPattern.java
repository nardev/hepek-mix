package ba.ima.hepek.effects;

/**
 * Defines SOS pattern in milliseconds array.
 * It can be useful for different effects.
 * 
 * @author ZeKoU - amerzec@gmail.com
 * @author Gondzo - gondzo@gmail.com
 * @author NarDev - valajbeg@gmail.com
 *
 */
public class SOSPattern {
	
	static final int dot = 100; // Length of a Morse Code "dot" in milliseconds
	static final int dash = 400; // Length of a Morse Code "dash" in milliseconds
	static final int short_gap = 100; // Length of Gap Between dots/dashes
	static final int medium_gap = 400; // Length of Gap Between Letters
	static final int long_gap = 800; // Length of Gap Between Words
	
	private static long[] pattern = { 0, // Start immediately
			dot, short_gap, dot, short_gap, dot, // s
			medium_gap, dash, short_gap, dash, short_gap, dash, // o
			medium_gap, dot, short_gap, dot, short_gap, dot, // s
			long_gap };
	
	
	public static long[] getSOSPattern(){ return pattern; }
	
}