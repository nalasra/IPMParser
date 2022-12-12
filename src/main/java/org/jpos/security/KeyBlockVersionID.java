package org.jpos.security;

public enum KeyBlockVersionID {
    KeyDerivationBindingMethod2010Edition('B');

    private final char code;

    KeyBlockVersionID(char code) {
        this.code = code;
    }

    public char getCode() {
        return code;
    }

}
