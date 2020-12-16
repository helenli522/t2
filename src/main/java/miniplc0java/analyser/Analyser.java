package miniplc0java.analyser;

import miniplc0java.error.*;
import miniplc0java.instruction.Instruction;
import miniplc0java.instruction.Operation;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.util.Pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    /** 当前偷看的 token */
    Token peekedToken = null;

    /** 符号表 */
    HashMap<String, SymbolEntry> symbolTable = new HashMap<>();

    /** 下一个变量的栈偏移 */
    int nextOffset = 0;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
    }

    public List<Instruction> analyse() throws CompileError {
        analyseProgram();
        return instructions;
    }

    /**
     * 查看下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            Token token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            return tokenizer.nextToken();
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     *
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        Token token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     *
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        Token token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     *
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        Token token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

    /**
     * 获取下一个变量的栈偏移
     *
     * @return
     */
    private int getNextVariableOffset() {
        return this.nextOffset++;
    }

    /**
     * 添加一个符号
     *
     * @param name          名字
     * @param isInitialized 是否已赋值
     * @param isConstant    是否是常量
     * @param curPos        当前 token 的位置（报错用）
     * @throws AnalyzeError 如果重复定义了则抛异常
     */
    private void addSymbol(String name, boolean isInitialized, boolean isConstant, int type, Pos curPos) throws AnalyzeError {
        if (this.symbolTable.get(name) != null) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
        } else {
            this.symbolTable.put(name, new SymbolEntry(name,isConstant,isInitialized,type,getNextVariableOffset()));
        }
    }

    /**
     * 设置符号为已赋值
     *
     * @param name   符号名称
     * @param curPos 当前位置（报错用）
     * @throws AnalyzeError 如果未定义则抛异常
     */
    private void initializeSymbol(String name, Pos curPos) throws AnalyzeError {
        SymbolEntry entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            entry.setInitialized(true);
        }
    }

    /**
     * 获取变量在栈上的偏移
     *
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 栈偏移
     * @throws AnalyzeError
     */
    private int getOffset(String name, Pos curPos) throws AnalyzeError {
        SymbolEntry entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.getStackOffset();
        }
    }

    /**
     * 获取变量是否是常量
     *
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 是否为常量
     * @throws AnalyzeError
     */
    private boolean isConstant(String name, Pos curPos) throws AnalyzeError {
        SymbolEntry entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.isConstant();
        }
    }

    private void analyseProgram() throws CompileError {
        // program -> decl_stmt* function*
        while(check(TokenType.LET_KW) || check(TokenType.CONST_KW))
            analyseDeclStmt();
        while(check(TokenType.FN_KW))
            analyseFunction();
        expect(TokenType.EOF);
    }

    private void analyseDeclStmt() throws CompileError {
        // decl_stmt -> let_decl_stmt | const_decl_stmt
        if(check(TokenType.LET_KW))
            analyseLetDeclStmt();
        else if(check(TokenType.CONST_KW))
            analyseConstDeclStmt();
        // Error?
    }

    private void analyseFunction() throws CompileError {
        throw new Error("Not implemented");
    }

    private void analyseLetDeclStmt() throws CompileError {
        while(nextIf(TokenType.LET_KW) != null) {
            // let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'
            expect(TokenType.IDENT);
            expect(TokenType.COLON);
            Token type = expect(TokenType.IDENT);
            if(!type.getValueString().equals("int"))
                throw new Error("InvalidVariableType");
            if(check(TokenType.ASSIGN)){
                next();
                analyseExpr();
            }
            expect(TokenType.SEMICOLON);
        }
    }

    private void analyseConstDeclStmt() throws CompileError {
        // const_decl_stmt -> 'const' IDENT ':' ty '=' expr ';'
        while(nextIf(TokenType.CONST_KW) != null){
            expect(TokenType.IDENT);
            expect(TokenType.COLON);
            Token type = expect(TokenType.IDENT);
            if(!type.getValueString().equals("int"))
                throw new Error("InvalidVariableType");
            expect(TokenType.ASSIGN);
            analyseExpr();
            expect(TokenType.SEMICOLON);
        }
    }

    private void analyseStatementSequence() throws CompileError {
        // 语句序列 -> 语句*
        // 语句 -> 赋值语句 | 输出语句 | 空语句

        while (true) {
            // 如果下一个 token 是……
            Token peeked = peek();
            if (peeked.getTokenType() == TokenType.IDENT) {
                // 调用相应的分析函数
                // 如果遇到其他非终结符的 FIRST 集呢？
            } else {
                // 都不是，摸了
                break;
            }
        }
        throw new Error("Not implemented");
    }

    private int analyseExpr() throws CompileError {
//        expr ->
//                operator_expr
//                | negate_expr
//                | assign_expr
//                | as_expr
//                | call_expr
//                | literal_expr
//                | ident_expr
//                | group_expr
        return 0;
    }
