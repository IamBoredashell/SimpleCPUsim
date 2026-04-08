import java.util.HashMap;
import java.util.Map;

public class Memory {
    private Map<Buffer, Buffer> memory;

    public Memory() {
        this.memory = new HashMap<>();
    }

    public void write(Buffer address, Buffer data) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }

        // store copies to avoid external mutation
        memory.put(new Buffer(address), new Buffer(data));
    }

    public Buffer read(Buffer address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }

        Buffer data = memory.get(address);

        if (data == null) {
            throw new RuntimeException("Memory read error: no data at given address");
        }

        // return copy to avoid mutation outside
        return new Buffer(data);
    }

    public boolean contains(Buffer address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        return memory.containsKey(address);
    }

    public void clear() {
        memory.clear();
    }

    public void print() {
        for (Map.Entry<Buffer, Buffer> entry : memory.entrySet()) {
            System.out.print("Address: ");
            entry.getKey().print();
            System.out.print(" -> Data: ");
            entry.getValue().print();
        }
    }
}
