package miniplc0java.tokenizer;

public enum TokenType {
    /** 空 */
    None,
    /** 文件尾 */
    EOF,
    /** 标识符 */
    IDENT,

    // 关键字
    /** fn */
    FN_KW,
    /** let */
    LET_KW,
    /** const */
    CONST_KW,
    /** as */
    AS_KW,
    /** while */
    WHILE_KW,
    /** if */
    IF_KW,
    /** else */
    ELSE_KW,
    /** return */
    RETURN_KW,

    // 扩展关键字
    BREAK_KW,
    CONTINUE_KW,

    // 字面量
    /** 无符号整数 */
    UINT_LITERAL,
    /** 字符串常量 */
    STRING_LITERAL,

    // 扩展字面量
    DOUBLE_LITERAL,
    CHAR_LITERAL,

    // 运算符
    /** + */
    PLUS,
    /** - */
    MINUS,
    /** * */
    MUL,
    /** / */
    DIV,
    /** = */
    ASSIGN,
    /** == */
    EQ,
    /** != */
    NEQ,
    /** < */
    LT,
    /** > */
    GT,
    /** <= */
    LE,
    /** >= */
    GE,
    /** ( */
    L_PAREN,
    /** ) */
    R_PAREN,
    /** { */
    L_BRACE,
    /** } */
    R_BRACE,
    /** -> */
    ARROW,
    /** , */
    COMMA,
    /** : */
    COLON,
    /** ; */
    SEMICOLON,

    // 注释
    COMMENT;

    @Override
    public String toString() {
        switch (this) {
            case None:
                return "NullToken";
            case IDENT:
                return "Identifier";
            // 关键字
            case FN_KW:
                return "Fn";
            case LET_KW:
                return "Let";
            case CONST_KW:
                return "Const";
            case AS_KW:
                return "As";
            case WHILE_KW:
                return "While";
            case IF_KW:
                return "If";
            case ELSE_KW:
                return "Else";
            case RETURN_KW:
                return "Return";
            // 字面量
            case UINT_LITERAL:
                return "UnsignedInteger";
            case STRING_LITERAL:
                return "String";
            // 运算符
            case PLUS:
                return "PlusSign";
            case MINUS:
                return "MinusSign";
            case MUL:
                return "MulSign";
            case DIV:
                return "DivSign";
            case ASSIGN:
                return "AssignSign";
            case EQ:
                return "EQSign";
            case NEQ:
                return "NEQSign";
            case LT:
                return "LTSign";
            case GT:
                return "GTSign";
            case LE:
                return "LESign";
            case GE:
                return "GESign";
            case L_PAREN:
                return "LeftBracket";
            case R_PAREN:
                return "RightBracket";
            case L_BRACE:
                return "LeftBigBracket";
            case R_BRACE:
                return "RightBigBracket";
            case ARROW:
                return "ArrowSign";
            case COMMA:
                return "CommaSign";
            case COLON:
                return "ColonSign";
            case SEMICOLON:
                return "SemicolonSign";
            case EOF:
                return "EOF";
            //扩展
            case BREAK_KW:
                return "break";
            case CONTINUE_KW:
                return "continue";
            case DOUBLE_LITERAL:
                return "double";
            case CHAR_LITERAL:
                return "char";
            case COMMENT:
                return "comment";
            default:
                return "InvalidToken";
        }
    }
}
