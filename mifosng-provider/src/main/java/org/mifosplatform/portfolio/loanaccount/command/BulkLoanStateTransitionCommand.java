package org.mifosplatform.portfolio.loanaccount.command;

import org.joda.time.LocalDate;

public class BulkLoanStateTransitionCommand {

    private final LocalDate eventDate;
    private final String note;
    private final String[] loans;

    public BulkLoanStateTransitionCommand(LocalDate eventDate, String note, String[] loans) {
        this.eventDate = eventDate;
        this.note = note;
        this.loans = loans;
    }
}
