package br.com.fiap.ecocomunidade.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class ApiConfig implements WebMvcConfigurer {
    @Bean
    public org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer validationClock() {
        // Datas locais do dominio seguem Sao Paulo, independentemente do fuso do Render.
        return configuration -> configuration.clockProvider(
            () -> java.time.Clock.system(java.time.ZoneId.of("America/Sao_Paulo")));
    }
    private final String[] origins;
    public ApiConfig(@Value("${app.cors.origins}") String[] origins) { this.origins = origins; }
    @Bean public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return mapper;
    }
    @Override public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins(origins)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("Content-Type", "Accept").exposedHeaders("Location");
    }
}
