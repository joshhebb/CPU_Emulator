package riscy_emulator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Josh Hebb
 * Emulator class (containing the main), class is responsible for fetching and executing instructions. 
 */

public class Emulator_main 
{
	private static PMI pmi;					// Processor-memory interface
	private static MEM mem;					// Main memory
	private static CPU cpu;					// Central-processing unit
	private static RandomAccessFile file;	// Binary File
	
	public static void main(String[] args) throws IOException
	{
		pmi = new PMI();
		mem = new MEM();
		cpu = new CPU();
		file = new RandomAccessFile(definitions.file_path,"r");
		loader();
		
		String op_code = "";
		while(!op_code.equals(definitions.HALT))	
		{
			stage1_fetch();
		 	stage2_decode();
		 	stage3_execute();
			stage4_memory();
			stage5_reg_write();
			
			String binary_rep = pad_binary(Integer.toBinaryString(cpu.IR),32);							// Convert the IR to a binary string representation
			op_code = (String) binary_rep.subSequence(binary_rep.length() - 6, binary_rep.length());	// Find the opcode to ensure it is not HALT
		}
	 	
		print_registers();
	}
	
	/**
	 * Instruction Fetch - Stage 1
	 */
	public static void stage1_fetch() throws IOException
	{
		pmi.memory_address = cpu.R[15];			// memory address = PC
		pmi.read_memory(file);					// read memory (make a method)
		cpu.IR = pmi.memory_data;				// IR = memory data
		cpu.R[15] = cpu.R[15] + 4;				// pc = pc + 4
	}
	
