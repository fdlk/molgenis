package org.molgenis.ui.converter;

import static org.eclipse.rdf4j.rio.RDFFormat.TURTLE;
import static org.molgenis.ui.converter.RDFMediaType.APPLICATION_TRIG;
import static org.molgenis.ui.converter.RDFMediaType.TEXT_TURTLE;

import java.io.IOException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.Rio;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

@Component
public class RdfConverter extends AbstractHttpMessageConverter<Model> {
  public RdfConverter() {
    super(TEXT_TURTLE, APPLICATION_TRIG);
  }

  @Override
  protected boolean supports(Class<?> aClass) {
    return Model.class.isAssignableFrom(aClass);
  }

  @Override
  protected Model readInternal(Class<? extends Model> aClass, HttpInputMessage httpInputMessage)
      throws IOException, HttpMessageNotReadableException {
    throw new HttpMessageNotReadableException("RDF support is readonly!");
  }

  @Override
  @RunAsSystem
  protected void writeInternal(Model model, HttpOutputMessage httpOutputMessage)
      throws IOException, HttpMessageNotWritableException {
    Rio.write(model, httpOutputMessage.getBody(), TURTLE);
    httpOutputMessage.getBody().close();
  }
}
