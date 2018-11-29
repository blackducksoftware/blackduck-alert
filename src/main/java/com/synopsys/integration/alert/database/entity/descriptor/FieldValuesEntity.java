package com.synopsys.integration.alert.database.entity.descriptor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Entity
@Table(schema = "ALERT", name = "FIELD_VALUES")
public class FieldValuesEntity extends DatabaseEntity {
    @Column(name = "CONFIG_ID")
    private Long configId;
    @Column(name = "FIELD_ID")
    private Long fieldId;
    @Column(name = "FIELD_VALUE")
    private String value;

    public FieldValuesEntity() {
        // JPA requires default constructor definitions
    }

    public FieldValuesEntity(final Long configId, final Long fieldId, final String value) {
        this.configId = configId;
        this.fieldId = fieldId;
        this.value = value;
    }

    public Long getConfigId() {
        return configId;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public String getValue() {
        return value;
    }
}
