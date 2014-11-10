package hudson.plugins.campfire;

import hudson.model.AbstractBuild;

import java.io.IOException;

public class CampfireNotifier extends BaseNotifier {

    private Campfire campfire;
    private Room room;
    private boolean sound;

//    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

//    @DataBoundConstructor
    public CampfireNotifier(String subdomain, String token, String room, String hudsonUrl, String notificationTemplate,
                        boolean ssl, boolean smartNotify, boolean sound) {
        super(hudsonUrl, notificationTemplate, smartNotify);
        initialize(subdomain, token, room, ssl, sound);
    }

    private void initialize()  {
        initialize(DESCRIPTOR.getSubdomain(), DESCRIPTOR.getToken(), room.getName(),
            DESCRIPTOR.getSsl(), DESCRIPTOR.getSound());
    }

    // getters for project configuration..
    // Configured room name / subdomain / token should be null unless different from descriptor/global values
    public String getConfiguredRoomName() {
        if ( DESCRIPTOR.getRoom().equals(room.getName()) ) {
            return null;
        } else {
            return room.getName();
        }
    }

    public String getConfiguredSubdomain() {
        if ( DESCRIPTOR.getSubdomain().equals(campfire.getSubdomain()) ) {
            return null;
        } else {
            return campfire.getSubdomain();
        }
    }

    public String getConfiguredToken() {
        if ( DESCRIPTOR.getToken().equals(campfire.getToken()) ) {
            return null;
        } else {
            return campfire.getToken();
        }
    }

    private void initialize(String subdomain, String token, String roomName, boolean ssl, boolean sound) {
        this.campfire = new Campfire(subdomain, token, ssl);
        this.room = campfire.findRoomByName(roomName);
        this.sound = sound;
        if ( this.room == null ) {
            throw new RuntimeException("Room '" + roomName + "' not found - verify name and room permissions");
        }
    }

    @Override
    protected void publishMessage(AbstractBuild<?, ?> build, String message) throws IOException {
        room.speak(message);

        if (sound) {
            String message_sound;
            if ("FAILURE".equals(build.getResult().toString())) {
                message_sound = "trombone";
            } else {
                message_sound = "rimshot";
            }
            room.play(message_sound);
        }
    }

    @Override
    protected void checkNotifierClientConnection() {
        if (campfire == null) {
            initialize();
        }
    }
}
