import java.util.*;

enum CUState {
    FETCH,
    DECODE,
    EXECUTE,
    HALT
}

public class ControlUnit {
    private Map<List<Byte>, List<MicroOp>> microcodeMap = new HashMap<>();
    private int size; // optional CU memory size
    private CUState state = CUState.FETCH;
    private List<Byte> fetchBuffer = new ArrayList<>();

    public ControlUnit(int size) {
        this.size = size;
    }

    /** Register an instruction sequence (arbitrary length) with micro-ops */
    public void register(List<Byte> instructionSequence, List<MicroOp> ops) {
        microcodeMap.put(instructionSequence, ops);
    }

    /** Main dynamic dispatch executor */
    public void execute(CPU cpu, String pcRegName) {
        state = CUState.FETCH;
        fetchBuffer.clear();

        while (true) {
            state = CUState.FETCH;

            // Read next byte from memory via user-specified register
            Buffer pcBuf = cpu.reg.read(pcRegName); // e.g., "PC" or "MAR"
            if (pcBuf.getSize() < 1) {
                System.out.println("PC/MAR buffer too small");
                state = CUState.HALT;
                return;
            }

            byte nextByte = cpu.memory.read(pcBuf).getByte(0);
            fetchBuffer.add(nextByte);

            state = CUState.DECODE;

            // Try to match fetchBuffer against any registered instruction
            List<MicroOp> matchedOps = null;
            boolean partialMatch = false;

            for (List<Byte> key : microcodeMap.keySet()) {
                if (fetchBuffer.equals(key)) {
                    matchedOps = microcodeMap.get(key);
                    break;
                } else if (key.size() >= fetchBuffer.size()) {
                    boolean prefixMatch = true;
                    for (int i = 0; i < fetchBuffer.size(); i++) {
                        if (!fetchBuffer.get(i).equals(key.get(i))) {
                            prefixMatch = false;
                            break;
                        }
                    }
                    if (prefixMatch) partialMatch = true;
                }
            }

            if (matchedOps != null) {
                state = CUState.EXECUTE;
                for (MicroOp op : matchedOps) {
                    op.execute(cpu);
                }
                return; // Instruction executed, stop CU until next step
            }

            if (!partialMatch) {
                state = CUState.HALT;
                System.out.println("Invalid instruction sequence: " + fetchBuffer);
                return;
            }

            // Else: partial match, fetch another byte in next iteration
        }
    }

    public CUState getState() {
        return state;
    }

    public int getSize() {
        return size;
    }
}
