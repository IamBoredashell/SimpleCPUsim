import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.*;

public class MicroCodeLoader {

private static Buffer parseKey(Object keyObj) {

    // case: list like [0x80, 0x28]
    if (keyObj instanceof List) {
        List<?> list = (List<?>) keyObj;

        byte[] arr = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = parseAny(list.get(i));
        }
        return new Buffer(arr);
    }

        // case: single value like 0x80
        return new Buffer(new byte[]{ parseAny(keyObj) });
    }

    private static byte parseAny(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).byteValue();  // ✅ handles YAML ints correctly
        }

        String s = obj.toString().trim().toLowerCase();

        if (s.startsWith("0x")) {
            return (byte) Integer.parseInt(s.substring(2), 16);
        }

        return (byte) Integer.parseInt(s);
    }

    public static void load(String filePath, ControlUnit cu) {
        try (InputStream in = java.nio.file.Files.newInputStream(java.nio.file.Paths.get(filePath))) {

            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(in);

            Map<Object, Object> microcode =
                    (Map<Object, Object>) data.get("microcode");

            for (Map.Entry<Object, Object> entry : microcode.entrySet()) {

                Buffer key = parseKey(entry.getKey());
                List<Map<String, Object>> opsList =
                        (List<Map<String, Object>>) entry.getValue();

                List<MicroOp> ops = new ArrayList<>();

                for (Map<String, Object> opMap : opsList) {
                    ops.add(parseOp(opMap));
                }
                System.out.println("Loaded key size: " + key.getSize());
                cu.register(key, ops);
            }

        } catch (Exception e) {
            throw new RuntimeException("Microcode load failed", e);
        }
    }

    // --- parse instruction key ---

    private static Buffer parseInstruction(String keyStr) {
        keyStr = keyStr.trim();

        // handle empty IR: []
        if (keyStr.equals("[]")) {
            return new Buffer(0);
        }

        List<Byte> bytes = new ArrayList<>();

        if (keyStr.startsWith("[") && keyStr.endsWith("]")) {
            String inner = keyStr.substring(1, keyStr.length() - 1).trim();

            if (!inner.isEmpty()) {
                String[] parts = inner.split(",");
                for (String p : parts) {
                    bytes.add(parseByte(p));
                }
            }
        } else {
            bytes.add(parseByte(keyStr));
        }

        byte[] arr = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) arr[i] = bytes.get(i);

        return new Buffer(arr);
    }

    // --- parse micro-op ---
    private static MicroOp parseOp(Map<String, Object> op) {
        String type = (String) op.get("type");

        switch (type) {
            case "NOP":
                return new NOP();

            case "IncReg":
                return new IncReg((String) op.get("reg"));

            case "DecReg":
                return new DecReg((String) op.get("reg"));

            case "FetchNext":
                return new FetchNext(
                        (String) op.get("pc"),
                        (String) op.get("ir")
                );

            case "ShiftIRLeft":
                return new ShiftIRLeft(
                        (String) op.get("ir"),
                        (Integer) op.get("count")
                );

            case "ClearIR":
                return new ClearIR(
                        (String) op.get("ir")
                );

            case "End":
                return new End();

            default:
                throw new RuntimeException("Unknown MicroOp: " + type);
        }
    }

    private static byte parseByte(String s) {
        s = s.trim().toLowerCase();
        if (s.startsWith("0x")) s = s.substring(2);
        return (byte) Integer.parseInt(s, 16);
    }
}
