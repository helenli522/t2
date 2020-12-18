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
        //let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'
        expect(TokenType.LET_KW);
        expect(TokenType.IDENT);
        expect(TokenType.COLON);
        analyseTy();
        if(check(TokenType.ASSIGN)){
            next();
            analyseExpr();
        }
        expect(TokenType.SEMICOLON);
    }

    private void analyseConstDeclStmt() throws CompileError {
        //const_decl_stmt -> 'const' IDENT ':' ty '=' expr ';'
        expect(TokenType.CONST_KW);
        expect(TokenType.IDENT);
        expect(TokenType.COLON);
        analyseTy();
        expect(TokenType.ASSIGN);
        analyseExpr();
        expect(TokenType.SEMICOLON);
    }

    /**类型系统*/
    private void analyseTy() throws CompileError {
        //ty -> IDENT 只能是void和int
        Token tt=peek();
        if(tt.getValue().equals("void")||tt.getValue().equals("int")){
            next();
        }
        //否则抛出异常
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

    /**表达式*/
    private void analyseExpr() throws CompileError {
        //expr ->
        //    | negate_expr
        //    | assign_expr
        //    | call_expr
        //    | literal_expr
        //    | ident_expr
        //    | group_expr
        //     (binary_operator expr||'as' ty)*
        if(check(TokenType.MINUS)) analyseNegateExpr();
        else if(check(TokenType.L_PAREN)) analyseGroupExpr();
        else if(check(TokenType.UINT_LITERAL)||check(TokenType.DOUBLE_LITERAL)||check(TokenType.STRING_LITERAL)) analyseLiteralExpr();
        else if(check(TokenType.IDENT)){
            //三个以IDENT开头的非终结符
            next();
            if(check(TokenType.L_PAREN)) analyseCallExpr();
            else if(check(TokenType.ASSIGN)) analyseAssignExpr();
            //可能要存符号表
        }
        while(check(TokenType.AS_KW)||check(TokenType.PLUS)||check(TokenType.MINUS)||check(TokenType.MUL)||check(TokenType.DIV)||check(TokenType.EQ)||check(TokenType.NEQ)||check(TokenType.LT)||check(TokenType.GT)||check(TokenType.LE)||check(TokenType.GE)){
            if(check(TokenType.AS_KW)) analyseAsExpr();
            else analyseOperatorExpr();
        }
    }
    /**运算符表达式*/
    private void analyseBinaryOperator() throws CompileError {
        //binary_operator -> '+' | '-' | '*' | '/' | '==' | '!=' | '<' | '>' | '<=' | '>='
        if(check(TokenType.PLUS)||check(TokenType.MINUS)||check(TokenType.MUL)||check(TokenType.DIV)||check(TokenType.EQ)||check(TokenType.NEQ)||check(TokenType.LT)||check(TokenType.GT)||check(TokenType.LE)||check(TokenType.GE)){
            next();
        }
        //记得抛异常
    }
    private void analyseOperatorExpr() throws CompileError {
        //operator_expr -> expr binary_operator expr
        //消除左递归
        analyseBinaryOperator();
        analyseExpr();
    }
    /**取反表达式*/
    private void analyseNegateExpr() throws CompileError {
        //negate_expr -> '-' expr
        expect(TokenType.MINUS);
        analyseExpr();
    }
    /**赋值表达式*/
    private void analyseAssignExpr() throws CompileError {
        //assign_expr -> l_expr '=' expr
        //l_expr已经判断过了
        expect(TokenType.ASSIGN);
        analyseExpr();
    }
    /**类型转换表达式*/
    private void analyseAsExpr() throws CompileError {
        //as_expr -> expr 'as' ty
        //消除左递归
        expect(TokenType.AS_KW);
        analyseTy();
    }
    /**函数调用表达式*/
    private void analyseCallParamList() throws CompileError {
        //call_param_list -> expr (',' expr)*
        analyseExpr();
        while(check(TokenType.COMMA)){
            next();
            analyseExpr();
        }
    }
    private void analyseCallExpr() throws CompileError {
        //call_expr -> IDENT '(' call_param_list? ')'
        //IDENT判断过了
        expect(TokenType.L_PAREN);
        if(!check(TokenType.R_PAREN)){
            analyseCallParamList();
        }
        expect(TokenType.R_PAREN);
    }
    /**字面量表达式*/
    private void analyseLiteralExpr() throws CompileError {
        //literal_expr -> UINT_LITERAL | DOUBLE_LITERAL | STRING_LITERAL
        if(check(TokenType.UINT_LITERAL)||check(TokenType.DOUBLE_LITERAL)||check(TokenType.STRING_LITERAL)){
            next();
        }
        //记得抛异常
    }

    /**标识符表达式*/
    //private void analyseIdentExpr() throws CompileError {
    //   //ident_expr -> IDENT
    //   expect(TokenType.IDENT);
    //
    //}

    /**括号表达式*/
    private void analyseGroupExpr() throws CompileError {
        //group_expr -> '(' expr ')'
        expect(TokenType.L_PAREN);
        analyseExpr();
        expect(TokenType.R_PAREN);
    }

    /**语句*/
    private void analyseStmt() throws CompileError {
        //stmt ->
        //      expr_stmt
        //    | decl_stmt *
        //    | if_stmt *
        //    | while_stmt *
        //    | return_stmt *
        //    | block_stmt *
        //    | empty_stmt *
        if(check(TokenType.IF_KW)) analyseIfStmt();
        else if(check(TokenType.WHILE_KW)) analyseWhileStmt();
        else if(check(TokenType.RETURN_KW)) analyseReturnStmt();
        else if(check(TokenType.L_BRACE)) analyseBlockStmt();
        else if(check(TokenType.SEMICOLON)) analyseEmptyStmt();
        else if(check(TokenType.LET_KW)||check(TokenType.CONST_KW)) analyseDeclStmt();
        else analyseExprStmt();
    }
    /**表达式语句*/
    private void analyseExprStmt() throws CompileError {
        //expr_stmt -> expr ';'
        analyseExpr();
        expect(TokenType.SEMICOLON);
    }
    /**控制流语句*/
    private void analyseIfStmt() throws CompileError {
        //if_stmt -> 'if' expr block_stmt ('else' (block_stmt | if_stmt))?
        expect(TokenType.IF_KW);
        analyseExpr();
        analyseBlockStmt();
        if(check(TokenType.ELSE_KW)){
            expect(TokenType.ELSE_KW);
            if(check(TokenType.L_BRACE)) analyseBlockStmt();
            else if(check(TokenType.IF_KW)) analyseIfStmt();
            //记得抛出异常
        }
    }
    private void analyseWhileStmt() throws CompileError {
        //while_stmt -> 'while' expr block_stmt
        expect(TokenType.WHILE_KW);
        analyseExpr();
        analyseBlockStmt();
    }
    private void analyseReturnStmt() throws CompileError {
        //return_stmt -> 'return' expr? ';'
        expect(TokenType.RETURN_KW);
        if(!check(TokenType.SEMICOLON)) analyseExpr();
        expect(TokenType.SEMICOLON);
    }
    /**代码块*/
    private void analyseBlockStmt() throws CompileError {
        //block_stmt -> '{' stmt* '}'
        expect(TokenType.L_BRACE);
        while(!check(TokenType.R_BRACE)) analyseStmt();
        expect(TokenType.R_BRACE);
    }
    /**空语句*/
    private void analyseEmptyStmt() throws CompileError {
        //empty_stmt -> ';'
        expect(TokenType.SEMICOLON);
    }
}