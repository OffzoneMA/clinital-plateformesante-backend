package com.clinitalPlatform.services;


import com.clinitalPlatform.dao.IDao;
import com.clinitalPlatform.models.Rendezvous;
import com.clinitalPlatform.repository.RdvRepository;
import com.clinitalPlatform.util.ClinitalModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(propagation = Propagation.REQUIRED)
//@Primary
public class RendezvousService implements IDao<Rendezvous> {

	@Autowired
	private RdvRepository rdvrepo;

	@Autowired
	private ClinitalModelMapper mapper;

	private final Logger LOGGER=LoggerFactory.getLogger(getClass());



	@Override
	public Rendezvous create(Rendezvous o) {
		return null;
	}

	@Override
	public void update(Rendezvous o) {

	}

	@Override
	public void delete(Rendezvous o) {

	}

	@Override
	public List<Rendezvous> findAll() {
		return null;
	}

	@Override
	public Optional<Rendezvous> findById(long id) {
		return rdvrepo.findById(id);
	}



	public List<Rendezvous> findRendezvousByMedAndDate(Long medecinId, LocalDateTime date) {

		return rdvrepo.findByDateAndMedecin(date.toLocalDate(), medecinId).stream()
		.map(rdv -> mapper.map(rdv, Rendezvous.class)).collect(Collectors.toList());

	}

		






 
}