//    private int analyseConstantExpression() throws CompileError {
//        // 常表达式 -> 符号? 无符号整数
//        boolean negative = false;
//        if (nextIf(TokenType.Plus) != null) {
//            negative = false;
//        } else if (nextIf(TokenType.Minus) != null) {
//            negative = true;
//        }
//
//        var token = expect(TokenType.Uint);
//
//        int value = (int) token.getValue();
//        if (negative) {
//            value = -value;
//        }
//
//        return value;
//    }

//    private void analyseExpression() throws CompileError {
//        // 表达式 -> 项 (加法运算符 项)*
//        // 项
//        analyseItem();
//
//        while (true) {
//            // 预读可能是运算符的 token
//            var op = peek();
//            if (op.getTokenType() != TokenType.Plus && op.getTokenType() != TokenType.Minus) {
//                break;
//            }
//
//            // 运算符
//            next();
//
//            // 项
//            analyseItem();
//
//            // 生成代码
//            if (op.getTokenType() == TokenType.Plus) {
//                instructions.add(new Instruction(Operation.ADD));
//            } else if (op.getTokenType() == TokenType.Minus) {
//                instructions.add(new Instruction(Operation.SUB));
//            }
//        }
//    }

    private void analyseAssignmentStatement() throws CompileError {
        // 赋值语句 -> 标识符 '=' 表达式 ';'

        // 分析这个语句

        // 标识符是什么？
        String name = null;
        SymbolEntry symbol = symbolTable.get(name);
        if (symbol == null) {
            // 没有这个标识符
            throw new AnalyzeError(ErrorCode.NotDeclared, /* 当前位置 */ null);
        } else if (symbol.isConstant()) {
            // 标识符是常量
            throw new AnalyzeError(ErrorCode.AssignToConstant, /* 当前位置 */ null);
        }
        // 设置符号已初始化
        initializeSymbol(name, null);

        // 把结果保存
        int offset = getOffset(name, null);
        instructions.add(new Instruction(Operation.STO, offset));
    }

    private void analyseItem() throws CompileError {
        // 项 -> 因子 (乘法运算符 因子)*

        // 因子

        while (true) {
            // 预读可能是运算符的 token
            Token op = null;

            // 运算符

            // 因子

            // 生成代码
            if (op.getTokenType() == TokenType.MUL) {
                instructions.add(new Instruction(Operation.MUL));
            } else if (op.getTokenType() == TokenType.DIV) {
                instructions.add(new Instruction(Operation.DIV));
            }
        }
    }

    private void analyseFactor() throws CompileError {
        // 因子 -> 符号? (标识符 | 无符号整数 | '(' 表达式 ')')

        boolean negate;
        if (nextIf(TokenType.MINUS) != null) {
            negate = true;
            // 计算结果需要被 0 减
            instructions.add(new Instruction(Operation.LIT, 0));
        } else {
            nextIf(TokenType.PLUS);
            negate = false;
        }

        if (check(TokenType.IDENT)) {
            // 是标识符

            // 加载标识符的值
            String name = /* 快填 */ null;
            SymbolEntry symbol = symbolTable.get(name);
            if (symbol == null) {
                // 没有这个标识符
                throw new AnalyzeError(ErrorCode.NotDeclared, /* 当前位置 */ null);
            } else if (!symbol.isInitialized) {
                // 标识符没初始化
                throw new AnalyzeError(ErrorCode.NotInitialized, /* 当前位置 */ null);
            }
            int offset = getOffset(name, null);
            instructions.add(new Instruction(Operation.LOD, offset));
        } else if (check(TokenType.UINT_LITERAL)) {
            // 是整数
            // 加载整数值
            int value = 0;
            instructions.add(new Instruction(Operation.LIT, value));
        } else if (check(TokenType.L_PAREN)) {
            // 是表达式
            // 调用相应的处理函数
        } else {
            // 都不是，摸了
            List<TokenType> tokenTypes = new ArrayList<>();
            tokenTypes.add(TokenType.IDENT);
            tokenTypes.add(TokenType.UINT_LITERAL);
            tokenTypes.add(TokenType.L_PAREN);
            throw new ExpectedTokenError(tokenTypes, next());
        }

        if (negate) {
            instructions.add(new Instruction(Operation.SUB));
        }
        throw new Error("Not implemented");
    }
}