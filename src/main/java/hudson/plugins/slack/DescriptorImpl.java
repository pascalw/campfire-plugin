package hudson.plugins.slack;

import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DescriptorImpl extends BuildStepDescriptor<Publisher> {
    private static final String DEFAULT_NOTIFICATION_TEMPLATE = "<%BUILD_URL%|%PROJECT_NAME% %BUILD_DISPLAY_NAME%> (%CHANGES%): %SMART_RESULT%";

    private boolean enabled = false;
    private String teamDomain;
    private String token;
    private String room;
    private String notificationTemplate = DEFAULT_NOTIFICATION_TEMPLATE;
    private boolean smartNotify;
    private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());

    public DescriptorImpl() {
        super(SlackNotifier.class);
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

    public String getTeamDomain() {
        return teamDomain;
    }

    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
        return true;
    }

    /**
     * @see hudson.model.Descriptor#newInstance(org.kohsuke.stapler.StaplerRequest)
     */
    @Override
    public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        String projectToken = req.getParameter("slackToken");
        String projectRoom = req.getParameter("slackRoom");
        String projectNotificationTemplate = req.getParameter("slackNotificationTemplate");
        String projectTeamDomain = req.getParameter("teamDomain");

        if ( projectRoom == null || projectRoom.trim().length() == 0 ) {
            projectRoom = room;
        }
        if ( projectToken == null || projectToken.trim().length() == 0 ) {
            projectToken = token;
        }
        if ( projectTeamDomain == null || projectTeamDomain.trim().length() == 0 ) {
            projectTeamDomain = teamDomain;
        }

        if ( projectNotificationTemplate == null || projectNotificationTemplate.trim().length() == 0 ) {
            projectNotificationTemplate = notificationTemplate;
        }
        try {
            return new SlackNotifier(projectTeamDomain, projectToken, projectRoom,
                projectNotificationTemplate, smartNotify);
        } catch (Exception e) {
            String message = "Failed to initialize Slack notifier - check your Slack notifier configuration settings: " + e.getMessage();
            LOGGER.log(Level.WARNING, message, e);
            throw new FormException(message, e, "");
        }
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        token = req.getParameter("slackToken");
        room = req.getParameter("slackRoom");
        notificationTemplate = req.getParameter("slackNotificationTemplate");
        if (notificationTemplate == null || notificationTemplate.trim().length() == 0) {
            notificationTemplate = DEFAULT_NOTIFICATION_TEMPLATE;
        }
        smartNotify = req.getParameter("slackSmartNotify") != null;
        teamDomain = req.getParameter("teamDomain");
        try {
            new SlackNotifier(teamDomain, token, room, notificationTemplate, smartNotify);
        } catch (Exception e) {
            String message = "Failed to initialize Slack notifier - check your global Slack notifier configuration settings: " + e.getMessage();
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
        return "Slack Notification";
    }

    /**
     * @see hudson.model.Descriptor#getHelpFile()
     */
    @Override
    public String getHelpFile() {
        return "/plugin/slack/help.html";
    }
}
