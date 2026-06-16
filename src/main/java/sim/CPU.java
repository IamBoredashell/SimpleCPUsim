package sim;

import java.math.BigInteger;
import java.util.*;

enum Endianness {
    LITTLE,
    BIG
}

public class CPU {

    public Registers reg;
    public Memory memory;
    public ControlUnit cu;
    public Map<String, FlagDef> flags;

    private Endianness endianness;
    private BigInteger iteration;
    private boolean running;
    Registers prevRegs;

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
        this.flags = new HashMap<>();
        this.prevRegs = reg.copy();
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

        prevRegs = reg.copy();
        cu.step(this, "IR");
        iteration = iteration.add(BigInteger.ONE);
	
	if (onStepComplete != null) {onStepComplete.run();}
    }

    public boolean evalAndWriteFlag(String name) {
        FlagDef fd = flags.get(name);
        boolean result = fd.condition.eval(reg, prevRegs);
        writeBit(fd.targetReg, fd.targetBit, result);
        return result;
    }

    private void writeBit(String regName, int bit, boolean val) {
        Buffer buf = reg.read(regName);
        int byteIdx = (buf.getSize() - 1) - (bit / 8);
        int bitIdx = bit % 8;
        if (byteIdx >= buf.getSize()) {
            throw new RuntimeException("Bit " + bit + " out of bounds for register " + regName + " (size " + buf.getSize() + " bytes)");
        }
        byte b = buf.getByte(byteIdx);
        if (val) {
            b = (byte) (b | (1 << bitIdx));
        } else {
            b = (byte) (b & ~(1 << bitIdx));
        }
        buf.setByte(b, byteIdx);
        reg.write(regName, buf);
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
        clone.flags = this.flags;
        clone.prevRegs = this.prevRegs;
        clone.setOnStepComplete(this.onStepComplete);
        return clone;
    }
}
