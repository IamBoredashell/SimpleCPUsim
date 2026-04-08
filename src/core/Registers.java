import java.util.HashMap;
import java.util.Map;

public class Registers {
    private Map<String, Buffer> regs;

    public Registers() {
        this.regs = new HashMap<>();
    }

    public void addReg(String name, int size) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Register name cannot be null or empty");
        }
        if (size < 0) {
            throw new IllegalArgumentException("Register size cannot be negative");
        }
        if (regs.containsKey(name)) {
            throw new RuntimeException("Register already exists: " + name);
        }

        regs.put(name, new Buffer(size));
    }

    public Buffer read(String name) {
        Buffer b = regs.get(name);
        if (b == null) {
            throw new RuntimeException("Register not found: " + name);
        }
        return new Buffer(b); // return copy
    }

    public void write(String name, Buffer value) {
        if (value == null) {
            throw new IllegalArgumentException("Cannot write null buffer to register");
        }

        Buffer target = regs.get(name);
        if (target == null) {
            throw new RuntimeException("Register not found: " + name);
        }

        if (target.getSize() != value.getSize()) {
            throw new RuntimeException("Register size mismatch for " + name);
        }

        regs.put(name, new Buffer(value)); // store copy
    }

    public void transfer(String from, String to, int fromIdx, int toIdx, int length) {
        Buffer src = regs.get(from);
        Buffer dst = regs.get(to);

        if (src == null || dst == null) {
            throw new RuntimeException("Invalid register name");
        }

        if (fromIdx < 0 || toIdx < 0 || length < 0 ||
            fromIdx + length > src.getSize() ||
            toIdx + length > dst.getSize()) {
            throw new RuntimeException("Out of bounds transfer");
        }

        for (int i = 0; i < length; i++) {
            dst.setByte(src.getByte(fromIdx + i), toIdx + i);
        }
    }

    public void printRegisters() {
        for (Map.Entry<String, Buffer> entry : regs.entrySet()) {
            System.out.print("Register: " + entry.getKey() + " -> ");
            entry.getValue().print();
        }
    }
    public void appendToReg(String name, byte value) {
        Buffer b = regs.get(name);
        if (b == null) {
            throw new RuntimeException("Register not found: " + name);
        }

        Buffer copy = new Buffer(b);
        copy.append(value);
        regs.put(name, copy);
    }

    public void clearReg(String name) {
        Buffer b = regs.get(name);
        if (b == null) {
            throw new RuntimeException("Register not found: " + name);
        }

        regs.put(name, new Buffer(0));
    }
}
