import java.io.*;
import java.nio.file.*;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Main <Prog.asm>");
            return;
        }

        String inputFile = args[0];
        String outputFile = inputFile.replace(".asm", ".hack");

        try {
            // First pass
            Parser parser = new Parser(new File(inputFile));
            SymbolTable symbolTable = new SymbolTable();
            firstPass(parser, symbolTable);

            // Second pass
            parser = new Parser(new File(inputFile));
            try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outputFile))) {
                secondPass(parser, symbolTable, writer);
            }
            System.out.println("Translation complete: " + outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // First Pass: Populate symbol table with label definitions
    private static void firstPass(Parser parser, SymbolTable symbolTable) {
        int Address = 0;
        while (parser.hasMoreLines()) {
            parser.advance();
            if (parser.instructionType() == Parser.InstructionType.L_INSTRUCTION) {
                symbolTable.addEntry(parser.symbol(), Address);
            } else {
                Address++;
            }
        }
    }

    //Translate instructions and write to output
    private static void secondPass(Parser parser, SymbolTable symbolTable, BufferedWriter writer) throws IOException {
        Code code = new Code();
        int RamCounter = 16;

        while (parser.hasMoreLines()) {
            parser.advance();

            if (parser.instructionType() == Parser.InstructionType.A_INSTRUCTION) {
                String symbol = parser.symbol();
                int address;

                // Handle symbol or numeric value
                if (Character.isDigit(symbol.charAt(0))) {
                    address = Integer.parseInt(symbol);
                } else {
                    if (!symbolTable.contains(symbol)) {
                        symbolTable.addEntry(symbol, RamCounter++);
                    }
                    address = symbolTable.getAddress(symbol);
                }

                //A-instruction as binary
                writer.write(String.format("%16s%n", Integer.toBinaryString(address)).replace(' ', '0'));
            }
            else if (parser.instructionType() == Parser.InstructionType.C_INSTRUCTION) {
                // write C-instruction
                String binary = "111" +
                        code.comp(parser.comp()) +
                        code.dest(parser.dest()) +
                        code.jump(parser.jump());
                writer.write(binary + "\n");
            }
        }
    }
}