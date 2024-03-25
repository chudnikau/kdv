package com.bookatop.property.reg.model;

import com.bookatop.security.annotation.SensitiveData;
import com.bookatop.security.serializes.HttpSensitiveDataSerializable;

public class WithImplSensitiveContactData implements HttpSensitiveDataSerializable {

    private String name;

    @SensitiveData
    private String bankAccountNr;

    @SensitiveData
    private Integer pinCode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBankAccountNr() {
        return bankAccountNr;
    }

    public void setBankAccountNr(String bankAccountNr) {
        this.bankAccountNr = bankAccountNr;
    }

    public Integer getPinCode() {
        return pinCode;
    }

    public void setPinCode(Integer pinCode) {
        this.pinCode = pinCode;
    }
}
