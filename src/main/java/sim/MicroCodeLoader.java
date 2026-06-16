package sim;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MicroCodeLoader {

    public static void load(String filePath, ControlUnit cu) {
        if (filePath == null || cu == null) {
            throw new IllegalArgumentException("File path and ControlUnit cannot be null");
        }

        try {
            Yaml yaml = new Yaml();
            InputStream input = Files.newInputStream(Paths.get(filePath));
            @SuppressWarnings("unchecked")
            Map<String, Object> data = yaml.load(input);

            if (data == null || !data.containsKey("microcode")) {
                throw new RuntimeException("Missing 'microcode' section in YAML");
            }

            Object mcObj = data.get("microcode");
            if (!(mcObj instanceof Map)) {
                throw new RuntimeException("'microcode' must be a map");
            }
            @SuppressWarnings("unchecked")
            Map<Object, Object> microcode = (Map<Object, Object>) mcObj;

            for (Map.Entry<Object, Object> entry : microcode.entrySet()) {

                Buffer instruction = parseInstruction(entry.getKey());

                Object opsObj = entry.getValue();
                if (!(opsObj instanceof List)) {
                    throw new RuntimeException("MicroOps must be a list");
                }

                List<?> rawOps = (List<?>) opsObj;
                if (rawOps.isEmpty()) {
                    throw new RuntimeException("Empty micro-op list for instruction");
                }

                List<MicroOp> microOps = new ArrayList<>();

                for (Object obj : rawOps) {
                    if (!(obj instanceof Map)) {
                        throw new RuntimeException("Invalid micro-op entry format");
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> opMap = (Map<String, Object>) obj;
                    microOps.add(parseSingleOp(opMap));
                }

                cu.register(instruction, microOps);
            }

        } catch (Exception e) {
            throw new RuntimeException("Microcode load failed", e);
        }
    }

    private static EvalFlag parseEvalFlag(Map<String, Object> opMap) {
        String name = (String) opMap.get("name");
        List<MicroOp> onTrue = null;
        List<MicroOp> onFalse = null;

        if (opMap.containsKey("onTrue")) {
            onTrue = parseOpsList(opMap.get("onTrue"));
        }
        if (opMap.containsKey("onFalse")) {
            onFalse = parseOpsList(opMap.get("onFalse"));
        }

        return new EvalFlag(name, onTrue, onFalse);
    }

    private static List<MicroOp> parseOpsList(Object raw) {
        List<MicroOp> ops = new ArrayList<>();
        if (!(raw instanceof List)) {
            throw new RuntimeException("Micro-op branch must be a list");
        }
        for (Object obj : (List<?>) raw) {
            if (!(obj instanceof Map)) {
                throw new RuntimeException("Invalid micro-op entry in branch");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> opMap = (Map<String, Object>) obj;
            ops.add(parseSingleOp(opMap));
        }
        return ops;
    }

    private static MicroOp parseSingleOp(Map<String, Object> opMap) {
        String type = (String) opMap.get("type");
        if (type == null) throw new RuntimeException("MicroOp missing 'type'");

        switch (type) {
            case "FetchNext":
                return new FetchNext((String) opMap.get("pc"), (String) opMap.get("ir"));
            case "IncReg":
                return new IncReg((String) opMap.get("reg"));
            case "DecReg":
                return new DecReg((String) opMap.get("reg"));
            case "ClearIR":
                return new ClearIR((String) opMap.get("ir"));
            case "End":
                return new End();
            case "AndReg":
                return new AndReg((String) opMap.get("r1"), (String) opMap.get("r2"));
            case "OrReg":
                return new OrReg((String) opMap.get("r1"), (String) opMap.get("r2"));
            case "XorReg":
                return new XorReg((String) opMap.get("r1"), (String) opMap.get("r2"));
            case "EvalFlag":
                return parseEvalFlag(opMap);
            default:
                throw new RuntimeException("Unknown MicroOp: " + type);
        }
    }

    private static Buffer parseInstruction(Object key) {

        if (key == null) {
            throw new RuntimeException("Instruction key cannot be null");
        }

        if (key instanceof List) {
            List<?> list = (List<?>) key;

            byte[] data = new byte[list.size()];

            for (int i = 0; i < list.size(); i++) {
                data[i] = parseByte(list.get(i));
            }

            return new Buffer(data);
        }

        return new Buffer(new byte[]{parseByte(key)});
    }

    private static byte parseByte(Object obj) {

        if (obj == null) {
            throw new RuntimeException("Invalid byte value: null");
        }

        String s = obj.toString().trim();

        if (s.isEmpty()) {
            throw new RuntimeException("Invalid byte value: empty string");
        }

        if (s.startsWith("0x") || s.startsWith("0X")) {
            return (byte) Integer.parseInt(s.substring(2), 16);
        }

        return (byte) Integer.parseInt(s);
    }
}
