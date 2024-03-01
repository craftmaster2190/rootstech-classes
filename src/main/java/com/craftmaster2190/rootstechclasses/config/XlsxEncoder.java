package com.craftmaster2190.rootstechclasses.config;

import com.craftmaster2190.rootstechclasses.util.JsonUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.csv.*;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

@Component
public class XlsxEncoder implements Encoder<JsonNode> {

  public static final String APPLICATION_XLSX_VALUE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  public static final MimeType APPLICATION_XLSX = MimeType.valueOf(APPLICATION_XLSX_VALUE);

  static <T> T invoke(Callable<T> invoke) {
    try {
      return invoke.call();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean canEncode(ResolvableType elementType, MimeType mimeType) {
    return elementType.equalsType(ResolvableType.forClass(JsonNode.class)) && (mimeType == null || APPLICATION_XLSX.equals(
        mimeType));
  }

  @Override
  public Flux<DataBuffer> encode(Publisher<? extends JsonNode> inputStream, DataBufferFactory bufferFactory, ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
    return Flux.from(inputStream)
        .map(jsonNode -> {
          if (!jsonNode.isArray() || !(jsonNode instanceof ArrayNode)) {
            throw new IllegalStateException("json was not an array!");
          }
          var array = (ArrayNode) jsonNode;

          var bytes = new ByteArrayOutputStream();
          Workbook workbook = new XSSFWorkbook();
          Sheet sheet = workbook.createSheet("Sheet1");
          AtomicInteger currentRowIndex = new AtomicInteger();
          Map<String, Integer> fieldToColumnIndexMap = new HashMap<>();

          try {
            JsonUtils.streamElements(array)
                .forEach(dataRow -> {
                  int currentRowIndex$ = currentRowIndex.getPlain();
                  var excelRow = sheet.createRow(currentRowIndex$);
                  if (currentRowIndex$ == 0) {
                    var column = new AtomicInteger();
                    dataRow.fieldNames().forEachRemaining(fieldName -> {
                      int column$ = column.getPlain();
                      fieldToColumnIndexMap.put(fieldName, column$);
                      excelRow.createCell(column$).setCellValue(fieldName);
                      column.getAndIncrement();
                    });
                  } else {
                    dataRow.fieldNames().forEachRemaining(fieldName -> {
                      excelRow.createCell(fieldToColumnIndexMap.get(fieldName)).setCellValue(dataRow.get(fieldName).asText(""));
                    });
                  }
                  currentRowIndex.getAndIncrement();
                });

            for (int columnIndex = 0; columnIndex < fieldToColumnIndexMap.size(); columnIndex++) {
              sheet.autoSizeColumn(columnIndex);
            }

            invoke(() -> {
              workbook.write(bytes);
              return null;
            });
          }
          finally {
            invoke(() -> {
              workbook.close();
              return null;
            });
          }

          return bufferFactory.wrap(bytes.toByteArray());
        });
  }

  @Override
  public List<MimeType> getEncodableMimeTypes() {
    return List.of(APPLICATION_XLSX);
  }
}
