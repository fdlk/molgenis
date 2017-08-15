package org.molgenis.security.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import java.io.IOException;
import org.molgenis.security.core.PermissionService;

/**
 * Directive that prints the body of the tag if the current user has no permission on entity
 *
 * <p>usage: &lt;@notHasPermission entity='celiacsprue' permission="WRITE"&gt;no write
 * permission&lt;/@notHasPermission&gt;
 */
public class NotHasPermissionDirective extends PermissionDirective {

  public NotHasPermissionDirective(PermissionService permissionService) {
    super(permissionService);
  }

  @Override
  protected void execute(boolean hasPermission, Environment env, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    if (!hasPermission) {
      body.render(env.getOut());
    }
  }
}
