package com.craftmaster2190.rootstechclasses.config;

import com.craftmaster2190.rootstechclasses.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

import static com.craftmaster2190.rootstechclasses.util.ExceptionalUtils.invoke;

@Component
public class XlsxEncoder implements Encoder<JsonNode> {

  public static final String APPLICATION_XLSX_VALUE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  public static final MimeType APPLICATION_XLSX = MimeType.valueOf(APPLICATION_XLSX_VALUE);

  private static CellStyle buildBoldTitleStyle(Workbook workbook) {
    CellStyle boldTitleStyle = workbook.createCellStyle();
    Font boldTitleFont = workbook.createFont();
    boldTitleFont.setBold(true);
    boldTitleStyle.setFont(boldTitleFont);
    return boldTitleStyle;
  }

  private static CellStyle builderHyperlinkStyle(Workbook workbook) {
    CellStyle hyperlinkStyle = workbook.createCellStyle();
    Font hyperlinkFont = workbook.createFont();
    hyperlinkFont.setUnderline(Font.U_SINGLE);
    hyperlinkFont.setColor(IndexedColors.BLUE.index);
    hyperlinkStyle.setFont(hyperlinkFont);
    return hyperlinkStyle;
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
          var array = JsonUtils.toArrayNodeOrThrow(jsonNode);

          var bytes = new ByteArrayOutputStream();
          Workbook workbook = new XSSFWorkbook();
          Sheet sheet = workbook.createSheet("RootsTech Classes 2025"); // TODO Move this somewhere else?
          AtomicInteger currentRowIndex = new AtomicInteger();
          Map<String, Integer> fieldToColumnIndexMap = new HashMap<>();

          CellStyle hyperlinkStyle = builderHyperlinkStyle(workbook);
          CellStyle boldTitleStyle = buildBoldTitleStyle(workbook);

          try {
            JsonUtils.streamElements(array)
                .forEach(dataRow -> {
                  int currentRowIndex$ = currentRowIndex.getPlain();
                  var excelRow = sheet.createRow(currentRowIndex$);
                  if (currentRowIndex$ == 0) { // Header row
                    var column = new AtomicInteger();
                    dataRow.fieldNames().forEachRemaining(fieldName -> {
                      int column$ = column.getPlain();
                      fieldToColumnIndexMap.put(fieldName, column$);
                      Cell cell = excelRow.createCell(column$);
                      cell.setCellValue(fieldName);
                      cell.setCellStyle(boldTitleStyle);
                      column.getAndIncrement();
                    });

                  }
                  else {
                    dataRow.fieldNames().forEachRemaining(fieldName -> {
                      String text = dataRow.get(fieldName).asText("");
                      Cell cell = excelRow.createCell(fieldToColumnIndexMap.get(fieldName));
                      cell.setCellValue(text);
                      if (UrlUtils.isValidUrl(text)) {
                        Hyperlink link = workbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
                        link.setAddress(text);
                        cell.setHyperlink(link);
                        cell.setCellStyle(hyperlinkStyle);
                      }
                    });
                  }
                  currentRowIndex.getAndIncrement();
                });

            for (int columnIndex = 0; columnIndex < fieldToColumnIndexMap.size(); columnIndex++) {
              sheet.autoSizeColumn(columnIndex);
            }

            sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, fieldToColumnIndexMap.size()));
            sheet.createFreezePane(0, 1);

            invoke(() -> workbook.write(bytes));
          }
          finally {
            invoke(workbook::close);
          }

          return bufferFactory.wrap(bytes.toByteArray());
        });
  }

  @Override
  public List<MimeType> getEncodableMimeTypes() {
    return List.of(APPLICATION_XLSX);
  }
}
