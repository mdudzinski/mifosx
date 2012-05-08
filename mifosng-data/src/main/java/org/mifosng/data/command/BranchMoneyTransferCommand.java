package org.mifosng.data.command;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

/**
 * Command used for create and update branch transfer of money.
 */
@XmlRootElement
public class BranchMoneyTransferCommand {

	private Long id;
	private Long fromOfficeId;
	private Long toOfficeId;
	private LocalDate transactionDate;
	private BigDecimal transactionAmount;

	protected BranchMoneyTransferCommand() {
		//
	}
	
	public BranchMoneyTransferCommand(final Long fromOfficeId, final Long toOfficeId, final LocalDate transactionDate, final BigDecimal transactionAmount) {
		this.fromOfficeId = fromOfficeId;
		this.toOfficeId = toOfficeId;
		this.transactionDate = transactionDate;
		this.transactionAmount = transactionAmount;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getFromOfficeId() {
		return fromOfficeId;
	}

	public void setFromOfficeId(Long fromOfficeId) {
		this.fromOfficeId = fromOfficeId;
	}

	public Long getToOfficeId() {
		return toOfficeId;
	}

	public void setToOfficeId(Long toOfficeId) {
		this.toOfficeId = toOfficeId;
	}

	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(LocalDate transactionDate) {
		this.transactionDate = transactionDate;
	}

	public BigDecimal getTransactionAmount() {
		return transactionAmount;
	}

	public void setTransactionAmount(BigDecimal transactionAmount) {
		this.transactionAmount = transactionAmount;
	}
}