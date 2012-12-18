package org.mifosplatform.portfolio.loanaccount.command;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;

public class GroupLoanChargeCommand {

    private final List<MemberLoanChargeCommand> memberCharges;
    private final LoanChargeCommand loanChargeCommand;

    public GroupLoanChargeCommand(final Set<String> parametersPassedInCommand, final Long id, final Long loanId, final Long chargeId,
            final BigDecimal amount, final Integer chargeTimeType, final Integer chargeCalculationType, final LocalDate specifiedDueDate) {
        loanChargeCommand = new LoanChargeCommand(parametersPassedInCommand, id, loanId, chargeId, amount, chargeTimeType,
                chargeCalculationType, specifiedDueDate);
        this.memberCharges = new ArrayList<MemberLoanChargeCommand>();
    }

    public void addMemberCharge(final Set<String> parametersPassedInCommand, Long clientId, BigDecimal amount) {
        this.memberCharges.add(new MemberLoanChargeCommand(parametersPassedInCommand, null, loanChargeCommand.getLoanId(),
                loanChargeCommand.getChargeId(), amount, loanChargeCommand.getChargeTimeType(), loanChargeCommand
                        .getChargeCalculationType(), loanChargeCommand.getSpecifiedDueDate(), clientId));
    }

    public List<MemberLoanChargeCommand> getMemberCharges() {
        return memberCharges;
    }

    public LoanChargeCommand getLoanChargeCommand() {
        return loanChargeCommand;
    }
}
