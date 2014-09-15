package br.com.anteros.persistence.serialization.jackson;

import br.com.anteros.persistence.session.SQLSessionFactory;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;

public class AnterosDeserializarModifier extends BeanDeserializerModifier {

	private SQLSessionFactory sessionFactory;

	public AnterosDeserializarModifier(SQLSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
			JsonDeserializer<?> deserializer) {
		if ((deserializer instanceof BeanDeserializer) && !(deserializer instanceof AnterosBeanDeserializer))
			return new AnterosBeanDeserializer(sessionFactory, (BeanDeserializer) deserializer, beanDesc.getType().getRawClass());

		return super.modifyDeserializer(config, beanDesc, deserializer);
	}
	

}
