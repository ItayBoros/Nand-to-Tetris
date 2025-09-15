import java.io.*;

public class VMWriter {
    private BufferedWriter writer;
    private boolean isFirstWrite = true;

    public VMWriter(File outputFile) {
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writePush(String segment, int index) {
        write("push " + segment + " " + index);
    }

    public void writePop(String segment, int index) {
        write("pop " + segment + " " + index);
    }

    public void writeArithmetic(String command) {
        write(command);
    }

    public void writeLabel(String label) {
        writeWithoutIndent("label " + label);
    }

    public void writeGoto(String label) {
        write("goto " + label);
    }

    public void writeIf(String label) {
        write("if-goto " + label);
    }

    public void writeCall(String name, int nArgs) {
        write("call " + name + " " + nArgs);
    }

    public void writeFunction(String name, int nLocals) {
        writeWithoutIndent("function " + name + " " + nLocals);
    }

    public void writeReturn() {
        write("return");
    }

    private void write(String command) {
        try {
            if (!isFirstWrite) {
                writer.newLine();
            }
            writer.write("    " + command);
            isFirstWrite = false;
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeWithoutIndent(String command) {
        try {
            if (!isFirstWrite) {
                writer.newLine();
            }
            writer.write(command);
            isFirstWrite = false;
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}