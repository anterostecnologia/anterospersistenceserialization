package br.com.anteros.persistence.serialization.jackson;

public class JacksonSerializationException extends RuntimeException {

	public JacksonSerializationException(String msg) {
		super(msg);
	}
	
	public JacksonSerializationException(Throwable e){
		super(e);
	}
}
