package miniplc0java.tokenizer;

import miniplc0java.error.ErrorCode;
import miniplc0java.error.TokenizeError;

public class Tokenizer {

    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
    /**
     * 获取下一个 Token
     * 
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar();
        if (Character.isDigit(peek)) {
            return lexUIntOrDouble();
        }
        else if (Character.isAlphabetic(peek)) {
            return lexIdentOrKeyword();
        }
        else if(peek == '_') {
            return lexIdent();
        }
        else if(peek == '"') {
            return lexString();
        }
        else if(peek == '\'') {
            return lexString();
        }
        else {
            return lexOperatorOrUnknown();
        }
    }

    private Token lexUIntOrDouble() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字为止:
        // -- 前进一个字符，并存储这个字符
        String str = "";
        int val = 0;
        while(Character.isDigit(it.peekChar())){
            str += it.nextChar();
        }
        val = Integer.parseInt(str);
        // 解析存储的字符串为无符号整数
        // 解析成功则返回无符号整数类型的token，否则返回编译错误
        //
        // Token 的 Value 应填写数字的值
        return new Token(TokenType.UINT_LITERAL, val, it.previousPos(), it.currentPos());
    }

    private Token lexIdentOrKeyword() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字或字母为止:
        // -- 前进一个字符，并存储这个字符
        String str = "";
        while(Character.isLetterOrDigit(it.peekChar())){
            str += it.nextChar();
        }
        // 尝试将存储的字符串解释为关键字
        // -- 如果是关键字，则返回关键字类型的 token
        // -- 否则，返回标识符
        if(str.equals("fn")) return new Token(TokenType.FN_KW , "fn", it.previousPos(), it.currentPos());
        else if(str.equals("let")) return new Token(TokenType.LET_KW , "let", it.previousPos(), it.currentPos());
        else if(str.equals("const")) return new Token(TokenType.CONST_KW, "const", it.previousPos(), it.currentPos());
        else if(str.equals("as")) return new Token(TokenType.AS_KW , "as", it.previousPos(), it.currentPos());
        else if(str.equals("while")) return new Token(TokenType.WHILE_KW , "while", it.previousPos(), it.currentPos());
        else if(str.equals("if")) return new Token(TokenType.IF_KW , "if", it.previousPos(), it.currentPos());
        else if(str.equals("else")) return new Token(TokenType.ELSE_KW , "else", it.previousPos(), it.currentPos());
        else if(str.equals("return"))  return new Token(TokenType.RETURN_KW,"return", it.previousPos(), it.currentPos());
        /**扩展c0*/
        else if(str.equals("break"))  return new Token(TokenType.BREAK_KW,"break", it.previousPos(), it.currentPos());
        else if(str.equals("continue"))  return new Token(TokenType.CONTINUE_KW,"continue", it.previousPos(), it.currentPos());
        else return new Token(TokenType.IDENT, str, it.previousPos(), it.currentPos());
    }

    private Token lexIdent() throws TokenizeError {
        String str = "";
        while(Character.isLetterOrDigit(it.peekChar())){
            str += it.nextChar();
        }
        return new Token(TokenType.IDENT, str, it.previousPos(), it.currentPos());
    }

    private Token lexString() throws TokenizeError {
        String str = "\"";
        char peek;
        it.nextChar();
        while(true) {
            peek = it.peekChar();
            if(peek != '"'){
                str += it.nextChar();
            }
            else{ //判断是不是转义的双引号
                if(str.length()>1 && str.charAt(str.length()-1) == '\\'){
                    if(str.charAt(str.length()-2) == '\\'){ //说明这个斜杠不是用来转义双引号的
                        str += it.nextChar();
                        break;
                    }
                    else{
                        str += it.nextChar();
                    }
                }
                else{
                    str += it.nextChar();
                    break;
                }
            }
        }
        return new Token(TokenType.STRING_LITERAL, str, it.previousPos(), it.currentPos());
    }

    private Token lexOperatorOrUnknown() throws TokenizeError {
        switch (it.nextChar()) {
            //无歧义的运算符
            case '+':
                return new Token(TokenType.PLUS, '+', it.previousPos(), it.currentPos());
            case '*':
                return new Token(TokenType.MUL, '*', it.previousPos(), it.currentPos());
            case '/':
                return new Token(TokenType.DIV, '/', it.previousPos(), it.currentPos());
            case '(':
                return new Token(TokenType.L_PAREN, '(', it.previousPos(), it.currentPos());
            case ')':
                return new Token(TokenType.R_PAREN, ')', it.previousPos(), it.currentPos());
            case '{':
                return new Token(TokenType.L_BRACE, '{', it.previousPos(), it.currentPos());
            case '}':
                return new Token(TokenType.R_BRACE, '}', it.previousPos(), it.currentPos());
            case ',':
                return new Token(TokenType.COMMA, ',', it.previousPos(), it.currentPos());
            case ':':
                return new Token(TokenType.COLON, ':', it.previousPos(), it.currentPos());
            case ';':
                return new Token(TokenType.SEMICOLON, ';', it.previousPos(), it.currentPos());
            //区别 MINUS 和 ARROW
            case '-':
                if(it.peekChar() == '>'){
                    it.nextChar();
                    return new Token(TokenType.ARROW, "->", it.previousPos(), it.currentPos());
                }
                else{
                    return new Token(TokenType.MINUS, '-', it.previousPos(), it.currentPos());
                }
            //区别 EQ 和 ASSIGN
            case '=':
                if(it.peekChar() == '='){
                    it.nextChar();
                    return new Token(TokenType.EQ, "==", it.previousPos(), it.currentPos());
                }
                else{
                    return new Token(TokenType.ASSIGN, '=', it.previousPos(), it.currentPos());
                }
            //判断 NEQ
            case '!':
                if(it.peekChar() == '='){
                    it.nextChar();
                    return new Token(TokenType.NEQ, "!=", it.previousPos(), it.currentPos());
                }
                else{
                    throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
                }
            //判断 LT 和 LE
            case '<':
                if(it.peekChar() == '='){
                    it.nextChar();
                    return new Token(TokenType.LE, "<=", it.previousPos(), it.currentPos());
                }
                else{
                    return new Token(TokenType.LT, '<', it.previousPos(), it.currentPos());
                }
            //判断 GT 和 GE
            case '>':
                if(it.peekChar() == '='){
                    it.nextChar();
                    return new Token(TokenType.LE, ">=", it.previousPos(), it.currentPos());
                }
                else{
                    return new Token(TokenType.LT, '>', it.previousPos(), it.currentPos());
                }

            default:
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }
}
