/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.barricrebirthsystem.rebirtherp.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Barima
 */
@Entity
@Table(name = "pr_salary_deduction")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PrSalaryDeduction.findAll", query = "SELECT p FROM PrSalaryDeduction p")
    , @NamedQuery(name = "PrSalaryDeduction.findById", query = "SELECT p FROM PrSalaryDeduction p WHERE p.id = :id")
    , @NamedQuery(name = "PrSalaryDeduction.findByLabel", query = "SELECT p FROM PrSalaryDeduction p WHERE p.label = :label")
         , @NamedQuery(name = "PrSalaryDeduction.findByTemp", query = "SELECT p FROM PrSalaryDeduction p WHERE p.templateId = :temp")
   })
public class PrSalaryDeduction implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "label")
    private String label;
    @Basic(optional = false)
    @NotNull
    @Column(name = "amount")
    private BigDecimal amount;
    @JoinColumn(name = "template_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private PrSalarytemplate templateId;
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Employee createdBy;

    public PrSalaryDeduction() {
    }

    public PrSalaryDeduction(Integer id) {
        this.id = id;
    }

    public PrSalaryDeduction(Integer id, String label, BigDecimal value) {
        this.id = id;
        this.label = label;
        this.amount = value;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

   

    public PrSalarytemplate getTemplateId() {
        return templateId;
    }

    public void setTemplateId(PrSalarytemplate templateId) {
        this.templateId = templateId;
    }

    public Employee getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Employee createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PrSalaryDeduction)) {
            return false;
        }
        PrSalaryDeduction other = (PrSalaryDeduction) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.barricrebirthsystem.rebirtherp.entities.PrSalaryDeduction[ id=" + id + " ]";
    }
    
}
