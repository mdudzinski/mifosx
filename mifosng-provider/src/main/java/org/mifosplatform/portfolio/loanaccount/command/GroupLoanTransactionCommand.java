package org.mifosplatform.portfolio.loanaccount.command;

import org.joda.time.LocalDate;

public class GroupLoanTransactionCommand {

    private final Long groupLoanId;
    private final LoanTransactionCommand[] membersTransactionsCommands;
    private final String note;
    private final LocalDate transactionDate;

    public GroupLoanTransactionCommand(Long groupLoanId, LoanTransactionCommand[] membersTransactionsCommands, String note,
            LocalDate transactionDate) {
        this.groupLoanId = groupLoanId;
        this.membersTransactionsCommands = membersTransactionsCommands;
        this.note = note;
        this.transactionDate = transactionDate;
    }

    public Long getGroupLoanId() {
        return groupLoanId;
    }

    public LoanTransactionCommand[] getMembersTransactionsCommands() {
        return membersTransactionsCommands;
    }

    public String getNote() {
        return note;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }
}
