package com.uniquehire.ems.kafka;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class EmsKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(EmsKafkaProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.attendance-marked}")  private String topicAttendance;
    @Value("${app.kafka.topics.leave-requested}")    private String topicLeaveReq;
    @Value("${app.kafka.topics.leave-approved}")     private String topicLeaveApproved;
    @Value("${app.kafka.topics.leave-rejected}")     private String topicLeaveRejected;
    @Value("${app.kafka.topics.payroll-processed}")  private String topicPayroll;
    @Value("${app.kafka.topics.employee-onboarded}") private String topicOnboarded;

    public void publishAttendanceMarked(AttendanceMarkedEvent event) {
        send(topicAttendance, String.valueOf(event.getEmployeeId()), event);
    }

    public void publishLeaveRequested(LeaveRequestEvent event) {
        event.setEventType("LEAVE_REQUESTED");
        send(topicLeaveReq, String.valueOf(event.getEmployeeId()), event);
    }

    public void publishLeaveApproved(LeaveRequestEvent event) {
        event.setEventType("LEAVE_APPROVED");
        send(topicLeaveApproved, String.valueOf(event.getEmployeeId()), event);
    }

    public void publishLeaveRejected(LeaveRequestEvent event) {
        event.setEventType("LEAVE_REJECTED");
        send(topicLeaveRejected, String.valueOf(event.getEmployeeId()), event);
    }

    public void publishPayrollProcessed(PayrollProcessedEvent event) {
        send(topicPayroll, String.valueOf(event.getEmployeeId()), event);
    }

    public void publishEmployeeOnboarded(EmployeeOnboardedEvent event) {
        send(topicOnboarded, String.valueOf(event.getEmployeeId()), event);
    }

    private void send(String topic, String key, Object payload) {
        CompletableFuture<?> future = kafkaTemplate.send(topic, key, payload);
        future.whenComplete((result, ex) -> {
            if (ex != null) log.error("Kafka publish failed → topic={} error={}", topic, ex.getMessage());
            else            log.debug("Kafka published  → topic={} key={}", topic, key);
        });
    }
}
