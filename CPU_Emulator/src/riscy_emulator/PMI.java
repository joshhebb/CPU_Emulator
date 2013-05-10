package riscy_emulator;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Josh Hebb
 * PMI class, holds the memory address and memory data and allows the reading writing to and from memory
 */

public class PMI 
{
	static int memory_address;
	static int memory_data;
	
	/**
	 * Read from memory (use the input binary file and return the data read from the address
	 */
	public void read_memory(RandomAccessFile file) throws IOException
	{
		file.seek(memory_address);
		int instruction_read = file.readInt();
		memory_data = instruction_read;
	}
	
	/**
	 * Write a word to memory, passed in as a MEM param. The word is set and memory is returned.
	 */
	public static MEM write_word(MEM memory)
	{
		MEM return_mem = memory;
		return_mem.addressable_memory[memory_address] = memory_data;
		return return_mem;
	}
}
