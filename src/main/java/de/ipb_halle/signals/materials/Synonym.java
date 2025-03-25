package de.ipb_halle.signals.materials;

import de.ipb_halle.signals.util.EmbeddedKeyValue;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="synonyms")
public class Synonym {
    private final static long serialVersionUID = 1L;

    public final static String ENTITY_ID = "id";

    @EmbeddedId
    private EmbeddedKeyValue id;

    /**
     * default constructor
     */
    public Synonym() {
        id = new EmbeddedKeyValue();
    }

    public Synonym(String id, String synonym) {
        this.id = new EmbeddedKeyValue(id, synonym);
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        Synonym other = (Synonym) o;
        return id.equals(other.getEmbeddedId());
    }

    public EmbeddedKeyValue getEmbeddedId() {
        return id;
    }

    public String getSynonymId() {
        return id.getId();
    }

    public String getOption() {
        return id.getValue();
    }

    @Override
    public int hashCode() {
        return getSynonymId().hashCode() + getOption().hashCode();
    }

    public void getSynonymId(String i) {
        id.setId(i);
    }

    public void setOption(String o) {
        id.setValue(o);
    }

    @Override
    public String toString() {
        return "Synonym{"
                 + id +
                '}';
    }
}
