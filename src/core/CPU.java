import java.math.BigInteger;

enum Endianness {
    LITTLE,
    BIG
}

public class CPU {

    public Registers reg;
    public Memory memory;
    public ControlUnit cu;

    private Endianness endianness;
    private BigInteger iteration;
    private boolean running;

    public CPU(Memory memory, Registers reg, ControlUnit cu, Endianness endianness) {
        if (memory == null || reg == null || cu == null || endianness == null) {
            throw new IllegalArgumentException("CPU components cannot be null");
        }

        this.memory = memory;
        this.reg = reg;
        this.cu = cu;
        this.endianness = endianness;

        this.iteration = BigInteger.ZERO;
        this.running = false;
    }

    public void setEndianness(Endianness e) {
        if (e == null) {
            throw new IllegalArgumentException("Endianness cannot be null");
        }
        this.endianness = e;
    }

    /** Execute ONE micro-op */
    public void step() {
        if (!running) {
            return;
        }

        cu.step(this, "IR");
        iteration = iteration.add(BigInteger.ONE);
    }

    /** Run continuously */
    public void run() {
        start();

        while (running) {
            step();
        }
    }

    /** Start CPU (no execution loop inside) */
    public void start() {
        running = true;
    }

    /** Stop CPU */
    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public BigInteger getIteration() {
        return iteration;
    }

    public Endianness getEndianness() {
        return endianness;
    }
}
