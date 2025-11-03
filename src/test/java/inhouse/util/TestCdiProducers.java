
package inhouse.util;

import de.ipb_halle.signals.ado.AdoRestService;
import de.ipb_halle.signals.dynEnum.DynEnumManager;
import de.ipb_halle.signals.field.FieldParser;
import de.ipb_halle.signals.sample.SampleProcessorBean;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import jakarta.interceptor.Interceptor;
import org.mockito.Mock;
import org.mockito.Mockito;

@Alternative
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
public class TestCdiProducers {
    @Produces
    @ApplicationScoped
    public DynEnumManager dynEnumManager() {
        return Mockito.mock(DynEnumManager.class);
    }

    @Produces
    @ApplicationScoped
    public FieldParser fieldParser() {
        return Mockito.mock(FieldParser.class);
    }

    @Produces
    @ApplicationScoped
    public SampleProcessorBean sampleProcessorBean() {
        return Mockito.mock(SampleProcessorBean.class);
    }

    @Produces
    @ApplicationScoped
    public AdoRestService adoRestService() {
        return Mockito.mock(AdoRestService.class);
    }
}
