import java.io.*;
import java.util.*;
import java.util.regex.*;

public class JackTokenizer {
    public static final String KEYWORD = "KEYWORD";
    public static final String SYMBOL = "SYMBOL";
    public static final String IDENTIFIER = "IDENTIFIER";
    public static final String INT_CONST = "INT_CONST";
    public static final String STRING_CONST = "STRING_CONST";

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "class", "constructor", "function", "method", "field", "static", "var",
            "int", "char", "boolean", "void", "true", "false", "null", "this",
            "let", "do", "if", "else", "while", "return"
    ));

    private List<String> tokens = new ArrayList<>();
    private List<String> tokenTypes = new ArrayList<>();
    private int currentIndex = -1;
    public String currentToken = null;

    public JackTokenizer(File inputFile) {
        try {
            String content = readFileContent(inputFile);
            content = removeComments(content);
            tokenize(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private String removeComments(String source) {
        StringBuilder result = new StringBuilder();
        int length = source.length();
        int i = 0;

        // We'll use an enum to help make the code more readable
        enum State { NORMAL, IN_STRING, IN_LINE_COMMENT, IN_BLOCK_COMMENT }
        State state = State.NORMAL;

        while (i < length) {
            char c = source.charAt(i);
            char next = (i + 1 < length) ? source.charAt(i + 1) : '\0';

            switch (state) {
                case NORMAL:
                    // Check for start of comments
                    if (c == '/' && next == '/') {
                        state = State.IN_LINE_COMMENT;
                        i += 2; // Skip both '/' characters
                        continue;
                    } else if (c == '/' && next == '*') {
                        state = State.IN_BLOCK_COMMENT;
                        i += 2; // Skip both '/' and '*'
                        continue;
                    } else if (c == '"') {
                        state = State.IN_STRING;
                        result.append(c);
                        i++;
                        continue;
                    } else {
                        result.append(c);
                        i++;
                        continue;
                    }

                case IN_STRING:
                    // In a string literal all characters are preserved until
                    // the closing quote. (Ignoring escaped quotes is an option.)
                    result.append(c);
                    if (c == '"') {
                        state = State.NORMAL;
                    }
                    i++;
                    continue;

                case IN_LINE_COMMENT:
                    // Skip characters until end of line:
                    if (c == '\n') {
                        result.append(c); // preserve the newline
                        state = State.NORMAL;
                    }
                    i++;
                    continue;

                case IN_BLOCK_COMMENT:
                    // Look for the closing "*/"
                    if (c == '*' && next == '/') {
                        state = State.NORMAL;
                        i += 2; // Skip the closing */
                    } else {
                        i++;
                    }
                    continue;
            }
        }

        return result.toString();
    }

    private void tokenize(String source) {
        Pattern pattern = Pattern.compile(
                "\"[^\"\\n]*\"|[{}()\\[\\].,;+\\-*/&|<>=~]|[A-Za-z_][A-Za-z0-9_]*|\\d+");
        Matcher matcher = pattern.matcher(source);

        while (matcher.find()) {
            String token = matcher.group();

            if (token.startsWith("\"")) {
                // String constant - remove quotes
                tokens.add(token.substring(1, token.length() - 1));
                tokenTypes.add(STRING_CONST);
            }
            else if (token.matches("[{}()\\[\\].,;+\\-*/&|<>=~]")) {
                tokens.add(token);
                tokenTypes.add(SYMBOL);
            }
            else if (token.matches("\\d+")) {
                int value = Integer.parseInt(token);
                if (value >= 0 && value <= 32767) {
                    tokens.add(token);
                    tokenTypes.add(INT_CONST);
                }
            }
            else if (KEYWORDS.contains(token.toLowerCase())) {
                tokens.add(token.toLowerCase());
                tokenTypes.add(KEYWORD);
            }
            else {
                tokens.add(token);
                tokenTypes.add(IDENTIFIER);
            }
        }
    }

    public boolean hasMoreTokens() {
        return currentIndex < tokens.size() - 1;
    }

    public void advance() {
        if (hasMoreTokens()) {
            currentIndex++;
            currentToken = tokens.get(currentIndex);
        }
    }

    public void back() {
        if (currentIndex > 0) {
            currentIndex--;
            currentToken = tokens.get(currentIndex);
        }
    }

    public String tokenType() {
        if (currentIndex >= 0 && currentIndex < tokenTypes.size()) {
            return tokenTypes.get(currentIndex);
        }
        return null;
    }
}