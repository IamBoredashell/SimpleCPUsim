import java.util.*;

enum CUState {
    FETCH,
    EXECUTE,
    HALT
}

public class ControlUnit {

    private CUState state = CUState.FETCH;

    private Map<Buffer, List<MicroOp>> microcodeMap = new HashMap<>();

    // step execution state
    private List<MicroOp> currentOps = null;
    private int opIndex = 0;
    private boolean instructionDone = false;

    public CUState getState() { return state; }

    public boolean isInstructionDone() { return instructionDone; }

    public void resetInstruction() {
        currentOps = null;
        opIndex = 0;
        instructionDone = false;
    }

    public void register(Buffer instruction, List<MicroOp> ops) {
        if (instruction == null) {
            throw new IllegalArgumentException("Instruction cannot be null");
        }
        if (ops == null || ops.isEmpty()) {
            throw new IllegalArgumentException("MicroOps cannot be null or empty");
        }

        microcodeMap.put(new Buffer(instruction), ops);
    }

    /** Execute ONE micro-op */
    public void step(CPU cpu, String irRegName) {

        if (cpu == null) throw new IllegalArgumentException("CPU cannot be null");

        if (!cpu.isRunning()) {
            state = CUState.HALT;
            return;
        }

        // Fetch instruction if needed
        if (currentOps == null) {
            Buffer ir = cpu.reg.read(irRegName);
            if (ir == null) throw new RuntimeException("IR register is null");

            currentOps = microcodeMap.get(ir);

            if (currentOps == null) {
                cpu.stop();
                state = CUState.HALT;
                throw new RuntimeException("CPU FAULT: No microcode for IR size " + ir.getSize());
            }

            opIndex = 0;
            instructionDone = false;
            state = CUState.FETCH;
        }

        state = CUState.EXECUTE;

        // --- EXCEPTION SAFETY NET ---
        try {
            MicroOp op = currentOps.get(opIndex++);
            op.execute(cpu);
        } catch (Exception e) {
            // If hardware faults, safely halt and wipe pointers to prevent OutOfBounds spam
            cpu.stop();
            currentOps = null;
            state = CUState.HALT;
            throw new RuntimeException("CPU Hardware Fault: " + e.getMessage(), e);
        }

        // 1. Check if instruction finished FIRST, so pointers reset safely
        if (opIndex >= currentOps.size()) {
            currentOps = null;
            instructionDone = true;
        }

        // 2. Check halt AFTER cleanup
        if (!cpu.isRunning()) {
            state = CUState.HALT;
            return;
        }
    }
    public ControlUnit copy() {
        ControlUnit clone = new ControlUnit();
        clone.state = this.state;
        clone.microcodeMap = this.microcodeMap; // Shared instruction map is safe
        clone.currentOps = this.currentOps;     // Reference to active micro-ops
        clone.opIndex = this.opIndex;
        clone.instructionDone = this.instructionDone;
        return clone;
    }

    public MicroOp getCurrentOp() {
    	if (currentOps != null && opIndex > 0 && opIndex <= currentOps.size()){
	    return currentOps.get(opIndex-1);
	}
	return null;
    }
}
