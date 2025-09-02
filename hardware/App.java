
public class App {
    public static void main(String[] args) {
        HardwareInfoExtractor extractor = new HardwareInfoExtractor("asdfqwefadsvfjJHJAJBUI123NBDIKA1");
         System.out.println("Hardware Validation Result: " + (extractor.LicenseCheck(args[0]) ? "Valid" : "Invalid"));
    }
}
