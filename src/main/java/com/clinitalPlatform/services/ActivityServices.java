package com.clinitalPlatform.services;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.clinitalPlatform.dto.ActivityDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinitalPlatform.models.LogActivityUser;
import com.clinitalPlatform.models.User;
import com.clinitalPlatform.repository.ActivityRespository;
import com.clinitalPlatform.services.interfaces.historyservices;
import com.clinitalPlatform.util.ClinitalModelMapper;
@Transactional
@Service
@Primary
public class ActivityServices implements historyservices {
    @Autowired
    private ActivityRespository activityRespository;

    @Autowired
	private ClinitalModelMapper clinitalModelMapper;

    

    public ActivityServices() {

    }

    public <T> void createActivity(Date timeActivity, String typeActivity, String description, T user) {
        LogActivityUser activity = new LogActivityUser();
        activity.setTimeActivity(timeActivity);
        activity.setTypeActivity(typeActivity);
        activity.setDescription(description);
    
        if (user != null) {
            if (user instanceof User) {
                activity.setUser((User) user);
            }
        }
    
        activityRespository.save(activity);
    }
   

    @Override
    public List<ActivityDTO> findAll()
    {
        return activityRespository
                .findAll()
                .stream()
                .map(activity->clinitalModelMapper.map(activity, ActivityDTO.class))
                .collect(Collectors.toList());
    }

    @Override
	public ActivityDTO getactivityByIdUser(Long id) throws Exception {
		 List<LogActivityUser> activity= activityRespository.findActivityByIdUsers(id);
		 return clinitalModelMapper.map(activity, ActivityDTO.class);
	}
    
    @Override
    public ActivityDTO create(ActivityDTO dto) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public ActivityDTO update(ActivityDTO dto, Long id) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void deleteById(Long id) throws Exception {
        // TODO Auto-generated method stub
        
    }
}
