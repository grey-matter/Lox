package com.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.lox.TokenType.*;

/*
    program     → declaration* EOF ;

    declaration → funDecl | varDecl
                | statement ;
    funDecl -> "fun" IDENTIFIER "(" parameters? ")" block;
    parameters -> IDENTIFIER ( "," IDENTIFIER )*    ;

    statement → exprStmt
          | printStmt
          | block
          | ifStmt
          | whileStmt
          | forStmt
          | returnStmt;

    returnStmt -> "return" expression? ";" ;
    forStmt -> "for" "(" ( varDecl | exprStmt ) expression? ";" expression ")" statement

    whileStmt -> "while" "(" expression ")" statement ;

    ifStmt -> "if" "(" expression ")" statement ("else" statement)? ;

    block     → "{" declaration* "}" ;

    varDecl → "var" IDENTIFIER ( "=" expression )? ";" ;

    exprStmt → expression ";" ;
    printStmt → "print" expression ";" ;

    expression → assignment ;
    assignment → IDENTIFIER "=" assignment
               | logic_or ;
    logic_or   → logic_and ( "or" logic_and )* ;
    logic_and  → equality ( "and" equality )* ;
    equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    comparison     → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
    addition       → multiplication ( ( "-" | "+" ) multiplication )* ;
    multiplication → unary ( ( "/" | "*" ) unary )* ;
    unary → ( "!" | "-" ) unary | call ;
    call  → primary ( "(" arguments? ")" )* ;
    arguments → expression ( "," expression )* ;

    primary → "true" | "false" | "nil"
    | NUMBER | STRING
    | "(" expression ")"
    | IDENTIFIER ;


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
        try {
            if (match(FUN))
                return funcDeclaration("function");
            if (match(VAR))
                return varDeclaration();
            return statement();
        } catch (ParseError e) {
            synchronize();
            return null;
        }
    }

    private Stmt funcDeclaration(String kind) {
        Token identifier = consume(IDENTIFIER, "Expected " + kind + " name.");

        consume(LEFT_PAREN, "Expected '(' after " + kind + " name.");

        List<Token> params = new ArrayList<>();
        if (peek().tokenType != RIGHT_PAREN) {
            do {
                params.add(consume(IDENTIFIER, "Expected parameter name."));
                if (params.size() >= 8)
                    error(peek(), "Parameters cannot be more than 8");
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expected ')'.");

        consume(LEFT_BRACE, "Expected '{' before " + kind + " body.");
        Stmt.Block statements = block();
        return new Stmt.Function(identifier, params, statements);
    }

    private Stmt.Block block() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd() && peek().tokenType != RIGHT_BRACE) {
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, "Expected matching '}', found '" + peek().lexeme + "'");
        return new Stmt.Block(statements);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expected var name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }
        consume(SEMICOLON, "Expected ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(FOR)) return forStmt();
        if (match(WHILE)) return whileStmt();
        if (match(IF)) return ifStmt();
        if (match(PRINT)) return printStmt();
        if (match(LEFT_BRACE)) return block();
        if (match(RETURN)) return returnStmt();
        return exprStmt();
    }

    private Stmt returnStmt() {
        Token keyword = previous();
        Expr expr = null;
        if (peek().tokenType != SEMICOLON) {
            expr = expression();
        }
        consume(SEMICOLON, "Expected ';' after return statement.");
        return new Stmt.Return(expr, keyword);
    }

    private Stmt forStmt() {
        consume(LEFT_PAREN, "Expected '(' after 'for'.");

        Stmt initializer = null;
        if (!match(SEMICOLON)) {
            if (match(VAR)) {
                initializer = varDeclaration();
            } else {
                initializer = exprStmt();
            }
        }

        Expr condition = new Expr.Literal(true);
        if (!match(SEMICOLON)) {
            condition = expression();
            consume(SEMICOLON, "Expected ';'.");
        }

        Expr increment = null;
        if (!match(RIGHT_PAREN)) {
            increment = expression();
            consume(RIGHT_PAREN, "Expected ')'.");
        }

        Stmt statement = statement();

        if (increment != null)
            statement = new Stmt.Block(Arrays.asList(statement, new Stmt.Expression(increment)));

        statement = new Stmt.While(condition, statement);

        if (initializer != null)
            return new Stmt.Block(Arrays.asList(initializer, statement));
        return statement;
    }

    private Stmt whileStmt() {
        consume(LEFT_PAREN, "Expected '(' " +
                "after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after condition.");

        return new Stmt.While(condition, statement());
    }

    private Stmt ifStmt() {
        consume(LEFT_PAREN, "Expected '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after condition.");

        Stmt thenBranch = statement();
        if (match(ELSE)) return new Stmt.If(condition, thenBranch, statement());
        return new Stmt.If(condition, thenBranch, null);
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
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(EQUAL)) {
            if (expr instanceof Expr.Variable) {
                return new Expr.Assign(((Expr.Variable) expr).name, assignment());
            }
            throw error(previous(), "Invalid assignment target.");
        }
        return expr;
    }

    private Expr or() {
        Expr expr = and();
        while (match(OR)) {
            expr = new Expr.Logical(expr, previous(), and());
        }
        return expr;
    }

    private Expr and() {
        Expr expr = equality();
        while (match(AND)) {
            expr = new Expr.Logical(expr, previous(), equality());
        }
        return expr;
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

        return call();
    }

    private Expr call() {
        Expr expr = primary();
        if (match(LEFT_PAREN)) {
            Token rightParen = peek();
            List<Expr> arguments = argument();
            consume(RIGHT_PAREN, "Expected ')' after arguments.");
            expr = new Expr.Call(expr, rightParen, arguments);
        }
        return expr;
    }

    private List<Expr> argument() {
        List<Expr> arguments = new ArrayList<>();
        do {
            arguments.add(expression());
            if (arguments.size() >= 8) {
                error(peek(), "Cannot have more than 8 args for a method.");
            }
        } while (match(COMMA));
        return arguments;
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
        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
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
//         advance();

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

    private Token consume(TokenType tokenType, String msg) {
        if (tokenType == peek().tokenType) {
            advance();
            return previous();
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
