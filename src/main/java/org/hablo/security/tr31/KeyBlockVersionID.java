package org.hablo.security.tr31;

public enum KeyBlockVersionID {
    KeyDerivationBindingMethod2010Edition("B");

    private final String value;

    KeyBlockVersionID(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
