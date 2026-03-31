import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

public class RegisterLoader {

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

                registers.addReg(name, size);
                System.out.println("Added register: " + name + " size=" + size);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
