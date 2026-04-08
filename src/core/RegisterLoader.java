import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

public class RegisterLoader {

 

private static Buffer parseBuffer(List<?> list) {
    if (list == null || list.isEmpty()) {
        return new Buffer(0);
    }

    byte[] arr = new byte[list.size()];

    for (int i = 0; i < list.size(); i++) {
        Object obj = list.get(i);

        if (obj instanceof Number) {
            arr[i] = ((Number) obj).byteValue();
        } else {
            String s = obj.toString().trim().toLowerCase();
            if (s.startsWith("0x")) {
                arr[i] = (byte) Integer.parseInt(s.substring(2), 16);
            } else {
                arr[i] = (byte) Integer.parseInt(s);
            }
        }
    }

    return new Buffer(arr);
}
    public static void loadRegistersFromYaml(String filePath, Registers registers) {
        try {
            Yaml yaml = new Yaml();
            InputStream input = new FileInputStream(filePath);

            // Load YAML as Map
            Map<String, Object> data = yaml.load(input);

            // Get registers list
            List<Map<String, Object>> regs =
                    (List<Map<String, Object>>) data.get("registers");

            if (regs == null) {
                System.out.println("No registers found in YAML.");
                return;
            }

            // Iterate and create registers
            for (Map<String, Object> reg : regs) {
                    String name = (String) reg.get("name");
                int size = (int) reg.get("size");

                // create register first
                registers.addReg(name, size);

                // handle optional init
                if (reg.containsKey("init")) {
                    Buffer initVal = parseBuffer((List<?>) reg.get("init"));
                    registers.write(name, initVal);
                }

                System.out.println("Added register: " + name + " size=" + size);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
