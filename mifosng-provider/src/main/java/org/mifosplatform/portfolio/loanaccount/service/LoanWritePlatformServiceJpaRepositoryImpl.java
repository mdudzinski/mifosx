package org.mifosplatform.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.security.exception.NoAuthorizationException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.staff.command.BulkTransferLoanOfficerCommand;
import org.mifosplatform.organisation.staff.command.BulkTransferLoanOfficerCommandValidator;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeRepository;
import org.mifosplatform.portfolio.charge.exception.ChargeIsNotActiveException;
import org.mifosplatform.portfolio.charge.exception.ChargeNotFoundException;
import org.mifosplatform.portfolio.charge.exception.LoanChargeNotFoundException;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.domain.Note;
import org.mifosplatform.portfolio.client.domain.NoteRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.fund.domain.Fund;
import org.mifosplatform.portfolio.loanaccount.command.AdjustLoanTransactionCommand;
import org.mifosplatform.portfolio.loanaccount.command.AdjustLoanTransactionCommandValidator;
import org.mifosplatform.portfolio.loanaccount.command.BulkLoanStateTransitionCommand;
import org.mifosplatform.portfolio.loanaccount.command.GroupLoanApplicationCommand;
import org.mifosplatform.portfolio.loanaccount.command.GroupLoanTransactionCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanApplicationCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanApplicationCommandValidator;
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommandValidator;
import org.mifosplatform.portfolio.loanaccount.command.LoanStateTransitionCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanStateTransitionCommandValidator;
import org.mifosplatform.portfolio.loanaccount.command.LoanTransactionCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanTransactionCommandValidator;
import org.mifosplatform.portfolio.loanaccount.command.UndoStateTransitionCommand;
import org.mifosplatform.portfolio.loanaccount.domain.DefaultLoanLifecycleStateMachine;
import org.mifosplatform.portfolio.loanaccount.domain.GroupLoan;
import org.mifosplatform.portfolio.loanaccount.domain.GroupLoanRepository;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanChargeRepository;
import org.mifosplatform.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepository;
import org.mifosplatform.portfolio.loanaccount.domain.LoanStatus;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.mifosplatform.portfolio.loanaccount.exception.LoanNotFoundException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanNotInSubmittedAndPendingApprovalStateCannotBeDeleted;
import org.mifosplatform.portfolio.loanaccount.exception.LoanOfficerAssignmentException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.command.CalculateLoanScheduleCommand;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.service.CalculationPlatformService;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRepository;
import org.mifosplatform.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.loanproduct.exception.InvalidCurrencyException;
import org.mifosplatform.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanWritePlatformServiceJpaRepositoryImpl implements LoanWritePlatformService {

	private final PlatformSecurityContext context;
	private final LoanRepository loanRepository;
    private final GroupLoanRepository groupLoanRepository;
	private final NoteRepository noteRepository;
	private final CalculationPlatformService calculationPlatformService;	
	private final LoanTransactionRepository loanTransactionRepository;
	private final LoanAssembler loanAssembler;
    private final GroupLoanAssembler groupLoanAssembler;
	private final ClientRepository clientRepository;
	private final LoanProductRepository loanProductRepository;
    private final ChargeRepository chargeRepository;
    private final LoanChargeRepository loanChargeRepository;
	private final LoanChargeAssembler loanChargeAssembler;

	@Autowired
	public LoanWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, 
			final LoanAssembler loanAssembler, final LoanChargeAssembler loanChargeAssembler, GroupLoanAssembler groupLoanAssembler,
			final LoanRepository loanRepository, GroupLoanRepository groupLoanRepository, final LoanTransactionRepository loanTransactionRepository,
			final NoteRepository noteRepository, final CalculationPlatformService calculationPlatformService,
			final ClientRepository clientRepository, final LoanProductRepository loanProductRepository,
            final ChargeRepository chargeRepository, final LoanChargeRepository loanChargeRepository) {
		this.context = context;
		this.loanAssembler = loanAssembler;
		this.loanChargeAssembler = loanChargeAssembler;
        this.groupLoanAssembler = groupLoanAssembler;
		this.loanRepository = loanRepository;
        this.groupLoanRepository = groupLoanRepository;
		this.loanTransactionRepository = loanTransactionRepository;
		this.noteRepository = noteRepository;
		this.calculationPlatformService = calculationPlatformService;
		this.clientRepository = clientRepository;
		this.loanProductRepository = loanProductRepository;
        this.chargeRepository = chargeRepository;
        this.loanChargeRepository = loanChargeRepository;
	}
	
	private boolean isBeforeToday(final LocalDate date) {
		return date.isBefore(new LocalDate());
	}
	
	private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
		List<LoanStatus> allowedLoanStatuses = Arrays.asList(LoanStatus.values());
		return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
	}
	
	@Transactional
	@Override
	public EntityIdentifier submitLoanApplication(final LoanApplicationCommand command) {

		AppUser currentUser = context.authenticatedUser();
		
		LoanApplicationCommandValidator validator = new LoanApplicationCommandValidator(command);
		validator.validate();

		LocalDate submittedOn = command.getSubmittedOnDate();
		if (this.isBeforeToday(submittedOn) && currentUser.hasNotPermissionForAnyOf("ALL_FUNCTIONS", "CREATEHISTORIC_LOAN", "PORTFOLIO_MANAGEMENT_SUPER_USER")) {
			throw new NoAuthorizationException("Cannot add backdated loan.");
		}

		Loan loan = loanAssembler.assembleFrom(command);

		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(command.getSubmittedOnNote())) {
			Note note = Note.loanNote(loan, command.getSubmittedOnNote());
			this.noteRepository.save(note);
		}
		
		return new EntityIdentifier(loan.getId());
	}

    @Transactional
    @Override
    public EntityIdentifier submitGroupLoanApplication(GroupLoanApplicationCommand command) {

        AppUser currentUser = context.authenticatedUser();

        LoanApplicationCommandValidator validator = new LoanApplicationCommandValidator(command.getLoanApplicationCommand());
        validator.validate();

        LocalDate submittedOn = command.getLoanApplicationCommand().getSubmittedOnDate();
        if (this.isBeforeToday(submittedOn) && currentUser.hasNotPermissionForAnyOf("ALL_FUNCTIONS", "CREATEHISTORIC_LOAN", "PORTFOLIO_MANAGEMENT_SUPER_USER")) {
            throw new NoAuthorizationException("Cannot add backdated loan.");
        }

        GroupLoan groupLoan = this.groupLoanAssembler.assembleFrom(command);

        this.groupLoanRepository.save(groupLoan);

        return new EntityIdentifier(groupLoan.getId());
    }

    @Transactional
	@Override
	public EntityIdentifier modifyLoanApplication(final LoanApplicationCommand command) {
		AppUser currentUser = context.authenticatedUser();
		
		LoanApplicationCommandValidator validator = new LoanApplicationCommandValidator(command);
		validator.validate();

		final LocalDate submittedOn = command.getSubmittedOnDate();
		if (this.isBeforeToday(submittedOn) && currentUser.hasNotPermissionForAnyOf("ALL_FUNCTIONS", "UPDATEHISTORIC_LOAN", "PORTFOLIO_MANAGEMENT_SUPER_USER")) {
			throw new NoAuthorizationException("Cannot modify backdated loan.");
		}

		final Loan loan = retrieveLoanBy(command.getLoanId());
		
		final LoanProduct loanProduct = this.loanProductRepository.findOne(command.getProductId());
		if (loanProduct == null) {
			throw new LoanProductNotFoundException(command.getProductId());
		}

		final Client client = this.clientRepository.findOne(command.getClientId());
		if (client == null || client.isDeleted()) {
			throw new ClientNotFoundException(command.getClientId());
		}
		
		final Fund fund = this.loanAssembler.findFundByIdIfProvided(command.getFundId());
		final LoanTransactionProcessingStrategy strategy = this.loanAssembler.findStrategyByIdIfProvided(command.getTransactionProcessingStrategyId());
		final Set<LoanCharge> charges = this.loanChargeAssembler.assembleFrom(command.getCharges(), loanProduct.getCharges(), loan.getPrincpal().getAmount());

        final LoanScheduleData loanSchedule = this.calculationPlatformService.calculateLoanSchedule(command.toCalculateLoanScheduleCommand());
        
        // FIXME - kw - restrict ability to modify loan to modifying the 'loan application' stage - once it has been disbursed, should be allowed to use this feature
        //       - other facilities are in place such as 'undo disbursal'
		loan.modifyLoanApplication(command, client, loanProduct, fund, strategy, loanSchedule, charges, defaultLoanLifecycleStateMachine());

		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(command.getSubmittedOnNote())) {
			Note note = Note.loanNote(loan, command.getSubmittedOnNote());
			this.noteRepository.save(note);
		}
		
		return new EntityIdentifier(loan.getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier deleteLoan(final Long loanId) {
		
		context.authenticatedUser();

		final Loan loan = retrieveLoanBy(loanId);
		
		if (loan.isNotSubmittedAndPendingApproval()) {
			throw new LoanNotInSubmittedAndPendingApprovalStateCannotBeDeleted(loanId);
		}
		
		List<Note> relatedNotes = this.noteRepository.findByLoanId(loan.getId());
		this.noteRepository.deleteInBatch(relatedNotes);

        if (loan.isMemberLoan()){
            loan.getGroupLoan().removeMemberLoan(loan);
        }
		this.loanRepository.delete(loanId);
		
		return new EntityIdentifier(loanId);
	}
	
	@Transactional
	@Override
	public EntityIdentifier approveLoanApplication(final LoanStateTransitionCommand command) {

		AppUser currentUser = context.authenticatedUser();
		
		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();

		final Loan loan = retrieveLoanBy(command.getLoanId());
		
		LocalDate eventDate = command.getEventDate();
		if (this.isBeforeToday(eventDate) && currentUser.canNotApproveLoanInPast()) {
			throw new NoAuthorizationException("User has no authority to approve loan with a date in the past.");
		}
		
		loan.approve(eventDate, defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);

		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanNote(loan, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getId());
	}

    @Transactional
    @Override
    public EntityIdentifier[] bulkApproveLoanApplication(BulkLoanStateTransitionCommand command) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Transactional
	@Override
	public EntityIdentifier undoLoanApproval(final UndoStateTransitionCommand command) {

		context.authenticatedUser();

		final Loan loan = retrieveLoanBy(command.getLoanId());
		
		loan.undoApproval(defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);

		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanNote(loan, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getId());
	}

	@Transactional
	@Override
	public EntityIdentifier rejectLoan(final LoanStateTransitionCommand command) {

		AppUser currentUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();

		final Loan loan = retrieveLoanBy(command.getLoanId());

		LocalDate eventDate = command.getEventDate();
		if (this.isBeforeToday(eventDate) && currentUser.canNotRejectLoanInPast()) {
			throw new NoAuthorizationException("User has no authority to reject loan with a date in the past.");
		}
		
		loan.reject(eventDate, defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);
		
		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanNote(loan, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getId());
	}

	@Transactional
	@Override
	public EntityIdentifier withdrawLoan(final LoanStateTransitionCommand command) {
		
		AppUser currentUser = context.authenticatedUser();

		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();
		
		final Loan loan = retrieveLoanBy(command.getLoanId());
		
		LocalDate eventDate = command.getEventDate();
		if (this.isBeforeToday(eventDate) && currentUser.canNotWithdrawByClientLoanInPast()) {
			throw new NoAuthorizationException("User has no authority to mark loan as withdrawn by client with a date in the past.");
		}
		
		loan.withdraw(eventDate, defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);

		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanNote(loan, noteText);
			this.noteRepository.save(note);
		}
		
		return new EntityIdentifier(loan.getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier disburseLoan(final LoanStateTransitionCommand command) {

		AppUser currentUser = context.authenticatedUser();
		
		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();

		final Loan loan = retrieveLoanBy(command.getLoanId());

		String noteText = command.getNote();
		LocalDate actualDisbursementDate = command.getEventDate();
		if (this.isBeforeToday(actualDisbursementDate) && currentUser.canNotDisburseLoanInPast()) {
			throw new NoAuthorizationException("User has no authority to disburse loan with a date in the past.");
		}

		if (loan.isRepaymentScheduleRegenerationRequiredForDisbursement(actualDisbursementDate)) {
			
			LocalDate repaymentsStartingFromDate = loan.getExpectedFirstRepaymentOnDate();
			LocalDate interestCalculatedFromDate = loan.getInterestChargedFromDate();

			BigDecimal principalAsDecimal = loan.repaymentScheduleDetail().getPrincipal().getAmount();
			BigDecimal interestRatePerYear = loan.repaymentScheduleDetail().getAnnualNominalInterestRate();
			Integer numberOfInstallments = loan.repaymentScheduleDetail().getNumberOfRepayments();
			
			Integer repaidEvery = loan.repaymentScheduleDetail().getRepayEvery();
			Integer selectedRepaymentFrequency = loan.repaymentScheduleDetail().getRepaymentPeriodFrequencyType().getValue();
			Integer selectedAmortizationMethod = loan.repaymentScheduleDetail().getAmortizationMethod().getValue();
			
			// FIXME - KW - use values from loan table here instead of inferring.
			Integer loanTermFrequency = repaidEvery * numberOfInstallments;
			Integer loanTermFrequencyType = selectedRepaymentFrequency;
			
			// use annual percentage rate to re-calculate loan schedule for late disbursement
			BigDecimal interestRatePerPeriod = interestRatePerYear;
			Integer interestRateFrequencyMethod = PeriodFrequencyType.YEARS.getValue();
			
			Integer interestMethod = loan.repaymentScheduleDetail().getInterestMethod().getValue();
			Integer interestCalculationInPeriod = loan.repaymentScheduleDetail().getInterestCalculationPeriodMethod().getValue();
			
			Set<LoanCharge> loanCharges = loan.getCharges();
			List<LoanChargeCommand> commands = new ArrayList<LoanChargeCommand>();
			for (LoanCharge loanCharge : loanCharges) {
				commands.add(loanCharge.toCommand());
			}
			
			LoanChargeCommand[] loanChargeCommands = commands.toArray(new LoanChargeCommand[commands.size()]);
			
			CalculateLoanScheduleCommand calculateCommand = new CalculateLoanScheduleCommand(
					loan.loanProduct().getId(),
					principalAsDecimal, 
					interestRatePerPeriod, interestRateFrequencyMethod, interestMethod, interestCalculationInPeriod,
					repaidEvery, selectedRepaymentFrequency, numberOfInstallments, selectedAmortizationMethod, 
					loanTermFrequency, loanTermFrequencyType,
					actualDisbursementDate, repaymentsStartingFromDate, interestCalculatedFromDate, loanChargeCommands);

			LoanScheduleData loanSchedule = this.calculationPlatformService.calculateLoanSchedule(calculateCommand);

			List<LoanRepaymentScheduleInstallment> modifiedLoanRepaymentSchedule = new ArrayList<LoanRepaymentScheduleInstallment>();
			
			for (LoanSchedulePeriodData scheduledLoanPeriod : loanSchedule.getPeriods()) {
				if (scheduledLoanPeriod.isRepaymentPeriod()) {
					LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(
							loan, 
							scheduledLoanPeriod.periodNumber(),
							scheduledLoanPeriod.periodFromDate(),
							scheduledLoanPeriod.periodDueDate(), 
							scheduledLoanPeriod.principalDue(),
							scheduledLoanPeriod.interestDue(),
							scheduledLoanPeriod.feeChargesDue(),
							scheduledLoanPeriod.penaltyChargesDue());
					
					modifiedLoanRepaymentSchedule.add(installment);
				}
			}
			loan.disburseWithModifiedRepaymentSchedule(actualDisbursementDate, modifiedLoanRepaymentSchedule, defaultLoanLifecycleStateMachine());
		} else {
			loan.disburse(actualDisbursementDate, defaultLoanLifecycleStateMachine(), true);
		}

		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanNote(loan, noteText);
			this.noteRepository.save(note);
		}
		
		return new EntityIdentifier(loan.getId());
	}

	@Transactional
	@Override
	public EntityIdentifier undoLoanDisbursal(final UndoStateTransitionCommand command) {

		context.authenticatedUser();

		Loan loan = this.loanRepository.findOne(command.getLoanId());
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}

		loan.undoDisbursal(defaultLoanLifecycleStateMachine());

		this.loanRepository.save(loan);

		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanNote(loan, noteText);
			this.noteRepository.save(note);
		}
		
		return new EntityIdentifier(loan.getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier makeLoanRepayment(final LoanTransactionCommand command) {

		AppUser currentUser = context.authenticatedUser();
		
		LoanTransactionCommandValidator validator = new LoanTransactionCommandValidator(command);
		validator.validate();
		
		Loan loan = this.loanRepository.findOne(command.getLoanId());
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}
		
		LocalDate transactionDate = command.getTransactionDate();
		if (this.isBeforeToday(transactionDate) && currentUser.canNotMakeRepaymentOnLoanInPast()) {
			throw new NoAuthorizationException("error.msg.no.permission.to.make.repayment.on.loan.in.past");
		}

		Money repayment = Money.of(loan.repaymentScheduleDetail()
				.getPrincipal().getCurrency(),
				command.getTransactionAmount());

		LoanTransaction loanRepayment = LoanTransaction.repayment(repayment, transactionDate);
		loan.makeRepayment(loanRepayment, defaultLoanLifecycleStateMachine());
		this.loanTransactionRepository.save(loanRepayment);
		this.loanRepository.save(loan);
		
		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanTransactionNote(loan, loanRepayment, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getId());
	}

    @Override
    public EntityIdentifier makeGroupLoanRepayment(GroupLoanTransactionCommand command) {

        AppUser currentUser = context.authenticatedUser();

        GroupLoan groupLoan = groupLoanRepository.findOne(command.getGroupLoanId());

        if (groupLoan == null){
            throw new LoanNotFoundException(command.getGroupLoanId());
        }

        LocalDate transactionDate = command.getTransactionDate();
        if (this.isBeforeToday(transactionDate) && currentUser.canNotMakeRepaymentOnLoanInPast()) {
            throw new NoAuthorizationException("error.msg.no.permission.to.make.repayment.on.loan.in.past");
        }

        for (LoanTransactionCommand memberCommand : command.getMembersTransactionsCommands()){
            makeLoanRepayment(memberCommand);
        }

        return new EntityIdentifier(groupLoan.getId());
    }

    @Transactional
	@Override
	public EntityIdentifier adjustLoanTransaction(final AdjustLoanTransactionCommand command) {

		context.authenticatedUser();

		AdjustLoanTransactionCommandValidator validator = new AdjustLoanTransactionCommandValidator(command);
		validator.validate();

		Loan loan = this.loanRepository.findOne(command.getLoanId());
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}

		LoanTransaction transactionToAdjust = this.loanTransactionRepository.findOne(command.getTransactionId());
		if (transactionToAdjust == null) {
			throw new LoanTransactionNotFoundException(command.getTransactionId());
		}
		
		final MonetaryCurrency currency = loan.repaymentScheduleDetail().getPrincipal().getCurrency();
		final Money transactionAmount = Money.of(currency, command.getTransactionAmount());

		// adjustment is only supported for repayments and waivers at present
		LocalDate transactionDate = command.getTransactionDate();
		LoanTransaction newTransactionDetail = LoanTransaction.repayment(transactionAmount, transactionDate);
		if (transactionToAdjust.isInterestWaiver()) {
			newTransactionDetail = LoanTransaction.waiver(loan, transactionAmount, transactionDate);
		}

		loan.adjustExistingTransaction(transactionToAdjust, newTransactionDetail, defaultLoanLifecycleStateMachine());

		if (newTransactionDetail.isGreaterThanZero(currency)) {
			this.loanTransactionRepository.save(newTransactionDetail);
		}

		this.loanRepository.save(loan);

		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanTransactionNote(loan, newTransactionDetail, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier waiveInterestOnLoan(final LoanTransactionCommand command) {
		
		context.authenticatedUser();

		final LoanTransactionCommandValidator validator = new LoanTransactionCommandValidator(command);
		validator.validate();
		
		final Loan loan = this.loanRepository.findOne(command.getLoanId());
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}
		
		final LoanTransaction waiveTransaction = loan.waiveInterest(command.getTransactionAmount(), command.getTransactionDate(), defaultLoanLifecycleStateMachine());
		
		this.loanTransactionRepository.save(waiveTransaction);
		this.loanRepository.save(loan);
		
		final String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			final Note note = Note.loanTransactionNote(loan, waiveTransaction, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier writeOff(final LoanTransactionCommand command) {
		context.authenticatedUser();

		final LoanTransactionCommandValidator validator = new LoanTransactionCommandValidator(command);
		validator.validateNonMonetaryTransaction();
		
		final Loan loan = this.loanRepository.findOne(command.getLoanId());
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}
		
		final LoanTransaction writeoff = loan.closeAsWrittenOff(command.getTransactionDate(), defaultLoanLifecycleStateMachine());
		
		this.loanTransactionRepository.save(writeoff);
		this.loanRepository.save(loan);
		
		final String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			final Note note = Note.loanTransactionNote(loan, writeoff, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier closeLoan(final LoanTransactionCommand command) {
		
		context.authenticatedUser();

		final LoanTransactionCommandValidator validator = new LoanTransactionCommandValidator(command);
		validator.validateNonMonetaryTransaction();
		
		final Loan loan = this.loanRepository.findOne(command.getLoanId());
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}
		
		final LoanTransaction possibleClosingTransaction = loan.close(command.getTransactionDate(), defaultLoanLifecycleStateMachine());
		if (possibleClosingTransaction != null) {
			this.loanTransactionRepository.save(possibleClosingTransaction);
		}
		this.loanRepository.save(loan);
		
		final String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			final Note note = Note.loanNote(loan, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier closeAsRescheduled(final LoanTransactionCommand command) {
		context.authenticatedUser();

		final LoanTransactionCommandValidator validator = new LoanTransactionCommandValidator(command);
		validator.validateNonMonetaryTransaction();
		
		final Loan loan = this.loanRepository.findOne(command.getLoanId());
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}
		
		loan.closeAsMarkedForReschedule(command.getTransactionDate(), defaultLoanLifecycleStateMachine());
		
		this.loanRepository.save(loan);
		
		final String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			final Note note = Note.loanNote(loan, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getId());
	}

	@Transactional
    @Override
    public EntityIdentifier addLoanCharge(final LoanChargeCommand command) {
        this.context.authenticatedUser();

        LoanChargeCommandValidator validator = new LoanChargeCommandValidator(command);
        validator.validateForCreate();

        final Loan loan = this.loanRepository.findOne(command.getLoanId());
        if (loan == null) {
            throw new LoanNotFoundException(command.getLoanId());
        }

        final Charge chargeDefinition = this.chargeRepository.findOne(command.getChargeId());
        if (chargeDefinition == null || chargeDefinition.isDeleted()) {
            throw new ChargeNotFoundException(command.getChargeId());
        }

        if (!chargeDefinition.isActive()) {
            throw new ChargeIsNotActiveException(chargeDefinition.getId(), chargeDefinition.getName());
        }
        
        final LoanCharge loanCharge = LoanCharge.createNew(loan, chargeDefinition, command);

        if (!loan.hasCurrencyCodeOf(chargeDefinition.getCurrencyCode())){
            String errorMessage = "Charge and Loan must have the same currency.";
            throw new InvalidCurrencyException("charge", "attach.to.loan", errorMessage);
        }

        loan.addLoanCharge(loanCharge);
        this.loanRepository.saveAndFlush(loan);

        return new EntityIdentifier(loanCharge.getId());
    }

	@Transactional
    @Override
    public EntityIdentifier updateLoanCharge(final LoanChargeCommand command) {

        this.context.authenticatedUser();

        LoanChargeCommandValidator validator = new LoanChargeCommandValidator(command);
        validator.validateForUpdate();

        final Long loanId = command.getLoanId();
        final Loan loan = retrieveLoanBy(loanId);
        
        final Long loanChargeId = command.getId();
		final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);
        
        loan.updateLoanCharge(loanCharge, command);
        
        this.loanRepository.save(loan);

        return new EntityIdentifier(loanCharge.getId());
    }
	
	@Transactional
	@Override
	public EntityIdentifier waiveLoanCharge(final LoanChargeCommand command) {
		
		 this.context.authenticatedUser();

//        LoanChargeCommandValidator validator = new LoanChargeCommandValidator(command);
//        validator.validateForUpdate();

        final Long loanId = command.getLoanId();
        final Loan loan = retrieveLoanBy(loanId);
        
        final Long loanChargeId = command.getId();
		final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);
		
		final LoanTransaction waiveTransaction = loan.waiveLoanCharge(loanCharge, defaultLoanLifecycleStateMachine());
		
		this.loanTransactionRepository.save(waiveTransaction);
		this.loanRepository.save(loan);
		
		final String noteText = ""; //command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			final Note note = Note.loanTransactionNote(loan, waiveTransaction, noteText);
			this.noteRepository.save(note);
		}
        
        return new EntityIdentifier(loanCharge.getId());
	}

	@Transactional
    @Override
    public EntityIdentifier deleteLoanCharge(final Long loanId, final Long loanChargeId) {

        this.context.authenticatedUser();

        final Loan loan = retrieveLoanBy(loanId);
        
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);
        
        loan.removeLoanCharge(loanCharge);
        this.loanRepository.save(loan);

        return new EntityIdentifier(loanCharge.getId());
    }
	
	private Loan retrieveLoanBy(final Long loanId) {
		final Loan loan = this.loanRepository.findOne(loanId);
        if (loan == null) {
            throw new LoanNotFoundException(loanId);
        }
		return loan;
	}

	private LoanCharge retrieveLoanChargeBy(final Long loanId, final Long loanChargeId) {
		final LoanCharge loanCharge = this.loanChargeRepository.findOne(loanChargeId);
        if (loanCharge == null) {
        	throw new LoanChargeNotFoundException(loanChargeId);
        }
        
        if (loanCharge.hasNotLoanIdentifiedBy(loanId)) {
        	throw new LoanChargeNotFoundException(loanChargeId, loanId);
        }
		return loanCharge;
	}

    @Transactional
    @Override
    public EntityIdentifier loanReassignment(BulkTransferLoanOfficerCommand command) {

        this.context.authenticatedUser();

        BulkTransferLoanOfficerCommandValidator validator = new BulkTransferLoanOfficerCommandValidator(command);
        validator.validateForLoanReassignment();

        final Staff fromLoanOfficer = loanAssembler.findLoanOfficerByIdIfProvided(command.getFromLoanOfficerId());
        final Staff toLoanOfficer = loanAssembler.findLoanOfficerByIdIfProvided(command.getToLoanOfficerId());

        final Loan loan = retrieveLoanBy(command.getLoanId());

        if (!loan.hasLoanOfficer(fromLoanOfficer)){
            throw new LoanOfficerAssignmentException(loan.getId(), fromLoanOfficer.getId());
        }

        loan.reassignLoanOfficer(toLoanOfficer, command.getAssignmentDate());

        this.loanRepository.saveAndFlush(loan);

        return new EntityIdentifier(loan.getId());
    }

    @Transactional
    @Override
    public EntityIdentifier bulkLoanReassignment(final BulkTransferLoanOfficerCommand command) {

        this.context.authenticatedUser();

        BulkTransferLoanOfficerCommandValidator validator = new BulkTransferLoanOfficerCommandValidator(command);
        validator.validateForBulkLoanReassignment();

        Staff fromLoanOfficer = loanAssembler.findLoanOfficerByIdIfProvided(command.getFromLoanOfficerId());
        Staff toLoanOfficer = loanAssembler.findLoanOfficerByIdIfProvided(command.getToLoanOfficerId());

        for (String loanIdString : command.getLoans()){
            final Long loanId = Long.valueOf(loanIdString);

            final Loan loan = retrieveLoanBy(loanId);
            
            if (!loan.hasLoanOfficer(fromLoanOfficer)){
                throw new LoanOfficerAssignmentException(loan.getId(), fromLoanOfficer.getId());
            }

            loan.reassignLoanOfficer(toLoanOfficer, command.getAssignmentDate());
            this.loanRepository.save(loan);
        }

        this.loanRepository.flush();

        return new EntityIdentifier(toLoanOfficer.getId());
    }
}