	/**
	 * Decode - Stage 2
	 */
	public static void stage2_decode() throws IOException
	{
		String binary_rep = pad_binary(Integer.toBinaryString(cpu.IR),32);
		String op_code = (String) binary_rep.subSequence(binary_rep.length() - 6, binary_rep.length());
		
		String r_src = "";	// Source Register (Rsrc)
		String r_dst = "";	// Destination Register (Rdst)
		String i_val = "";	// Immediate value (Imm16)
		
		switch(op_code)
		{
			case definitions.ADDI: // ----------------------------  ADDI	(I-Type) ---------------------------- //
				r_src = binary_rep.substring(0,5); 
				r_dst = binary_rep.substring(5,10); 
				i_val = binary_rep.substring(10,26);
				
				cpu.c_RF_write = 1;									// Writing to a register, therefore set RF_write control signal
				cpu.c_reg_address_A = Integer.parseInt(r_src,2);	// Calculate reg_address_A
				cpu.RA = cpu.R[Integer.parseInt(r_src,2)];			// Set RA
				
				cpu.c_mux_B_select = 1;								// Using immediate value, therefore set mux_B to 1
				cpu.c_mux_Y_select = 0;								// Using the value from the ALU, therefore set mux_Y to 0
				cpu.c_ALU_op = 2;									// Set the ALU operation to 2 (Add)
				
				System.out.println("Add Immediate: Store in R"+Integer.parseInt(r_dst,2)+", Add: ("+cpu.RA +")+("+calc_imm_val(i_val)+")");
				break;
				
			case definitions.BR: // ----------------------------  BR  	(B-Type) ---------------------------- //
				cpu.c_condition_signal = 1;
				i_val = binary_rep.substring(0,26);
				
				//System.out.println("Branch to address:"+calc_imm_val(i_val));
				break;
				
			case definitions.ANDI: // ----------------------------  ANDI	(I-Type) ---------------------------- //
				r_src = binary_rep.substring(0,5); 
				r_dst = binary_rep.substring(5,10); 
				i_val = binary_rep.substring(10,26);
				
				cpu.c_mux_B_select = 1;
				cpu.c_mux_Y_select = 0;
				cpu.c_ALU_op = 0;
				cpu.c_RF_write = 1;
				
				cpu.c_reg_address_A = Integer.parseInt(r_src,2);
				cpu.RA = cpu.R[cpu.c_reg_address_A];
				
				System.out.println("And Immediate: Rdst:"+Integer.parseInt(r_dst,2)+", RA:"+cpu.RA+", Immediate Value:"+calc_imm_val(i_val));
				break;
				
			case definitions.BGE: // ----------------------------  BGE 	(I-Type) ---------------------------- //
				r_src = binary_rep.substring(0,5);  
				r_dst = binary_rep.substring(5,10);  // actually r_src_two but i'll call it a dst for simplicity
				i_val = binary_rep.substring(10,26);
				
				cpu.c_reg_address_A = Integer.parseInt(r_src,2);
				cpu.c_reg_address_B = Integer.parseInt(r_dst,2);				
				cpu.RA = cpu.R[cpu.c_reg_address_A];
				cpu.RB = cpu.R[cpu.c_reg_address_B];
				
				cpu.c_mux_B_select = 0;
				cpu.c_mux_Y_select = 0;
				cpu.c_ALU_op = 5;
				
				System.out.println("Branch if greater than or equal: "+cpu.RA+">="+cpu.RB+", label:"+calc_imm_val(i_val));
				break;
				
			case definitions.ORI: // ----------------------------  ORI 	(I-Type) ---------------------------- //
				r_src = binary_rep.substring(0,5);  
				r_dst = binary_rep.substring(5,10);  
				i_val = binary_rep.substring(10,26);
				
				cpu.c_mux_B_select = 1;
				cpu.c_mux_Y_select = 0;
				cpu.c_ALU_op = 1;
				cpu.c_RF_write = 1;
				
				cpu.c_reg_address_A = Integer.parseInt(r_src,2);
				
				cpu.RA = cpu.R[cpu.c_reg_address_A];
				
				System.out.println("Or immediate: Rdst:"+Integer.parseInt(i_val,2)+", RA:"+cpu.RA+", Immediate Value:"+calc_imm_val(i_val));
				break;
				
			case definitions.STW: // ----------------------------  STW 	(I-Type) ---------------------------- //
				r_src = binary_rep.substring(0,5);
				r_dst = binary_rep.substring(5,10);
				i_val = binary_rep.substring(10,26);
				
				
				cpu.c_reg_address_A = Integer.parseInt(r_src,2);
				cpu.c_reg_address_B = Integer.parseInt(r_dst,2);
				cpu.RA = cpu.R[cpu.c_reg_address_A];
				cpu.RB = cpu.R[cpu.c_reg_address_B];
				
				cpu.c_ALU_op = 2;			// Add
				cpu.c_mux_Y_select = 1;		// Memory
				
				System.out.println("Store value in R"+Integer.parseInt(r_dst,2)+" at address: "+calc_imm_val(i_val)+" in memory");
				break;
				
			case definitions.BLT: // ----------------------------  BLT 	(I-Type) ---------------------------- //
				r_src = binary_rep.substring(0,5);  
				r_dst = binary_rep.substring(5,10);  // actually r_src_two but i'll call it a dst for simplicity
				i_val = binary_rep.substring(10,26);
				
				cpu.c_reg_address_A = Integer.parseInt(r_src,2);
				cpu.c_reg_address_B = Integer.parseInt(r_dst,2);				
				cpu.RA = cpu.R[cpu.c_reg_address_A];
				cpu.RB = cpu.R[cpu.c_reg_address_B];
				
				cpu.c_mux_B_select = 0;
				cpu.c_mux_Y_select = 0;
				cpu.c_ALU_op = 3;
				
				System.out.println("Branch if less than: "+cpu.RA+"<"+cpu.RB+", label:"+calc_imm_val(i_val));
				break;
				
			case definitions.LDW: // ---------------------------- LDW 		(I-Type) ---------------------------- //
				
				r_src = binary_rep.substring(0,5);  
				r_dst = binary_rep.substring(5,10);
				i_val = binary_rep.substring(10,26);
				
				cpu.c_reg_address_A = Integer.parseInt(r_src,2);
				cpu.RA = cpu.R[cpu.c_reg_address_A];
				cpu.c_mux_Y_select = 1;
				cpu.c_ALU_op = 2;
				cpu.c_mux_B_select = 1;
				cpu.c_RF_write = 1;
				
				System.out.println("Load word: into R:"+Integer.parseInt(r_dst,2)+", Rsrc:"+cpu.RA+", offset:"+calc_imm_val(i_val));
				break;
				
			case definitions.BNE: // ---------------------------- BNE	 	(I-Type) ---------------------------- //
				r_src = binary_rep.substring(0,5);  
				r_dst = binary_rep.substring(5,10);  // actually r_src_two but i'll call it a dst for simplicity

				cpu.c_reg_address_A = Integer.parseInt(r_src,2);
				cpu.c_reg_address_B = Integer.parseInt(r_dst,2);				
				cpu.RA = cpu.R[cpu.c_reg_address_A];
				cpu.RB = cpu.R[cpu.c_reg_address_B];
				
				cpu.c_mux_B_select = 0;
				cpu.c_mux_Y_select = 0;
				cpu.c_ALU_op = 9;
				
				System.out.println("Branch if not equal: "+cpu.RA+"!="+cpu.RB+", label:"+calc_imm_val(i_val));
				break;
				
			case definitions.BEQ: // ---------------------------- BEQ 		(I-Type) ---------------------------- //
				r_src = binary_rep.substring(0,5);  
				r_dst = binary_rep.substring(5,10);  // actually r_src_two but i'll call it a dst for simplicity

				cpu.c_reg_address_A = Integer.parseInt(r_src,2);
				cpu.c_reg_address_B = Integer.parseInt(r_dst,2);				
				cpu.RA = cpu.R[cpu.c_reg_address_A];
				cpu.RB = cpu.R[cpu.c_reg_address_B];
				
				cpu.c_mux_B_select = 0;
				cpu.c_mux_Y_select = 0;
				cpu.c_ALU_op = 8;
				
				System.out.println("Branch if equal: "+cpu.RA+"=="+cpu.RB+", label:"+calc_imm_val(i_val));
				break;
				
			case definitions.R_TYPE: // ---------------------------- R-TYPE 		(R-Type) ---------------------------- //
				String opcode_ext = binary_rep.substring(binary_rep.length()-12, binary_rep.length()-6);
				
				String r_src_one = binary_rep.substring(0,5);  
				String r_src_two = binary_rep.substring(5,10);	// Don't need atm
				String r_dst_one = binary_rep.substring(10,15);
				
				
				if(opcode_ext.equals(definitions.AND))	// -------------------- And 	(R-Type) -------------------- //
				{
					cpu.c_mux_B_select = 0;
					cpu.c_mux_Y_select = 0;
					cpu.c_ALU_op = 0;
					cpu.c_RF_write = 1;
					
					cpu.c_reg_address_A = Integer.parseInt(r_src_one,2);
					cpu.c_reg_address_B = Integer.parseInt(r_src_two,2);
					
					cpu.RA = cpu.R[cpu.c_reg_address_A];
					cpu.RB = cpu.R[cpu.c_reg_address_B];
					
					System.out.println("And: Rdst:"+Integer.parseInt(r_dst_one,2)+", RA:"+cpu.RA+", RB:"+cpu.RB);
				}
				else if(opcode_ext.equals(definitions.ADD)) // --------------- Add 	(R-Type) -------------------- //
				{
					cpu.c_mux_B_select = 0;
					cpu.c_ALU_op = 2;
					cpu.c_mux_Y_select = 0;
					cpu.c_RF_write = 1;
					
					cpu.c_reg_address_A = Integer.parseInt(r_src_one,2);
					cpu.c_reg_address_B = Integer.parseInt(r_src_two,2);
					
					cpu.RA = cpu.R[cpu.c_reg_address_A];
					cpu.RB = cpu.R[cpu.c_reg_address_B];
					
					System.out.println("Add: Rdst:"+Integer.parseInt(r_dst_one,2)+", RA:"+cpu.RA+", RB:"+cpu.RB);
				}
				else if(opcode_ext.equals(definitions.SUB)) // --------------- Sub 	(R-Type) -------------------- //
				{
					cpu.c_mux_B_select = 0;
					cpu.c_ALU_op = 6;			// ALU operation is subtract (6)
					cpu.c_mux_Y_select = 0;
					cpu.c_RF_write = 1;
					
					cpu.c_reg_address_A = Integer.parseInt(r_src_one,2);
					cpu.c_reg_address_B = Integer.parseInt(r_src_two,2);
					
					cpu.RA = cpu.R[cpu.c_reg_address_A];
					cpu.RB = cpu.R[cpu.c_reg_address_B];
					
					System.out.println("Sub: Rdst:"+Integer.parseInt(r_dst_one,2)+", RA:"+cpu.RA+", RB:"+cpu.RB);
				}
				else if(opcode_ext.equals(definitions.OR)) // --------------- Or  	(R-Type) -------------------- //
				{
					cpu.c_mux_B_select = 0;
					cpu.c_mux_Y_select = 0;
					cpu.c_ALU_op = 1	;
					cpu.c_RF_write = 1;
					
					cpu.c_reg_address_A = Integer.parseInt(r_src_one,2);
					cpu.c_reg_address_B = Integer.parseInt(r_src_two,2);
					
					cpu.RA = cpu.R[cpu.c_reg_address_A];
					cpu.RB = cpu.R[cpu.c_reg_address_B];
					
					System.out.println("Or: Rdst:"+Integer.parseInt(r_dst_one,2)+", RA:"+cpu.RA+", RB:"+cpu.RB);
				}	
				break;
		}
		
	}
	
