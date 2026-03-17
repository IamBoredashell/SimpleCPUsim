import org.yaml.snakeyaml.Yaml;
import java.util.Map;

public class TestYaml {
    public static void main(String[] args) {
        String data = "\nYaml Test: Library snakeyaml\n Version:2.3\n";
        Yaml yaml = new Yaml();
        Map<String, Object> obj = yaml.load(data);
        System.out.println("Loaded YAML:");
        System.out.println(obj);
    }
}
