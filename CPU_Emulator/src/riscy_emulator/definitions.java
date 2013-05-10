package riscy_emulator;

import java.io.File;

/**
 * @author Josh Hebb
 *
 *	Simple class with constants of the opcodes, opex codes and the path to the binary file.
 */

public class definitions 
{
	
	/* Opcodes */
	public static final String HALT = "000000";
	public static final String ADDI = "000100";
	public static final String BR 	= "000110";
	public static final String ANDI = "001100";
	public static final String BGE 	= "001110";
	public static final String ORI 	= "010100";
	public static final String STW 	= "010101";
	public static final String BLT 	= "010110";
	public static final String LDW 	= "010111";

	public static final String BNE 	= "011110";
	public static final String BEQ 	= "100110";
	public static final String R_TYPE = "111010";
	
	/* Opex Codes */
	public static final String AND 	= "001110";
	public static final String OR 	= "010110";
	public static final String ADD 	= "110001";
	public static final String SUB 	= "111001";

	
	public static final String path = System.getProperty("user.dir") + "//listadd_little_endian.bin";
	public static final String file_path = path.replace("\\", "//");

}
