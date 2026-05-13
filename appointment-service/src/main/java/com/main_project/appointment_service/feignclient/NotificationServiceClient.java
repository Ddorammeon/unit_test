package com.main_project.appointment_service.feignclient;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "notification-service", url = "${feign.notification-service.url}")
public interface NotificationServiceClient {

}
