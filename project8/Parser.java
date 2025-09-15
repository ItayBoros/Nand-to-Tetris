import java.io.BufferedReader;
import java.io.IOException;

enum CommandType {
    C_ARITHMETIC,
    C_PUSH,
    C_POP,
    C_LABEL,
    C_GOTO,
    C_IF_GOTO,
    C_FUNCTION,
    C_RETURN,
    C_CALL
}

public class Parser {

    private final BufferedReader reader;
    private String currentCommand;
    public Parser(BufferedReader reader) {
        this.reader = reader;
    }

    public boolean hasMoreCommands() {
        try {
            return reader.ready();
        } catch (IOException e) {
            throw new RuntimeException("Error checking for more commands", e);
        }
    }

    public void advance() {
        try {
            currentCommand = reader.readLine();
            if (currentCommand != null) {
                int commentIndex = currentCommand.indexOf("//");
                if (commentIndex >= 0) {
                    currentCommand = currentCommand.substring(0, commentIndex);
                }
                currentCommand = currentCommand.trim();
                if (currentCommand.isEmpty()) {
                    advance();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading the next command", e);
        }
    }

    public CommandType commandType() {
        String command = currentCommand.split(" ")[0];
        if (command.equals("push")) {
            return CommandType.C_PUSH;
        }
        else if (command.equals("pop")) {
            return CommandType.C_POP;
        }
        else if (command.equals("label")) {
            return CommandType.C_LABEL;
        }
        else if (command.equals("goto")) {
            return CommandType.C_GOTO;
        }
        else if(command.equals("if-goto")){
            return CommandType.C_IF_GOTO;
        }
        else if( command.equals("function")){
            return CommandType.C_FUNCTION;
        }
        else if (command.equals("return")) {
            return CommandType.C_RETURN;
        }
        else if (command.equals("call")) {
            return CommandType.C_CALL;
        }
        return CommandType.C_ARITHMETIC;

    }

    public String arg1() {
        if (commandType() == CommandType.C_ARITHMETIC) {
            return currentCommand.split(" ")[0];
        }
        if (commandType() != CommandType.C_RETURN){
            return currentCommand.split(" ")[1];
        }
        return null;
    }

    public int arg2() {
        if (commandType() == CommandType.C_PUSH || commandType() == CommandType.C_POP || commandType() == CommandType.C_FUNCTION || commandType() == CommandType.C_CALL) {
            return Integer.parseInt(currentCommand.split(" ")[2]);
        }
        throw new UnsupportedOperationException("arg2 is not supported for this command type");
    }
}