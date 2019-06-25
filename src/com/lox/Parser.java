package com.lox;

import java.util.ArrayList;
import java.util.List;

import static com.lox.TokenType.*;

/*
    expression     → equality ;
    equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    comparison     → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
    addition       → multiplication ( ( "-" | "+" ) multiplication )* ;
    multiplication → unary ( ( "/" | "*" ) unary )* ;
    unary          → ( "!" | "-" ) unary
                   | primary ;
    primary        → NUMBER | STRING | "false" | "true" | "nil"
                   | "(" expression ")" ;

    program   → statement* EOF ;
    statement → exprStmt
              | printStmt ;

    exprStmt  → expression ";" ;
    printStmt → "print" expression ";" ;
*/

public class Parser {
    private List<Token> tokens;
    private int current = 0;

    static class ParseError extends RuntimeException {}
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        try {
            return program();
        } catch (ParseError e) {
            return null;
        }
    }

    private List<Stmt> program() {
        List<Stmt> stmts = new ArrayList<>();
        while (!isAtEnd()) {
            stmts.add(declaration());
        }
        consume(EOF, "Expected EOF marker.");
        return stmts;
    }

    private Stmt declaration() {
        if (match(VAR))
            return varDeclaration();
        return statement();
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expected var name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }
    }

    private Stmt statement() {
        if (match(PRINT)) return printStmt();
        return exprStmt();
    }

    private Stmt exprStmt() {
        Expr value = expression();
        consume(SEMICOLON, "Expected ; after value");
        return new Stmt.Expression(value);
    }

    private Stmt printStmt() {
        Expr value = expression();
        consume(SEMICOLON, "Expected ; after value");
        return new Stmt.Print(value);
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token op = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr left = addition();
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token op = previous();
            Expr right = addition();
            left = new Expr.Binary(left, op, right);
        }
        return left;
    }

    private Expr addition() {
        Expr left = multiplication();
        while (match(PLUS, MINUS)) {
            Token op = previous();
            Expr right = multiplication();
            left = new Expr.Binary(left, op, right);
        }
        return  left;
    }

    private Expr multiplication() {
        Expr left = unary();
        while (match(STAR, SLASH)) {
            Token op = previous();
            Expr right = unary();
            left = new Expr.Binary(left, op, right);
        }
        return  left;
    }

    private Expr unary() {
        if (match(PLUS, MINUS, BANG)) {
            return new Expr.Unary(previous(), unary());
        }

        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) {
            return new Expr.Literal(false);
        }
        if (match(TRUE)) {
            return new Expr.Literal(true);
        }
        if (match(NIL)) {
            return new Expr.Literal(null);
        }
        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expected ')' after expression");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Invalid token.");
    }

    private ParseError error(Token token, String msg) {
        Main.error(token, msg);
        return new ParseError();
    }

    public void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().tokenType == SEMICOLON) return;
            switch (peek().tokenType) {
                case FOR:
                case WHILE:
                case IF:
                case VAR:
                case CLASS:
                case FUN:
                case RETURN:
                case SUPER:
                    return;
            }
            advance();
        }
    }

    private boolean isAtEnd() {
        return peek().tokenType == EOF;
    }

    private Token advance() {
        current++;
        return previous();
    }

    private Token peek() {
        return  tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private void consume(TokenType tokenType, String msg) {
        if (tokenType == peek().tokenType) {
            advance();
            return;
        }

        throw error(peek(), msg);
    }

    private boolean match(TokenType ... types) {
        for (TokenType type : types) {
            if (peek().tokenType == type) {
                advance();
                return true;
            }
        }
        return false;
    }

}
