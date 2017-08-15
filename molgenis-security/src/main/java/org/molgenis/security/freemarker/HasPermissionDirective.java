package org.molgenis.security.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import java.io.IOException;
import org.molgenis.security.core.PermissionService;

/**
 * Directive that prints the body of the tag if the current user has permission on entity
 *
 * <p>usage: &lt;@hasPermission entityTypeId='celiacsprue' permission="WRITE"&gt;write
 * permission&lt;/@hasPermission&gt;
 */
public class HasPermissionDirective extends PermissionDirective {
  public HasPermissionDirective(PermissionService permissionService) {
    super(permissionService);
  }

  @Override
  protected void execute(boolean hasPermission, Environment env, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    if (hasPermission) {
      body.render(env.getOut());
    }
  }
}
