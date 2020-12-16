package miniplc0java.analyser;

import miniplc0java.tokenizer.Tokenizer;

public class ExprParser {
    private final Tokenizer tokenizer;
    private final VariableChecker checker;

    public ExprParser(Tokenizer tokenizer, VariableChecker checker) {
        this.tokenizer = tokenizer;
        this.checker = checker;
    }
    /**
     * OPG
     * A -> i = A | B    // look ahead 2 token
     * B -> B < C | B > C | B <= C | B >= C | B == C | B != C | C
     * C -> C + D | C - D | D
     * D -> D * E | D / E | E
     * E -> E as id | F
     * F -> -F | G
     * G -> id() | id(H) | I // look ahead 2 token
     * H -> A, H | A
     * I -> (A) | id | literal
     * <p>
     * a grammar that removing left recursion, parsed by LL(2)
     * A -> i = A | B
     * B -> C { < C | > C | <= C | >= C | == C | != C }
     * C -> D { + D | - D }
     * D -> E { * E | / E }
     * E -> F { as id }
     * F -> -F | G
     * G -> id() | id(H) | I
     * H -> A, H | A
     * I -> (A) | id | literal
     */

}
