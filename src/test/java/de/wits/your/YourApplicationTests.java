package de.wits.your;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by Jonas on 28.11.2016.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = YourApplication.class)
public class YourApplicationTests {

    @Test
    public void contextLoads() {

    }

    /* Enable if you want an export of your schema

    @Autowired
    private Hibernate5DDLExporter exporter;

    @Test
    public void exportSchema() throws Exception {
        exporter.schemaExport("create.sql", "target/");
        exporter.schemaUpdate("update.sql", "target/");

    }

    */
}
