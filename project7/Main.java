import java.io.*;

public class Main {

    private static CodeWriter Writer;

    public static void main(String[] args) throws IOException {
        File inputFile = new File(args[0]);
        if (inputFile.isDirectory()) {
            File[] files = inputFile.listFiles((dir, name) -> name.endsWith(".vm"));
            File outputFile = new File(inputFile, inputFile.getName() + ".asm");
            Writer = new CodeWriter(outputFile);
            if (files != null) {
                for (File file : files) {
                    translator(file);
                }
            }
        } else {
            File outputFile = new File(inputFile.getParentFile(),
                    inputFile.getName().split(".vm")[0] + ".asm");
            Writer = new CodeWriter(outputFile);
            translator(inputFile);
        }
        Writer.close();
    }


    private static void translator(File file) {

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            Parser parser = new Parser(reader);
            Writer.setFileName(new File(file.getName().split(".vm")[0] + ".asm"));

            while (parser.hasMoreCommands()) {
                parser.advance();
                if(parser.commandType() == CommandType.C_POP||parser.commandType() == CommandType.C_PUSH) {
                    Writer.WritePushPop(parser.commandType(), parser.arg1(), parser.arg2());
                } else {
                    Writer.WriteArithmetic(parser.arg1());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}