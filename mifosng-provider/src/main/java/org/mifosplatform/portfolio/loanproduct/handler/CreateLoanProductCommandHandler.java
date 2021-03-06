package org.mifosplatform.portfolio.loanproduct.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.portfolio.loanproduct.service.LoanProductWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateLoanProductCommandHandler implements NewCommandSourceHandler {

    private final LoanProductWritePlatformService writePlatformService;

    @Autowired
    public CreateLoanProductCommandHandler(final LoanProductWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Transactional
    @Override
    public EntityIdentifier processCommand(final JsonCommand command) {

        return this.writePlatformService.createLoanProduct(command);
    }
}