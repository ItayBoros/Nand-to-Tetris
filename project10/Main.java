import java.io.File;


public class JackAnalyzer {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java JackAnalyzer <path to .jack file or directory>");
            return;
        }
        File input = new File(args[0]);
        if (!input.exists()) {
            System.out.println("Error: File or directory does not exist: " + args[0]);
            return;
        }

        if (input.isDirectory()) {
            //directory case - process all .jack files
            File[] files = input.listFiles();
            if (files == null) {
                System.out.println("No files found in directory: " + input.getAbsolutePath());
                return;
            }
            for (File f : files) {
                if (f.getName().toLowerCase().endsWith(".jack")) {
                    // Output name - change .jack -> .xml
                    File output = new File(
                        f.getAbsolutePath().replaceAll("\\.jack$", ".xml")
                    );
                    CompilationEngine eng = new CompilationEngine(f, output);
                    eng.compileClass();
                    eng.closeWriter();
                }
            }
        } else {
            //single .jack file case
            if (!input.getName().toLowerCase().endsWith(".jack")) {
                System.out.println("Error: Not a .jack file: " + input.getName());
                return;
            }
            File output = new File(
                input.getAbsolutePath().replaceAll("\\.jack$", ".xml")
            );
            CompilationEngine eng = new CompilationEngine(input, output);
            eng.compileClass();
            eng.closeWriter();
        }
    }
}