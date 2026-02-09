package ticketholder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class TicketHolderApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketHolderApplication.class, args);
    }
}
