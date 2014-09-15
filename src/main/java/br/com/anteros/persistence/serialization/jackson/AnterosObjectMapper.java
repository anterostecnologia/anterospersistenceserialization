package br.com.anteros.persistence.serialization.jackson;

import br.com.anteros.persistence.serialization.jackson.AnterosPersistenceJacksonModule.Feature;
import br.com.anteros.persistence.session.SQLSessionFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AnterosObjectMapper extends ObjectMapper {

	private static final long serialVersionUID = 1L;

	protected SQLSessionFactory sessionFactory;

	private AnterosPersistenceJacksonModule module;

	public AnterosObjectMapper(SQLSessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory; 
		this.createModule();
		this.registerModule(module);
	}

	public AnterosPersistenceJacksonModule getModule() {
		return module;
	}
	
	public AnterosObjectMapper enable(Feature feature){
		module.enable(feature);
		this.registerModule(module);
		return this;
	}
	
	public AnterosObjectMapper disable(Feature feature){
		module.disable(feature);
		this.registerModule(module);
		return this;
	}
	
	protected void createModule(){
		this.module = new AnterosPersistenceJacksonModule(sessionFactory, this.getDeserializationContext());
	}
	

}
