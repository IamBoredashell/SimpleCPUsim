package sim;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class FlagLoader {

    public static Map<String, FlagDef> loadFlagsFromYaml(String filePath, Registers regs) {
        if (filePath == null || regs == null) {
            throw new IllegalArgumentException("File path and Registers cannot be null");
        }

        try {
            Yaml yaml = new Yaml();
            InputStream input = new FileInputStream(filePath);
            Map<String, Object> data = yaml.load(input);

            if (data == null || !data.containsKey("flags")) {
                throw new RuntimeException("Missing 'flags' section in YAML");
            }

            Object flObj = data.get("flags");
            if (!(flObj instanceof List)) {
                throw new RuntimeException("'flags' must be a list");
            }

            List<?> rawFlags = (List<?>) flObj;
            Map<String, FlagDef> flags = new LinkedHashMap<>();

            for (Object obj : rawFlags) {
                if (!(obj instanceof Map)) {
                    throw new RuntimeException("Invalid flag entry format");
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> flag = (Map<String, Object>) obj;

                String name = (String) flag.get("name");
                String conditionStr = (String) flag.get("condition");
                String targetStr = (String) flag.get("target");

                if (name == null || conditionStr == null) {
                    throw new RuntimeException("Flag missing required fields (name, condition)");
                }
                if (targetStr == null) {
                    targetStr = (String) flag.get("traget");
                    if (targetStr == null) {
                        throw new RuntimeException("Flag '" + name + "' missing target");
                    }
                }

                if (flags.containsKey(name)) {
                    throw new RuntimeException("Duplicate flag name: " + name);
                }

                FlagDef fd = FlagDef.parse(name, conditionStr, targetStr);
                fd.validate(regs.getRegisterNames());
                flags.put(name, fd);
            }

            return flags;
        } catch (Exception e) {
            throw new RuntimeException("Flag loading failed", e);
        }
    }
}
