package com.ken207.openbank.domain.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ken207.openbank.domain.Branch;
import com.ken207.openbank.domain.Customer;
import com.ken207.openbank.domain.Product;
import com.ken207.openbank.domain.enums.AccoStcd;
import com.ken207.openbank.domain.enums.ChnlDvcd;
import com.ken207.openbank.domain.enums.TxtnDvcd;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "subjCd")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Account {
    @Id @GeneratedValue
    @Column(name = "account_id")
    private String id; //계좌식별번호
    private String acno; //계좌번호
    private String custNo; //고객번호
    private String passwd; //비밀번호
    private String newDt; //신규일자
    private String trmtDt; //해지일자
    private String lastTrnDt; //최종거래일자
    private String lastIntsDt; //최종이자계산일자
    private Long accoBlnc;

    @Enumerated(EnumType.STRING)
    private AccoStcd accoStcd; //계좌상태코드

    @Enumerated(EnumType.STRING)
    private AccoStcd accoNewStcd; //계좌신규상태코드

    @Enumerated(EnumType.STRING)
    private AccoStcd accoTrmtStcd; //계좌해지상태코드

    @Enumerated(EnumType.STRING)
    private TxtnDvcd txtnDvcd; //과세구분코드

    @Enumerated(EnumType.STRING)
    private ChnlDvcd regChnlDvcd;

    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "accounts")
    private Customer customer;

    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id")
    private Product product; //상품코드

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "branch_id", insertable=false, updatable=false)
    private Branch newBranch;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "branch_id", insertable=false, updatable=false)
    private Branch mngBranch;

    @OneToMany(fetch = LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "account_log_id")
    private List<AccountLog> dataLog = new ArrayList<>();

    /**
     * 신규
     * @return
     */
    public void newAccount() {

    }

    /**
     * 입금
     */
    public void inAmount() {

    }

    /**
     * 출금
     */
    public void outAmount() {

    }

    /**
     * 해지
     */
    public void closeAmount() {

    }
}
