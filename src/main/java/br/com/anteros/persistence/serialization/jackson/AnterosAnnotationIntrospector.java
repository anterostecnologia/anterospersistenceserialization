/*******************************************************************************
 * Copyright 2012 Anteros Tecnologia
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
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
		return super.findTypeResolver(config, ac, baseType);
	}
	
	@Override
	protected TypeResolverBuilder<?> _findTypeResolver(MapperConfig<?> config, Annotated ann, JavaType baseType) {
		Annotated annotatedAdapter = new AnterosAnnotatedAdapter(sessionFactory, ann);
		TypeResolverBuilder<?> _findTypeResolver = super._findTypeResolver(config, annotatedAdapter, baseType);
		return _findTypeResolver;
	}

	@Override
	public List<NamedType> findSubtypes(Annotated a) {
		return super.findSubtypes(new AnterosAnnotatedAdapter(sessionFactory, a));
	}
	
	@Override
	public ObjectIdInfo findObjectIdInfo(Annotated ann) {
		return super.findObjectIdInfo(new AnterosAnnotatedAdapter(sessionFactory, ann));
	}
	
	@Override
	public ObjectIdInfo findObjectReferenceInfo(Annotated ann, ObjectIdInfo objectIdInfo) {
		return super.findObjectReferenceInfo(new AnterosAnnotatedAdapter(sessionFactory, ann), objectIdInfo);
	}
}
