package com.smart.elderly.export;

import java.util.List;

public interface ExportDataProvider<T> {
    String getExportType();
    String getExportTypeDesc();
    List<String> getHeaders();
    List<T> fetchData(String exportParams);
    List<Object> convertToRow(T data);
    String generateFileName();
}
