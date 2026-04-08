import java.util.*;

// Base interface
interface MicroOp { void execute(CPU cpu); }

// --- No-op ---
class NOP implements MicroOp {
    public void execute(CPU cpu) { }
}

// --- Increment register ---
class IncReg implements MicroOp {
    private String reg;

    public IncReg(String reg) {
        this.reg = reg;
    }

    public void execute(CPU cpu) {
        Buffer b = cpu.reg.read(reg);
        int size = b.getSize();

        Endianness e = cpu.getEndianness();

        if (e == Endianness.LITTLE) {
            for (int i = 0; i < size; i++) {
                byte v = b.getByte(i);
                v++;
                b.setByte(v, i);
                if (v != 0) break; // stop if no carry
            }
        } else {
            for (int i = size - 1; i >= 0; i--) {
                byte v = b.getByte(i);
                v++;
                b.setByte(v, i);
                if (v != 0) break;
            }
        }

        cpu.reg.write(reg, b);
    }
}
// --- Decrement register ---
class DecReg implements MicroOp {
    private String reg;

    public DecReg(String reg) {
        this.reg = reg;
    }

    public void execute(CPU cpu) {
        Buffer b = cpu.reg.read(reg);
        int size = b.getSize();

        Endianness e = cpu.getEndianness();

        if (e == Endianness.LITTLE) {
            for (int i = 0; i < size; i++) {
                byte v = b.getByte(i);
                v--;
                b.setByte(v, i);
                if (v != (byte)0xFF) break; // stop if no borrow
            }
        } else {
            for (int i = size - 1; i >= 0; i--) {
                byte v = b.getByte(i);
                v--;
                b.setByte(v, i);
                if (v != (byte)0xFF) break;
            }
        }

        cpu.reg.write(reg, b);
    }
}
// --- Fetch next byte into IR ---
class FetchNext implements MicroOp {
    private String pcReg;
    private String irReg;

    public FetchNext(String pcReg, String irReg) {
        this.pcReg = pcReg;
        this.irReg = irReg;
    }

    public void execute(CPU cpu) {
        Buffer pc = cpu.reg.read(pcReg);
        byte b = cpu.memory.read(pc).getByte(0);

        cpu.reg.appendToReg(irReg, b);

        int val = pc.getByte(0) & 0xFF;
        pc.setByte((byte)(val + 1), 0);
        cpu.reg.write(pcReg, pc);
    }
}

// --- Shift IR left ---
class ShiftIRLeft implements MicroOp {
    private String irReg;
    private int count;

    public ShiftIRLeft(String irReg, int count) {
        this.irReg = irReg;
        this.count = count;
    }

    public void execute(CPU cpu) {
        Buffer ir = cpu.reg.read(irReg);
        Buffer shifted = ir.slice(count);
        cpu.reg.write(irReg, shifted);
    }
}

// --- Clear IR ---
class ClearIR implements MicroOp {
    private String irReg;

    public ClearIR(String irReg) {
        this.irReg = irReg;
    }

    public void execute(CPU cpu) {
        cpu.reg.clearReg(irReg);
    }
}

// --- End instruction ---
class End implements MicroOp {
    public void execute(CPU cpu) {
        cpu.stop();
    }
}
