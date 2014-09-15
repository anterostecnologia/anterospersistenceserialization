package br.com.anteros.persistence.serialization.jackson;

import br.com.anteros.persistence.serialization.jackson.AnterosPersistenceJacksonModule.Feature;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AnterosObjectMapper extends ObjectMapper {

	private static final long serialVersionUID = 1L;

	private AnterosPersistenceJacksonModule module;

	public AnterosObjectMapper() {
		super();
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
		this.module = new AnterosPersistenceJacksonModule(this.getDeserializationContext());
	}
	

}
