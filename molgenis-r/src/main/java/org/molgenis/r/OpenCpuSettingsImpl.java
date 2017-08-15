package org.molgenis.r;

import static org.molgenis.r.OpenCpuSettingsMeta.HOST;
import static org.molgenis.r.OpenCpuSettingsMeta.PORT;
import static org.molgenis.r.OpenCpuSettingsMeta.ROOT_PATH;
import static org.molgenis.r.OpenCpuSettingsMeta.SCHEME;

import org.molgenis.data.settings.DefaultSettingsEntity;
import org.springframework.stereotype.Component;

@Component
public class OpenCpuSettingsImpl extends DefaultSettingsEntity implements OpenCpuSettings {
  private static final long serialVersionUID = 1L;

  static final String ID = "OpenCpuSettings";

  public OpenCpuSettingsImpl() {
    super(ID);
  }

  @Override
  public String getScheme() {
    return getString(SCHEME);
  }

  @Override
  public String getHost() {
    return getString(HOST);
  }

  @Override
  public int getPort() {
    return getInt(PORT);
  }

  @Override
  public String getRootPath() {
    return getString(ROOT_PATH);
  }
}
