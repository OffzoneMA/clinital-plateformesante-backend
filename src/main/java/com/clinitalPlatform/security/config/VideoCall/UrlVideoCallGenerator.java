package com.clinitalPlatform.security.config.VideoCall;

import org.springframework.stereotype.Component;

import static net.andreinc.mockneat.types.enums.StringType.ALPHA_NUMERIC;
import static net.andreinc.mockneat.types.enums.StringType.HEX;
import static net.andreinc.mockneat.unit.text.Strings.strings;

@Component
public class UrlVideoCallGenerator {

  
    public String joinConference() {
        String roomName = generateRoomName(); // Generate a unique room name
        return  getConferenceUrl(roomName);
    }

    private String generateRoomName() {
        // Implement your room name generation logic here
        // Return a unique room name
        String code = strings().size(10).types(ALPHA_NUMERIC, HEX).get();
		return code;
    }

    private String getConferenceUrl(String roomName) {
        return "https://meet.jit.si/" + roomName;
    }
    
}



