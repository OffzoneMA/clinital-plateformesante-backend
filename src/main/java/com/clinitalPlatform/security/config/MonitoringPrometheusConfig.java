package com.clinitalPlatform.security.config;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.scheduling.annotation.Scheduled;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import com.sun.management.OperatingSystemMXBean;

@Configuration
public class MonitoringPrometheusConfig {

    private final MeterRegistry meterRegistry;
    private final Map<String, Timer> endpointTimers = new ConcurrentHashMap<>();
    private final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

    public MonitoringPrometheusConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initializeSystemMetrics();
        initializeBusinessMetrics();
    }

    private void initializeSystemMetrics() {
        // Métriques système
        Gauge.builder("system.cpu.usage", osBean, OperatingSystemMXBean::getCpuLoad)
                .description("Utilisation CPU système")
                .register(meterRegistry);

        Gauge.builder("system.memory.free", osBean, OperatingSystemMXBean::getFreeMemorySize)
                .description("Mémoire système libre")
                .baseUnit("bytes")
                .register(meterRegistry);

        Gauge.builder("system.memory.total", osBean, OperatingSystemMXBean::getTotalMemorySize)
                .description("Mémoire système totale")
                .baseUnit("bytes")
                .register(meterRegistry);
    }

    private void initializeBusinessMetrics() {
        // Existing metrics
        Counter.builder("app.appointments.scheduled")
                .description("Nombre total de rendez-vous programmés")
                .register(meterRegistry);

        Counter.builder("app.appointments.canceled")
                .description("Nombre total de rendez-vous annulés")
                .register(meterRegistry);

        Counter.builder("app.doctors.active")
                .description("Nombre de médecins actifs")
                .register(meterRegistry);

        // Nouvelles métriques de performance
        Timer.builder("app.appointment.duration")
                .description("Durée moyenne des consultations")
                .register(meterRegistry);

        // Métriques d'urgence
        Counter.builder("app.emergency.requests")
                .description("Nombre de demandes urgentes")
                .register(meterRegistry);

        // Métriques de prescription
        Counter.builder("app.prescriptions.created")
                .description("Nombre de prescriptions créées")
                .register(meterRegistry);

        // Métriques de paiement
        Counter.builder("app.payments.processed")
                .tag("method", "card")
                .description("Paiements traités par type")
                .register(meterRegistry);

        Counter.builder("app.payments.processed")
                .tag("method", "cash")
                .description("Paiements traités par type")
                .register(meterRegistry);

        // Métriques de notification
        Counter.builder("app.notifications.sent")
                .tag("type", "sms")
                .description("Notifications envoyées par type")
                .register(meterRegistry);

        Counter.builder("app.notifications.sent")
                .tag("type", "email")
                .description("Notifications envoyées par type")
                .register(meterRegistry);

        // Métriques d'annulation
        Counter.builder("app.appointments.canceled")
                .tag("reason", "patient")
                .description("Annulations par raison")
                .register(meterRegistry);

        Counter.builder("app.appointments.canceled")
                .tag("reason", "doctor")
                .description("Annulations par raison")
                .register(meterRegistry);


        // Métriques des utilisateurs actifs
        Gauge.builder("app.users.active.total", this, MonitoringPrometheusConfig::getCurrentActiveUsers)
                .description("Nombre total d'utilisateurs actifs")
                .register(meterRegistry);

        // Métriques par rôle avec tags
        Gauge.builder("app.users.active.by.role", this, MonitoringPrometheusConfig::getActiveUsersByRole)
                .tag("role", "patient")
                .description("Patients actifs")
                .register(meterRegistry);

        Gauge.builder("app.users.active.by.role", this, MonitoringPrometheusConfig::getActiveUsersByRole)
                .tag("role", "medecin")
                .description("Médecins actifs")
                .register(meterRegistry);

        // Métriques des sessions utilisateurs
        Counter.builder("app.users.login")
                .description("Nombre total de connexions")
                .register(meterRegistry);

        Counter.builder("app.users.login.failed")
                .description("Nombre de tentatives de connexion échouées")
                .register(meterRegistry);

        Timer.builder("app.users.session.duration")
                .description("Durée moyenne des sessions utilisateurs")
                .register(meterRegistry);

        // Métriques de gestion de compte
        Counter.builder("app.users.registration")
                .tag("type", "patient")
                .description("Nouvelles inscriptions par type d'utilisateur")
                .register(meterRegistry);

        Counter.builder("app.users.registration")
                .tag("type", "medecin")
                .description("Nouvelles inscriptions par type d'utilisateur")
                .register(meterRegistry);

        Counter.builder("app.users.profile.updates")
                .description("Nombre de mises à jour de profil")
                .register(meterRegistry);

        Counter.builder("app.users.password.reset")
                .description("Nombre de réinitialisations de mot de passe")
                .register(meterRegistry);

        // Métriques de désactivation/suppression
        Counter.builder("app.users.deactivated")
                .tag("reason", "voluntary")
                .description("Comptes désactivés par raison")
                .register(meterRegistry);

        Counter.builder("app.users.deactivated")
                .tag("reason", "administrative")
                .description("Comptes désactivés par raison")
                .register(meterRegistry);

        // Métriques de vérification
        Counter.builder("app.users.verification")
                .tag("status", "pending")
                .description("Statut des vérifications utilisateur")
                .register(meterRegistry);

        Counter.builder("app.users.verification")
                .tag("status", "approved")
                .description("Statut des vérifications utilisateur")
                .register(meterRegistry);
    }

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> prometheusMetricsFilter() {
        FilterRegistrationBean<OncePerRequestFilter> registrationBean = new FilterRegistrationBean<>();

        OncePerRequestFilter filter = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                String path = request.getRequestURI();
                String method = request.getMethod();
                long startTime = System.nanoTime();

                try {
                    // Métriques de base des requêtes
                    meterRegistry.counter("app.requests.total",
                            "path", path,
                            "method", method).increment();

                    // Métriques spécifiques au domaine médical
                    trackMedicalMetrics(path, method);

                    filterChain.doFilter(request, response);

                    // Métriques de performance
                    trackPerformanceMetrics(path, response.getStatus());

                } catch (Exception e) {
                    // Métriques d'erreurs détaillées
                    trackErrorMetrics(path, e);
                    throw e;
                } finally {
                    // Métriques de durée et latence
                    recordTimingMetrics(path, method, startTime);
                }
            }
        };

        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    private void trackMedicalMetrics(String path, String method) {
        // Métriques RDV
        if (path.contains("/api/rdv")) {
            if (method.equals("POST")) {
                meterRegistry.counter("app.appointments.created").increment();
            } else if (method.equals("DELETE")) {
                meterRegistry.counter("app.appointments.canceled").increment();
            }
            trackAppointmentTypeMetrics(path);
        }

        // Métriques Patients
        if (path.contains("/api/patient")) {
            meterRegistry.counter("app.patient.interactions").increment();
            if (method.equals("POST")) {
                meterRegistry.counter("app.patient.registrations").increment();
            }
        }

        // Métriques Médecins
        if (path.contains("/api/med")) {
            meterRegistry.counter("app.doctor.interactions").increment();
            trackDoctorSpecialtyMetrics(path);
        }
    }

    private void trackAppointmentTypeMetrics(String path) {
        if (path.contains("/urgent")) {
            meterRegistry.counter("app.appointments.urgent").increment();
        } else if (path.contains("/routine")) {
            meterRegistry.counter("app.appointments.routine").increment();
        }
    }

    private void trackDoctorSpecialtyMetrics(String path) {
        // Exemple de suivi par spécialité
        List<String> specialties = Arrays.asList("cardiology", "pediatrics", "dermatology");
        for (String specialty : specialties) {
            if (path.contains(specialty)) {
                meterRegistry.counter("app.doctor.specialty.requests", "specialty", specialty).increment();
            }
        }
    }

    private void trackPerformanceMetrics(String path, int status) {
        meterRegistry.counter("app.response.status",
                "path", path,
                "status", String.valueOf(status)).increment();

        // Suivi des performances par type de requête
        if (path.contains("/api/rdv")) {
            meterRegistry.counter("app.appointment.processing",
                    "status", status >= 400 ? "failed" : "success").increment();
        }
    }

    private void trackErrorMetrics(String path, Exception e) {
        meterRegistry.counter("app.errors",
                "path", path,
                "error_type", e.getClass().getSimpleName()).increment();

        // Suivi spécifique des erreurs métier
        if (e instanceof IllegalStateException) {
            meterRegistry.counter("app.business.errors",
                    "type", "validation").increment();
        }
    }

    private void recordTimingMetrics(String path, String method, long startTime) {
        long duration = System.nanoTime() - startTime;
        Timer timer = endpointTimers.computeIfAbsent(
                path + "." + method,
                k -> Timer.builder("app.request.duration")
                        .tag("path", path)
                        .tag("method", method)
                        .register(meterRegistry)
        );
        timer.record(duration, TimeUnit.NANOSECONDS);
    }

    @Scheduled(fixedRate = 60000)
    public void updateSystemMetrics() {
        // Mise à jour des métriques système toutes les minutes
        meterRegistry.gauge("system.memory.free",
                osBean.getFreeMemorySize());
        meterRegistry.gauge("system.memory.total",
                osBean.getTotalMemorySize());
    }

    private double getUsedMemory() {
        return memoryBean.getHeapMemoryUsage().getUsed();
    }

    private double getTotalMemory() {
        return memoryBean.getHeapMemoryUsage().getMax();
    }
    // Méthode utilitaire pour vérifier les routes authentifiées
    private boolean isAuthenticatedRoute(String path) {
        String[] authenticatedRoutes = {
                "/api/demandes/**", "/api/med/**", "/api/doc/**", "/api/shares/**",
                "/api/medecinSchedule/**", "/api/patient/**", "/api/rdv/patient/**",
                "/api/rdv/today/**", "/api/rdv/med/**", "/api/rdv/rdvs/medecin",
                "/api/users/**", "/api/rdv/**", "/api/medecinSchedule/fromCreno/**",
                "/api/rdv/rdvs/patient", "/api/rdv/patient/rdvbyday", "/api/med/equipe",
                "api/med/getAllPatients"
        };
        return Arrays.stream(authenticatedRoutes)
                .anyMatch(route -> pathMatches(path, route.replace("/**", "")));
    }

    // Méthode utilitaire pour vérifier les routes publiques
    private boolean isPermitAllRoute(String path) {
        String[] permitAllRoutes = {"/api/auth/**","/api/users/me" ,  "/api/users/activity/**","/api/demandes/create","/api/cabinet/**",
                "/api/med/medecins",
                "/api/users/byEmail" ,
                "/api/users/updateMail/**" ,
                "/api/med/medById/**",
                "/api/med/medByName",
                "/api/med/medByNameOrSpecAndVille/**",
                "/api/med/medByNameAndSpec",
                "/api/med/medByNameOrSpec",
                "/api/med/medByVille",
                "/api/med/getAllSpec",
                "/api/med/medByNameCabinetOrSpec",
                "/api/med/cabinets/**",
                "/api/med/agenda/**",
                "/api/med/medByCabinetName",
                "/api/med/medecins/schedules/filter",
                "/api/med/byLangue/**",
                "/api/med/allmedecins/**",
                "/api/med/medByLetter/**",
                "/api/med/by_motif_consultation/**",
                "/api/med/combinedfilter",
                "/api/ville/**",
                "/api/specialites/**",
                "/api/langues/**",
                "/api/tarifmed/**",
                "/api/medecinSchedule/fromCreno/**",
                "/api/rdv/rdvs/patient",
                "/api/rdv/patient/rdvbyday",
                "/api/cabinet/medecin/**",
                "/api/medecinSchedule/shedulebyMed/**",
                "/api/med/schedulesofMed/**",
                "/api/med/creneaux/**"
        };
        return Arrays.stream(permitAllRoutes)
                .anyMatch(route -> pathMatches(path, route.replace("/**", "")));
    }

    private boolean pathMatches(String path, String pattern) {
        return path.startsWith(pattern);
    }

    // Méthodes utilitaires pour les nouvelles métriques
    private double calculateDoctorAvailability() {
        // Logique pour calculer la disponibilité des médecins
        return 0.0; // À implémenter selon la logique métier
    }

    private double calculateAppointmentsPerDoctor() {
        // Logique pour calculer le nombre moyen de RDV par médecin
        return 0.0; // À implémenter selon la logique métier
    }

    private void trackUserMetrics(String path, String method) {
        // Métriques d'authentification
        if (path.contains("/api/auth")) {
            if (path.contains("/login")) {
                meterRegistry.counter("app.users.login").increment();
            } else if (path.contains("/register")) {
                String userType = determineUserType(path);
                meterRegistry.counter("app.users.registration", "type", userType).increment();
            }
        }

        // Métriques de gestion de profil
        if (path.contains("/api/users")) {
            if (method.equals("PUT") || method.equals("PATCH")) {
                meterRegistry.counter("app.users.profile.updates").increment();
            }
            trackUserActivityMetrics(path, method);
        }

        // Métriques de sécurité
        if (path.contains("/api/auth/password-reset")) {
            meterRegistry.counter("app.users.password.reset").increment();
        }
    }

    private void trackUserActivityMetrics(String path, String method) {
        // Suivi des actions spécifiques aux utilisateurs
        if (path.contains("/activity")) {
            meterRegistry.counter("app.users.profile.views").increment();
        } else if (path.contains("/updateMail")) {
            meterRegistry.counter("app.users.settings.changes").increment();
        }
    }

    private String determineUserType(String path) {
        if (path.contains("/patient")) {
            return "patient";
        } else if (path.contains("/medecin")) {
            return "medecin";
        }
        return "unknown";
    }

    // Méthodes utilitaires pour les métriques utilisateur
    private double getCurrentActiveUsers() {
        // À implémenter : logique pour obtenir le nombre d'utilisateurs actifs
        return 0.0;
    }

    private double getActiveUsersByRole() {
        // À implémenter : logique pour obtenir le nombre d'utilisateurs actifs par rôle
        return 0.0;
    }

    @Scheduled(fixedRate = 300000) // Toutes les 5 minutes
    public void updateBusinessMetrics() {
        // Mise à jour des métriques métier
        meterRegistry.gauge("app.doctors.availability", calculateDoctorAvailability());
        meterRegistry.gauge("app.appointments.per.doctor", calculateAppointmentsPerDoctor());
        meterRegistry.gauge("app.users.active.total", getCurrentActiveUsers());
    }
}
