package com.lox;

import com.lox.Token;

import java.util.List;

abstract class Expr {
    interface Visitor<R> {
        R visitBinaryExpr(Binary expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitUnaryExpr(Unary expr);
        R visitVariableExpr(Variable expr);
        R visitAssignExpr(Assign assign);
        R visitLogicalExpr(Logical expr);
        R visitCallExpr(Call expr);
        R visitAnonymousFunctionExpr(AnonymousFunction anonymousFunction);
    }
    abstract <T> T accept(Visitor<T> visitor);
    static class Binary extends Expr {
        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        final Expr left;
        final Token operator;
        final Expr right;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }
    static class Grouping extends Expr {
        Grouping(Expr expression) {
            this.expression = expression;
        }

        final Expr expression;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }
    static class Literal extends Expr {
        Literal(Object value) {
            this.value = value;
        }

        final Object value;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }
    static class Unary extends Expr {
        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        final Token operator;
        final Expr right;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    static class Variable extends Expr{
        final Token name;

        public Variable(Token name) {
            this.name = name;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }

    static class Assign extends Expr {
        final Token name;
        final Expr value;

        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    static class Logical extends Expr{
        final Token operator;
        final Expr leftExpr, rightExpr;

        public Logical(Expr leftExpr, Token operator, Expr rightExpr) {
            this.operator = operator;
            this.leftExpr = leftExpr;
            this.rightExpr = rightExpr;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    static class Call extends Expr{
        Expr callee;
        Token paren;
        List<Expr>  arguments;

        public Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitCallExpr(this);
        }
    }

    static class AnonymousFunction extends Expr {
        final List<Token> params;
        final Stmt.Block body;

        public AnonymousFunction(List<Token> params, Stmt.Block body) {
            this.params = params;
            this.body = body;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitAnonymousFunctionExpr(this);
        }
    }
}