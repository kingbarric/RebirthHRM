/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.barricrebirthsystem.rebirtherp.entities;

import java.io.Serializable;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Barima
 */
@Entity
@Table(name = "file_share")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "FileShare.findAll", query = "SELECT f FROM FileShare f")
    , @NamedQuery(name = "FileShare.findById", query = "SELECT f FROM FileShare f WHERE f.id = :id")
    , @NamedQuery(name = "FileShare.findByAccepted", query = "SELECT f FROM FileShare f WHERE f.accepted = :accepted")
    , @NamedQuery(name = "FileShare.findByDateShared", query = "SELECT f FROM FileShare f WHERE f.dateShared = :dateShared")
    , @NamedQuery(name = "FileShare.findByDateAccepted", query = "SELECT f FROM FileShare f WHERE f.dateAccepted = :dateAccepted")})
public class FileShare implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "accepted")
    private Boolean accepted;
    @Column(name = "date_shared")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateShared;
    @Column(name = "date_accepted")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateAccepted;
    
    @JoinColumn(name = "from", referencedColumnName = "id")
    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    private Employee from;
    
    @JoinColumn(name = "to", referencedColumnName = "id")
    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    private Employee to;
    
    @JoinColumn(name = "file_id", referencedColumnName = "id")
    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    private FileArchive fileId;

    public FileShare() {
    }

    public FileShare(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public Date getDateShared() {
        return dateShared;
    }

    public void setDateShared(Date dateShared) {
        this.dateShared = dateShared;
    }

    public Date getDateAccepted() {
        return dateAccepted;
    }

    public void setDateAccepted(Date dateAccepted) {
        this.dateAccepted = dateAccepted;
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
        if (!(object instanceof FileShare)) {
            return false;
        }
        FileShare other = (FileShare) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.barricrebirthsystem.rebirtherp.entities.FileShare[ id=" + id + " ]";
    }

    public Employee getFrom() {
        return from;
    }

    public void setFrom(Employee from) {
        this.from = from;
    }

    public Employee getTo() {
        return to;
    }

    public void setTo(Employee to) {
        this.to = to;
    }

    public FileArchive getFileId() {
        return fileId;
    }

    public void setFileId(FileArchive fileId) {
        this.fileId = fileId;
    }
    
    
}
