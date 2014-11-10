package hudson.plugins.hipchat;

import hudson.Extension;
import hudson.ProxyConfiguration;
import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.model.Result;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.logging.Logger;

public class HipchatNotifier extends BaseNotifier {

    private String token;
    private String room;

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    private static final Logger LOGGER = Logger.getLogger(HipchatNotifier.class.getName());

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
    public HipchatNotifier(String token, String room, String notificationTemplate,
                           boolean smartNotify) {
        super(notificationTemplate, smartNotify);

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
        PostMethod post = new PostMethod("https://api.hipchat.com/v2/room/" + this.room + "/notification");
        post.setRequestHeader("Authorization", "Bearer " + token);
        post.setRequestHeader("Content-Type", "application/json");

        String color = build.getResult() == Result.SUCCESS ? "green" : "red";

        String body = String.format("{\n" +
            "  \"color\": \"%s\",\n" +
            "  \"message_format\": \"text\",\n" +
            "  \"message\": \"%s\",\n" +
            "  \"notify\": true\n" +
            "}", color, message);

        LOGGER.info("Sending to hipchat: " + body);
        post.setRequestEntity(new StringRequestEntity(body, "application/xml", "UTF8"));

        int response = createHttpClient().executeMethod(post);
        LOGGER.info("Got " + response + " from Hipchat API");
    }

    @Override
    protected void checkNotifierClientConnection() {}
}
