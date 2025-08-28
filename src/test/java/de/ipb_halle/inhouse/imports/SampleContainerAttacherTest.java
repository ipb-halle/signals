package de.ipb_halle.inhouse.imports;

import de.ipb_halle.inhouse.InhouseContainer;
import de.ipb_halle.inhouse.InhouseCorrelation;
import de.ipb_halle.inhouse.InhouseDbService;
import de.ipb_halle.signals.PostgresqlContainerExtension;
import de.ipb_halle.signals.inventory.ContainerRestService;
import de.ipb_halle.signals.sample.Sample;
import de.ipb_halle.tda.DeploymentElement;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test; // JUnit 5!
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(PostgresqlContainerExtension.class)
public  class SampleContainerAttacherTest {

    @Inject
    @DeploymentElement
    private InhouseDbService dbService;

    private ContainerRestService rest;

    private SampleContainerAttacher attacher;

    private final Integer PROC_ID = 1;
    private final Integer MOLPROC_ID = 31;

    @BeforeEach
    void setUp() {
        rest = mock(ContainerRestService.class);

        // фикстуры в БД
        InhouseCorrelation corr = new InhouseCorrelation();
        corr.setProcedureId(PROC_ID);
        corr.setContext("molproc");
        corr.setCorrId(MOLPROC_ID);
        dbService.save(corr);

        InhouseContainer c1 = new InhouseContainer();
        c1.setMolProcId(MOLPROC_ID);
        dbService.save(c1);

        InhouseContainer c2 = new InhouseContainer();
        c2.setMolProcId(MOLPROC_ID);
        dbService.save(c2);
    }

    @Test
    void attaches_containers_loaded_from_real_db() {
        Sample sample = new Sample();
        String eid = "sample:test";

    }
}
