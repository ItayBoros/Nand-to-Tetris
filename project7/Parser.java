import java.io.BufferedReader;
import java.io.IOException;

enum CommandType {
    C_ARITHMETIC,
    C_PUSH,
    C_POP,
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
        } else if (command.equals("pop")) {
            return CommandType.C_POP;
        }
        return CommandType.C_ARITHMETIC;
    }

    public String arg1() {
        if (commandType() == CommandType.C_ARITHMETIC) {
            return currentCommand;
        }
        return currentCommand.split(" ")[1];
    }

    public int arg2() {
        if (commandType() == CommandType.C_PUSH || commandType() == CommandType.C_POP) {
            return Integer.parseInt(currentCommand.split(" ")[2]);
        }
        throw new UnsupportedOperationException("arg2 is not supported for this command type");
    }
}