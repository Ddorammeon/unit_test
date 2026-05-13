package com.do_an.paymentservice.config;

import com.do_an.paymentservice.aggregate.PaymentAggregate;
import com.do_an.paymentservice.entity.Payment;
import com.github.kagkarlsson.scheduler.Scheduler;
import com.thoughtworks.xstream.XStream;

import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configuration;
import org.axonframework.config.ConfigurationScopeAwareProvider;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.DefaultDeadlineManagerSpanFactory;
import org.axonframework.deadline.dbscheduler.DbSchedulerDeadlineManager;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.ScopeAwareProvider;
import org.axonframework.modelling.command.Repository;
import org.axonframework.serialization.Serializer;
import org.axonframework.tracing.SpanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;


@org.springframework.context.annotation.Configuration
public class AxonConfig {
    @Autowired
    public void configureXStream(XStream xStream) {
        xStream.allowTypesByWildcard(new String[]{
                "com.do_an.**",
                "com.main_project.**"
        });
    }

    @Bean
    public Repository<PaymentAggregate> paymentAggregateRepository(EventStore eventStore) {
        return EventSourcingRepository.builder(PaymentAggregate.class)
                .eventStore(eventStore)
                .build();
    }

    @Bean
    public DeadlineManager deadlineManager(
            Scheduler scheduler,
            Configuration configuration,
            @Qualifier("eventSerializer") Serializer serializer,
            TransactionManager transactionManager,
            SpanFactory spanFactory) {
        ScopeAwareProvider scopeAwareProvider = new ConfigurationScopeAwareProvider(configuration);
        return DbSchedulerDeadlineManager.builder()
                .scheduler(scheduler)
                .scopeAwareProvider(scopeAwareProvider)
                .serializer(serializer)
                .transactionManager(transactionManager)
                .spanFactory(DefaultDeadlineManagerSpanFactory.builder()
                        .spanFactory(spanFactory)
                        .build())
                .startScheduler(false)
                .build();
    }


}

