package com.clinitalPlatform.dao;

import java.util.List;
import java.util.Optional;


public interface IDao<S> {
	
	public S create(S o);
	public void update(S o);
	public void delete(S o);
	public List<S> findAll();
	public Optional<S> findById(long id);

	
}
