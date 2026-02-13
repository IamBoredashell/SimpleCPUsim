public class BufferTest {
    public static void main(String[] args) {
        System.out.println("Buffer test\n");

        // Create buffer
        Buffer buf = new Buffer(4);

        // Initialize with 0x01
        byte[] b = {(byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01};
        buf.setData(b);

        // Normal read
        System.out.println("Normal read:\n");
        buf.print();

        // Set to zero
        System.out.println("Replace with 0:\n");
        System.out.println("Before " + buf.isZero());
        buf.setZero();
        buf.print();
        System.out.println("After " + buf.isZero());

        // GetData print
        System.out.println("GetData:");
        for (byte value : buf.getData()) {
            System.out.printf("%02X ", value);
        }
        System.out.println();
    }
}

