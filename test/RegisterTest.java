public class RegisterTest {

    public static void main(String[] args) {

        Registers regs = new Registers();

        System.out.println("===== ADD REGISTERS =====");
        regs.addReg("R1", 4);
        regs.addReg("R2", 8);
        regs.addReg("R3", 6);
        regs.printRegisters();

        System.out.println("\n===== SET BYTES IN R1 =====");
        regs.setRegByte("R1", 0, (byte) 0x11);
        regs.setRegByte("R1", 1, (byte) 0x22);
        regs.setRegByte("R1", 2, (byte) 0x33);
        regs.setRegByte("R1", 3, (byte) 0x44);
        regs.printRegisters();

        System.out.println("\n===== WRITE FULL BUFFER TO R2 =====");
        Buffer temp = new Buffer(8);
        byte[] data = temp.getData();
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i + 1); // 01 02 03 ...
        }
        temp.setData(data);
        regs.write("R2", temp);
        regs.printRegisters();

        System.out.println("\n===== TRANSFER (R1 -> R2) =====");
        System.out.println("Copy 2 bytes from R1[0] → R2[4]");
        regs.transfer("R1", 0, "R2", 4, 2);
        regs.printRegisters();

        System.out.println("\n===== READ REGISTER R1 =====");
        Buffer readBuf = regs.read("R1");
        System.out.print("Read R1: ");
        readBuf.print();

        System.out.println("\n===== MODIFY READ BUFFER (SHOULD NOT AFFECT ORIGINAL) =====");
        readBuf.setByte((byte) 0xFF, 0);
        System.out.print("Modified Copy: ");
        readBuf.print();

        System.out.print("Original R1: ");
        regs.read("R1").print();

        System.out.println("\n===== DELETE REGISTER R3 =====");
        regs.delReg("R3");
        regs.printRegisters();

        System.out.println("\n===== INVALID TRANSFER TEST =====");
        regs.transfer("R1", 0, "R2", 7, 5); // should trigger bounds error

        System.out.println("\n===== TEST COMPLETE =====\n");
    }
}
