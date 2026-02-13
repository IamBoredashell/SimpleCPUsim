public class Buffer {
    private byte[] data;
    private int size;

    // Constructor that creates a buffer of the specified size
    public Buffer(int size) {
        this.size = size;
        this.data = new byte[size]; // Initialize the byte array with the given size
    }
    // Copy Constructor
    public Buffer(Buffer other) {
        this.data = other.data.clone();
    }


    // Constructor that initializes the buffer with a given byte array
    public Buffer(byte[] data) {
        this.size = data.length;
        this.data = data.clone();
    }

    // Get the data array
    public byte[] getData() {
        return this.data.clone();
    }

    // Set the data array
    public void setData(byte[] data) {
        if (data.length == this.size) {
            this.data = data.clone();
        } else {
            System.out.println("Error: Data size does not match buffer size.");
        }
    }


    public void setZero() {
        // Iterate over the data array and set each byte to zero
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
    }


    public boolean isZero() {
        // Iterate through the byte array and check if each byte is 0
        for (byte b : data) {
            if (b != 0) {
                return false;  // Return false if any byte is not zero
            }
        }
        return true;  // Return true if all bytes are zero
    }


    // Get the size of the buffer
    public int getSize() {
        return this.size;
    }

    // Print the buffer's content in hexadecimal format
    public void print() {
        for (byte b : data) {
            System.out.printf("%02X ", b);
        }
        System.out.println();
    }
}

