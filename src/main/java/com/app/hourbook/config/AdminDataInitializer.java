
package com.app.hourbook.config;

import com.app.hourbook.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AdminDataInitializer {

    private final AdminService adminService;

    @Bean
    public ApplicationRunner initializeAdmins() {
        return args -> adminService.createDefaultAdminsIfMissing();
    }
}
