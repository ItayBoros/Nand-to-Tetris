import java.io.File;

public class CompilationEngine {
    private JackTokenizer tokenizer;
    private SymbolTable symbolTable;
    private VMWriter vmWriter;
    private String currentClass;
    private int labelCounter = 0;

    public CompilationEngine(File inputFile, File outputFile) {
        tokenizer = new JackTokenizer(inputFile);
        symbolTable = new SymbolTable();
        vmWriter = new VMWriter(outputFile);

        if (tokenizer.hasMoreTokens()) {
            tokenizer.advance();
        }
    }

    public void compileClass() {
        // Get first token
        //tokenizer.advance();
        if (!tokenizer.currentToken.equals("class")) {
            throw new IllegalStateException("File must start with 'class'");
        }

        // Get class name
        tokenizer.advance();
        currentClass = tokenizer.currentToken;

        // Get '{'
        tokenizer.advance();
        if (!tokenizer.currentToken.equals("{")) {
            throw new IllegalStateException("Expected '{'");
        }
        tokenizer.advance();

        // Compile class vars
        while (tokenizer.currentToken.equals("static") ||
                tokenizer.currentToken.equals("field")) {
            compileClassVarDec();
        }

        // Compile subroutines
        while (tokenizer.currentToken.equals("constructor") ||
                tokenizer.currentToken.equals("function") ||
                tokenizer.currentToken.equals("method")) {
            compileSubroutine();
        }

        vmWriter.close();
    }

    private void compileClassVarDec() {
        // static | field
        String kind = tokenizer.currentToken;
        tokenizer.advance();

        // type
        String type = tokenizer.currentToken;
        tokenizer.advance();

        // First varName
        String name = tokenizer.currentToken;
        SymbolTable.Kind symKind = kind.equals("static")
                ? SymbolTable.Kind.STATIC
                : SymbolTable.Kind.FIELD;
        symbolTable.define(name, type, symKind);
        tokenizer.advance();

        // Additional varNames
        while (tokenizer.currentToken.equals(",")) {
            tokenizer.advance(); // skip ','
            name = tokenizer.currentToken;
            symbolTable.define(name, type, symKind);
            tokenizer.advance();
        }

        tokenizer.advance(); // skip ';'
    }

    private void compileSubroutine() {
        String subroutineType = tokenizer.currentToken;
        tokenizer.advance(); // skip constructor/function/method
        tokenizer.advance(); // skip return type
        String subroutineName = tokenizer.currentToken;
        tokenizer.advance(); // skip name

        symbolTable.startSubroutine();

        // If method, add this as first argument
        if (subroutineType.equals("method")) {
            symbolTable.define("this", currentClass, SymbolTable.Kind.ARG);
        }

        tokenizer.advance(); // skip '('
        compileParameterList();
        tokenizer.advance(); // skip ')'
        tokenizer.advance(); // skip '{'

        // Compile var declarations
        while (tokenizer.currentToken.equals("var")) {
            compileVarDec();
        }

        // Write function declaration
        int nLocals = symbolTable.varCount(SymbolTable.Kind.VAR);
        vmWriter.writeFunction(currentClass + "." + subroutineName, nLocals);

        // Setup this pointer for constructors and methods
        if (subroutineType.equals("constructor")) {
            int nFields = symbolTable.varCount(SymbolTable.Kind.FIELD);
            vmWriter.writePush("constant", nFields);
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop("pointer", 0);
        } else if (subroutineType.equals("method")) {
            vmWriter.writePush("argument", 0);
            vmWriter.writePop("pointer", 0);
        }

        compileStatements();
        tokenizer.advance(); // skip '}'
    }

    private void compileParameterList() {
        if (!tokenizer.currentToken.equals(")")) {
            String type = tokenizer.currentToken;
            tokenizer.advance();
            String name = tokenizer.currentToken;
            symbolTable.define(name, type, SymbolTable.Kind.ARG);
            tokenizer.advance();

            while (tokenizer.currentToken.equals(",")) {
                tokenizer.advance(); // skip ','
                type = tokenizer.currentToken;
                tokenizer.advance();
                name = tokenizer.currentToken;
                symbolTable.define(name, type, SymbolTable.Kind.ARG);
                tokenizer.advance();
            }
        }
    }

