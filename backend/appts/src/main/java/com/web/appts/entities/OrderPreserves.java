package com.web.appts.entities;


import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Entity
@Table(
        name = "order_preserves"
)
@Cache(
        usage = CacheConcurrencyStrategy.READ_WRITE
)
public class OrderPreserves {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(
            name = "id"
    )
    private Long id;

    @Column(
            name = "Verkooporder"
    )
    private String orderNumber;

    @Column(
            name = "Regel"
    )
    private String regel;

    @Column(
            name = "Backorder"
    )
    private String backOrder;

    @Column(
            name = "SME"
    )
    private String sme;
    @Column(
            name = "SPU"
    )
    private String spu;
    @Column(
            name = "MON_LB"
    )
    private String monLb;
    @Column(
            name = "MON_TR"
    )
    private String monTr;
    @Column(
            name = "MWE"
    )
    private String mwe;
    @Column(
            name = "SER"
    )
    private String ser;
    @Column(
            name = "TRA"
    )
    private String tra;
    @Column(
            name = "EXP"
    )
    private String exp;
    @Column(
            name = "exclamation"
    )
    private String exclamation;
    @Column(
            name = "excNote"
    )
    private String excNote;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getRegel() {
        return regel;
    }

    public void setRegel(String regel) {
        this.regel = regel;
    }

    public String getBackOrder() {
        return backOrder;
    }

    public void setBackOrder(String backOrder) {
        this.backOrder = backOrder;
    }

    public String getSme() {
        return sme;
    }

    public void setSme(String sme) {
        this.sme = sme;
    }

    public String getSpu() {
        return spu;
    }

    public void setSpu(String spu) {
        this.spu = spu;
    }

    public String getMonLb() {
        return monLb;
    }

    public void setMonLb(String monLb) {
        this.monLb = monLb;
    }

    public String getMonTr() {
        return monTr;
    }

    public void setMonTr(String monTr) {
        this.monTr = monTr;
    }

    public String getMwe() {
        return mwe;
    }

    public void setMwe(String mwe) {
        this.mwe = mwe;
    }

    public String getSer() {
        return ser;
    }

    public void setSer(String ser) {
        this.ser = ser;
    }

    public String getTra() {
        return tra;
    }

    public void setTra(String tra) {
        this.tra = tra;
    }

    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    public String getExclamation() {
        return exclamation;
    }

    public void setExclamation(String exclamation) {
        this.exclamation = exclamation;
    }

    public String getExcNote() {
        return excNote;
    }

    public void setExcNote(String excNote) {
        this.excNote = excNote;
    }
}
