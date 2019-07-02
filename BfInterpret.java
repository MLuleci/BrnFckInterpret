import java.io.*;
import java.util.*;

public class BfInterpret {
	private static final int DATA_ARR_SIZE = 30000;
	private byte[] m_mem;
	private int m_mptr;
	private ArrayList<Character> m_prog;
	private int m_pptr;

	/**
	 * Initialize instance variables.
	*/
	public BfInterpret()
	{
		m_mem = null;
		m_mptr = 0;
		m_prog = null;
		m_pptr = 0;
	}

	/** 
	 * Reads given Brainf*ck file from disk into memory, fallback to Repl() on error.
	 * @param file Input filename, must end with ".bf"
	*/
	public void Load(String file)
	{
		Init();
		if (file.isEmpty() || !file.endsWith(".bf")) {
			System.err.format("Error: Invalid filename '%s'\n", file);
			Repl();
		} else {
			// Read file into memory
			String in = new String();
			try {
				FileReader fr = new FileReader(file);
				while (fr.ready()) {
					char ch = (char) fr.read();
					in = in.concat(String.valueOf(ch));
				}
				fr.close();
			} catch (Exception ex) {
				System.err.format("Error: %s\n", ex.getMessage());
				Repl();
			}
			Scrub(in);
		}
	}

	/**
	 * Clears program memory and program counter.
	*/
	public void Unload()
	{
		m_prog = new ArrayList<Character>();
		m_pptr = 0;
	}

	/**
	 * Clears data memory and data pointer.
	*/
	public void Init() 
	{
		m_mem = new byte[DATA_ARR_SIZE];
		m_mptr = 0;	
	}

	/**
	 * Enters an infinite Read, Execute, Print Loop (REPL).
	 * In addition to standard Brainf*ck tokens users can also input:
	 * 	'#' - Exits the loop
	 * 	'*' - Clears data memory
	 *	'!' - Repeats last input
	 * The tokens listed above must be input alone in order to be interpreted.
	*/
	public void Repl()
	{
		String last = new String();
		while (true) {
			String in = System.console().readLine("> ");

			// Parse special tokens
			if (in.equals("#")) {
				break;
			} else if (in.equals("*")) {
				Init();
				System.out.println("OK");
				continue;
			} else if (in.equals("!")) {
				in = last;
			}

			// Load input into memory then execute
			in = Scrub(in);
			Execute();
			if (in.indexOf('.') != -1) // Print extra newline if input had print(s)
				System.out.print('\n');
			System.out.println(in);
			last = in;
		}
	}

	/**
	 * Helper function that validates the input string of tokens
	 * before loading them into program memory. Handles bracket matching
	 * and returns the final valid string.
	 * @param in Input string of tokens
	 * @return Valid string of tokens
	*/
	private String Scrub(String in)
	{
		Unload();

		// Sieve valid okens and check for matching brackets
		m_prog.ensureCapacity(in.length());
		Stack<Character> bracket = new Stack<Character>();
		for (int i = 0; i < in.length(); i++) {
			char ch = in.charAt(i);
			switch (ch) {	
				case ']':
					if (!bracket.empty()) {
						bracket.pop();
						m_prog.add(ch);
					} else {
						bracket.push(ch);
						i = in.length();
					}
					break;
				case '[':
					bracket.push(ch);
					m_prog.add(ch);
					break;
				case '>':
				case '<':
				case '+':
				case '-':
				case '.':
				case ',':
					m_prog.add(ch);
					break;
				default:
					continue;
			}
		}
		if (!bracket.empty()) {
			System.err.println("Error: Mismatching brackets");
			Repl();
		}

		// Create string from token list
		String ret = new String();
		for (Character ch : m_prog)
			ret = ret.concat(ch.toString());
		return ret;
	}

	/**
	 * Interprets and executes tokens in program memory.
	 * Must be called after either Repl() or Load().
	*/
	public void Execute()
	{
		if (m_prog == null) {
			System.err.println("Error: No program loaded");
			return;
		}
		while (m_pptr < m_prog.size()) { // Loop until program counter is past the end
			char ch = m_prog.get(m_pptr).charValue();
			switch (ch) {
				case '>':
					m_mptr++;
					if (m_mptr > DATA_ARR_SIZE)
						m_mptr = 0;
					break;
				case '<':
					m_mptr--;
					if (m_mptr < 0)
						m_mptr = DATA_ARR_SIZE;
					break;
				case '+':
					m_mem[m_mptr]++;
					break;
				case '-':
					m_mem[m_mptr]--;
					break;
				case '.':
				{
					char c = (char) m_mem[m_mptr];
					System.out.print(c);
					break;
				}
				case ',':
				{
					try {
						byte c = (byte) System.in.read();
						m_mem[m_mptr] = c;
					} catch (Exception ex) {
						System.err.format("Error: %s\n", ex.getMessage());
						return; // Abort execution on read error
					}
					break;
				}
				
				// For '[' & ']' loop over tokens until the other is reached
				case '[':
					if (m_mem[m_mptr] == 0) {
						do {
							m_pptr++;
						} while (m_prog.get(m_pptr).charValue() != ']');
					}
					break;
				case ']':
					if (m_mem[m_mptr] != 0) {
						do {
							m_pptr--;
						} while (m_prog.get(m_pptr).charValue() != '[');
					}
					break;
			}
			m_pptr++;
		}
	}

	public static void main(String[] args)
	{
		BfInterpret bf = new BfInterpret();
		if (args.length > 0) {
			bf.Load(args[0]);
			bf.Execute();
		} else {
			bf.Init();
			bf.Repl();
		}
	}
}