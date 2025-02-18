import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class Parser {
    public enum InstructionType {
        A_INSTRUCTION,
        C_INSTRUCTION,
        L_INSTRUCTION
    }
    protected String currLine;
    protected static int lineCounter=0;
    protected BufferedReader reader;
    protected String nextLine;

    public Parser(File file) {
        try {
            reader = new BufferedReader(new FileReader(file));
            currLine = null;
            nextLine = readNextValidLine();
        } catch (IOException e) {
            throw new RuntimeException("Error initializing parser: " + e.getMessage(), e);
        }
    }

    private String readNextValidLine() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("//")) {
                    if (line.contains("//")) {
                        line = line.substring(0, line.indexOf("//")).trim(); // Remove inline comments
                    }
                    return line;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + e.getMessage(), e);
        }
        return null;
    }

    public void advance() {
        currLine = nextLine;
        nextLine = readNextValidLine();
        if (currLine != null && !currLine.startsWith("(")) {
            lineCounter++; // Increment only for A and C instructions
        }
    }
    public boolean hasMoreLines(){
        return nextLine!=null;
    }
    public InstructionType instructionType() {
        if (currLine.startsWith("@")) {
            return InstructionType.A_INSTRUCTION;
        } else if (currLine.startsWith("(") && currLine.endsWith(")")) {
            return InstructionType.L_INSTRUCTION;
        } else {
            return InstructionType.C_INSTRUCTION;
        }
    }
    public String symbol() { // remove @ or () in symbols and return the symbol itself2
        if(instructionType() == InstructionType.A_INSTRUCTION){
            return currLine.substring(1);
        }
        if(instructionType() == InstructionType.L_INSTRUCTION){
            return currLine.substring(1, currLine.length() - 1);
        }
        return null;
    }
    public String dest(){
        if(currLine.contains("=")){
            return currLine.split("=")[0];
        }
        return "null";
    }
    public String comp(){
        String compPart = currLine;
        if(compPart.contains("=")) {
            compPart = currLine.split("=")[1];
        }
        if(compPart.contains(";")) {
            compPart = currLine.split(";")[0];
        }
        return compPart;
    }
    public String jump(){
        if(currLine.contains(";")){
            return currLine.substring(currLine.indexOf(";")+1);
        }
        return "";
    }
}
