package timdev.timdev.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import timdev.timdev.dto.ExternalDriverDTO;

@Converter(autoApply = false)
public class ExternalDriverDTOConverter implements AttributeConverter<ExternalDriverDTO, Long> {

    @Override
    public Long convertToDatabaseColumn(ExternalDriverDTO attribute) {
        // store only external driver id in DB
        return attribute != null ? attribute.getId() : null;
    }

    @Override
    public ExternalDriverDTO convertToEntityAttribute(Long dbData) {
        if (dbData == null) return null;
        // create a placeholder DTO containing only id
        ExternalDriverDTO dto = new ExternalDriverDTO();
        dto.setId(dbData);
        return dto;
    }
}
