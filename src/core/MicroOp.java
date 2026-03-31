import java.util.List;

// Base interface
interface MicroOp {
    void execute(CPU cpu);
}

// No-op
class NOP implements MicroOp {
    @Override
    public void execute(CPU cpu) {
        // Do nothing
    }
}

// Load register from memory
class LoadRegFromMem implements MicroOp {
    private String destReg;       // destination register
    private List<String> addrRegs; // optional registers to form address buffer

    public LoadRegFromMem(String destReg, List<String> addrRegs) {
        this.destReg = destReg;
        this.addrRegs = addrRegs;
    }

    @Override
    public void execute(CPU cpu) {
        // Build address buffer from addrRegs
        Buffer address;
        if (addrRegs == null || addrRegs.isEmpty()) {
            System.out.println("LoadRegFromMem: No address registers provided");
            return;
        } else if (addrRegs.size() == 1) {
            address = cpu.reg.read(addrRegs.get(0));
            if (address == null) return;
        } else {
            // concatenate multiple registers
            int totalSize = 0;
            for (String r : addrRegs) {
                Buffer b = cpu.reg.read(r);
                if (b == null) return;
                totalSize += b.getSize();
            }
            byte[] addrData = new byte[totalSize];
            int pos = 0;
            for (String r : addrRegs) {
                Buffer b = cpu.reg.read(r);
                for (int i = 0; i < b.getSize(); i++) {
                    addrData[pos++] = b.getByte(i);
                }
            }
            address = new Buffer(addrData);
        }

        // Read memory
        Buffer value = cpu.memory.read(address);
        Buffer dest = cpu.reg.read(destReg);
        if (dest == null) return;

        // Ensure value fits destination register
        if (value.getSize() != dest.getSize()) {
            System.out.println("LoadRegFromMem: Size mismatch between memory and destination register");
            return;
        }

        // Write value into destination register
        cpu.reg.write(destReg, value);
    }
}

// Store register to memory
class StoreRegToMem implements MicroOp {
    private String srcReg;       // source register
    private List<String> addrRegs; // optional registers to form address buffer

    public StoreRegToMem(String srcReg, List<String> addrRegs) {
        this.srcReg = srcReg;
        this.addrRegs = addrRegs;
    }

    @Override
    public void execute(CPU cpu) {
        // Build address buffer from addrRegs
        Buffer address;
        if (addrRegs == null || addrRegs.isEmpty()) {
            System.out.println("StoreRegToMem: No address registers provided");
            return;
        } else if (addrRegs.size() == 1) {
            address = cpu.reg.read(addrRegs.get(0));
            if (address == null) return;
        } else {
            // concatenate multiple registers
            int totalSize = 0;
            for (String r : addrRegs) {
                Buffer b = cpu.reg.read(r);
                if (b == null) return;
                totalSize += b.getSize();
            }
            byte[] addrData = new byte[totalSize];
            int pos = 0;
            for (String r : addrRegs) {
                Buffer b = cpu.reg.read(r);
                for (int i = 0; i < b.getSize(); i++) {
                    addrData[pos++] = b.getByte(i);
                }
            }
            address = new Buffer(addrData);
        }

        // Read source register
        Buffer src = cpu.reg.read(srcReg);
        if (src == null) return;

        // Write to memory
        cpu.memory.write(address, src);
    }
}
