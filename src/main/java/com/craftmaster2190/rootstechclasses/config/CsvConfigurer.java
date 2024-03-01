package com.craftmaster2190.rootstechclasses.config;

import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.annotation.*;

@Configuration
public class CsvConfigurer {

  @Bean
  public CodecCustomizer myCustomCodecCustomizer(CsvEncoder csvEncoder, XlsxEncoder xlsxEncoder) {
    return configurer -> {
      configurer.customCodecs()
          .register(csvEncoder);

      configurer.customCodecs()
          .register(xlsxEncoder);
    };
  }

}
