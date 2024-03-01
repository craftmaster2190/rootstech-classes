package com.craftmaster2190.rootstechclasses.util;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.*;
import java.util.stream.Stream;

public class JsonUtils {

  public static Stream<JsonNode> streamElements(JsonNode jsonNode) {
    return StreamUtils.stream(jsonNode.elements());
  }

  public static ObjectNode objectNodeOf(ObjectMapper objectMapper, Map<String, String> map) {
    var object = objectMapper.createObjectNode();
    map.forEach(object::put);
    return object;
  }

  public static JsonNode arrayNodeOf(ObjectMapper objectMapper, List<? extends JsonNode> list) {
    var array = objectMapper.createArrayNode();
    list.forEach(array::add);
    return array;
  }
}
