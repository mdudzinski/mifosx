package org.mifosplatform.portfolio.loanaccount.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.portfolio.loanaccount.service.LoanApplicationWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanApplicationWithdrawnByApplicantCommandHandler implements NewCommandSourceHandler {

    private final LoanApplicationWritePlatformService writePlatformService;

    @Autowired
    public LoanApplicationWithdrawnByApplicantCommandHandler(final LoanApplicationWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Transactional
    @Override
    public EntityIdentifier processCommand(final JsonCommand command) {

        return this.writePlatformService.applicantWithdrawsFromLoanApplication(command.resourceId(), command);
    }
}