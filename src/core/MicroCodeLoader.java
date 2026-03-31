import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.*;

public class MicroCodeLoader {

    /** Load microcode from YAML for dynamic-length instructions */
    public static void loadMicroCodeFromYaml(String filePath, ControlUnit cu) {
        try (InputStream in = java.nio.file.Files.newInputStream(java.nio.file.Paths.get(filePath))) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(in);

            // Optional control unit size
            Integer size = (Integer) data.get("control_unit_size");
            if (size != null) {
                System.out.println("Control unit size: " + size);
            }

            Map<String, Object> microcode = (Map<String, Object>) data.get("microcode");

            for (String keyStr : microcode.keySet()) {
                // Parse instruction sequence: either single byte "0x01" or multiple "[0x80,0x01]"
                List<Byte> instructionSequence = new ArrayList<>();
                String trimmed = keyStr.trim();
                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    // Multi-byte instruction
                    String[] parts = trimmed.substring(1, trimmed.length() - 1).split(",");
                    for (String part : parts) {
                        instructionSequence.add((byte) Integer.parseInt(part.trim().replace("0x", ""), 16));
                    }
                } else {
                    // Single-byte instruction
                    instructionSequence.add((byte) Integer.parseInt(trimmed.replace("0x", ""), 16));
                }

                List<Map<String, Object>> opsList = (List<Map<String, Object>>) microcode.get(keyStr);
                List<MicroOp> microOps = new ArrayList<>();

                for (Map<String, Object> opMap : opsList) {
                    String type = (String) opMap.get("type");

                    switch (type) {
                        case "NOP":
                            microOps.add(new NOP());
                            break;
                        case "LoadRegFromMem": {
                            String reg = (String) opMap.get("reg");
                            List<String> addrRegs = (List<String>) opMap.get("addrRegs");
                            microOps.add(new LoadRegFromMem(reg, addrRegs));
                            break;
                        }
                        case "StoreRegToMem": {
                            String reg = (String) opMap.get("reg");
                            List<String> addrRegs = (List<String>) opMap.get("addrRegs");
                            microOps.add(new StoreRegToMem(reg, addrRegs));
                            break;
                        }
                        default:
                            System.out.println("Unknown MicroOp type: " + type);
                    }
                }

                cu.register(instructionSequence, microOps);
                System.out.println("Registered instruction sequence: " + instructionSequence);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
