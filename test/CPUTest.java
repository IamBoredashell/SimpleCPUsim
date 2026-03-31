import java.util.*;
import java.io.*;
import org.yaml.snakeyaml.Yaml;

public class CPUTest {

    public static void main(String[] args) {
        System.out.println("=== CPU File-Driven Test ===\n");

        // --- Registers ---
        Registers registers = new Registers();
        String regFile = "test/CPUTest/registers.yaml";
        RegisterLoader.loadRegistersFromYaml(regFile, registers);

        // --- Memory ---
        // Example: address buffer = 1 byte, data buffer = 1 byte
        Memory memory = new Memory(1, 1);

        // --- Control Unit ---
        ControlUnit cu = new ControlUnit(256);
        String microFile = "test/CPUTest/microcode.yaml";
        MicroCodeLoader.loadMicroCodeFromYaml(microFile, cu);

        // --- CPU ---
        CPU cpu = new CPU(memory, registers, cu);

        // --- Load "program" into memory ---
        // Expect a YAML file: program: [0x01, 0x02, 0x01, ...]
        try (InputStream in = new FileInputStream("test/CPUTest/program.yaml")) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(in);
            List<Integer> program = (List<Integer>) data.get("program");
            if (program == null) {
                System.out.println("No program found in YAML.");
                return;
            }

            for (int i = 0; i < program.size(); i++) {
                // Address buffer
                Buffer addrBuf = new Buffer(1);
                addrBuf.setByte((byte) i, 0);
                // Data buffer
                Buffer dataBuf = new Buffer(1);
                dataBuf.setByte(program.get(i).byteValue(), 0);
                memory.write(addrBuf, dataBuf);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // --- Execute program ---
        System.out.println("\n--- Running program ---");

        // Here we assume a "PC" register exists
        Buffer pc = registers.read("PC");
        for (int step = 0; step < 16; step++) { // limit steps for safety
            int pcVal = pc.getByte(0) & 0xFF;

            Buffer addrBuf = new Buffer(1);
            addrBuf.setByte((byte) pcVal, 0);

            Buffer opcodeBuf = memory.read(addrBuf);
            byte opcode = opcodeBuf.getByte(0);

            System.out.printf("PC=%d, opcode=0x%02X\n", pcVal, opcode);

            // Execute microcode dynamically
            cu.execute(cpu, "PC");

            // Increment PC manually for test simplicity
            pc.setByte((byte) (pcVal + 1), 0);

            // Print A register if exists
            if (registers.read("A") != null) {
                Buffer aVal = registers.read("A");
                System.out.print("Register A: ");
                aVal.print();
            }

            System.out.println();
        }

        System.out.println("\n=== CPU Test Complete ===");
    }
}