    private void compileVarDec() {
        tokenizer.advance(); // skip 'var'
        String type = tokenizer.currentToken;
        tokenizer.advance();

        // First var name
        String name = tokenizer.currentToken;
        symbolTable.define(name, type, SymbolTable.Kind.VAR);
        tokenizer.advance();

        // Additional var names
        while (tokenizer.currentToken.equals(",")) {
            tokenizer.advance(); // skip ','
            name = tokenizer.currentToken;
            symbolTable.define(name, type, SymbolTable.Kind.VAR);
            tokenizer.advance();
        }

        tokenizer.advance(); // skip ';'
    }

    private void compileStatements() {
        while (true) {
            String token = tokenizer.currentToken;
            switch (token) {
                case "let":    compileLet();    break;
                case "if":     compileIf();     break;
                case "while":  compileWhile();  break;
                case "do":     compileDo();     break;
                case "return": compileReturn(); break;
                default: return;
            }
        }
    }

    private void compileLet() {
        tokenizer.advance(); // skip 'let'
        String varName = tokenizer.currentToken;
        tokenizer.advance(); // move past varName

        boolean isArray = false;
        if (tokenizer.currentToken.equals("[")) {
            // CHANGED: push index first, then array base
            isArray = true;
            tokenizer.advance();           // skip '['
            compileExpression();           // push index first
            tokenizer.advance();           // skip ']'
            pushVariable(varName);         // now push array base
            vmWriter.writeArithmetic("add");
        }

        tokenizer.advance(); // skip '='
        compileExpression();
        tokenizer.advance(); // skip ';'

        if (isArray) {
            // array assignment
            vmWriter.writePop("temp", 0);
            vmWriter.writePop("pointer", 1);
            vmWriter.writePush("temp", 0);
            vmWriter.writePop("that", 0);
        } else {
            popVariable(varName);
        }
    }

    private void compileIf() {
        String labelEnd  = currentClass + "_" + labelCounter++;
        String labelElse = currentClass + "_" + labelCounter++;

        tokenizer.advance(); // skip 'if'
        tokenizer.advance(); // skip '('
        compileExpression();
        tokenizer.advance(); // skip ')'

        vmWriter.writeArithmetic("not");
        vmWriter.writeIf(labelElse);

        tokenizer.advance(); // skip '{'
        compileStatements();
        tokenizer.advance(); // skip '}'

        vmWriter.writeGoto(labelEnd);
        vmWriter.writeLabel(labelElse);

        if (tokenizer.currentToken.equals("else")) {
            tokenizer.advance(); // skip 'else'
            tokenizer.advance(); // skip '{'
            compileStatements();
            tokenizer.advance(); // skip '}'
        }

        vmWriter.writeLabel(labelEnd);
    }

    private void compileWhile() {
        String labelExp = currentClass + "_" + labelCounter++;
        String labelEnd = currentClass + "_" + labelCounter++;

        tokenizer.advance(); // skip 'while'
        tokenizer.advance(); // skip '('

        vmWriter.writeLabel(labelExp);
        compileExpression();
        vmWriter.writeArithmetic("not");
        vmWriter.writeIf(labelEnd);

        tokenizer.advance(); // skip ')'
        tokenizer.advance(); // skip '{'
        compileStatements();
        tokenizer.advance(); // skip '}'

        vmWriter.writeGoto(labelExp);
        vmWriter.writeLabel(labelEnd);
    }

    private void compileDo() {
        tokenizer.advance(); // skip 'do'
        compileSubroutineCall();
        vmWriter.writePop("temp", 0); // discard return value
        tokenizer.advance(); // skip ';'
    }

    private void compileReturn() {
        tokenizer.advance(); // skip 'return'

        if (!tokenizer.currentToken.equals(";")) {
            compileExpression();
        } else {
            // no return expression => push 0
            vmWriter.writePush("constant", 0);
        }

        vmWriter.writeReturn();
        tokenizer.advance(); // skip ';'
    }

    private void compileSubroutineCall() {
        String firstPart = tokenizer.currentToken;
        tokenizer.advance();

        String name = "";
        String className = "";
        int nArgs = 0;

        if (tokenizer.currentToken.equals(".")) {
            String objName = firstPart;
            tokenizer.advance(); // skip '.'
            name = tokenizer.currentToken; // subroutine name
            tokenizer.advance();

            SymbolTable.Kind kind = symbolTable.kindOf(objName);
            if (kind != SymbolTable.Kind.NONE) {
                // objName is a variable; push it as first arg (this)
                pushVariable(objName);
                nArgs = 1;
                className = symbolTable.typeOf(objName);
            } else {
                // objName is a class name
                className = objName;
            }
        } else {
            // method call on this class
            name = firstPart;
            className = currentClass;
            // push 'this' as first argument
            vmWriter.writePush("pointer", 0);
            nArgs = 1;
        }

        tokenizer.advance(); // skip '('
        nArgs += compileExpressionList();
        tokenizer.advance(); // skip ')'

        vmWriter.writeCall(className + "." + name, nArgs);
    }

