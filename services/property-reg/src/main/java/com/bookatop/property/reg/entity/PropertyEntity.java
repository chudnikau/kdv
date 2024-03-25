package com.bookatop.property.reg.entity;

import com.bookatop.property.reg.api.enums.PropertyJsonType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Setter
@Getter
@Entity
@Table(name = "properties")
public class PropertyEntity {

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_json", nullable = false)
    private String propertyJson;

    @Column(name = "property_json_type", nullable = false)
    private PropertyJsonType propertyJsonType;

    @Column(name = "created_date", nullable = false)
    @CreationTimestamp
    private Timestamp createdDate;

    @Column(name = "modified_date", nullable = false)
    @CreationTimestamp
    private Timestamp modifiedDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @OneToOne(mappedBy = "propertyEntity")
    private UserPropertyEntity userPropertyEntity;

    public PropertyEntity() {
        /*
          The JPA specification requires all Entity classes to have a default no-arg constructor.
          This can be either public or protected.
        */
    }

    public PropertyEntity(String propertyJson,
                          PropertyJsonType propertyJsonType,
                          Boolean isActive) {
        this.propertyJson = propertyJson;
        this.propertyJsonType = propertyJsonType;
        this.isActive = isActive;
    }
}
