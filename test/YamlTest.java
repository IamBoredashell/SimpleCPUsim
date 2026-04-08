import org.yaml.snakeyaml.Yaml;
import java.util.Map;

public class YamlTest {
    public static void main(String[] args) {
        System.out.println("\n===== YAML LIB LOAD =====");
        String data = "\nYaml Test: Library snakeyaml\n Version:2.3\n";
        System.out.println("String:"+data);
        Yaml yaml = new Yaml();
        Map<String, Object> obj = yaml.load(data);
        System.out.println("Loaded YAML:");
        System.out.println(obj);
        System.out.println("\n===== TEST COMPLETE =====\n");
    }
}
