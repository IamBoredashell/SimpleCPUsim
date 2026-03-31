import java.util.HashMap;

public class Registers {
    private HashMap<String, Buffer> registers;

    public Registers() {
        this.registers = new HashMap<>();
    }
    // Copy constructor (deep copy of registers)
    public Registers(Registers other) {
        this.registers = new HashMap<>();  // Initialize a new HashMap
        // Iterate over the keys of the original 'registers' HashMap
        for (String key : other.registers.keySet()) {
            // For each key, get the corresponding Buffer and create a new Buffer
            this.registers.put(key, new Buffer(other.registers.get(key)));  // Assuming Buffer has a copy constructor
        }
    }
    // Add register with custom size
    public void addReg(String name, int size) {
        registers.put(name, new Buffer(size));
    }

    // Delete register
    public void delReg(String name) {
        registers.remove(name);
    }

    // Read (returns copy)
    public Buffer read(String name) {
        if (!registers.containsKey(name)) {
            System.out.println("Register not found: " + name);
            return null;
        }
        return new Buffer(registers.get(name));
    }

    // Write full register
    public void write(String name, Buffer value) {
        if (!registers.containsKey(name)) {
            System.out.println("Register not found: " + name);
            return;
        }
        registers.put(name, new Buffer(value));
    }

    public void transfer(String src, int srcStart,String dest, int destStart,int length) {

        if (!registers.containsKey(src) || !registers.containsKey(dest)) {
            System.out.println("Invalid register name");
            return;
        }

        Buffer srcBuf = registers.get(src);
        Buffer destBuf = registers.get(dest);

        // bounds check
        if (srcStart + length > srcBuf.getSize() ||
            destStart + length > destBuf.getSize()) {
            System.out.println("Out of bounds transfer");
            return;
        }

        // direct byte copy (efficient)
        for (int i = 0; i < length; i++) {
            byte val = srcBuf.getByte(srcStart + i);
            destBuf.setByte(val, destStart + i);
        }
    }

    //Manual management
    public void setRegByte(String name, int index, byte value) {
        if (!registers.containsKey(name)) return;
        registers.get(name).setByte(value, index);
    }

    // Print all registers
    public void printRegisters() {
        for (String name : registers.keySet()) {
            System.out.print("Register: " + name + " -> ");
            registers.get(name).print();
        }
    }
}
