package sim;

public class MicroOpTest {
    public static void main(String[] args) {
        System.out.println("=== MicroOpTest ===");

        Registers regs = new Registers();
        regs.addReg("A", 2);
        regs.addReg("B", 2);

        regs.write("A", new Buffer(new byte[]{0x01, 0x01}));
        regs.write("B", new Buffer(new byte[]{0x01, 0x00}));

        CPU cpu = new CPU(new Memory(), regs, new ControlUnit(), Endianness.LITTLE);

        System.out.println("Before:");
        regs.printRegisters();

        new IncReg("A").execute(cpu);
        new DecReg("B").execute(cpu);
        new AndReg("A", "B").execute(cpu);

        System.out.println("After:");
        regs.printRegisters();

        System.out.println("==================");
    }
}
