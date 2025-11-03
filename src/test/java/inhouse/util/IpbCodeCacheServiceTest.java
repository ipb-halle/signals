package inhouse.util;

import de.ipb_halle.inhouse.util.IpbCodeCacheService;
import de.ipb_halle.inhouse.util.IpbCodeNormalizer;
import de.ipb_halle.signals.ado.Ado;
import de.ipb_halle.signals.ado.AdoDbService;
import de.ipb_halle.signals.ado.AdoEntity;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.*;
import org.junit.runner.RunWith;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Properties;

@RunWith(ApplicationComposer.class)
public class IpbCodeCacheServiceTest {

    // -------------------- 1) Testcontainers PostgreSQL --------------------------
    @ClassRule
    public static PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("signals")
            .withUsername("signals")
            .withPassword("signals");

    // -------------------- 2) DataSource for TomEE/OpenEJB -----------------------
    @Configuration
    public Properties config() {
        Properties p = new Properties();
        p.put("jdbc/signalsDS", "new://Resource?type=DataSource");
        p.put("jdbc/signalsDS.JtaManaged", "true"); // важно для JTA-EM
        p.put("jdbc/signalsDS.JdbcDriver", "org.postgresql.Driver");
        p.put("jdbc/signalsDS.JdbcUrl", container.getJdbcUrl());
        p.put("jdbc/signalsDS.UserName", container.getUsername());
        p.put("jdbc/signalsDS.Password", container.getPassword());
        p.put("openejb.deployments.classpath", "false");
        return p;
    }

    // -------------------- 3) Which classes to be loaded by container -------------------
    @Module
    @Classes(cdi = true, value = {
            // EJB/services
            IpbCodeCacheService.class,
            AdoDbService.class,
            // Entities/models
            Ado.class,
            AdoEntity.class,
            TestCdiProducers.class
    })
    public EjbJar ejbModule() {
        EjbJar ejb = new EjbJar();
        ejb.addEnterpriseBean(new SingletonBean(de.ipb_halle.inhouse.util.IpbCodeCacheService.class));
        ejb.addEnterpriseBean(new SingletonBean(de.ipb_halle.signals.ado.AdoDbService.class));
        return ejb;
    }

    // -------------------- 4) Programmed PersistenceUnit -----------------------
    @Module
    public Persistence persistenceModule() {
        PersistenceUnit pu = new PersistenceUnit("signalsDB");
        pu.setJtaDataSource("jdbc/signalsDS");
        pu.setProvider("org.hibernate.jpa.HibernatePersistenceProvider");

        // registration of persistence classes entity-класс(ы)
        pu.getClazz().add(AdoEntity.class.getName());

        // JPA/Hibernate properties for autogeneration of schema
        pu.getProperties().put("jakarta.persistence.schema-generation.database.action", "drop-and-create");
        pu.getProperties().put("hibernate.hbm2ddl.auto", "create-drop");
        pu.getProperties().put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        pu.getProperties().put("hibernate.show_sql", "false");
        pu.getProperties().put("hibernate.format_sql", "true");

        return new Persistence(pu);
    }

    // -------------------- 5) JPA and resources -------------------------------------
    @PersistenceContext(unitName = "signalsDB")
    private EntityManager em;

    @Resource
    private UserTransaction utx;

    @EJB
    private IpbCodeCacheService cacheService;

    private static final String TEMPLATE_ID = "TEMPLATE_IPB";

    @Before
    public void setUp() throws Exception {
        // schema is made by hibernate , we fill the table with values
        utx.begin();
        em.joinTransaction();
        for (int i = 0; i <= 10; i++) {
            String code = String.format("IPB_%06d", i);
            AdoEntity e = new AdoEntity()
                    .setId("id-" + i)
                    .setEid("eid-" + i)
                    .setName(code)
                    .setDescription("Test ADO " + code)
                    .setType(0)
                    .setIpbCode(code)
                    .setTemplateId(TEMPLATE_ID);
            em.persist(e);
        }
        utx.commit();

        // Initialization cache with 10 elements
        cacheService.initialize(TEMPLATE_ID, 10, null, null, false);
    }

    @After
    public void tearDown() throws Exception {
        utx.begin();
        em.joinTransaction();
        em.createQuery("DELETE FROM AdoEntity").executeUpdate();
        utx.commit();
        cacheService.clear();
    }

    // -------------------- 6) Test ------------------------------------------
    @Test
    public void pickAdoForIpb_returnsExpectedBucket_byModulo10() {
        String raw = "IPB_002433"; // 2433 % 10 = 3
        int numeric = IpbCodeNormalizer.extractNumeric(raw).orElseThrow();
        Assert.assertEquals(2433, numeric);

        Ado picked = cacheService.pickAdoForIpb(raw);
        Assert.assertNotNull(picked);

        List<Ado> snapshot = cacheService.getCachedAdos();
        Assert.assertEquals(10, snapshot.size());

        Ado expected = snapshot.get(Math.floorMod(numeric, snapshot.size())); // index 3
        System.out.println(expected.toString());
        Assert.assertEquals(expected.getEid(), picked.getEid());
        Assert.assertEquals(expected.getIpbCode(), picked.getIpbCode());
    }
}
