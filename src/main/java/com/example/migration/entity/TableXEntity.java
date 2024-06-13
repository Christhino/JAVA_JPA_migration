package com.example.migration.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table(name = "table_x")
public class TableXEntity {
    @Id
    private Long id;

    @Column(name = "field_a")
    private String fieldA;

    @Column(name = "field_b")
    private String fieldB;

    @Column(name = "field_c")
    private String fieldC;


    public String getFieldC() {
        return fieldC;
    }

    public void setFieldC(String fieldC) {
        this.fieldC = fieldC;
    }

    public String getFieldB() {
        return fieldB;
    }

    public void setFieldB(String fieldB) {
        this.fieldB = fieldB;
    }

    public String getFieldA() {
        return fieldA;
    }

    public void setFieldA(String fieldA) {
        this.fieldA = fieldA;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



}
