package sim;

public class RegistersTest {
    public static void main(String[] args) {
        System.out.println("=== RegistersTest ===");

        Registers regs = new Registers();

        regs.addReg("A", 2);
        regs.addReg("B", 2);

        regs.write("A", new Buffer(new byte[]{0x01, 0x02}));
        regs.write("B", new Buffer(new byte[]{0x03, 0x04}));

        regs.printRegisters();

        System.out.println("==================");
    }
}
