package com.lox;

import java.util.List;

abstract class Stmt {
  interface Visitor<R> {
    R visitExpressionStmt(Expression stmt);
    R visitPrintStmt(Print stmt);
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
    public Var() {
    }

    final Token name;
    final Expr initializer;

    @Override
    <T> void accept(Visitor<T> visitor) {

    }
  }
}
