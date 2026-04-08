import java.util.*;

enum CUState {
    FETCH,
    EXECUTE,
    HALT
}

public class ControlUnit {

    private CUState state = CUState.FETCH;

    private Map<Buffer, List<MicroOp>> microcodeMap = new HashMap<>();

    // 🔑 step execution state
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
        if (instruction == null)
            throw new RuntimeException("Instruction cannot be null");
        microcodeMap.put(instruction, ops);
    }

    /** Execute ONE micro-op */
    public void step(CPU cpu, String irRegName) {

        if (!cpu.isRunning()) {
            state = CUState.HALT;
            return;
        }

        // 🔄 fetch new instruction if needed
        if (currentOps == null) {
            Buffer ir = cpu.reg.read(irRegName);

            currentOps = microcodeMap.get(ir);

            if (currentOps == null) {
                state = CUState.HALT;
                throw new RuntimeException("No microcode for IR");
            }

            opIndex = 0;
            instructionDone = false;
            state = CUState.FETCH;
        }

        state = CUState.EXECUTE;

        // ▶ execute ONE micro-op
        MicroOp op = currentOps.get(opIndex++);
        op.execute(cpu);

        // ✅ instruction finished
        if (opIndex >= currentOps.size()) {
            currentOps = null;
            instructionDone = true;
        }
    }
}
