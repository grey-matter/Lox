package com.lox;

import java.util.List;

public class LoxAnonymousFunction implements LoxCallable{
    final private Expr.AnonymousFunction declaration;
    final private Environment closure;

    public LoxAnonymousFunction(Expr.AnonymousFunction declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i=0;i < arguments.size(); ++i) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return e) {
            return e.value;
        }
        return null;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }
}