	/**
	 * ALU - Stage 3
	 */
	public static void stage3_execute() throws IOException
	{
		String binary_rep = pad_binary(Integer.toBinaryString(cpu.IR),32);
		String op_code = (String) binary_rep.subSequence(binary_rep.length() - 6, binary_rep.length());
		int immediate_value = 0;
		
		if(op_code.equals(definitions.STW))							// If the operation is a STW (store)
		{				
			String imm_val = binary_rep.substring(10,26);
			immediate_value = calc_imm_val(imm_val);					// Get the immediate value
			
			cpu.RZ = ALU(cpu.c_ALU_op, cpu.RA, immediate_value);		// RZ = ALU result from RA and immediate value
			cpu.RM = cpu.RB;											// RM = RB
		}
		else 														// If the operation isn't a store
		{
			if(cpu.c_mux_B_select == 0)									// If mux_B = 0 then use RB
			{
				cpu.RZ = ALU(cpu.c_ALU_op, cpu.RA, cpu.RB);					// Set RZ = ALU result from RA and RB
			}
			else if(cpu.c_mux_B_select == 1)							// If mux_B = 1 then use immediate value
			{
				String imm_val = binary_rep.substring(10,26);				
				immediate_value = calc_imm_val(imm_val);					// Get the immediate value
				
				cpu.RZ = ALU(cpu.c_ALU_op, cpu.RA, immediate_value);		// RZ = ALU result from RA and immediate value
			} 
			if(cpu.c_condition_signal == 1)								// If the condition signal is set then condition passed
			{
				String imm_val = "";
				if(op_code.equals(definitions.BR))							// If the condition is BR (branch)
				{
					imm_val = binary_rep.substring(0,26);						// Get the immediate value (B-Type)
				}
				else
				{
					imm_val = binary_rep.substring(10,26);						// Get the immediate value (I-Type)
				}
				short imm_value = (short) Integer.parseInt(imm_val,2);
				immediate_value = imm_value;
				cpu.R[15] = cpu.R[15] + immediate_value;					// Update the program counter (PC)
			}
		}
	}
	 
