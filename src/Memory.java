import java.util.HashMap; 

// long a = 0xFFFFL;                // 65535
// long b = 0xFFFFFFFFFFFFFFFFL;    // -1 (because long is signed)
// long c = 0x7FFFFFFFFFFFFFFFL;    // Long.MAX_VALUE
// long d = 0x8000000000000000L;    // Long.MIN_VALUE


public class Memory{
    private HashMap<Long, Buffer> memory;
    private int bufferSize;

    public Memory(int bufferSize) {
        this.memory = new HashMap<>();
        this.bufferSize = bufferSize;
    }

    public Buffer read(Long address){
        Buffer buffer = new Buffer(bufferSize);
        buffer.setZero();
        if(!memory.containsKey(address)){
            return buffer;
        }
        return new Buffer(memory.get(address));

    }

    public void write(Long address, Buffer val) {
        if (val.isZero()) {
            memory.remove(address);
            return;
        }
        memory.put(address, new Buffer(val));
        return;

    }


    public void printMemory() {
        for (Long address : memory.keySet()){
            Buffer buffer = memory.get(address);
            System.out.print("Address: " + address + " -> Data: ");
            buffer.print(); 
        }
    }
}
