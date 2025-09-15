import java.io.*;
import java.util.*;
import java.util.regex.*;
public class JackTokenizer {
    // Token types
    public static final String KEYWORD      = "KEYWORD";
    public static final String SYMBOL       = "SYMBOL";
    public static final String IDENTIFIER   = "IDENTIFIER";
    public static final String INT_CONST    = "INT_CONST";
    public static final String STRING_CONST = "STRING_CONST";
    
    public String currentToken  = null;
    private List<String> tokens = new ArrayList<>();
    private List<String> types  = new ArrayList<>();
    private int currentIndex    = -1;

    //keywords
    private static final Set<String> KEYWORDS_SET = new HashSet<>(Arrays.asList(
        "class", "constructor", "function", "method", "field", "static", "var",
        "int", "char", "boolean", "void", "true", "false", "null", "this",
        "let", "do", "if", "else", "while", "return"
    ));


    public JackTokenizer(File inputFile) {
        try {
            String content = readFile(inputFile);
            content = removeComments(content);
            tokenize(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   
    private String readFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private String removeComments(String text) {
        
        text = text.replaceAll("(?s)/\\*.*?\\*/", "");
        
        text = text.replaceAll("//.*", "");
         
        return text;
    }


    private void tokenize(String text) {
        //tokenize the input text by regex
        String regex = "\"([^\"\\n]*)\"|([{}()\\[\\].,;+\\-*/&|<>=~])|([A-Za-z_]\\w*)|(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String strLiteral = matcher.group(1);
            String symbol     = matcher.group(2);
            String word       = matcher.group(3);
            String intConst   = matcher.group(4);

            if (strLiteral != null) {
                //string constant
                tokens.add(strLiteral);
                types.add(STRING_CONST);
            } else if (symbol != null) {
                tokens.add(symbol);
                types.add(SYMBOL);
            } else if (word != null) {
                //could be a keyword or identifier
                String lower = word.toLowerCase();
                if (KEYWORDS_SET.contains(lower)) {
                    tokens.add(lower); 
                    types.add(KEYWORD);
                } else {
                    tokens.add(word);
                    types.add(IDENTIFIER);
                }
            } else if (intConst != null) {
                tokens.add(intConst);
                types.add(INT_CONST);
            }
        }
    }

    public boolean hasMoreTokens() {
        return (currentIndex + 1) < tokens.size();
    }

    public void advance() {
        if (hasMoreTokens()) {
            currentIndex++;
            currentToken = tokens.get(currentIndex);
        }
    }

    public String tokenType() {
        if (currentIndex >= 0 && currentIndex < types.size()) {
            return types.get(currentIndex);
        }
        return null;
    }

    public String keyWord() {
        if (tokenType() != null && tokenType().equals(KEYWORD)) {
            return currentToken.toUpperCase();
        }
        return "";
    }
}