	/**
	 * Memory Access - Stage 4
	 */
	public static void stage4_memory() throws IOException
	{
		String binary_rep = pad_binary(Integer.toBinaryString(cpu.IR),32);
		String op_code = (String) binary_rep.subSequence(binary_rep.length() - 6, binary_rep.length());

		if(cpu.c_mux_Y_select == 0)					// If mux_Y is 0, use value from ALU (RZ)
		{
			cpu.RY = cpu.RZ;
		}
		else if(cpu.c_mux_Y_select == 1) 			// If mux_Y is set to 1, then must use memory
		{
			if(op_code.equals(definitions.STW)) 		// If the instruction is a STW (store)
			{
				pmi.memory_address = cpu.RZ;				// memory address = RZ
				pmi.memory_data = cpu.RM;					// memory data = RM
				mem = pmi.write_word(mem);					// write memory
			}
			else if (op_code.equals(definitions.LDW)) 	// If the instruction is a LDW (load)
			{
				pmi.memory_address = cpu.RZ;				// Memory address = RZ
				pmi.read_memory(file);						// Read memory
				cpu.RY = PMI.memory_data;					// RY = memory data
			}
		}
		else if(cpu.c_mux_Y_select == 2) 				// PC-Temp (Not required for this assignment)
		{
			
		}
		
	}
	
