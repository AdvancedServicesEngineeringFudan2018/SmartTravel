package vesseldevA;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
//@EnableAsync
public class VesseldevASpringBootApplication {
    private Logger logger = LoggerFactory.getLogger(VesseldevASpringBootApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(VesseldevASpringBootApplication.class, args);
    }

}
