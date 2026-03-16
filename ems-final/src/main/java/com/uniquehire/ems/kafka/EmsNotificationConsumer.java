package com.uniquehire.ems.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EmsNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmsNotificationConsumer.class);

    @KafkaListener(topics = "${app.kafka.topics.attendance-marked}",
                   groupId = "${spring.kafka.consumer.group-id}")
    public void onAttendanceMarked(AttendanceMarkedEvent event) {
        log.info("EVENT: Attendance → {} | {} | {}",
            event.getEmployeeName(), event.getMarkMethod(), event.getLocation());
        // TODO: emailService.sendAttendanceConfirmation(event.getEmail(), event.getCheckIn());
    }

    @KafkaListener(topics = "${app.kafka.topics.leave-requested}",
                   groupId = "${spring.kafka.consumer.group-id}")
    public void onLeaveRequested(LeaveRequestEvent event) {
        log.info("EVENT: Leave Requested → {} | {} days",
            event.getEmployeeName(), event.getNumDays());
        // TODO: emailService.notifyManager(event.getManagerEmail(), event);
    }

    @KafkaListener(topics = "${app.kafka.topics.leave-approved}",
                   groupId = "${spring.kafka.consumer.group-id}")
    public void onLeaveApproved(LeaveRequestEvent event) {
        log.info("EVENT: Leave Approved → {}", event.getEmployeeName());
        // TODO: emailService.notifyEmployee(event.getEmail(), "Your leave has been approved");
    }

    @KafkaListener(topics = "${app.kafka.topics.leave-rejected}",
                   groupId = "${spring.kafka.consumer.group-id}")
    public void onLeaveRejected(LeaveRequestEvent event) {
        log.info("EVENT: Leave Rejected → {}", event.getEmployeeName());
        // TODO: emailService.notifyEmployee(event.getEmail(), "Your leave was rejected");
    }

    @KafkaListener(topics = "${app.kafka.topics.payroll-processed}",
                   groupId = "${spring.kafka.consumer.group-id}")
    public void onPayrollProcessed(PayrollProcessedEvent event) {
        log.info("EVENT: Payroll → {} | {} | net={}",
            event.getEmployeeName(), event.getPayrollMonth(), event.getNetSalary());
        // TODO: emailService.sendPayslip(event.getEmail(), event);
    }

    @KafkaListener(topics = "${app.kafka.topics.employee-onboarded}",
                   groupId = "${spring.kafka.consumer.group-id}")
    public void onEmployeeOnboarded(EmployeeOnboardedEvent event) {
        log.info("EVENT: Onboarded → {} | {}", event.getEmployeeName(), event.getDepartment());
        // TODO: emailService.sendWelcomeEmail(event.getEmail(), event.getEmployeeName());
    }
}
