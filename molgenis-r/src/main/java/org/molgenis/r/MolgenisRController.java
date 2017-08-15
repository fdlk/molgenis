package org.molgenis.r;

import javax.servlet.http.HttpServletRequest;
import org.molgenis.security.token.TokenExtractor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
/** Serves the molgenis R api client script. */
public class MolgenisRController {
  private static final String URI = "/molgenis.R";
  private static final String API_URI = "/api/";

  /**
   * Shows the MOLGENIS R API client script.
   *
   * @param request The {@link HttpServletRequest}, used to figure out the api URL and token.
   * @param model {@link Model} containing api URL and token
   * @return View name for the R API client script
   */
  @RequestMapping(method = RequestMethod.GET, value = URI)
  public String showMolgenisRApiClient(HttpServletRequest request, Model model) {
    String apiUrl;
    if (StringUtils.isEmpty(request.getHeader("X-Forwarded-Host"))) {
      apiUrl =
          request.getScheme()
              + "://"
              + request.getServerName()
              + ":"
              + request.getLocalPort()
              + API_URI;
    } else {
      apiUrl = request.getScheme() + "://" + request.getHeader("X-Forwarded-Host") + API_URI;
    }

    // If the request contains a molgenis security token, use it
    String token = TokenExtractor.getToken(request);
    if (token != null) {
      model.addAttribute("token", token);
    }

    model.addAttribute("api_url", apiUrl);

    return "molgenis.R";
  }
}
