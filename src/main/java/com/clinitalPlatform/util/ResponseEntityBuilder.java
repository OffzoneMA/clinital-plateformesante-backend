package com.clinitalPlatform.util;

import org.springframework.http.ResponseEntity;

public class ResponseEntityBuilder {

	// Méthode pour construire un ResponseEntity à partir d'un ApiError
	public static ResponseEntity<Object> build(ApiError apiError) {
		// Crée un ResponseEntity avec le corps de l'erreur et le statut HTTP correspondant
        return new ResponseEntity<>(apiError, apiError.getStatus());
  }
}
