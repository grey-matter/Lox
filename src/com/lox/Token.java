package com.lox;

public class Token {
    TokenType tokenType;
    String lexeme;
    Object literal;
    int line;

    public Token(TokenType tokenType, String lexeme, Object literal, int line) {
        this.tokenType = tokenType;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString() {
        return tokenType + " " + lexeme + " " + literal;
    }
}
