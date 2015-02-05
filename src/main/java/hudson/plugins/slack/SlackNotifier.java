package hudson.plugins.slack;

import hudson.Extension;
import hudson.ProxyConfiguration;
import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.model.Result;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SlackNotifier extends BaseNotifier {

    private String token;
    private String room;
    private String teamDomain;

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    private static final Logger LOGGER = Logger.getLogger(SlackNotifier.class.getName());

    // getters for project configuration..
    // Configured room name / subdomain / token should be null unless different from descriptor/global values
    public String getConfiguredRoom() {
        if ( DESCRIPTOR.getRoom().equals(room) ) {
            return null;
        } else {
            return room;
        }
    }

    public String getConfiguredToken() {
        if ( DESCRIPTOR.getToken().equals(token) ) {
            return null;
        } else {
            return token;
        }
    }

    public String getConfiguredNotificationTemplate() {
        if ( DESCRIPTOR.getNotificationTemplate().equals(notificationTemplate) ) {
            return null;
        } else {
            return notificationTemplate;
        }
    }

    @DataBoundConstructor
    public SlackNotifier(String teamDomain, String token, String room, String notificationTemplate,
                         boolean smartNotify) {
        super(notificationTemplate, smartNotify);

        this.teamDomain = teamDomain;
        this.token = token;
        this.room = room;
    }

    protected HttpClient createHttpClient() {
        HttpClient client = new HttpClient();

        client.getParams().setParameter("http.useragent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; en-us) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16");
        ProxyConfiguration proxy = Hudson.getInstance().proxy;

        if (proxy != null) {
            client.getHostConfiguration().setProxy(proxy.name, proxy.port);
        }
        return client;
    }

    @Override
    protected void publishMessage(AbstractBuild<?, ?> build, String message) throws IOException {
        String url = "https://" + teamDomain + ".slack.com/services/hooks/jenkins-ci?token=" + token;
        String color = build.getResult() == Result.SUCCESS ? "good" : "danger";

        PostMethod post = new PostMethod(url);
        JSONObject json = new JSONObject();

        try {
            JSONObject field = new JSONObject();
            field.put("short", false);
            field.put("value", message);

            JSONArray fields = new JSONArray();
            fields.add(field);

            JSONObject attachment = new JSONObject();
            attachment.put("fallback", message);
            attachment.put("color", color);
            attachment.put("fields", fields);
            JSONArray attachments = new JSONArray();
            attachments.add(attachment);

            json.put("channel", this.room);
            json.put("attachments", attachments);

            post.addParameter("payload", json.toString());
            post.getParams().setContentCharset("UTF-8");
            int responseCode = createHttpClient().executeMethod(post);
            String response = post.getResponseBodyAsString();
            if(responseCode != HttpStatus.SC_OK) {
                LOGGER.log(Level.WARNING, "Slack post may have failed. Response: " + response);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error posting to Slack", e);
        } finally {
            LOGGER.info("Posting succeeded");
            post.releaseConnection();
        }
    }

    @Override
    protected void checkNotifierClientConnection() {}
}
