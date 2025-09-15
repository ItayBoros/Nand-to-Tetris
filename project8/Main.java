import java.io.*;

public class Main {

    private static CodeWriter Writer;

    public static void main(String[] args) throws IOException {
        File inputFile = new File(args[0]);
        if (inputFile.isDirectory()) {
            File[] files = inputFile.listFiles((dir, name) -> name.endsWith(".vm"));
            File outputFile = new File(inputFile, inputFile.getName() + ".asm");
            Writer = new CodeWriter(outputFile);

            boolean hasSysFile = false;
            if (files != null) {
                for (File file : files) {
                    if (file.getName().equals("Sys.vm")) {
                        hasSysFile = true;
                        break;
                    }
                }
            }

            if (hasSysFile) {
                Writer.WriteInit();
            }

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
                switch (parser.commandType()) {
                    case C_POP:
                    case C_PUSH:
                        Writer.WritePushPop(parser.commandType(), parser.arg1(), parser.arg2());
                        break;
                    case C_ARITHMETIC:
                        Writer.WriteArithmetic(parser.arg1());
                        break;
                    case C_LABEL:
                        Writer.WriteLabel(parser.arg1());
                        break;
                    case C_GOTO:
                        Writer.WriteGoto(parser.arg1());
                        break;
                    case C_IF_GOTO:
                        Writer.WriteIf_GoTo(parser.arg1());
                        break;
                    case C_FUNCTION:
                        Writer.writeFunction(parser.arg1(), parser.arg2());
                        break;
                    case C_RETURN:
                        Writer.writeReturn();
                        break;
                    case C_CALL:
                        Writer.WriteCall(parser.arg1(), parser.arg2());
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}