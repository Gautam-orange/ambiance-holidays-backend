package com.ambianceholidays.domain.settings;

import jakarta.persistence.*;

@Entity
@Table(name = "system_settings")
public class SystemSetting {

    @Id
    private String key;

    @Column(nullable = false)
    private String value;

    @Column(name = "data_type", nullable = false)
    private String dataType = "STRING";

    @Column(columnDefinition = "TEXT")
    private String description;

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getDataType() { return dataType; }
    public String getDescription() { return description; }
}
