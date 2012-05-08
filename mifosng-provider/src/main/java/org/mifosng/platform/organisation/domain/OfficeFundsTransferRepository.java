package org.mifosng.platform.organisation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OfficeFundsTransferRepository extends JpaRepository<OfficeFundsTransfer, Long>, JpaSpecificationExecutor<OfficeFundsTransfer> {
    // no added behaviour
}
