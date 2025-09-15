import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
    private JackTokenizer tokenizer;
    private BufferedWriter writer;
    private String tab = "";

    public CompilationEngine(File input, File output) {
        tokenizer = new JackTokenizer(input);
        if (tokenizer.hasMoreTokens()) {
            tokenizer.advance();
        }
        try {
            writer = new BufferedWriter(new FileWriter(output));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeWriter() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String escapeXml(String token) {
        // Important to replace & first so we don't double-escape
        token = token.replace("&", "&amp;");
        token = token.replace("<", "&lt;");
        token = token.replace(">", "&gt;");
        token = token.replace("\"", "&quot;");
        return token;
    }
    private void writeTag(String word, String type) {
        try {
            String escaped = escapeXml(word);
            writer.write(tab + "<" + type + "> " + escaped + " </" + type + ">\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void compileClass() {
        try {
            writer.write(tab + "<class>\n");
            pushIndent();

            // 'class'
            writeTag(tokenizer.currentToken, "keyword");
            tokenizer.advance();
            // className
            writeTag(tokenizer.currentToken, "identifier");
            tokenizer.advance();
            // '{'
            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();
            // classVarDec
            while (tokenizer.keyWord().equals("STATIC") || tokenizer.keyWord().equals("FIELD")) {
                compileClassVarDec();
            }
            // subroutineDec
            while (tokenizer.keyWord().equals("CONSTRUCTOR") ||
                    tokenizer.keyWord().equals("FUNCTION") ||
                    tokenizer.keyWord().equals("METHOD")) {
                compileSubRoutine();
            }
            // '}'
            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();
            popIndent();
            writer.write(tab + "</class>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compileClassVarDec() {
        try {
            writer.write(tab + "<classVarDec>\n");
            pushIndent();
            writeTag(tokenizer.currentToken, "keyword");
            tokenizer.advance();
            identifierKeyword();
            tokenizer.advance();
            writeTag(tokenizer.currentToken, "identifier");
            tokenizer.advance();
            while (tokenizer.currentToken.equals(",")) {
                writeTag(",", "symbol");
                tokenizer.advance();
                writeTag(tokenizer.currentToken, "identifier");
                tokenizer.advance();
            }

            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();

            popIndent();
            writer.write(tab + "</classVarDec>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void compileSubRoutine() {
        try {
            writer.write(tab + "<subroutineDec>\n");
            pushIndent();

            //'constructor'/'function'/'method'
            writeTag(tokenizer.currentToken, "keyword");
            tokenizer.advance();

            //'void' or type
            identifierKeyword();
            tokenizer.advance();

            //subroutineName
            writeTag(tokenizer.currentToken, "identifier");
            tokenizer.advance();

            //'('
            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();
            //parameterList
            compileParameterList();
            // ')'
            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();

            //subroutineBody
            compileSubroutineBody();
            popIndent();
            writer.write(tab + "</subroutineDec>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compileParameterList() {
        try {
            writer.write(tab + "<parameterList>\n");
            pushIndent();
            if (!tokenizer.currentToken.equals(")")) {
                // type
                identifierKeyword();
                tokenizer.advance();
                // varName
                writeTag(tokenizer.currentToken, "identifier");
                tokenizer.advance();

                // check for More parameters
                while (tokenizer.currentToken.equals(",")) {
                    // ','
                    writeTag(tokenizer.currentToken, "symbol");
                    tokenizer.advance();
                    // type
                    identifierKeyword();
                    tokenizer.advance();
                    // varName
                    writeTag(tokenizer.currentToken, "identifier");
                    tokenizer.advance();
                }
            }
            popIndent();
            writer.write(tab + "</parameterList>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compileSubroutineBody() {
        try {
            writer.write(tab + "<subroutineBody>\n");
            pushIndent();
            //'{'
            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();

            //varDec*
            while (tokenizer.currentToken.equals("var")) {
                compileVarDec();
            }

            // statements
            compileStatements();
            //'}'
            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();

            popIndent();
            writer.write(tab + "</subroutineBody>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void compileVarDec() {
        try {
            writer.write(tab + "<varDec>\n");
            pushIndent();

            //'var'
            writeTag(tokenizer.currentToken, "keyword");
            tokenizer.advance();
            //type
            identifierKeyword();
            tokenizer.advance();

            writeTag(tokenizer.currentToken, "identifier");
            tokenizer.advance();
            while (tokenizer.currentToken.equals(",")) {
                writeTag(tokenizer.currentToken, "symbol");
                tokenizer.advance();
                writeTag(tokenizer.currentToken, "identifier");
                tokenizer.advance();
            }

            // ';'
            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();

            popIndent();
            writer.write(tab + "</varDec>\n");



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compileStatements() {
        try {
            writer.write(tab + "<statements>\n");
            pushIndent();

            //parse statements until we find a symbol like "}" or else
            while (tokenizer.tokenType() != null && tokenizer.tokenType().equals("KEYWORD")) {
                String kw = tokenizer.currentToken;
                switch (kw) {
                    case "let":
                        compileLet();
                        break;
                    case "if":
                        compileIf();
                        break;
                    case "while":
                        compileWhile();
                        break;
                    case "do":
                        compileDo();
                        break;
                    case "return":
                        compileReturn();
                        break;
                    default:
                        //if not a statement keyword
                        return;
                }
            }

            popIndent();
            writer.write(tab + "</statements>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compileLet() {
        try {
            writer.write(tab + "<letStatement>\n");
            pushIndent();

            // 'let'
            writeTag(tokenizer.currentToken, "keyword");
            tokenizer.advance();

            // varName
            writeTag(tokenizer.currentToken, "identifier");
            tokenizer.advance();

            // optional: '[' expression ']'
            if (tokenizer.currentToken.equals("[")) {
                writeTag("[", "symbol");
                tokenizer.advance();
                compileExpression();
                writeTag("]", "symbol");
                tokenizer.advance();
            }

            // '='
            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();

            compileExpression();

            // ';'
            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();

            popIndent();
            writer.write(tab + "</letStatement>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compileIf() {
        try {
            writer.write(tab + "<ifStatement>\n");
            pushIndent();

            // 'if'
            writeTag(tokenizer.currentToken, "keyword");
            tokenizer.advance();

            // '('
            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();

            // expression
            compileExpression();

            // ')'
            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();

            // '{'
            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();

            // statements
            compileStatements();

            // '}'
            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();

            //optional else
            if (tokenizer.currentToken.equals("else")) {
                writeTag("else", "keyword");
                tokenizer.advance();
                writeTag("{", "symbol");
                tokenizer.advance();
                compileStatements();
                writeTag("}", "symbol");
                tokenizer.advance();
            }

            popIndent();
            writer.write(tab + "</ifStatement>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compileWhile() {
        try {
            writer.write(tab + "<whileStatement>\n");
            pushIndent();

            // 'while'
            writeTag(tokenizer.currentToken, "keyword");
            tokenizer.advance();

            // '('
            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();

            // expression
            compileExpression();

            // ')'
            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();

            // '{'
            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();

            // statements
            compileStatements();

            // '}'
            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();

            popIndent();
            writer.write(tab + "</whileStatement>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compileDo() {
        try {
            writer.write(tab + "<doStatement>\n");
            pushIndent();

            // 'do'
            writeTag(tokenizer.currentToken, "keyword");
            tokenizer.advance();

            // subroutineCall
            compileSubRoutineCall();

            // ';'
            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();

            popIndent();
            writer.write(tab + "</doStatement>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compileReturn() {
        try {
            writer.write(tab + "<returnStatement>\n");
            pushIndent();


            writeTag(tokenizer.currentToken, "keyword");
            tokenizer.advance();

            if (!tokenizer.currentToken.equals(";")) {
                compileExpression();
            }

            // ';'
            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();

            popIndent();
            writer.write(tab + "</returnStatement>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compileSubRoutineCall() {
        writeTag(tokenizer.currentToken, "identifier");
        tokenizer.advance();
        // have '.' subroutineName
        if (tokenizer.currentToken.equals(".")) {
            writeTag(tokenizer.currentToken, "symbol");
            tokenizer.advance();
            writeTag(tokenizer.currentToken, "identifier");
            tokenizer.advance();
        }
        // '('
        writeTag(tokenizer.currentToken, "symbol");
        tokenizer.advance();
        // expressionList
        compileExpressionList();
        // ')'
        writeTag(tokenizer.currentToken, "symbol");
        tokenizer.advance();
    }

    private void compileExpressionList() {
        try {
            writer.write(tab + "<expressionList>\n");
            pushIndent();
            // if not ")"
            if (!tokenizer.currentToken.equals(")")) {
                // 1st expression
                compileExpression();
                // check if there is another exp
                while (tokenizer.currentToken.equals(",")) {
                    writeTag(tokenizer.currentToken, "symbol");
                    tokenizer.advance();
                    compileExpression();
                }
            }

            popIndent();
            writer.write(tab + "</expressionList>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compileExpression() {
        try {
            writer.write(tab + "<expression>\n");
            pushIndent();

            compileTerm();

            //check if current token is an operator
            while (isOp(tokenizer.currentToken)) {
                writeTag(tokenizer.currentToken, "symbol");
                tokenizer.advance();
                compileTerm();
            }

            popIndent();
            writer.write(tab + "</expression>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compileTerm() {
        try {
            writer.write(tab + "<term>\n");
            pushIndent();

            String tType = tokenizer.tokenType();
            String token = tokenizer.currentToken;

            if (tType.equals(JackTokenizer.INT_CONST)) {
                writeTag(token, "integerConstant");
                tokenizer.advance();
            } else if (tType.equals(JackTokenizer.STRING_CONST)) {
                writeTag(token, "stringConstant");
                tokenizer.advance();
            } else if (tType.equals(JackTokenizer.KEYWORD) &&
                    (token.equals("true") || token.equals("false") ||
                            token.equals("null") || token.equals("this"))) {
                writeTag(token, "keyword");
                tokenizer.advance();
            } else if (token.equals("-") || token.equals("~")) {
                writeTag(token, "symbol");
                tokenizer.advance();
                compileTerm();
            } else if (tType.equals(JackTokenizer.IDENTIFIER)) {
                writeTag(token, "identifier");
                tokenizer.advance();
                if (tokenizer.currentToken.equals("[")) {
                    // array access
                    writeTag("[", "symbol");
                    tokenizer.advance();
                    compileExpression();
                    writeTag("]", "symbol");
                    tokenizer.advance();
                } else if (tokenizer.currentToken.equals("(") || tokenizer.currentToken.equals(".")) {
                    // subroutineCall
                    writeTag(tokenizer.currentToken, "symbol");
                    tokenizer.advance();
                    if (tokenizer.tokenType().equals(JackTokenizer.IDENTIFIER)) {
                        writeTag(tokenizer.currentToken, "identifier");
                        tokenizer.advance();
                    }
                    // if there's a dot we already consumed it and check again
                    if (tokenizer.currentToken.equals("(")) {
                        writeTag(tokenizer.currentToken, "symbol");
                        tokenizer.advance();
                        compileExpressionList();
                        writeTag(tokenizer.currentToken, "symbol");
                        tokenizer.advance();
                    }
                }
            } else if (token.equals("(")) {
                writeTag("(", "symbol");
                tokenizer.advance();
                compileExpression();
                writeTag(tokenizer.currentToken, "symbol"); // should be ')'
                tokenizer.advance();
            }

            popIndent();
            writer.write(tab + "</term>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pushIndent() {
        tab += "  ";
    }

    private void popIndent() {
        if (tab.length() > 0) {
            tab = tab.substring(2);
        }
    }


    private boolean isOp(String token) {
        return token.matches("[+\\-*/&|<>=]");
    }

    private void identifierKeyword() {
        if (tokenizer.tokenType().equals(JackTokenizer.KEYWORD)) {
            writeTag(tokenizer.currentToken, "keyword");
        } else {
            writeTag(tokenizer.currentToken, "identifier");
        }
    }
}