package com.lox;

import java.util.List;

abstract class Stmt {
    interface Visitor<R> {
        R visitExpressionStmt(Expression stmt);
        R visitPrintStmt(Print stmt);
        R visitVarStmt(Var stmt);
        R visitBlockStmt(Block stmt);
    }
    abstract <T> void accept(Visitor<T> visitor);
    static class Expression extends Stmt {
        Expression(Expr expression) {
            this.expression = expression;
        }

        final Expr expression;

        @Override
        <T> void accept(Visitor<T> visitor) {
            visitor.visitExpressionStmt(this);
        }
    }
    static class Print extends Stmt {
        Print(Expr expression) {
            this.expression = expression;
        }

        final Expr expression;

        @Override
        <T> void accept(Visitor<T> visitor) {
            visitor.visitPrintStmt(this);
        }
    }

    static class Var extends Stmt {
        public Var(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        final Token name;
        final Expr initializer;

        @Override
        <T> void accept(Visitor<T> visitor) {
            visitor.visitVarStmt(this);
        }
    }

    static class Block extends Stmt {
        List<Stmt> statements;

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        <T> void accept(Visitor<T> visitor) {
            visitor.visitBlockStmt(this);
        }
    }
}
