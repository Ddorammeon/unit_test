package com.main_project.appointment_service.aggregate;

import com.do_an.common.command.RollbackAppointmentCommand;
import com.do_an.common.command.StartAppointmentCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.modelling.command.Repository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentCommandHandler {
    private final Repository<AppointmentAggregate> appointmentAggregateRepository;

    @CommandHandler
    public void handle(CheckInAppointmentCommand command) throws Exception {
        log.info("Nhận lệnh CheckInAppointmentCommand cho AppointmentId: {}", command.getAppointmentId());
        appointmentAggregateRepository.newInstance(() -> new AppointmentAggregate(command));
    }

    @CommandHandler
    public void handle(StartAppointmentCommand command) throws Exception {
        log.info("Nhận lệnh StartAppointmentCommand cho AppointmentId: {}", command.getAppointmentId());
        appointmentAggregateRepository.load(command.getAppointmentId().toString())
                .execute(aggregate -> aggregate.handle(command));
    }

    @CommandHandler
    public void handle(RollbackAppointmentCommand command) throws Exception {
        log.warn("Nhận lệnh RollbackAppointmentCommand cho AppointmentId: {} reason={}",
                command.getAppointmentId(), command.getReason());
        appointmentAggregateRepository.load(command.getAppointmentId().toString())
                .execute(aggregate -> aggregate.handle(command));
    }
}
