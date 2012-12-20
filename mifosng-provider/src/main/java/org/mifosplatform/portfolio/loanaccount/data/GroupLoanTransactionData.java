package org.mifosplatform.portfolio.loanaccount.data;

import org.joda.time.LocalDate;

import java.util.Collection;

public class GroupLoanTransactionData {

    @SuppressWarnings("unused")
    private final Collection<LoanTransactionData> membersTransactions;
    @SuppressWarnings("unused")
    private final LocalDate date;

    public GroupLoanTransactionData(Collection<LoanTransactionData> membersTransactions, LocalDate date) {
        this.membersTransactions = membersTransactions;
        this.date = date;
    }
}
