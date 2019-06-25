package com.lox;

import com.lox.Token;

abstract class Expr {
  interface Visitor<R> {
    R visitBinaryExpr(Binary expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitUnaryExpr(Unary expr);
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
      return null;
    }
  }
}