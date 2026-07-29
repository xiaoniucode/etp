/*
 *    Copyright 2026 lxien
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package io.github.lxien.orbien.server.web.entity.converter;

import io.github.lxien.orbien.core.transport.compress.CompressionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CompressionTypeConverter implements AttributeConverter<CompressionType, String> {

    @Override
    public String convertToDatabaseColumn(CompressionType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.toConfigValue();
    }

    @Override
    public CompressionType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return CompressionType.of(dbData);
    }
}
