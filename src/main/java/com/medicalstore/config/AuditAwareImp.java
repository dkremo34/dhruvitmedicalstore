package com.medicalstore.config;

import com.medicalstore.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Component(value = "auditorAware")
public class AuditAwareImp implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();
            Object user = request.getSession().getAttribute(Constants.USER_SESSION_KEY);
            if (user instanceof com.medicalstore.model.UserDetails) {
                return Optional.of(((com.medicalstore.model.UserDetails) user).getFullName());
            }
            return Optional.ofNullable(user != null ? user.toString() : "System");
        } catch (Exception e) {
            return Optional.of("System");
        }
    }
}
