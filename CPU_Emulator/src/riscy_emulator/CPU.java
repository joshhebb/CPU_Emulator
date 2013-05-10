package riscy_emulator;

/**
 * @author Josh Hebb
 * CPU class, holds the registers, inter-stage registers and control signals.
 */

public class CPU 
{
	// Register Definition (R0..R31), each register is 32 bits (4 bytes)
	int[] R = new int[32]; // PC IS REGISTER 15, SP IS REGISTER R27, FP IS REGISTER 28
	
	int IR;			// Instruction Register

	
	int RA;			// Stage 2-3
	int RB;
	int RZ;			// Stage 3-4
	int RM;			
	int RY;			// Stage 4-5
	 
	// Control Signals 
	int c_reg_address_A;
	int c_reg_address_B;
	int c_reg_address_C;
	
	int c_mux_B_select;
	int c_mux_C_select;
	int c_mux_Y_select;
	//int c_mux_MA_select;
	//int c_mux_INC_select;
	int c_RF_write; // register write
	
	int c_condition_signal;
	
	int c_ALU_op;
	
}
