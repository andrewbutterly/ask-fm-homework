package ab.asktest.controller;
/**
 * Main executable. 
 * Put in for ease of use/testing/execution.
 * 
 * @author andrewb
 * @version 0.0.1
 * */
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import ab.asktest.controller.util.QuestionHelper;
import ab.asktest.dao.APIRepository;
import ab.asktest.dao.APIRepositoryImpl;

@SpringBootApplication
@EnableCaching
@Configuration
@EnableJpaRepositories
@ComponentScan(basePackages = { "ab.asktest.controller.*", "ab.asktest.dao.obj.*" })
@PropertySource("classpath:application.properties")
public class Application {
	
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Value("${general.defaultCountry}")
    private String defaultCountry;
    
    @Value("${general.maxQPerSecondPerCountry}")
    private int maxQPerSecondPerCountry;
    
    @Value("${general.geoIPURL}")
    private String geoIPURL;
    
    @Value("${general.maxQuestionLength}")
    private int maxQuestionLength;
    
    @Value("${general.allowQuestionReposting}")
    private boolean allowQuestionReposting;
    
    @Bean
    public QuestionController QuestionController() {
        return new QuestionController();
    }
    
    @Bean
    public QuestionHelper QuestionHelper() {
        return new QuestionHelper(defaultCountry, maxQPerSecondPerCountry, geoIPURL, maxQuestionLength, allowQuestionReposting);
    }
    @Bean
    public APIRepository APIRepository() {
        return new APIRepositoryImpl();
    } 
    
    @Bean
    @ConfigurationProperties(prefix="spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("ipCountryCache");
    }
    
}
