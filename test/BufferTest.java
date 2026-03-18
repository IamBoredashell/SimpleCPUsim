public class BufferTest {
    public static void main(String[] args) {
        System.out.println("Buffer test\n");

        // Create buffer
        Buffer buf = new Buffer(4);

        // ===== WRITE TEST =====
        System.out.println("\n===== WRITE TEST =====");
        byte[] b = {(byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01};
        buf.setData(b);
        buf.print();

        // ===== ZERO TEST =====
        System.out.println("\n===== ZERO TEST =====");
        System.out.println("Replace with 0:\n");
        System.out.println("Before isZero(): " + buf.isZero());
        buf.setZero();
        buf.print();
        System.out.println("After isZero(): " + buf.isZero());

        // ===== GETDATA TEST =====
        System.out.println("\n===== GETDATA TEST =====");
        System.out.println("GetData:");
        for (byte value : buf.getData()) {
            System.out.printf("%02X ", value);
        }
        System.out.println();

        // ===== SETDATA TEST =====
        System.out.println("\n===== SETDATA TEST =====");
        byte[] newData = {(byte)0x0A, (byte)0x0B, (byte)0x0C, (byte)0x0D};
        buf.setData(newData);
        buf.print();

        // Test wrong size
        System.out.println("Attempt to set wrong size:");
        byte[] wrongSize = {(byte)0x01, (byte)0x02};
        buf.setData(wrongSize);

        // ===== SETBYTE TEST =====
        System.out.println("\n===== SETBYTE TEST =====");
        buf.setByte((byte)0xFF, 2);
        buf.print();

        // Test out-of-bounds

        System.out.println("Attempt to get wrong index:");
        buf.setByte((byte)0xAA, 10);

        // ===== GETBYTE TEST =====
        System.out.println("\n===== GETBYTE TEST =====");
        byte value = buf.getByte(2);
        System.out.printf("Value at index 2: %02X\n", value);

        // Test out-of-bounds
        buf.getByte(10);

        // ===== COPY CONSTRUCTOR TEST =====
        System.out.println("\n===== COPY CONSTRUCTOR TEST =====");
        Buffer copyBuf = new Buffer(buf);
        System.out.print("Original: ");
        buf.print();
        System.out.print("Copy:     ");
        copyBuf.print();

        // Modify original to verify deep copy
        buf.setByte((byte)0x11, 0);
        System.out.println("After modifying original:");
        System.out.print("Original: ");
        buf.print();
        System.out.print("Copy:     ");
        copyBuf.print();

        // ===== BYTE ARRAY CONSTRUCTOR TEST =====
        System.out.println("\n===== ARRAY CONSTRUCTOR TEST =====");
        byte[] arr = {(byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD};
        Buffer arrBuf = new Buffer(arr);
        arrBuf.print();

        // Modify original array to ensure cloning
        arr[0] = 0x00;
        System.out.println("After modifying source array:");
        arrBuf.print();

        // ===== SIZE TEST =====
        System.out.println("\n===== SIZE TEST =====");
        System.out.println("Buffer size: " + buf.getSize());
        System.out.println("\n===== TEST COMPLETE =====\n");
    }
}
