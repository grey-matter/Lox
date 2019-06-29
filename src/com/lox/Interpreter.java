package com.lox;

import java.util.List;
import java.util.Objects;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private Environment environment = new Environment();
    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError e) {
            Main.runtimeError(e);
        }
    }

    private void execute(Stmt statement) {
        statement.accept(this);
    }

    private String stringify(Object value) {
        if (value == null) return "nil";

        if (value instanceof Double) {
            String number = value.toString();
            if (number.endsWith(".0")) {
                return number.substring(0, number.length() - 2);
            }
            return number;
        }
        return value.toString();
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.tokenType) {
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (Double)left - (Double)right;
            case PLUS:
                if (left instanceof String || right instanceof String)
                    return stringify(left) + stringify(right);
                if (left instanceof Double && right instanceof Double)
                    return (Double)left + (Double)right;
                throw new RuntimeError(expr.operator, "Operands must be numbers or strings");
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (Double)left * (Double)right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if ((Double) right == 0)
                    throw new RuntimeError(expr.operator, "Divide by zero attempted.");
                return (Double)left / (Double)right;
            case GREATER:
                return (Double)left > (Double)right;
            case GREATER_EQUAL:
                return (Double)left >= (Double)right;
            case LESS:
                return (Double)left < (Double) right;
            case LESS_EQUAL:
                return (Double)left <= (Double)right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }
        return null;
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isEqual(Object a, Object b) {
        return Objects.equals(a, b);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object value = evaluate(expr.right);

        switch (expr.operator.tokenType) {
            case MINUS:
                checkNumberOperand(expr.operator, value);
                return - (Double) value;
            case PLUS:
                checkNumberOperand(expr.operator, value);
                return (Double)value;
            case BANG:
                return !isTruthy(value);
        }
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign assign) {
        Object value = evaluate(assign.value);
        environment.assign(assign.name, value);
        return value;
    }

    private void checkNumberOperand(Token operator, Object value) {
        if (value instanceof Double)
            return;

        throw new RuntimeError(operator, "Operand should be a number.");
    }

    private boolean isTruthy(Object value) {
        return value != null && (value instanceof Boolean ? (Boolean) value : true);
    }


    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        System.out.println(stringify(evaluate(stmt.expression)));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt, new Environment(environment));
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        Object conditionValue = evaluate(stmt.condition);
        if (isTruthy(conditionValue)) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null){
            execute(stmt.elseBranch);
        }
        return null;
    }

    private void executeBlock(Stmt.Block stmt, Environment environment) {
        Environment prev = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : stmt.statements) {
                execute(statement);
            }
        } finally {
            this.environment = prev;
        }
    }

    static class RuntimeError extends RuntimeException {
        Token token;
        public RuntimeError(Token operator, String msg) {
            super(msg);
            token = operator;
        }
    }
}
