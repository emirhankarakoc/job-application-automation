package com.karakoc.scraper.websitescraper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
    private static final String SPLIT_CHAR = ";";

    @Override
    public String convertToDatabaseColumn(List<String> list) {
        return list == null ? "" : String.join(SPLIT_CHAR, list);
    }

    @Override
    public List<String> convertToEntityAttribute(String joined) {
        if (joined == null || joined.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(joined.split(SPLIT_CHAR));
    }
}

