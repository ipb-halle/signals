package de.ipb_halle.signals.util;

import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class EmbeddedKeyValue {
    public static final String ID = "id";
    public static final String VALUE = "value";
    private final static long serialVersionUID = 1L;

    private String id;

    private String value;

    /**
     * default constructor
     */
    public EmbeddedKeyValue() {
    }

    public EmbeddedKeyValue(String id, String value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        EmbeddedKeyValue other = (EmbeddedKeyValue) o;
        return Objects.equals(id, other.id)
                && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String v) {
        this.value = v;
    }
}