	/**
	 * Destination Register - Stage 5
	 */
	public static void stage5_reg_write()
	{
		if(cpu.c_RF_write == 1)									// If RF_write is set we must write RY to a register
		{
			String binary_rep = pad_binary(Integer.toBinaryString(cpu.IR),32);
			String op_code = (String) binary_rep.subSequence(binary_rep.length() - 6, binary_rep.length());
			String r_dst = "";
			
			if(op_code.equals(definitions.R_TYPE))				// If the instruction is an R-Type
			{
				r_dst = binary_rep.substring(10,15);			// Set the destination register
			}
			else												// If not, it's an I-Type (Or B)
			{
				r_dst = binary_rep.substring(5,10);				// Set the destination register
			}
			
			cpu.R[Integer.parseInt(r_dst,2)] = cpu.RY;			// Insert into the destination register Register Y (RY)
		}
		
		reset_control_signals();								// reset the control signals to 0
	}
	
	/**
	 * Calculate the integer value of the immediate value (accounts for negatives in 2's complement)
	 */
	public static int calc_imm_val(String imm_val)
	{
		int immediate_value = 0;
		
		if(imm_val.charAt(0) == '1')									// If the first character is a 1, it should be a negative value
		{
			short imm_value = (short) Integer.parseInt(imm_val,2);		// Save the value into a short (16 bytes) 
			immediate_value = (int) imm_value;							// and cast it into an integer
		}
		else															// If the first character isnt 1, it's a positive so obtain it through the Integer class (.parseInt)
		{
			immediate_value = Integer.parseInt(imm_val,2);
		}
		
		return immediate_value;
	}
	
	
	/**
	 * Print out the registers and their contents
	 */
	public static void print_registers()
	{
		for(int i=0; i < cpu.R.length; i++)
		{
			System.out.println("[R:" + i + "]" +cpu.R[i]);
		}
	}
	
	/**
	 * ALU method, note that I was unsure of the typical ALU operation codes so I used the following:
	 * 0: And, 1: Or, 2: Add, 3: If less-than, 4: If greater-than, 5: If greater-than or equal
	 * 6: Substract, 7: If less-than or equal, 8: If-equal, 9: Not equal
	 */
	public static int ALU(int ALU_op, int input_A, int input_B)
	{
		switch(ALU_op)
		{
			case 0: 							// And
				return input_A & input_B;
			case 1: 							// Or
				return input_A | input_B;
			case 2: 							// Add
				return input_A + input_B;
			case 3: 							// If less-than
				if(input_A < input_B) {
					cpu.c_condition_signal = 1; // Set condition signal
				}
				break;
			case 4: 							// If greater-than
				if(input_A > input_B) {
					cpu.c_condition_signal = 1; // Set condition signal
				}
				break;
			case 5:								// If greater-than or equal
				if(input_A >= input_B) {
					cpu.c_condition_signal = 1; // Set condition signal
				}
				break;	
			case 6: 							// Subtract
				return input_A - input_B;
			case 7: 							// If less-than or equal
				if(input_A <= input_B) {
					cpu.c_condition_signal = 1; // Set condition signal
				}
				break;
			case 8: 							// If equal
				if(input_A == input_B) {
					cpu.c_condition_signal = 1;	// Set condition signal
				}
				break;
			case 9: 							// If not-equal
				if(input_A != input_B) {
					cpu.c_condition_signal = 1; // Set condition signal
				}
		}
		return 0;
	}
	
	/**
	 * Reset all of the control signals (after each instruction execution in Stage 5)
	 */
	public static void reset_control_signals()
	{
		cpu.c_reg_address_A = 0;
		cpu.c_reg_address_B = 0;
		cpu.c_reg_address_C = 0;
		cpu.c_mux_B_select = 0;
		cpu.c_mux_C_select = 0;
		cpu.c_mux_Y_select = 0;
		cpu.c_RF_write = 0;
		cpu.c_condition_signal = 0;
		cpu.c_ALU_op = 0;
		cpu.RA = 0;
		cpu.RB = 0;
		cpu.RM = 0;
		cpu.RY = 0;
		cpu.RZ = 0;
	}
	
	
	/**
	 * Read and set the program counter
	 */
	public static void loader() throws IOException
	{
		file.seek(0);
		short code_offset = file.readShort();
		cpu.R[15] = code_offset;
	}
	
	/**
	 * Pad a binary string presentation to length (padding_length)
	 */
	public static String pad_binary(String opcode, int padding_length)
	{
		String binary_padding = opcode;
		while(binary_padding.length() < padding_length)
		{
			binary_padding = "0" + binary_padding;
		}
		return binary_padding;
	}
	
}
