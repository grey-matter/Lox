package com.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes;

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
        scopes = new Stack<>();
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            Main.error(expr.name, "Cannot use variable name in its own initializer.");
        }
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign assign) {
        resolve(assign.value);
        resolveLocal(assign, assign.name);
        return null;
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1;i >= 0; --i) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                this.interpreter.resolve(expr, scopes.size() - 1 - i);
                break;
            }
        }
        // Assume global: apparently globals are more "dynamic",
        // hence not tracked in scopes stack, god know why the fuck!!
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.leftExpr);
        resolve(expr.rightExpr);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {

        resolve(expr.callee);
        for (Expr arg : expr.arguments) {
            resolve(arg);
        }
        return null;
    }

    @Override
    public Void visitAnonymousFunctionExpr(Expr.AnonymousFunction anonymousFunction) {
        beginScope();
        for (Token param : anonymousFunction.params) {
            declare(param);
            define(param);
        }
        anonymousFunction.body.statements.forEach(stmt -> resolve(stmt));
        endScope();
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void define(Token name) {
        if (scopes.isEmpty())
            return;
        scopes.peek().put(name.lexeme, true);
    }

    private void declare(Token name) {
        if (scopes.isEmpty())
            return;
        scopes.peek().put(name.lexeme, false);
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    public void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void endScope() {
        scopes.pop();
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null)
            resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.statement);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function function) {
        declare(function.name);
        define(function.name);

        resolveFunction(function);
        return null;
    }

    private void resolveFunction(Stmt.Function function) {
        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        function.body.statements.forEach(stmt -> resolve(stmt));
        endScope();
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (stmt.value != null)
            resolve(stmt.value);
        return null;
    }
}
