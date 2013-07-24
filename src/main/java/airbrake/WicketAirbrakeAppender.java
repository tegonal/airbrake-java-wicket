package airbrake;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;

import com.tegonal.wicket.authentication.DbAuthenticatedWebSessionIF;
import com.tegonal.wicket.authentication.WebUserIF;

public class WicketAirbrakeAppender extends AirbrakeAppender {

  public AirbrakeNotice newNoticeFor(final Throwable throwable) {
    AirbrakeNoticeBuilderUsingFilteredSystemProperties noticeBuilder = new AirbrakeNoticeBuilderUsingFilteredSystemProperties(
        apiKey, backtrace, throwable, env);
    
    AuthenticatedWebSession authenticatedWebSession = null;
    try {
      authenticatedWebSession = AuthenticatedWebSession.get();
    } catch (WicketRuntimeException wre) {

    }

    if (authenticatedWebSession != null) {
      Map<String, Object> sessiontMap = new HashMap<String, Object>();
      for(String attributeName : authenticatedWebSession.getAttributeNames()) {
        sessiontMap.put(attributeName, authenticatedWebSession.getAttribute(attributeName));
      }
      noticeBuilder.session(sessiontMap);
      
      if(authenticatedWebSession instanceof DbAuthenticatedWebSessionIF) {
        DbAuthenticatedWebSessionIF dbAuthenticatedWebSession = (DbAuthenticatedWebSessionIF) authenticatedWebSession;
        WebUserIF webUser = dbAuthenticatedWebSession.getWebUser();
        if (webUser != null) {
          noticeBuilder.setUser(webUser.getUserIdentification(),
              webUser.getUserBezeichnung(), webUser.getUserEmail(),
              webUser.getUserIdentification());
        }
      }
    }     
    
    RequestCycle requestCycle = null;
    try {
      requestCycle = RequestCycle.get();
    } catch(Exception e) {
      
    }
    if(requestCycle != null) {
      Url url = requestCycle.getRequest().getUrl();
      String action = null;
      String component = null;
      
      noticeBuilder.setRequest(url.toString(), component, action);
      
      IRequestParameters requestParameters = requestCycle.getRequest().getRequestParameters();
      Map<String, Object> requestMap = new HashMap<String, Object>();
      for(String paramName : requestParameters.getParameterNames()) {
        requestMap.put(paramName, requestParameters.getParameterValue(paramName));
      }
      noticeBuilder.request(requestMap);
    }
    
    return noticeBuilder.newNotice();
  }
}
