import java.io.*;

public class CodeWriter {
    private static BufferedWriter writer;
    private static String fileName;
    private int symbolCount = 0;

    public CodeWriter(File file) {
        writer = null;
        File outputFile = new File(file.getAbsolutePath());
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
            fileName = file.getName();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFileName(File file) {
        fileName = file.getName();
    }
    public void WriteArithmetic(String line) {
        try {

            switch (line) {
                case "add":
                    writer.write("@SP\n");
                    writer.write("M=M-1\n");
                    writer.write("A=M\n");
                    writer.write("D=M\n");
                    writer.write("@SP\n");
                    writer.write("M=M-1\n");
                    writer.write("A=M\n");
                    writer.write("M=D+M\n");
                    writer.write("@SP\n");
                    writer.write("M=M+1\n");
                    break;
                case "sub":
                    writer.write("@SP\n");
                    writer.write("M=M-1\n");
                    writer.write("A=M\n");
                    writer.write("D=M\n");
                    writer.write("@SP\n");
                    writer.write("M=M-1\n");
                    writer.write("A=M\n");
                    writer.write("M=M-D\n");
                    writer.write("@SP\n");
                    writer.write("M=M+1\n");
                    break;

                case "neg":
                    writer.write("@SP\n");
                    writer.write("M=M-1\n");
                    writer.write("A=M\n");
                    writer.write("M=-M\n");
                    writer.write("@SP\n");
                    writer.write("M=M+1\n");
                    break;
                case "and":
                    writer.write("@SP\n");
                    writer.write("M=M-1\n");
                    writer.write("A=M\n");
                    writer.write("D=M\n");
                    writer.write("@SP\n");
                    writer.write("M=M-1\n");
                    writer.write("A=M\n");
                    writer.write("M=D&M\n");
                    writer.write("@SP\n");
                    writer.write("M=M+1\n");
                    break;
                case "or":
                    writer.write("@SP\n");
                    writer.write("M=M-1\n");
                    writer.write("A=M\n");
                    writer.write("D=M\n");
                    writer.write("@SP\n");
                    writer.write("M=M-1\n");
                    writer.write("A=M\n");
                    writer.write("M=D|M\n");
                    writer.write("@SP\n");
                    writer.write("M=M+1\n");
                    break;

                case "not":
                    writer.write("@SP\n");
                    writer.write("M=M-1\n");
                    writer.write("A=M\n");
                    writer.write("M=!M\n");
                    writer.write("@SP\n");
                    writer.write("M=M+1\n");
                    break;

                case "eq":
                    writer.write("@SP\n");
                    writer.write("M=M-1\n");
                    writer.write("A=M\n");
                    writer.write("D=M\n");
                    writer.write("@SP\n");
                    writer.write("M=M-1\n");
                    writer.write("A=M\n");
                    writer.write("D=M-D\n");
                    writer.write("@LABEL" + symbolCount + "\n");
                    writer.write("D;JEQ\n");
                    writer.write("@SP\n");
                    writer.write("A=M\n");
                    writer.write("M=0\n");
                    writer.write("@ENDLABEL" + symbolCount + "\n");
                    writer.write("0;JMP\n");
                    writer.write("(LABEL" + symbolCount + ")\n");
                    writer.write("@SP\n");
                    writer.write("A=M\n");
                    writer.write("M=-1\n");
                    writer.write("(ENDLABEL" + symbolCount + ")\n");
                    writer.write("@SP\n");
                    writer.write("M=M+1\n");
                    symbolCount++;
                    break;

                case "gt":
                    writer.write("@SP\n");
                    writer.write("M=M-1\n");
                    writer.write("A=M\n");
                    writer.write("D=M\n");
                    writer.write("@SP\n");
                    writer.write("M=M-1\n");
                    writer.write("A=M\n");
                    writer.write("D=M-D\n");
                    writer.write("@LABEL" + symbolCount + "\n");
                    writer.write("D;JGT\n");
                    writer.write("@SP\n");
                    writer.write("A=M\n");
                    writer.write("M=0\n");
                    writer.write("@ENDLABEL" + symbolCount + "\n");
                    writer.write("0;JMP\n");
                    writer.write("(LABEL" + symbolCount + ")\n");
                    writer.write("@SP\n");
                    writer.write("A=M\n");
                    writer.write("M=-1\n");
                    writer.write("(ENDLABEL" + symbolCount + ")\n");
                    writer.write("@SP\n");
                    writer.write("M=M+1\n");
                    symbolCount++;
                    break;

                case "lt":
                    writer.write("@SP\n");
                    writer.write("M=M-1\n");
                    writer.write("A=M\n");
                    writer.write("D=M\n");
                    writer.write("@SP\n");
                    writer.write("M=M-1\n");
                    writer.write("A=M\n");
                    writer.write("D=M-D\n");
                    writer.write("@LABEL" + symbolCount + "\n");
                    writer.write("D;JLT\n");
                    writer.write("@SP\n");
                    writer.write("A=M\n");
                    writer.write("M=0\n");
                    writer.write("@ENDLABEL" + symbolCount + "\n");
                    writer.write("0;JMP\n");
                    writer.write("(LABEL" + symbolCount + ")\n");
                    writer.write("@SP\n");
                    writer.write("A=M\n");
                    writer.write("M=-1\n");
                    writer.write("(ENDLABEL" + symbolCount + ")\n");
                    writer.write("@SP\n");
                    writer.write("M=M+1\n");
                    symbolCount++;
                    break;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void WritePushPop(CommandType command, String segment, int index) {
        try {
            // PUSH command
            if (command == CommandType.C_PUSH) {
                switch (segment) {
                    case "constant":
                        writer.write("@" + index + "\n");
                        writer.write("D=A\n");
                        break;
                    case "local":
                        writer.write("@LCL\n");
                        writer.write("D=M\n");
                        writer.write("@" + index + "\n");
                        writer.write("A=D+A\n");
                        writer.write("D=M\n");
                        break;
                    case "argument":
                        writer.write("@ARG\n");
                        writer.write("D=M\n");
                        writer.write("@" + index + "\n");
                        writer.write("A=D+A\n");
                        writer.write("D=M\n");
                        break;
                    case "this":
                        writer.write("@THIS\n");
                        writer.write("D=M\n");
                        writer.write("@" + index + "\n");
                        writer.write("A=D+A\n");
                        writer.write("D=M\n");
                        break;
                    case "that":
                        writer.write("@THAT\n");
                        writer.write("D=M\n");
                        writer.write("@" + index + "\n");
                        writer.write("A=D+A\n");
                        writer.write("D=M\n");
                        break;
                    case "pointer":
                        writer.write("@" + (3 + index) + "\n");
                        writer.write("D=M\n");
                        break;
                    case "temp":
                        writer.write("@" + (5 + index) + "\n");
                        writer.write("D=M\n");
                        break;
                    case "static":
                        writer.write("@" + fileName.split("\\.")[0] + "." + index + "\n");
                        writer.write("D=M\n");
                        break;
                }
                writer.write("@SP\n");
                writer.write("A=M\n");
                writer.write("M=D\n");
                writer.write("@SP\n");
                writer.write("M=M+1\n");
            }
            // POP command
            else {
                switch (segment) {
                    case "local":
                        writer.write("@LCL\n");
                        writer.write("D=M\n");
                        writer.write("@" + index + "\n");
                        writer.write("D=D+A\n");
                        break;
                    case "argument":
                        writer.write("@ARG\n");
                        writer.write("D=M\n");
                        writer.write("@" + index + "\n");
                        writer.write("D=D+A\n");
                        break;
                    case "this":
                        writer.write("@THIS\n");
                        writer.write("D=M\n");
                        writer.write("@" + index + "\n");
                        writer.write("D=D+A\n");
                        break;
                    case "that":
                        writer.write("@THAT\n");
                        writer.write("D=M\n");
                        writer.write("@" + index + "\n");
                        writer.write("D=D+A\n");
                        break;
                    case "pointer":
                        writer.write("@" + (3 + index) + "\n");
                        writer.write("D=A\n");
                        break;
                    case "temp":
                        writer.write("@" + (5 + index) + "\n");
                        writer.write("D=A\n");
                        break;
                    case "static":
                        writer.write("@" + fileName.split("\\.")[0] + "." + index + "\n");
                        writer.write("D=A\n");
                        break;
                }
                writer.write("@R13\n");
                writer.write("M=D\n");
                writer.write("@SP\n");
                writer.write("M=M-1\n");
                writer.write("A=M\n");
                writer.write("D=M\n");
                writer.write("@R13\n");
                writer.write("A=M\n");
                writer.write("M=D\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void close() throws IOException {
        writer.close();
    }
}