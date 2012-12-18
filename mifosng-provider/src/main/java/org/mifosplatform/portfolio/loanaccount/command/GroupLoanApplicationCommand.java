package org.mifosplatform.portfolio.loanaccount.command;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.loanaccount.loanschedule.command.CalculateLoanScheduleCommand;

public class GroupLoanApplicationCommand {

    private final MemberLoanCommand[] memberLoans;
    private final GroupLoanChargeCommand[] charges;

    private final LoanApplicationCommand loanApplicationCommand;

    public GroupLoanApplicationCommand(Set<String> modifiedParameters, Long loanId, Long groupId, Long productId, String externalId,
            Long fundId, Long loanOfficerId, Long transactionProcessingStrategyId, BigDecimal principal, BigDecimal inArrearsTolerance,
            Integer loanTermFrequency, Integer loanTermFrequencyType, Integer numberOfRepayments, Integer repaymentEvery,
            BigDecimal interestRatePerPeriod, Integer repaymentFrequencyType, Integer interestRateFrequencyType, Integer amortizationType,
            Integer interestType, Integer interestCalculationPeriodType, LocalDate expectedDisbursementDate,
            LocalDate repaymentsStartingFromDate, LocalDate interestChargedFromDate, LocalDate submittedOnDate, String submittedOnNote,
            MemberLoanCommand[] memberLoans, GroupLoanChargeCommand[] charges) {
        this.memberLoans = memberLoans;
        this.charges = charges;
        LoanChargeCommand[] loanChargesData = null;
        if (charges != null && charges.length > 0){
            loanChargesData = new LoanChargeCommand[charges.length];
            for (int i = 0; i < charges.length; i++) {
                loanChargesData[i] = charges[i].getLoanChargeCommand();
            }
        }

        this.loanApplicationCommand = new LoanApplicationCommand(modifiedParameters, loanId, null, groupId, productId, externalId, fundId,
                transactionProcessingStrategyId, submittedOnDate, submittedOnNote, expectedDisbursementDate, repaymentsStartingFromDate,
                interestChargedFromDate, principal, interestRatePerPeriod, interestRateFrequencyType, interestType,
                interestCalculationPeriodType, repaymentEvery, repaymentFrequencyType, numberOfRepayments, amortizationType,
                loanTermFrequency, loanTermFrequencyType, inArrearsTolerance, loanChargesData, loanOfficerId);
    }

    public CalculateLoanScheduleCommand toCalculateLoanScheduleCommand() {
        return this.loanApplicationCommand.toCalculateLoanScheduleCommand();
    }

    public List<LoanApplicationCommand> getMembersLoanApplicationCommands() {
        List<LoanApplicationCommand> membersLoanApplicationCommands = new ArrayList<LoanApplicationCommand>();

        for (MemberLoanCommand memberLoan : memberLoans) {
            membersLoanApplicationCommands.add(new LoanApplicationCommand(memberLoan.getModifiedParameters(), loanApplicationCommand
                    .getLoanId(), memberLoan.getClientId(), null, loanApplicationCommand.getProductId(), memberLoan.getExternalId(),
                    loanApplicationCommand.getFundId(), loanApplicationCommand.getTransactionProcessingStrategyId(), loanApplicationCommand
                            .getSubmittedOnDate(), loanApplicationCommand.getSubmittedOnNote(), loanApplicationCommand
                            .getExpectedDisbursementDate(), loanApplicationCommand.getRepaymentsStartingFromDate(), loanApplicationCommand
                            .getInterestChargedFromDate(), memberLoan.getPrincipal(), loanApplicationCommand.getInterestRatePerPeriod(),
                    loanApplicationCommand.getInterestRateFrequencyType(), loanApplicationCommand.getInterestType(), loanApplicationCommand
                            .getInterestCalculationPeriodType(), loanApplicationCommand.getRepaymentEvery(), loanApplicationCommand
                            .getRepaymentFrequencyType(), loanApplicationCommand.getNumberOfRepayments(), loanApplicationCommand
                            .getAmortizationType(), loanApplicationCommand.getLoanTermFrequency(), loanApplicationCommand
                            .getLoanTermFrequencyType(), loanApplicationCommand.getInArrearsTolerance(), null, loanApplicationCommand
                            .getLoanOfficerId()));
        }

        return membersLoanApplicationCommands;
    }

    public GroupLoanChargeCommand[] getCharges() {
        return charges;
    }

    public LoanApplicationCommand getLoanApplicationCommand() {
        return loanApplicationCommand;
    }
}
