<#include "resource-macros.ftl">
<#include "theme-macros.ftl">
<!DOCTYPE html>
<html>
<head>
  <title>Login</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="icon" href="<@resource_href "/img/favicon.ico"/>" type="image/x-icon">
  <#-- Include bootstrap 4 theme CSS -->
  <#--<link rel="stylesheet"-->
        <#--href="<@theme_href "/css/bootstrap-4/${app_settings.bootstrapTheme?html}"/>" type="text/css"-->
        <#--id="bootstrap-theme">-->
  <link href="//getbootstrap.com/docs/4.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
  <link rel="stylesheet" href="<@resource_href "/css/login.css"/>" type="text/css">
  <#-- Include jQuery v3.3.1 to support bootstrap.js -->
  <script type="text/javascript"
          src="<@resource_href "/js/bootstrap-4/jquery-3.3.1.min.js"/>"></script>
  <#-- Include the JS bundle for bootstrap 4 which includes popper.js -->
  <script type="text/javascript"
          src="<@resource_href "/js/bootstrap-4/bootstrap.bundle.min.js"/>"></script>
  <script src="<@resource_href "/js/dist/molgenis-global-ui.js"/>"></script>
  <script src="<@resource_href "/js/dist/molgenis-vendor-bundle.js"/>"></script>
  <script src="<@resource_href "/js/dist/molgenis-global.js"/>"></script>
  <script src="<@resource_href "/js/jquery.validate.min.js"/>"></script>
  <script src="<@resource_href "/js/handlebars.min.js"/>"></script>
  <script src="<@resource_href "/js/molgenis.js"/>"></script>
</head>
<body class="text-center">
<form class="form-signin" id="login-form" method="POST" action="/login">
  <button type="button" class="close pull-right" onclick="location.href='/'"><span
        aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
  <h1 class="h4" id="signin-header">Sign in</h1>

  <div id="alert-container"></div>

  <#if authentication_oidc_clients?has_content>
    <#list authentication_oidc_clients as clientUrl, clientName>
      <a href="${clientUrl}" class="btn btn-primary btn-block" role="button">With ${clientName}</a>
    </#list>
    <div class="hr-sect">OR</div>
  </#if>

  <label for="username-field" class="sr-only">Username</label>
  <input id="username-field" type="text" placeholder="Username" class="form-control"
         name="username" required autofocus>
  <label for="password-field" class="sr-only">Password</label>
  <input id="password-field" type="password" placeholder="Password" class="form-control"
         name="password" required>

  <p class="pull-right">
    <a class="modal-href" href="/account/password/reset" data-target="resetpassword-modal-container">
      <small>Forgot password?</small>
    </a>
  </p>
  <div id="register-modal-container"></div>
  <div id="resetpassword-modal-container"></div>

  <button id="signin-button" type="submit" class="btn btn-success btn-block">Sign in</button>
  <#--<#if authentication_sign_up>-->
    <div class="row" style="margin-top: 20px;">
      <div class="col-md-12 text-center">
        <small>Don't have an account? <a class="modal-href" href="/account/register"
                                         data-target="register-modal-container">Sign up</a></small>
      </div>
    </div>
  <#--</#if>-->
</form>

<style>
  .modal-container-padding {
    padding: 0%;
  }
</style>

<script type="text/javascript">
  $(function () {
    var modal = $('#login-modal')
    var submitBtn = $('#login-btn')
    var form = $('#login-form')
    form.validate()

    <#-- modal events -->
    modal.on('hide.bs.modal', function (e) {
      e.stopPropagation()
      form[0].reset()
      $('.text-error', modal).remove()
      $('.alert', modal).remove()
    })

    <#-- form events -->
    form.submit(function (e) {
      if (!form.valid()) {
        e.preventDefault()
        e.stopPropagation()
      }
    })

    submitBtn.click(function (e) {
      e.preventDefault()
      e.stopPropagation()
      form.submit()
    })

    $('input', form).add(submitBtn).keydown(
      function (e) { <#-- use keydown, because keypress doesn't work cross-browser -->
        if (e.which == 13) {
          e.preventDefault()
          e.stopPropagation()
          form.submit()
        }
      })

    <#-- submodal events -->
    $(document).on('molgenis-registered', function (e, msg) {
      $('#alert-container', modal).empty()
      $('#alert-container', modal).html(
        $('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> '
          + msg + '</div>'))
    })
    $(document).on('molgenis-passwordresetted', function (e, msg) {
      $('#alert-container', modal).empty()
      $('#alert-container', modal).html(
        $('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> '
          + msg + '</div>'))
    })

  })
</script>
<script type="text/javascript">
  $(function () {
    <#if errorMessage??>
    $('#alert-container').html(
      $('<div class="alert alert-block alert-danger alert-dismissable fade in"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><strong>Warning!</strong> ${errorMessage?html}</div>'))
    </#if>
  })
</script>
</body>
</html>	
