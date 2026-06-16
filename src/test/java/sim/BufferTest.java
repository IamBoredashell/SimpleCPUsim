package sim;

public class BufferTest {
    public static void main(String[] args) {
        System.out.println("=== BufferTest ===");

        Buffer b = new Buffer(2);
        b.setByte((byte)0x12, 0);
        b.setByte((byte)0x34, 1);

        System.out.print("Buffer: ");
        b.print();

        b.append((byte)0x56);
        System.out.print("After append: ");
        b.print();

        System.out.println("==================");
    }
}
