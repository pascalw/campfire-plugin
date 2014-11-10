package hudson.plugins.hipchat;

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
    private String token;
    private String room;
    private String notificationTemplate = DEFAULT_NOTIFICATION_TEMPLATE;
    private boolean smartNotify;
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

    public String getToken() {
        return token;
    }

    public String getRoom() {
        return room;
    }

    public String getNotificationTemplate() {
        return notificationTemplate;
    }

    public boolean getSmartNotify() {
        return smartNotify;
    }

    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
        return true;
    }

    /**
     * @see hudson.model.Descriptor#newInstance(org.kohsuke.stapler.StaplerRequest)
     */
    @Override
    public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        String projectToken = req.getParameter("hipchatToken");
        String projectRoom = req.getParameter("hipchatRoom");
        String projectNotificationTemplate = req.getParameter("hipchatNotificationTemplate");
        if ( projectRoom == null || projectRoom.trim().length() == 0 ) {
            projectRoom = room;
        }
        if ( projectToken == null || projectToken.trim().length() == 0 ) {
            projectToken = token;
        }
        if ( projectNotificationTemplate == null || projectNotificationTemplate.trim().length() == 0 ) {
            projectNotificationTemplate = notificationTemplate;
        }
        try {
            return new HipchatNotifier(projectToken, projectRoom,
                projectNotificationTemplate, smartNotify);
        } catch (Exception e) {
            String message = "Failed to initialize hipchat notifier - check your hipchat notifier configuration settings: " + e.getMessage();
            LOGGER.log(Level.WARNING, message, e);
            throw new FormException(message, e, "");
        }
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        token = req.getParameter("hipchatToken");
        room = req.getParameter("hipchatRoom");
        notificationTemplate = req.getParameter("hipchatNotificationTemplate");
        if (notificationTemplate == null || notificationTemplate.trim().length() == 0) {
            notificationTemplate = DEFAULT_NOTIFICATION_TEMPLATE;
        }
        smartNotify = req.getParameter("hipchatSmartNotify") != null;
        try {
            new HipchatNotifier(token, room, notificationTemplate, smartNotify);
        } catch (Exception e) {
            String message = "Failed to initialize hipchat notifier - check your global hipchat notifier configuration settings: " + e.getMessage();
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
        return "/plugin/hipchat/help.html";
    }
}
