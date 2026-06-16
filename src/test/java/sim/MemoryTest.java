package sim;

public class MemoryTest {
    public static void main(String[] args) {
        System.out.println("=== MemoryTest ===");

        Memory mem = new Memory();

        Buffer addr = new Buffer(new byte[]{0x01});
        Buffer data = new Buffer(new byte[]{0x55});

        mem.write(addr, data);

        Buffer read = mem.read(addr);

        System.out.print("Read data: ");
        read.print();

        System.out.println("==================");
    }
}
