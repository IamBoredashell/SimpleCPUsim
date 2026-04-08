public class CPUTest {
    public static void main(String[] args) {

        Memory mem = new Memory(1, 1);
        Registers regs = new Registers();
        ControlUnit cu = new ControlUnit();

        // Load registers
        RegisterLoader.loadRegistersFromYaml("test/CPUTest/registers.yaml", regs);

        // Load microcode
        MicroCodeLoader.load("test/CPUTest/microcode.yaml", cu);

        // Program:
        // 80 50 → INC A
        // 80 50 → INC A
        // 80 51 → DEC A
        // FF    → HALT

        mem.write(new Buffer(new byte[]{0x00}), new Buffer(new byte[]{(byte)0x80}));
        mem.write(new Buffer(new byte[]{0x01}), new Buffer(new byte[]{(byte)0x50}));

        mem.write(new Buffer(new byte[]{0x02}), new Buffer(new byte[]{(byte)0x80}));
        mem.write(new Buffer(new byte[]{0x03}), new Buffer(new byte[]{(byte)0x50}));

        mem.write(new Buffer(new byte[]{0x04}), new Buffer(new byte[]{(byte)0x80}));
        mem.write(new Buffer(new byte[]{0x05}), new Buffer(new byte[]{(byte)0x51}));

        mem.write(new Buffer(new byte[]{0x06}), new Buffer(new byte[]{(byte)0xFF}));

        // CPU
        CPU cpu = new CPU(mem, regs, cu, Endianness.LITTLE);
        cpu.start();

        while (cpu.isRunning()) {

            cu.step(cpu, "IR");

            Buffer ir = regs.read("IR");
            Buffer pc = regs.read("PC");

            System.out.print("IR: ");
            for (int i = 0; i < ir.getSize(); i++) {
                System.out.print(String.format("%02X ", ir.getByte(i) & 0xFF));
            }
            System.out.println();

            System.out.print("PC: ");
            for (int i = 0; i < pc.getSize(); i++) {
                System.out.print(String.format("%02X ", pc.getByte(i) & 0xFF));
            }
            System.out.println();
            // debug after each micro-op
            regs.printRegisters();

            System.out.println("---- step ----");
        }
        // Final state
        System.out.println("---- FINAL STATE ----");
        regs.printRegisters();
    }
}
