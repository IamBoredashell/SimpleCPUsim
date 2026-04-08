import java.util.Arrays;
public class Buffer {
    private byte[] data;

    // Constructor that creates a buffer of the specified size
    public Buffer(int size) {
        this.data = new byte[size]; // Initialize the byte array with the given size
    }
    // Copy Constructor
    public Buffer(Buffer other) {
        this.data = other.data.clone();
    }


    // Constructor that initializes the buffer with a given byte array
    public Buffer(byte[] data) {
        this.data = data.clone();
    }

    // Get the data array
    public byte[] getData() {
        return this.data.clone();
    }

    // Set the data array
    public void setData(byte[] data) {
        if (data.length == this.data.length) {
            this.data = data.clone();
        } else {
            System.out.println("Error: Data size does not match buffer size.");
        }
    }

    public void setByte(byte data, int index) {
        if (index >= 0 && index < this.data.length){
            this.data[index]=data;
       } else {
            System.out.println("Error: Data index is out of bound");
       }
    }
    public byte getByte(int index) {
        if (index >= 0 && index < this.data.length){
        return data[index];
        }else{
            System.out.println("Error: Data index is out of bound");
        }
        
        return 0;
    }


    //Set inside bytes to zero
    public void setZero() {
        // Iterate over the data array and set each byte to zero
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
    }

    //Check if zero
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
        return this.data.length;
    }

    // Print the buffer's content in hexadecimal format
    public void print() {
        for (byte b : data) {
            System.out.printf("%02X ", b);
        }
        System.out.println();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Buffer)) return false;
        Buffer other = (Buffer) o;
        return java.util.Arrays.equals(this.data, other.data);
    }
    @Override
    public int hashCode() {
        return java.util.Arrays.hashCode(data);
    }

    public void append(byte b) {
        byte[] newData = new byte[data.length + 1];
        System.arraycopy(data, 0, newData, 0, data.length);
        newData[data.length] = b;
        data = newData;
    }

    public void clear() {
        data = new byte[0];
    }

    public Buffer slice(int start) {
        if (start >= data.length) return new Buffer(0);
        byte[] newData = new byte[data.length - start];
        System.arraycopy(data, start, newData, 0, newData.length);
        return new Buffer(newData);
    }
}

