package com.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();
    private Environment enclosing;

    public Environment() {
        this.enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    void define(String name, Object value) {
        values.put(name, value);
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        if (enclosing != null)
            return enclosing.get(name);

        throw new Interpreter.RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        
        throw new Interpreter.RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    public Object getAt(Integer dist, Token name) {
        return ancestor(dist).values.get(name.lexeme);
    }

    private Environment ancestor(Integer dist) {
        Environment environment = this;
        for (int i=0;i < dist; ++i) {
            environment = environment.enclosing;
        }
        return environment;

    }

    public void assignAt(Integer dist, Token name, Object value) {
        ancestor(dist).values.put(name.lexeme, value);
    }
}
