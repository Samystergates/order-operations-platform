
package com.web.appts.configurations;

import java.time.LocalDate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
  private LocalDate allowedEndDate;
  private String key;

  public WebMvcConfig() {
    this.allowedEndDate = LocalDate.MAX;
    this.key = "RYGB";
  }

  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new ExpiryInterceptor(this.allowedEndDate, this.key));
  }

  public String getKey() {
    return this.key;
  }

  public void setKey(String key) {
    this.key = key;
  }
}
