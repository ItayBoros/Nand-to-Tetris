import java.io.*;

public class CodeWriter {
    private static BufferedWriter writer;
    private static String fileName;
    private int symbolCount = 0;
    private static int nLabelNum = 1;

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
    public void WriteInit() {
        try {
            writer.write("@256" + "\n");
            writer.write("D=A" + "\n");
            writer.write("@SP" + "\n");
            writer.write("M=D" + "\n");
            WriteCall("Sys.init", 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void WriteGoto(String line){
        try{
            writer.write("@" + line + "\n");
            writer.write("0;JMP\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void WriteLabel(String line){
        try{
            writer.write("(" + line + ")\n");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void WriteIf_GoTo(String line){
        try{
            writer.write("@SP\n");
            writer.write("M=M-1\n");
            writer.write("A=M\n");
            writer.write("D=M\n");
            writer.write("@"+line+"\n");
            writer.write("D;JNE\n");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void WriteCall(String function, int nNumArgs){
        String strLabel = function + "$ret."+ nLabelNum;
        nLabelNum++;
        try{
            writer.write("@");
            writer.write(strLabel);
            writer.write("\n");
            writer.write("D=A\n");
            writer.write("@SP\n");
            writer.write("A=M\n");
            writer.write("M=D\n");
            writer.write("@SP\n");
            writer.write("M=M+1\n");
            String[] segs = { "LCL", "ARG", "THIS", "THAT" };
            // push segs to the stack
            for (String seg : segs) {
                writer.write("@" + seg + "\n");
                writer.write("D=M" + "\n");
                writer.write("@SP" + "\n");
                writer.write("A=M" + "\n");
                writer.write("M=D" + "\n");
                writer.write("@SP" + "\n");
                writer.write("M=M+1" + "\n");
            }
            // ARG = SP - 5 - Nargs
            writer.write("@SP" + "\n");
            writer.write("D=M" + "\n");
            writer.write("@5" + "\n");
            writer.write("D=D-A" + "\n");
            writer.write("@" + nNumArgs + "\n");
            writer.write("D=D-A" + "\n");
            writer.write("@ARG" + "\n");
            writer.write("M=D" + "\n");
            // LCL = SP
            writer.write("// LCL = SP" + "\n");
            writer.write("@SP" + "\n");
            writer.write("D=M" + "\n");
            writer.write("@LCL" + "\n");
            writer.write("M=D" + "\n");
            WriteGoto(function);
            WriteLabel(strLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void writeFunction(String function, int args) {
        WriteLabel(function);
        for (int i = 0; i < args; i++) {
            WritePushPop(CommandType.C_PUSH, "constant", 0);
        }
    }

    public void writeReturn() {
        try {
            // endFrame = LCL (save in R14)
            writer.write("// endFrame = LCL (save in R14)" + "\n");
            writer.write("@LCL" + "\n");
            writer.write("D=M" + "\n");
            writer.write("@R14" + "\n");
            writer.write("M=D" + "\n");
            // retAddr
            writer.write("@5" + "\n");
            writer.write("A=D-A" + "\n");
            writer.write("D=M" + "\n");
            writer.write("@R15" + "\n");
            writer.write("M=D" + "\n");
            // *ARG = pop()
            WritePushPop(CommandType.C_POP, "argument", 0);
            // SP = ARG + 1
            writer.write("@ARG" + "\n");
            writer.write("D=M" + "\n");
            writer.write("@SP" + "\n");
            writer.write("M=D+1" + "\n");
            // restore THAT THIS ARG and LCL of the caller
            String[] segs = { "THAT", "THIS", "ARG", "LCL" };
            for (String seg : segs) {
                writer.write("@R14" + "\n");
                writer.write("D=M-1" + "\n");
                writer.write("AM=D" + "\n");
                writer.write("D=M" + "\n");
                writer.write("@" + seg + "\n");
                writer.write("M=D" + "\n");
            }
            // goto retAddr (saved in R15)
            writer.write("@R15" + "\n");
            writer.write("A=M" + "\n");
            writer.write("0;JMP" + "\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
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