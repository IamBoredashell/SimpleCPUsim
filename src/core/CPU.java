import java.math.BigInteger;

enum Endianness{
    LITTLE,
    BIG
}

public class CPU {
    public Registers reg;       // all CPU registers
    public Memory memory;       // memory
    public ControlUnit cu;      // microcode execution unit
    public Endianness endianness;
    private BigInteger iteration;  // tracks number of micro-op executions
    private boolean running;       // CPU running status

    public CPU(Memory memory, Registers reg, ControlUnit cu, Endianness endianness) {
        this.memory = memory;
        this.reg = reg;
        this.cu = cu;
        this.iteration = BigInteger.ZERO;
        this.running = false;
        this.endianness = endianness;
    }
    public void setEndianness(Endianness e) {
        this.endianness = e;
    }
    /** Step once: let CU dynamically fetch/decode/execute using specified PC/MAR register */
    public void step(String pcRegName) {
        if (!running) {
            System.out.println("CPU not running. Call start() first.");
            return;
        }
        while (isRunning()) {
            cu.step(this, "IR");
        }
        iteration = iteration.add(BigInteger.ONE);
    }

    /** Run n steps */
    public void run(String pcRegName, int n) {
        start();
        for (int i = 0; i < n; i++) {
            step(pcRegName);
            if (!running) break; // CU may have halted
        }
    }

    /** Start CPU execution */
    public void start() {
        running = true;
    }

    /** Stop CPU execution */
    public void stop() {
        running = false;
    }

    /** Get iteration count */
    public BigInteger getIteration() {
        return iteration;
    }

    public Endianness getEndianness(){
        return endianness;
    }
    /** Get CPU running state */
    public boolean isRunning() {
        return running;
    }
}
