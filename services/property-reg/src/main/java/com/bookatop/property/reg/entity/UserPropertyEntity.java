package com.bookatop.property.reg.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "user_properties")
public class UserPropertyEntity {

    @Id
    private Long propertyId;

    @Column(name = "ref_property_type_id", nullable = false)
    private Long refPropTypeId;

    @Column(name = "ref_property_category_id", nullable = false)
    private Long refPropCatId;

    @Column(name = "ref_user_id", nullable = false)
    private Long refUserId;

    @OneToOne
    @JoinColumn(name = "property_id", referencedColumnName = "id")
    private PropertyEntity propertyEntity;

    public UserPropertyEntity() {
        /*
          The JPA specification requires all Entity classes to have a default no-arg constructor.
          This can be either public or protected.
        */
    }

    public UserPropertyEntity(Long propertyId, Long refPropTypeId, Long refPropCatId, Long refUserId) {
        this.propertyId = propertyId;
        this.refPropTypeId = refPropTypeId;
        this.refPropCatId = refPropCatId;
        this.refUserId = refUserId;
    }
}