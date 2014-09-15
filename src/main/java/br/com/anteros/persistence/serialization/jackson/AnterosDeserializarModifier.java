package br.com.anteros.persistence.serialization.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;

public class AnterosDeserializarModifier extends BeanDeserializerModifier {

	public AnterosDeserializarModifier() {
	}

	@Override
	public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
			JsonDeserializer<?> deserializer) {
		if ((deserializer instanceof BeanDeserializer) && !(deserializer instanceof AnterosBeanDeserializer))
			return new AnterosBeanDeserializer((BeanDeserializer) deserializer, beanDesc.getType().getRawClass());

		return super.modifyDeserializer(config, beanDesc, deserializer);
	}
	

}
