package com.craftmaster2190.rootstechclasses.config;

import com.craftmaster2190.rootstechclasses.util.JsonUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.csv.*;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

@Component
public class CsvEncoder implements Encoder<JsonNode> {

  public static final MimeType TEXT_CSV = MimeType.valueOf("text/csv");

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
    return elementType.equalsType(ResolvableType.forClass(JsonNode.class)) && (mimeType == null || TEXT_CSV.equals(
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
          CsvMapper csvMapper = CsvMapper.csvBuilder()
              .enable(JsonGenerator.Feature.IGNORE_UNKNOWN)
              .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
              .build();
          var csvSchemaBuilder = CsvSchema.builder()
              .setUseHeader(true);
          var csvSchemaWriter = new AtomicReference<SequenceWriter>();

          try {
            JsonUtils.streamElements(array)
                .forEach(row -> {
                  if (csvSchemaWriter.getPlain() == null) {
                    row.fieldNames()
                        .forEachRemaining(csvSchemaBuilder::addColumn);
                    var objectWriter = csvMapper.writer()
                        .with(csvSchemaBuilder.build());
                    csvSchemaWriter.setPlain(invoke(() -> objectWriter.writeValues(bytes)));
                  }
                  invoke(() -> csvSchemaWriter.getPlain()
                      .write(row));
                });
          }
          finally {
            invoke(() -> {
              var io = csvSchemaWriter.getPlain();
              if (io != null) {
                io.close();
              }
              return null;
            });
          }

          return bufferFactory.wrap(bytes.toByteArray());
        });
  }

  @Override
  public List<MimeType> getEncodableMimeTypes() {
    return List.of(TEXT_CSV);
  }
}
