package de.ipb_halle.signals.util;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class EmbeddedIntValue {
    public static final String ID = "id";
    public static final String VALUE = "value";
    private final static long serialVersionUID = 1L;

    private Integer id;

    private String value;

    /**
     * default constructor
     */
    public EmbeddedIntValue() {
    }

    public EmbeddedIntValue(Integer id, String value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        EmbeddedIntValue other = (EmbeddedIntValue) o;
        return Objects.equals(id, other.id)
                && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String v) {
        this.value = v;
    }
}
