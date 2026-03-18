public class MemoryTest {
    public static void main(String[] args) {
        System.out.println("\nMemory test\n");

        Memory mem = new Memory(4);
        Buffer buf = new Buffer(4);


        System.out.println("\n===== Memory Write Address=====");
        // Write first buffer
        byte[] b = {(byte)0xF1, (byte)0x31, (byte)0xA1, (byte)0xC1};
        buf.setData(b);
        mem.write(0xFFFFL, buf);

        // Write second buffer
        b = new byte[]{(byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01};
        buf.setData(b);
        mem.write(0xEEEEL, buf);

        System.out.println("Print Memory\n");
        mem.printMemory();

        System.out.println("\n===== Zero Test=====");
        System.out.println("0xEEEEL or 61166 should be deleted");
        b = new byte[]{(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
        buf.setData(b);
        mem.write(0xEEEEL, buf);

        mem.printMemory();
        System.out.println("\n===== TEST COMPLETE =====\n");
    }
}

