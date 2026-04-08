import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

public class RegisterLoader {

    public static void loadRegistersFromYaml(String filePath, Registers registers) {
        if (filePath == null || registers == null) {
            throw new IllegalArgumentException("File path and Registers cannot be null");
        }

        try {
            Yaml yaml = new Yaml();
            InputStream input = new FileInputStream(filePath);

            Map<String, Object> data = yaml.load(input);

            if (data == null || !data.containsKey("registers")) {
                throw new RuntimeException("Missing 'registers' section in YAML");
            }

            // ✅ Safe cast
            Object regsObj = data.get("registers");
            if (!(regsObj instanceof List)) {
                throw new RuntimeException("'registers' must be a list");
            }

            List<?> rawRegs = (List<?>) regsObj;

            if (rawRegs.isEmpty()) {
                throw new RuntimeException("No registers defined in YAML");
            }

            for (Object obj : rawRegs) {

                if (!(obj instanceof Map)) {
                    throw new RuntimeException("Invalid register entry format");
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> reg = (Map<String, Object>) obj;

                String name = (String) reg.get("name");
                Object sizeObj = reg.get("size");

                if (name == null || name.isEmpty()) {
                    throw new RuntimeException("Register missing valid name");
                }

                if (sizeObj == null) {
                    throw new RuntimeException("Register size missing for: " + name);
                }

                int size = (int) sizeObj;

                registers.addReg(name, size);

                // Optional init
                if (reg.containsKey("init")) {
                    Object initObj = reg.get("init");

                    if (!(initObj instanceof List)) {
                        throw new RuntimeException("Init must be a list for register: " + name);
                    }

                    Buffer initVal = parseBuffer((List<?>) initObj);

                    if (initVal.getSize() != size) {
                        throw new RuntimeException("Init size mismatch for register: " + name);
                    }

                    registers.write(name, initVal);
                }

                System.out.println("Added register: " + name + " size=" + size);
            }

        } catch (Exception e) {
            throw new RuntimeException("Register loading failed", e);
        }
    }

    private static Buffer parseBuffer(List<?> list) {
        if (list == null) {
            throw new IllegalArgumentException("Init buffer cannot be null");
        }

        byte[] data = new byte[list.size()];

        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);

            if (obj == null) {
                throw new RuntimeException("Invalid byte in init buffer: null");
            }

            String s = obj.toString().trim();

            if (s.isEmpty()) {
                throw new RuntimeException("Invalid byte in init buffer: empty string");
            }

            if (s.startsWith("0x") || s.startsWith("0X")) {
                data[i] = (byte) Integer.parseInt(s.substring(2), 16);
            } else {
                data[i] = (byte) Integer.parseInt(s);
            }
        }

        return new Buffer(data);
    }
}
