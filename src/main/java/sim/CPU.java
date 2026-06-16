package sim;

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

    private Runnable onStepComplete;

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

    public void setOnStepComplete(Runnable onStepComplete){
    	this.onStepComplete = onStepComplete;
    }

    public void step() {
        if (!running) {
            return;
        }

        cu.step(this, "IR");
        iteration = iteration.add(BigInteger.ONE);
	
	if (onStepComplete != null) {onStepComplete.run();}
    }

    public void run() {
        start();

        while (running) {
            step();
        }
    }

    public void start() {
        running = true;
    }

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

    public CPU copy() {
        CPU clone = new CPU(this.memory.copy(), this.reg.copy(), this.cu.copy(), this.endianness);
        clone.iteration = this.iteration;
        clone.running = false; 
        clone.setOnStepComplete(this.onStepComplete);
        return clone;
    }
}
