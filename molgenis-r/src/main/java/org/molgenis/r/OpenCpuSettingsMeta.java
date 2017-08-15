package org.molgenis.r;

import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.STRING;

import org.molgenis.data.settings.DefaultSettingsEntityType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenCpuSettingsMeta extends DefaultSettingsEntityType {

  @Value("${opencpu.uri.scheme:http}")
  private String defaultScheme;

  @Value("${opencpu.uri.host:localhost}")
  private String defaultHost;

  @Value("${opencpu.uri.port:80}")
  private String defaultPort;

  @Value("${opencpu.uri.path:/ocpu/}")
  private String defaultRootPath;

  static final String SCHEME = "scheme";
  static final String HOST = "host";
  static final String PORT = "port";
  static final String ROOT_PATH = "rootPath";

  public OpenCpuSettingsMeta() {
    super(OpenCpuSettingsImpl.ID);
  }

  @Override
  public void init() {
    super.init();
    setLabel("OpenCPU settings");
    setDescription(
        "OpenCPU, a framework for embedded scientific computing and reproducible research, "
            + "settings.");
    addAttribute(SCHEME)
        .setDefaultValue(defaultScheme)
        .setNillable(false)
        .setLabel("URI scheme")
        .setDescription("Open CPU URI scheme (e.g. http).");
    addAttribute(HOST)
        .setDefaultValue(defaultHost)
        .setNillable(false)
        .setLabel("URI host")
        .setDescription("Open CPU URI host (e.g. localhost).");
    addAttribute(PORT)
        .setDataType(INT)
        .setDefaultValue(defaultPort)
        .setNillable(false)
        .setLabel("URI port")
        .setDescription("Open CPU URI port (e.g. 80).");
    addAttribute(ROOT_PATH)
        .setDataType(STRING)
        .setDefaultValue(defaultRootPath)
        .setNillable(false)
        .setLabel("URI path")
        .setDescription("Open CPU URI root path (e.g. /ocpu/).");
  }
}
