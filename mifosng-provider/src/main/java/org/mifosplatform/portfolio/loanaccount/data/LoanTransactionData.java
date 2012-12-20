package org.mifosplatform.portfolio.loanaccount.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object representing a loan transaction.
 */
public class LoanTransactionData {

	@SuppressWarnings("unused")
	private Long id;
	
	private EnumOptionData type;
	
	private LocalDate date;
	
	@SuppressWarnings("unused")
	private CurrencyData currency;
	
	@SuppressWarnings("unused")
	private BigDecimal amount;

    @SuppressWarnings("unused")
    private final Long loanId;
    @SuppressWarnings("unused")
    private final String loanProductName;

	@SuppressWarnings("unused")
	private final BigDecimal principalPortion;
	@SuppressWarnings("unused")
	private final BigDecimal interestPortion;
	@SuppressWarnings("unused")
	private final BigDecimal feeChargesPortion;
	@SuppressWarnings("unused")
	private final BigDecimal penaltyChargesPortion;

    public LoanTransactionData(Long id, EnumOptionData type, CurrencyData currency, LocalDate date, BigDecimal amount,
            BigDecimal principalPortion, BigDecimal interestPortion, BigDecimal feeChargesPortion, BigDecimal penaltyChargesPortion,
            Long loanId, String loanProductName) {
        this.id = id;
        this.type = type;
        this.currency = currency;
        this.date = date;
        this.amount = amount;
        this.principalPortion = principalPortion;
        this.interestPortion = interestPortion;
        this.feeChargesPortion = feeChargesPortion;
        this.penaltyChargesPortion = penaltyChargesPortion;
        this.loanId = loanId;
        this.loanProductName = loanProductName;
    }

    public LoanTransactionData(
			final Long id,
			final EnumOptionData transactionType, 
			final CurrencyData currency,
			final LocalDate date, 
			final BigDecimal amount, 
			final BigDecimal principalPortion, 
			final BigDecimal interestPortion, 
			final BigDecimal feeChargesPortion, 
			final BigDecimal penaltyChargesPortion) {
		this.id = id;
		this.type = transactionType;
		this.currency = currency;
		this.date = date;
		this.amount = amount;
		this.principalPortion = principalPortion;
		this.interestPortion = interestPortion;
		this.feeChargesPortion = feeChargesPortion;
		this.penaltyChargesPortion = penaltyChargesPortion;
        this.loanId = null;
        this.loanProductName = null;
	}

	public LocalDate dateOf() {
		return this.date;
	}

	public boolean isNotDisbursement() {
		return type.getId().equals(Integer.valueOf(1));
	}
}