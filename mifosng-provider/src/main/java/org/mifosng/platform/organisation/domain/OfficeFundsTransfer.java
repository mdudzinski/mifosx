package org.mifosng.platform.organisation.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "portfolio_office_funds_transfers")
public class OfficeFundsTransfer extends AbstractAuditableCustom<AppUser, Long> {

	@ManyToOne
    @JoinColumn(name = "org_id", nullable = false)
    private final Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_office_id")
    private Office       from;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_office_id")
    private Office       to;

	@Column(name = "transaction_date", nullable = false)
    @Temporal(TemporalType.DATE)
	private Date transactionDate;
    
	@Embedded
	private MonetaryCurrency currency;
	
	@Column(name = "transaction_amount", scale = 6, precision = 19, nullable = false)
	private BigDecimal transactionAmount;

    protected OfficeFundsTransfer() {
        this.organisation = null;
        this.transactionDate = null;
    }

	public static OfficeFundsTransfer create(Organisation org, Office fromOffice, Office toOffice, LocalDate transactionLocalDate, Money amount) {
		
		Date transactionDate = null;
		if (transactionLocalDate != null) {
			transactionDate = transactionLocalDate.toDate();
		}
		
		return new OfficeFundsTransfer(org, fromOffice, toOffice, transactionDate, amount);
	}
	
	private OfficeFundsTransfer(Organisation org, Office fromOffice, Office toOffice, Date transactionDate, Money amount) {
		this.organisation = org;
		this.from = fromOffice;
		this.to = toOffice;
		this.transactionDate = transactionDate;
		this.currency = amount.getCurrency();
		this.transactionAmount = amount.getAmount();
	}

	public Office getFrom() {
		return from;
	}

	public void setFrom(Office from) {
		this.from = from;
	}

	public Office getTo() {
		return to;
	}

	public void setTo(Office to) {
		this.to = to;
	}

	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

	public MonetaryCurrency getCurrency() {
		return currency;
	}

	public void setCurrency(MonetaryCurrency currency) {
		this.currency = currency;
	}

	public BigDecimal getTransactionAmount() {
		return transactionAmount;
	}

	public void setTransactionAmount(BigDecimal transactionAmount) {
		this.transactionAmount = transactionAmount;
	}

	public Organisation getOrganisation() {
		return organisation;
	}
}