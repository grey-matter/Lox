package com.lox;

public class Return extends RuntimeException {

    Object value;

    public Return( Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
