package com.clinitalPlatform.dto;

import com.clinitalPlatform.models.MedecinFollowersID;

import lombok.Data;

@Data
public class MedecinNetworkDTO {

    private MedecinFollowersID id;
    private long medecin;
    private long follower;
    private String Comment;
    
    public MedecinNetworkDTO() {
        super();
    }
    
    public MedecinNetworkDTO(long medecin, long follower, String comment) {
        super();
        this.medecin = medecin;
        this.follower = follower;
        this.Comment = comment;
        this.id=new MedecinFollowersID(medecin, follower);
    }
    
    
    
    
}