    private int compileExpressionList() {
        int nArgs = 0;
        if (!tokenizer.currentToken.equals(")")) {
            compileExpression();
            nArgs = 1;

            while (tokenizer.currentToken.equals(",")) {
                tokenizer.advance(); // skip ','
                compileExpression();
                nArgs++;
            }
        }
        return nArgs;
    }

    private void compileExpression() {
        compileTerm();

        while (isOperator(tokenizer.currentToken)) {
            String op = tokenizer.currentToken;
            tokenizer.advance();
            compileTerm();
            writeOperator(op);
        }
    }

    private void compileTerm() {
        String tokenType = tokenizer.tokenType();
        String token = tokenizer.currentToken;

        if (tokenType.equals(JackTokenizer.INT_CONST)) {
            vmWriter.writePush("constant", Integer.parseInt(token));
            tokenizer.advance();
        }
        else if (tokenType.equals(JackTokenizer.STRING_CONST)) {
            String str = token;
            vmWriter.writePush("constant", str.length());
            vmWriter.writeCall("String.new", 1);
            for (char c : str.toCharArray()) {
                vmWriter.writePush("constant", (int)c);
                vmWriter.writeCall("String.appendChar", 2);
            }
            tokenizer.advance();
        }
        else if (tokenType.equals(JackTokenizer.KEYWORD)) {
            switch (token) {
                case "true":
                    vmWriter.writePush("constant", 1);
                    vmWriter.writeArithmetic("neg");
                    break;
                case "false":
                case "null":
                    vmWriter.writePush("constant", 0);
                    break;
                case "this":
                    vmWriter.writePush("pointer", 0);
                    break;
            }
            tokenizer.advance();
        }
        else if (token.equals("(")) {
            tokenizer.advance();
            compileExpression();
            tokenizer.advance(); // skip ')'
        }
        else if (token.equals("-") || token.equals("~")) {
            String unaryOp = token;
            tokenizer.advance();
            compileTerm();
            vmWriter.writeArithmetic(unaryOp.equals("-") ? "neg" : "not");
        }
        else {
            // could be varName, varName[expr], subroutineCall
            String name = token;
            tokenizer.advance();

            if (tokenizer.currentToken.equals("[")) {
                // CHANGED: push index first, then array base
                tokenizer.advance(); // skip '['
                compileExpression(); // push index
                tokenizer.advance(); // skip ']'
                pushVariable(name);  // push array base
                vmWriter.writeArithmetic("add");
                vmWriter.writePop("pointer", 1);
                vmWriter.writePush("that", 0);

            } else if (tokenizer.currentToken.equals("(") ||
                    tokenizer.currentToken.equals(".")) {
                // subroutine call => we rewound one token
                tokenizer.back();
                compileSubroutineCall();
            } else {
                // just a variable reference
                pushVariable(name);
            }
        }
    }

    private void pushVariable(String name) {
        SymbolTable.Kind kind = symbolTable.kindOf(name);
        int index = symbolTable.indexOf(name);
        vmWriter.writePush(kindToSegment(kind), index);
    }

    private void popVariable(String name) {
        SymbolTable.Kind kind = symbolTable.kindOf(name);
        int index = symbolTable.indexOf(name);
        vmWriter.writePop(kindToSegment(kind), index);
    }

    private String kindToSegment(SymbolTable.Kind kind) {
        switch (kind) {
            case STATIC: return "static";
            case FIELD:  return "this";
            case ARG:    return "argument";
            case VAR:    return "local";
            default:     throw new IllegalArgumentException("Unknown kind: " + kind);
        }
    }

    private boolean isOperator(String token) {
        return "+-*/&|<>=".contains(token);
    }

    private void writeOperator(String op) {
        switch (op) {
            case "+": vmWriter.writeArithmetic("add"); break;
            case "-": vmWriter.writeArithmetic("sub"); break;
            case "*": vmWriter.writeCall("Math.multiply", 2); break;
            case "/": vmWriter.writeCall("Math.divide", 2);   break;
            case "&": vmWriter.writeArithmetic("and");  break;
            case "|": vmWriter.writeArithmetic("or");   break;
            case "<": vmWriter.writeArithmetic("lt");   break;
            case ">": vmWriter.writeArithmetic("gt");   break;
            case "=": vmWriter.writeArithmetic("eq");   break;
        }
    }
}