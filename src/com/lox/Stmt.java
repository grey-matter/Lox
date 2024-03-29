package com.lox;

import java.util.List;

abstract class Stmt {
    interface Visitor<R> {
        R visitExpressionStmt(Expression stmt);
        R visitPrintStmt(Print stmt);
        R visitVarStmt(Var stmt);
        R visitBlockStmt(Block stmt);
        R visitIfStmt(If stmt);
        R visitWhileStmt(While stmt);
        R visitFunctionStmt(Function function);
        R visitReturnStmt(Return stmt);
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

    static class If extends Stmt {
        Expr condition;
        Stmt thenBranch, elseBranch;

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        <T> void accept(Visitor<T> visitor) {
            visitor.visitIfStmt(this);
        }
    }

    static class While extends Stmt {

        final Expr condition;
        final Stmt statement;

        public While(Expr condition, Stmt statement) {
            this.condition = condition;
            this.statement = statement;
        }

        @Override
        <T> void accept(Visitor<T> visitor) {
            visitor.visitWhileStmt(this);
        }
    }

    static class Function extends Stmt {
        final Token name;
        final List<Token> params;
        final Block body;

        public Function(Token name, List<Token> params, Block body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        <T> void accept(Visitor<T> visitor) {
            visitor.visitFunctionStmt(this);
        }
    }

    static class Return extends Stmt {
        final Expr value;
        final Token keyword;

        public Return(Expr expr, Token keyword) {
            this.value = expr;
            this.keyword = keyword;
        }

        @Override
        <T> void accept(Visitor<T> visitor) {
            visitor.visitReturnStmt(this);
        }
    }
}
