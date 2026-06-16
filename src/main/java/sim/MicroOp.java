package sim;

public abstract class MicroOp {
    public abstract void execute(CPU cpu);
}

class FetchNext extends MicroOp {
    private String pcReg;
    private String irReg;

    public FetchNext(String pc, String ir) {
        if (pc == null || ir == null) {
            throw new IllegalArgumentException("PC/IR cannot be null");
        }
        this.pcReg = pc;
        this.irReg = ir;
    }

    public void execute(CPU cpu) {
        if (cpu == null) throw new IllegalArgumentException("CPU null");

        Buffer pc = cpu.reg.read(pcReg);
        Buffer data = cpu.memory.read(pc);

        byte b = data.getByte(0);

        cpu.reg.appendToReg(irReg, b);

        incrementBuffer(pc);
        cpu.reg.write(pcReg, pc);
    }

    private void incrementBuffer(Buffer buf) {
        for (int i = buf.getSize() - 1; i >= 0; i--) {
            int val = (buf.getByte(i) & 0xFF) + 1;
            buf.setByte((byte) val, i);
            if (val <= 0xFF) break;
        }
    }
}

class ClearIR extends MicroOp {
    private String ir;

    public ClearIR(String ir) {
        if (ir == null) throw new IllegalArgumentException("IR null");
        this.ir = ir;
    }

    public void execute(CPU cpu) {
        cpu.reg.clearReg(ir);
    }
}

class End extends MicroOp {
    public void execute(CPU cpu) {
        cpu.stop();
    }
}

class IncReg extends MicroOp {
    private String reg;

    public IncReg(String reg) {
        if (reg == null) throw new IllegalArgumentException("Reg null");
        this.reg = reg;
    }

    public void execute(CPU cpu) {
        Buffer b = cpu.reg.read(reg);
        increment(b);
        cpu.reg.write(reg, b);
    }

    private void increment(Buffer buf) {
        for (int i = buf.getSize() - 1; i >= 0; i--) {
            int val = (buf.getByte(i) & 0xFF) + 1;
            buf.setByte((byte) val, i);
            if (val <= 0xFF) break;
        }
    }
}

class DecReg extends MicroOp {
    private String reg;

    public DecReg(String reg) {
        if (reg == null) throw new IllegalArgumentException("Reg null");
        this.reg = reg;
    }

    public void execute(CPU cpu) {
        Buffer b = cpu.reg.read(reg);
        decrement(b);
        cpu.reg.write(reg, b);
    }

    private void decrement(Buffer buf) {
        for (int i = buf.getSize() - 1; i >= 0; i--) {
            int val = (buf.getByte(i) & 0xFF) - 1;
            buf.setByte((byte) val, i);
            if (val >= 0) break;
        }
    }
}

class AndReg extends MicroOp {
    private String r1, r2;

    public AndReg(String r1, String r2) {
        if (r1 == null || r2 == null) {
            throw new IllegalArgumentException("Registers cannot be null");
        }
        this.r1 = r1;
        this.r2 = r2;
    }

    public void execute(CPU cpu) {
        Buffer a = cpu.reg.read(r1);
        Buffer b = cpu.reg.read(r2);

        if (a.getSize() != b.getSize()) {
            throw new RuntimeException("Register size mismatch");
        }

        for (int i = 0; i < a.getSize(); i++) {
            a.setByte((byte) (a.getByte(i) & b.getByte(i)), i);
        }

        cpu.reg.write(r1, a);
    }
}

class OrReg extends MicroOp {
    private String r1, r2;

    public OrReg(String r1, String r2) {
        if (r1 == null || r2 == null) {
            throw new IllegalArgumentException("Registers cannot be null");
        }
        this.r1 = r1;
        this.r2 = r2;
    }

    public void execute(CPU cpu) {
        Buffer a = cpu.reg.read(r1);
        Buffer b = cpu.reg.read(r2);

        if (a.getSize() != b.getSize()) {
            throw new RuntimeException("Register size mismatch");
        }

        for (int i = 0; i < a.getSize(); i++) {
            a.setByte((byte) (a.getByte(i) | b.getByte(i)), i);
        }

        cpu.reg.write(r1, a);
    }
}

class XorReg extends MicroOp {
    private String r1, r2;

    public XorReg(String r1, String r2) {
        if (r1 == null || r2 == null) {
            throw new IllegalArgumentException("Registers cannot be null");
        }
        this.r1 = r1;
        this.r2 = r2;
    }

    public void execute(CPU cpu) {
        Buffer a = cpu.reg.read(r1);
        Buffer b = cpu.reg.read(r2);

        if (a.getSize() != b.getSize()) {
            throw new RuntimeException("Register size mismatch");
        }

        for (int i = 0; i < a.getSize(); i++) {
            a.setByte((byte) (a.getByte(i) ^ b.getByte(i)), i);
        }

        cpu.reg.write(r1, a);
    }
}
