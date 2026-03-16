package com.uniquehire.ems.service.interfaces;

import com.uniquehire.ems.dto.*;

import java.math.BigDecimal;
import java.util.List;

public interface IPayrollService {

    PayrollSummaryResponse bulkProcessPayroll(PayrollProcessRequest request);
    PayrollResponse        processSingleEmployee(Long employeeId, String month);
    List<PayrollResponse>  getPayrollByMonth(String month);
    List<PayrollResponse>  getPayrollByEmployee(Long employeeId);
    PayrollResponse        getPayrollByEmployeeAndMonth(Long employeeId, String month);
    PayrollResponse        markAsPaid(Long payrollId, String paymentDate, String reference);
    PayrollSummaryResponse getPayrollSummary(String month);
    TaxBreakdownResponse   calculateTax(BigDecimal annualSalary);
}
