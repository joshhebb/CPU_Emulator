package riscy_emulator;

/**
 * @author Josh Hebb
 * MEM (memory) class, holds the addressable memory in word format (32-bit integers)
 */

public class MEM 
{
	public static int[] addressable_memory = new int[16384];	// Addressable memory (16 384 32-bit words, or 65536 bytes)
	
	/**
	 * Dump the memory printing the word number and its contents in binary format
	 */
	public static void dump()
	{
		for(int i =0; i < addressable_memory.length;i++)
		{
			System.out.println(i+": "+Integer.toBinaryString(addressable_memory[i]));
		}
	}
}
