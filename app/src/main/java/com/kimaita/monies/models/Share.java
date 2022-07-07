package com.kimaita.monies.models;

import java.util.Objects;

public class Share {

    public double count;
    public String name;
    public String amount;
    public double share;
    public boolean isIn;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Share share = (Share) o;
        return name.equals(share.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
