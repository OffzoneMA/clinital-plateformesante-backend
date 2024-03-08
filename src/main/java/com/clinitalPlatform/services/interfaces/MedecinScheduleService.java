package com.clinitalPlatform.services.interfaces;

import com.clinitalPlatform.models.MedecinSchedule;
import com.clinitalPlatform.payload.request.MedecinScheduleRequest;

public interface MedecinScheduleService {

    MedecinSchedule create(MedecinScheduleRequest medecinScheduledrequest, Long id) throws Exception;

    MedecinSchedule update(MedecinScheduleRequest medecinScheduledrequest, Long id) throws Exception;

    void deleteById(Long id) throws Exception;

}