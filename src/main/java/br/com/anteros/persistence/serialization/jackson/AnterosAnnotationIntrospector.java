package br.com.anteros.persistence.serialization.jackson;

import java.util.List;

import br.com.anteros.persistence.metadata.annotation.Transient;
import br.com.anteros.persistence.session.SQLSessionFactory;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;

public class AnterosAnnotationIntrospector extends JacksonAnnotationIntrospector {
	private static final long serialVersionUID = 1L;

	protected boolean _cfgCheckTransient;

	private SQLSessionFactory sessionFactory;

	public AnterosAnnotationIntrospector(SQLSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public AnterosAnnotationIntrospector setUseTransient(boolean state) {
		_cfgCheckTransient = state;
		return this;
	}

	@Override
	public Version version() {
		return ModuleVersion.instance.version();
	}

	@Override
	public boolean hasIgnoreMarker(AnnotatedMember m) {
		if (m.hasAnnotation(Transient.class))
			return _cfgCheckTransient && m.hasAnnotation(Transient.class);
		else
			return _isIgnorable(m);
	}

	@Override
	public TypeResolverBuilder<?> findTypeResolver(MapperConfig<?> config, AnnotatedClass ac, JavaType baseType) {
		return _findTypeResolver(config, new AnterosAnnotatedAdapter(sessionFactory, ac), baseType);
	}

	@Override
	public List<NamedType> findSubtypes(Annotated a) {
		return super.findSubtypes(new AnterosAnnotatedAdapter(sessionFactory, a));
	}
	@Override
	public ReferenceProperty findReferenceType(AnnotatedMember member) {
		ReferenceProperty ref = super.findReferenceType(new AnterosAnnotatedMemberAdapter(sessionFactory, member));
		return ref;
	}
}
