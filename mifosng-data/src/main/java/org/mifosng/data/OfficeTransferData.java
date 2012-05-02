package org.mifosng.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

@XmlRootElement(name = "officeTransfer")
public class OfficeTransferData implements Serializable {

	private OfficeData fromOffice;
	private Long toOffice;
	private List<OfficeData> possibleToOffices = new ArrayList<OfficeData>();

	private LocalDate paymentDate;
	private BigDecimal paymentAmount;
	private String note;

	public OfficeTransferData() {
		//
	}

	public OfficeData getFromOffice() {
		return fromOffice;
	}

	public void setFromOffice(OfficeData fromOffice) {
		this.fromOffice = fromOffice;
	}

	public Long getToOffice() {
		return toOffice;
	}

	public void setToOffice(Long toOffice) {
		this.toOffice = toOffice;
	}

	public List<OfficeData> getPossibleToOffices() {
		return possibleToOffices;
	}

	public void setPossibleToOffices(List<OfficeData> possibleToOffices) {
		this.possibleToOffices = possibleToOffices;
	}

	public LocalDate getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(LocalDate paymentDate) {
		this.paymentDate = paymentDate;
	}

	public BigDecimal getPaymentAmount() {
		return paymentAmount;
	}

	public void setPaymentAmount(BigDecimal paymentAmount) {
		this.paymentAmount = paymentAmount;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}