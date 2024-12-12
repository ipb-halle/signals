package de.ipb_halle.signals.attachment;

import de.ipb_halle.signals.dynEnum.DynEnum;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("AttachmentType")
public class AttachmentType extends DynEnum<AttachmentType> {

    /**
     * private no-argument constructor
     */
    private AttachmentType() { }

    private AttachmentType(String v) {
        super(v);
    }

    public static AttachmentType valueOf(String v) {
        return new AttachmentType(v);
    }
}
