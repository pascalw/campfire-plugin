package hudson.plugins.campfire;

import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DescriptorImpl extends BuildStepDescriptor<Publisher> {
    private static final String DEFAULT_NOTIFICATION_TEMPLATE = "%PROJECT_NAME% %BUILD_DISPLAY_NAME% (%CHANGES%): %SMART_RESULT% (%BUILD_URL%)";

    private boolean enabled = false;
    private String subdomain;
    private String token;
    private String room;
    private String hudsonUrl;
    private String notificationTemplate = DEFAULT_NOTIFICATION_TEMPLATE;
    private boolean ssl;
    private boolean smartNotify;
    private boolean sound;
    private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());

    public DescriptorImpl() {
        super(HipchatNotifier.class);
        load();
    }

    public String getDefaultNotificationTemplate() {
        return DEFAULT_NOTIFICATION_TEMPLATE;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public String getToken() {
        return token;
    }

    public String getRoom() {
        return room;
    }

    public String getHudsonUrl() {
        return hudsonUrl;
    }

    public String getNotificationTemplate() {
        return notificationTemplate;
    }

    public boolean getSsl() {
        return ssl;
    }

    public boolean getSmartNotify() {
        return smartNotify;
    }

    public boolean getSound() {
        return sound;
    }

    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
        return true;
    }

    /**
     * @see hudson.model.Descriptor#newInstance(org.kohsuke.stapler.StaplerRequest)
     */
    @Override
    public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        String projectSubdomain = req.getParameter("campfireSubdomain");
        String projectToken = req.getParameter("campfireToken");
        String projectRoom = req.getParameter("campfireRoom");
        String projectNotificationTemplate = req.getParameter("campfireNotificationTemplate");
        if ( projectRoom == null || projectRoom.trim().length() == 0 ) {
            projectRoom = room;
        }
        if ( projectToken == null || projectToken.trim().length() == 0 ) {
            projectToken = token;
        }
        if ( projectSubdomain == null || projectSubdomain.trim().length() == 0 ) {
            projectSubdomain = subdomain;
        }
        if ( projectNotificationTemplate == null || projectNotificationTemplate.trim().length() == 0 ) {
            projectNotificationTemplate = notificationTemplate;
        }
        try {
            return new HipchatNotifier(projectSubdomain, projectToken, projectRoom, hudsonUrl,
                projectNotificationTemplate, ssl, smartNotify, sound);
        } catch (Exception e) {
            String message = "Failed to initialize campfire notifier - check your campfire notifier configuration settings: " + e.getMessage();
            LOGGER.log(Level.WARNING, message, e);
            throw new FormException(message, e, "");
        }
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        subdomain = req.getParameter("campfireSubdomain");
        token = req.getParameter("campfireToken");
        room = req.getParameter("campfireRoom");
        hudsonUrl = req.getParameter("campfireHudsonUrl");
        if ( hudsonUrl != null && !hudsonUrl.endsWith("/") ) {
            hudsonUrl = hudsonUrl + "/";
        }
        notificationTemplate = req.getParameter("campfireNotificationTemplate");
        if (notificationTemplate == null || notificationTemplate.trim().length() == 0) {
            notificationTemplate = DEFAULT_NOTIFICATION_TEMPLATE;
        }
        ssl = req.getParameter("campfireSsl") != null;
        smartNotify = req.getParameter("campfireSmartNotify") != null;
        sound = req.getParameter("campfireSound") != null;
        try {
            new HipchatNotifier(subdomain, token, room, hudsonUrl, notificationTemplate, ssl, smartNotify, sound);
        } catch (Exception e) {
            String message = "Failed to initialize campfire notifier - check your global campfire notifier configuration settings: " + e.getMessage();
            LOGGER.log(Level.WARNING, message, e);
            throw new FormException(message, e, "");
        }
        save();
        return super.configure(req, json);
    }

    /**
     * @see hudson.model.Descriptor#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return "Campfire Notification";
    }

    /**
     * @see hudson.model.Descriptor#getHelpFile()
     */
    @Override
    public String getHelpFile() {
        return "/plugin/campfire/help.html";
    }
}
