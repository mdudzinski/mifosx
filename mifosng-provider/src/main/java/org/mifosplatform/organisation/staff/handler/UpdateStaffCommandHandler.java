package org.mifosplatform.organisation.staff.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.organisation.staff.service.StaffWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateStaffCommandHandler implements NewCommandSourceHandler {

    private final StaffWritePlatformService writePlatformService;

    @Autowired
    public UpdateStaffCommandHandler(final StaffWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Transactional
    @Override
    public EntityIdentifier processCommand(final JsonCommand command) {

        return this.writePlatformService.updateStaff(command.resourceId(), command);
    }
}