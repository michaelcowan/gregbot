/*
 * Copyright (c) 2023 Mike Cowan.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package io.blt.gregbot.core.properties;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringCollectionDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Simple module to modify deserialization with the following attributes:
 * <ol>
 *     <li>{@link Map} is non-null</li>
 *     <li>{@link Map} is unmodifiable</li>
 *     <li>{@link Map} keys are ordered</li>
 *     <li>{@link List} of {@link String} is non-null</li>
 *     <li>{@link List} of {@link String} is unmodifiable</li>
 * </ol>
 * <p>
 * NOTE: Jackson currently doesn't support empty Maps using a value type without a default constructor (e.g. records)
 * <a href="https://github.com/FasterXML/jackson-databind/issues/3084">jackson-databind issue 3084</a>
 * </p>
 */
public class PropertiesModule extends SimpleModule {

    public PropertiesModule() {

        setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyMapDeserializer(
                    DeserializationConfig config,
                    MapType type,
                    BeanDescription beanDesc,
                    JsonDeserializer<?> deserializer) {

                class Deserializer extends StdDeserializer<Map<?, ?>>
                        implements ContextualDeserializer, ResolvableDeserializer {
                    private final BeanDeserializerModifier modifier;
                    private final MapDeserializer deserializer;

                    Deserializer(BeanDeserializerModifier modifier, MapDeserializer deserializer) {
                        super(Map.class);
                        this.modifier = modifier;
                        this.deserializer = deserializer;
                    }

                    @Override
                    public Map<?, ?> deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
                        return Collections.unmodifiableMap(deserializer.deserialize(jp, dc));
                    }

                    @Override
                    public JsonDeserializer<?> createContextual(DeserializationContext dc, BeanProperty property)
                            throws JsonMappingException {
                        return modifier.modifyMapDeserializer(config, type, beanDesc,
                                deserializer.createContextual(dc, property));
                    }

                    @Override
                    public void resolve(DeserializationContext dc) throws JsonMappingException {
                        deserializer.resolve(dc);
                    }

                    @Override
                    public Map<?, ?> getNullValue(DeserializationContext dc) {
                        return Map.of();
                    }
                }

                return new Deserializer(this, (MapDeserializer) deserializer);
            }

            @Override
            public JsonDeserializer<?> modifyCollectionDeserializer(
                    DeserializationConfig config,
                    CollectionType type,
                    BeanDescription beanDesc,
                    JsonDeserializer<?> deserializer) {

                class Deserializer extends StdDeserializer<Collection<String>> implements ContextualDeserializer {
                    private final BeanDeserializerModifier modifier;
                    private final StringCollectionDeserializer deserializer;
                    
                    Deserializer(BeanDeserializerModifier modifier, StringCollectionDeserializer deserializer) {
                        super(Collection.class);
                        this.modifier = modifier;
                        this.deserializer = deserializer;
                    }

                    @Override
                    public Collection<String> deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
                        return List.copyOf(deserializer.deserialize(jp, dc));
                    }

                    @Override
                    public JsonDeserializer<?> createContextual(DeserializationContext dc, BeanProperty property)
                            throws JsonMappingException {
                        return modifier.modifyCollectionDeserializer(config, type, beanDesc,
                                deserializer.createContextual(dc, property));
                    }

                    @Override
                    public Collection<String> getNullValue(DeserializationContext dc) {
                        return List.of();
                    }
                }

                // Currently only supports List<String> though could be extended for Set<String> and Queue<String>
                if (deserializer instanceof StringCollectionDeserializer stringCollectionDeserializer
                    && List.class.isAssignableFrom(type.getRawClass())) {
                    return new Deserializer(this, stringCollectionDeserializer);
                }

                return deserializer;
            }
        });

    }

}
