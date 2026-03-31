import java.util.HashMap;

public class Memory {
    private HashMap<Buffer, Buffer> memory;
    private int dataBufferSize;
    private int addressBufferSize;

    public Memory(int addressBufferSize, int dataBufferSize) {
        this.memory = new HashMap<>();
        this.addressBufferSize = addressBufferSize;
        this.dataBufferSize = dataBufferSize;
    }

    // Copy constructor
    public Memory(Memory other) {
        this.dataBufferSize = other.dataBufferSize;
        this.addressBufferSize = other.addressBufferSize;
        this.memory = new HashMap<>();
        for (Buffer key : other.memory.keySet()) {
            this.memory.put(new Buffer(key), new Buffer(other.memory.get(key)));
        }
    }

    // Read from memory at a buffer address
    public Buffer read(Buffer address) {
        if (address.getSize() != addressBufferSize) {
            throw new RuntimeException("Invalid address buffer size");
        }

        Buffer zero = new Buffer(dataBufferSize);
        zero.setZero();

        Buffer data = memory.get(address);
        if (data == null) return zero;

        return new Buffer(data); // return a copy
    }

    // Write to memory at a buffer address
    public void write(Buffer address, Buffer val) {
        if (address.getSize() != addressBufferSize) {
            throw new RuntimeException("Invalid address buffer size");
        }
        if (val.getSize() != dataBufferSize) {
            throw new RuntimeException("Invalid data buffer size");
        }

        if (val.isZero()) {
            memory.remove(address);
            return;
        }

        // Copy both key and value to prevent mutating key later
        memory.put(new Buffer(address), new Buffer(val));
    }

    // Print all memory contents
    public void printMemory() {
        for (Buffer addr : memory.keySet()) {
            System.out.print("Address: ");
            addr.print();
            System.out.print(" -> Data: ");
            memory.get(addr).print();
        }
    }

    // Optional getters
    public int getDataBufferSize() {
        return dataBufferSize;
    }

    public int getAddressBufferSize() {
        return addressBufferSize;
    }
}
